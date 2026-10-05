package game.backend.battle;

import java.util.ArrayList;
import java.util.List;
import game.database.CardCatalog;
import game.backend.model.Card;
import game.backend.model.Player;
import game.backend.model.Enemy;
import game.backend.model.RunUpgrades;

/** 병합된 전투 엔진. 백엔드 Player/Enemy로 상태를 관리하고 최신 덱·중독·대응 규칙을 계산합니다. */
public final class Battle {
    public static final int PLAYER_MAX_HP = Player.MAX_HP;
    public static final int ENEMY_MAX_HP = Enemy.MAX_HP;

    private final Player player = new Player();
    private final Enemy enemy = new Enemy();

    // 상점 강화(공격 +1, 방어 +1, 체력 +10).
    private final RunUpgrades upgrades;

    private int turn;
    private final List<Card> hand = new ArrayList<>();
    private final List<Card> deck;
    private final List<Card> draw = new ArrayList<>();
    private final List<Card> discard = new ArrayList<>();

    private final List<Card> enemyHand = new ArrayList<>();
    private final List<Card> enemyDraw = new ArrayList<>();
    private final List<Card> enemyDiscard = new ArrayList<>();

    private Card lastEnemyCard;
    private boolean reactions, attackUsed, healUsed, pendingEnemy;
    private int spellUses;
    private Card pendingAttack;

    public void enableReactions() {
        reactions = true;
        reset();
    }

    public Card pendingAttack() {
        return pendingAttack;
    }

    public boolean waitingForDefense() {
        return pendingAttack != null && pendingEnemy;
    }

    // 중독은 각각 독립된 [피해, 남은 턴]으로 보관합니다.
    private final List<int[]> poisons = new ArrayList<>();

    public Battle() {
        deck = null;
        upgrades = new RunUpgrades();
        reset();
    }

    public Battle(List<Card> cards) {
        this(cards, new RunUpgrades());
    }

    public Battle(List<Card> cards, RunUpgrades upgrades) {
        if (!game.backend.model.PlayerDeck.valid(cards)) {
            throw new IllegalArgumentException("잘못된 덱");
        }
        if (upgrades == null) {
            throw new IllegalArgumentException("강화 정보가 필요합니다.");
        }
        deck = List.copyOf(cards);
        this.upgrades = upgrades;
        reset();
    }

    public void reset() {
        player.setMaxHp(Player.MAX_HP + upgrades.bonusHp());
        player.reset();
        enemy.reset();

        turn = 1;
        pendingAttack = null;
        attackUsed = false;
        healUsed = false;
        spellUses = 0;

        hand.clear();
        draw.clear();
        discard.clear();
        poisons.clear();

        enemyHand.clear();
        enemyDraw.clear();
        enemyDiscard.clear();
        lastEnemyCard = null;

        for (int i = 0; i < (reactions ? 2 : 4); i++) {
            enemyDraw.addAll(
                CardCatalog.allCards().subList(0, reactions ? 10 : 5)
            );
        }

        java.util.Collections.shuffle(enemyDraw);
        fillEnemyHand();

        if (deck != null) {
            draw.addAll(deck);
            java.util.Collections.shuffle(draw);
        }

        drawToHandLimit();
    }

    /**
     * 손패가 최대 5장이 되도록 부족한 수만큼 카드를 뽑습니다.
     *
     * 사용하지 않은 카드는 손패에 그대로 남기고,
     * 사용해서 비어진 자리만 새 카드로 보충합니다.
     */
    private void drawToHandLimit() {
        while (hand.size() < 5) {
            if (draw.isEmpty()) {
                if (discard.isEmpty()) {
                    break;
                }
                draw.addAll(discard);
                discard.clear();
                java.util.Collections.shuffle(draw);
            }

            hand.add(draw.remove(draw.size() - 1));
        }
    }

    public boolean canPlay(int index) {
        // 에너지 제한 없이 카드 장수 제한으로만 조절합니다.
        if (index < 0 || index >= hand.size()) {
            return false;
        }

        var type = hand.get(index).type();

        // 상대 공격에 대응 중에는 방어 카드만 사용할 수 있으며,
        // 방어 등급은 카드 cost를 기준으로 공격 등급 이상이어야 합니다.
        if (pendingAttack != null) {
            if (!pendingEnemy || type != game.backend.model.CardType.DEFENSE) {
                return false;
            }
            return hand.get(index).cost() >= pendingAttack.cost();
        }

        if (isOver()) {
            return false;
        }

        if (!reactions) {
            return true;
        }

        // 공격/회복은 각각 턴당 1장, 주문은 턴당 2장까지 사용할 수 있습니다.
        if (type == game.backend.model.CardType.ATTACK) {
            return !attackUsed;
        }
        if (type == game.backend.model.CardType.HEAL) {
            return !healUsed;
        }
        if (type == game.backend.model.CardType.SPELL) {
            return spellUses < 2;
        }

        return false;
    }

    public String play(int index) {
        if (!canPlay(index)) {
            return "카드를 사용할 수 없습니다.";
        }

        Card card = hand.remove(index);
        discard.add(card);

        if (reactions && card.type() == game.backend.model.CardType.ATTACK) {
            attackUsed = true;
            pendingAttack = card;
            pendingEnemy = false;
            return "공격 공개 · 상대 방어 대기";
        }

        if (reactions && card.type() == game.backend.model.CardType.DEFENSE) {
            return resolveAttack(card);
        }

        if (reactions && card.type() == game.backend.model.CardType.HEAL) {
            healUsed = true;
        }

        if (reactions && card.type() == game.backend.model.CardType.SPELL) {
            spellUses++;
        }

        var data = CardCatalog.effect(card);

        if (data != null) {
            enemy.takeDamage(data.damage());
            player.addBlock(data.block());
            player.heal(data.heal());

            if (data.poison() > 0) {
                poisons.add(new int[] { data.poison(), data.duration() });
            }

            return card.name() + " · " + card.description();
        }

        String effect;

        switch (card.type()) {
            case ATTACK -> {
                int damage = card.power() + player.consumeBonusDamage();
                enemy.takeDamage(damage);
                player.consumeBonusDamage();
                effect = "적에게 피해 " + damage;
            }
            case DEFENSE -> {
                player.addBlock(card.power());
                effect = "방어도 +" + card.power();
            }
            case HEAL -> {
                int before = player.hp();
                player.heal(card.power());
                effect = "체력 " + (player.hp() - before) + " 회복";
            }
            case SPELL -> {
                player.addBonusDamage(card.power());
                effect = "이번 턴 다음 공격 피해 +" + card.power();
            }
            default -> throw new IllegalStateException();
        }

        return card.name() + " · " + effect
                + (enemy.isDefeated() ? " — 승리!" : "");
    }

    public String endTurn() {
        if (isOver()) {
            return "전투가 끝났습니다.";
        }

        lastEnemyCard = null;

        for (var poison : poisons) {
            enemy.takeDamage(poison[0]);
            poison[1]--;
        }

        poisons.removeIf(p -> p[1] <= 0);

        if (enemy.isDefeated()) {
            return "중독 피해로 승리!";
        }

        int attack = enemyIntent();

        if (deck != null && !enemyHand.isEmpty()) {
            lastEnemyCard = enemyHand.remove(0);
            enemyDiscard.add(lastEnemyCard);
        }

        int damage = player.takeDamage(attack);
        player.clearBlock();
        player.consumeBonusDamage();

        if (player.hp() == 0) {
            return "적의 공격! 피해 " + damage + " — 패배";
        }

        turn++;

        // 기존 손패는 유지하고, 사용해서 비어진 자리만 보충합니다.
        drawToHandLimit();

        return "적의 공격! 피해 " + damage + " · 새 턴 시작";
    }

    public int playerHp() {
        return player.hp();
    }

    public int playerMaxHp() {
        return player.maxHp();
    }

    /** 상대 공격을 공개하되 체력은 아직 변경하지 않습니다. */
    public Card revealEnemyAttack() {
        if (isOver() || pendingAttack != null) {
            return null;
        }

        for (var poison : poisons) {
            enemy.takeDamage(poison[0]);
            poison[1]--;
        }

        poisons.removeIf(p -> p[1] <= 0);

        if (isOver()) {
            return null;
        }

        for (int i = 0; i < enemyHand.size(); i++) {
            if (enemyHand.get(i).type() == game.backend.model.CardType.ATTACK) {
                pendingAttack = enemyHand.remove(i);
                enemyDiscard.add(pendingAttack);
                lastEnemyCard = pendingAttack;
                pendingEnemy = true;
                return pendingAttack;
            }
        }

        return null;
    }

    public Card chooseEnemyDefense() {
        if (pendingAttack == null || pendingEnemy) {
            return null;
        }

        for (int i = 0; i < enemyHand.size(); i++) {
            if (enemyHand.get(i).type() == game.backend.model.CardType.DEFENSE
                    && enemyHand.get(i).cost() >= pendingAttack.cost()) {
                Card defense = enemyHand.remove(i);
                enemyDiscard.add(defense);
                return defense;
            }
        }

        return null;
    }

    public Card commitDefense(int index) {
        if (!waitingForDefense() || !canPlay(index)) {
            return null;
        }

        Card card = hand.remove(index);
        discard.add(card);
        return card;
    }

    public String resolveAttack(Card defense) {
        if (pendingAttack == null) {
            return "대기 중인 공격 없음";
        }

        var effect = defense == null ? null : CardCatalog.effect(defense);
        int shield = effect == null ? 0 : effect.block();
        int counter = effect == null ? 0 : effect.damage();

        // 상점 강화: 내 방어 카드는 방어량 +defenseBonus,
        // 내 공격 카드는 피해 +attackBonus.
        if (pendingEnemy && defense != null) {
            shield += upgrades.defenseBonus();
        }

        int attackPower =
                pendingAttack.power()
                + (pendingEnemy ? 0 : upgrades.attackBonus());

        int damage = Math.max(0, attackPower - shield);

        if (pendingEnemy) {
            player.takeDamage(damage);
            enemy.takeDamage(counter);
        } else {
            enemy.takeDamage(damage);
            player.takeDamage(counter);
        }

        pendingAttack = null;
        pendingEnemy = false;

        return "공격 피해 " + damage
                + " · 방어 " + shield
                + " · 반격 피해 " + counter;
    }

    public void startNextRound() {
        if (isOver() || pendingAttack != null) {
            return;
        }

        turn++;
        player.clearBlock();
        player.consumeBonusDamage();
        attackUsed = false;
        healUsed = false;
        spellUses = 0;

        // 기존 손패는 유지하고 부족한 카드만 보충합니다.
        drawToHandLimit();
    }

    public int enemyHp() {
        return enemy.hp();
    }

    public int block() {
        return player.block();
    }

    public int bonus() {
        return player.bonusDamage();
    }

    public int turn() {
        return turn;
    }

    public int enemyIntent() {
        if (reactions) {
            return enemyHand.stream()
                    .filter(c -> c.type() == game.backend.model.CardType.ATTACK)
                    .findFirst()
                    .map(Card::power)
                    .orElse(0);
        }

        return deck == null
                ? (turn % 3 == 0 ? 12 : 7)
                : enemyHand.isEmpty() ? 0 : enemyHand.get(0).power();
    }

    public void prepareEnemyTurn() {
        if (!isOver()) {
            fillEnemyHand();
        }
    }

    private void fillEnemyHand() {
        // 적 덱은 소진되면 다시 섞지 않습니다.
        while (enemyHand.size() < 5 && !enemyDraw.isEmpty()) {
            enemyHand.add(enemyDraw.remove(enemyDraw.size() - 1));
        }
    }

    public int enemyHandCount() {
        return enemyHand.size();
    }

    public int enemyDrawCount() {
        return enemyDraw.size();
    }

    public int enemyDiscardCount() {
        return enemyDiscard.size();
    }

    public Card lastEnemyCard() {
        return lastEnemyCard;
    }

    /** 적 체력 0 또는 적 덱 소진(플레이어 생존) 시 승리입니다. */
    public boolean playerWon() {
        return isOver() && player.hp() > 0;
    }

    public boolean isOver() {
        return player.hp() == 0
                || enemy.isDefeated()
                || (pendingAttack == null
                    && enemyHand.isEmpty()
                    && enemyDraw.isEmpty());
    }

    public List<Card> hand() {
        return List.copyOf(hand);
    }

    public int drawCount() {
        return draw.size();
    }

    public int discardCount() {
        return discard.size();
    }

    public int poisonDamage() {
        return poisons.stream().mapToInt(p -> p[0]).sum();
    }
}
