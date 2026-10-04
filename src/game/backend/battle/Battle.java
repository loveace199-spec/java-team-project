package game.backend.battle;

import java.util.ArrayList;
import java.util.List;
import game.database.CardCatalog;
import game.backend.model.Card;
import game.backend.model.Player;
import game.backend.model.Enemy;

/** 병합된 전투 엔진. 백엔드 Player/Enemy로 상태를 관리하고 최신 덱·중독·대응 규칙을 계산합니다. */
public final class Battle {
    public static final int PLAYER_MAX_HP = Player.MAX_HP;
    public static final int ENEMY_MAX_HP = Enemy.MAX_HP;
    public static final int MAX_ENERGY = Player.MAX_ENERGY;
    private final Player player = new Player();
    private final Enemy enemy = new Enemy();
    private int turn;
    private final List<Card> hand = new ArrayList<>();
    private final List<Card> deck;
    private final List<Card> draw = new ArrayList<>(), discard = new ArrayList<>();
    private final List<Card> enemyHand=new ArrayList<>(), enemyDraw=new ArrayList<>(), enemyDiscard=new ArrayList<>();
    private Card lastEnemyCard;
    private boolean reactions, mainCardUsed, pendingEnemy;
    private Card pendingAttack;
    public void enableReactions() { reactions=true;reset(); }
    public Card pendingAttack() { return pendingAttack; }
    public boolean waitingForDefense() { return pendingAttack!=null && pendingEnemy; }
    // 중독은 각각 독립된 [피해, 남은 턴]으로 보관하고 상대 행동 전에 적용합니다.
    private final List<int[]> poisons = new ArrayList<>();

    public Battle() { deck=null; reset(); }
    public Battle(List<Card> cards) {
        if(!game.backend.model.PlayerDeck.valid(cards)) throw new IllegalArgumentException("잘못된 덱");
        deck=List.copyOf(cards); reset();
    }

    public void reset() {
        // 전투를 처음 상태로 되돌립니다. 화면 디자인을 바꾸려면 이 파일은 건드리지 않아도 됩니다.
        player.reset();
        enemy.reset();
        turn = 1;
        pendingAttack=null;mainCardUsed=false;
        hand.clear(); draw.clear(); discard.clear(); poisons.clear();
        enemyHand.clear();enemyDraw.clear();enemyDiscard.clear();lastEnemyCard=null;
        // 상대 시제품 덱: 실제 공격 카드 5종을 4장씩 사용합니다. 덱 편집 규칙과는 별개입니다.
        for(int i=0;i<(reactions?2:4);i++) enemyDraw.addAll(CardCatalog.allCards().subList(0,reactions?10:5));
        java.util.Collections.shuffle(enemyDraw);fillEnemyHand();
        if(deck!=null) { draw.addAll(deck); java.util.Collections.shuffle(draw); }
        refill();
    }

    private void refill() {
        // 실제 덱은 남은 손패를 버리고 5장을 뽑습니다. 기본 생성자는 이전 시안 검사 전용입니다.
        discard.addAll(hand); hand.clear();
        if(deck==null) { hand.addAll(CardCatalog.sampleHand()); return; }
        for(int i=0;i<5;i++) {
            if(draw.isEmpty()) { draw.addAll(discard); discard.clear(); java.util.Collections.shuffle(draw); }
            if(!draw.isEmpty()) hand.add(draw.remove(draw.size()-1));
        }
    }

    public boolean canPlay(int index) {
        // 잘못된 카드 위치, 전투 종료, 에너지 부족일 때 사용을 막습니다.
        if(isOver() || index<0 || index>=hand.size() || hand.get(index).cost()>player.energy()) return false;
        if(!reactions) return true;
        var type=hand.get(index).type();
        if(pendingAttack!=null) return pendingEnemy && type==game.backend.model.CardType.DEFENSE;
        return type!=game.backend.model.CardType.DEFENSE && (!mainCardUsed || type==game.backend.model.CardType.SPELL);
    }

    public String play(int index) {
        // 카드 한 장 사용: 가능 여부 검사 → 손패 제거 → 비용 지불 → 효과 적용.
        if (!canPlay(index)) return "카드를 사용할 수 없습니다.";
        Card card = hand.remove(index);
        player.spendEnergy(card.cost());
        discard.add(card);
        if(reactions && card.type()==game.backend.model.CardType.ATTACK) {
            mainCardUsed=true;pendingAttack=card;pendingEnemy=false;
            return "공격 공개 · 상대 방어 대기";
        }
        if(reactions && card.type()==game.backend.model.CardType.DEFENSE) return resolveAttack(card);
        if(reactions && card.type()==game.backend.model.CardType.HEAL) mainCardUsed=true;
        var data=CardCatalog.effect(card);
        if(data!=null) {
            enemy.takeDamage(data.damage());
            player.addBlock(data.block());
            player.heal(data.heal());
            if(data.poison()>0) poisons.add(new int[]{data.poison(),data.duration()});
            return card.name()+" · "+card.description();
        }
        String effect;
        // [임시 효과 규칙] 종류에 따라 power를 서로 다르게 적용합니다.
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
        return card.name() + " · " + effect + (enemy.isDefeated() ? " — 승리!" : "");
    }

    public String endTurn() {
        // 적 공격 - 방어도만큼 피해. 음수 피해는 0으로 제한합니다.
        // 살아 있으면 다음 턴: 에너지 3, 고정 손패로 갱신합니다.
        if (isOver()) return "전투가 끝났습니다.";
        lastEnemyCard=null;
        for(var poison:poisons) { enemy.takeDamage(poison[0]); poison[1]--; }
        poisons.removeIf(p -> p[1]<=0);
        if(enemy.isDefeated()) return "중독 피해로 승리!";
        // 사용한 한 장만 제거합니다. 상대 손패는 연출 중 5 → 4장으로 유지됩니다.
        int attack=enemyIntent();
        if(deck!=null && !enemyHand.isEmpty()) {
            lastEnemyCard=enemyHand.remove(0);enemyDiscard.add(lastEnemyCard);
        }
        int damage = player.takeDamage(attack);
        player.clearBlock();
        player.consumeBonusDamage();
        if (player.hp() == 0) return "적의 공격! 피해 " + damage + " — 패배";
        turn++;
        player.refillEnergy();
        refill();
        return "적의 공격! 피해 " + damage + " · 새 턴 시작";
    }

    public int playerHp() { return player.hp(); }
    /** 상대 공격을 공개하되 체력은 아직 변경하지 않습니다. 방어 선택 후에만 판정합니다. */
    public Card revealEnemyAttack() {
        if(isOver() || pendingAttack!=null) return null;
        for(var poison:poisons) {enemy.takeDamage(poison[0]);poison[1]--;}
        poisons.removeIf(p->p[1]<=0);
        if(isOver()) return null;
        for(int i=0;i<enemyHand.size();i++) if(enemyHand.get(i).type()==game.backend.model.CardType.ATTACK) {
            pendingAttack=enemyHand.remove(i);enemyDiscard.add(pendingAttack);lastEnemyCard=pendingAttack;pendingEnemy=true;return pendingAttack;
        }
        return null;
    }
    public Card chooseEnemyDefense() {
        if(pendingAttack==null || pendingEnemy) return null;
        for(int i=0;i<enemyHand.size();i++) if(enemyHand.get(i).type()==game.backend.model.CardType.DEFENSE) {
            Card defense=enemyHand.remove(i);enemyDiscard.add(defense);return defense;
        }
        return null;
    }
    public Card commitDefense(int index) {
        if(!waitingForDefense() || !canPlay(index)) return null;
        Card card=hand.remove(index);player.spendEnergy(card.cost());discard.add(card);return card;
    }
    public String resolveAttack(Card defense) {
        if(pendingAttack==null) return "대기 중인 공격 없음";
        var effect=defense==null?null:CardCatalog.effect(defense);
        int shield=effect==null?0:effect.block(),counter=effect==null?0:effect.damage();
        int damage=Math.max(0,pendingAttack.power()-shield);
        if(pendingEnemy) {player.takeDamage(damage);enemy.takeDamage(counter);}
        else {enemy.takeDamage(damage);player.takeDamage(counter);}
        pendingAttack=null;
        return "공격 피해 "+damage+" · 방어 "+shield+" · 반격 피해 "+counter;
    }
    public void startNextRound() {
        if(isOver() || pendingAttack!=null) return;
        turn++;player.refillEnergy();player.clearBlock();player.consumeBonusDamage();mainCardUsed=false;refill();
    }
    public int enemyHp() { return enemy.hp(); }
    public int energy() { return player.energy(); }
    public int block() { return player.block(); }
    public int bonus() { return player.bonusDamage(); }
    public int turn() { return turn; }
    public int enemyIntent() {
        if(reactions) return enemyHand.stream().filter(c->c.type()==game.backend.model.CardType.ATTACK).findFirst().map(Card::power).orElse(0);
        return deck==null ? (turn % 3 == 0 ? 12 : 7) : enemyHand.isEmpty()?0:enemyHand.get(0).power();
    }
    public void prepareEnemyTurn() { if(!isOver()) fillEnemyHand(); }
    private void fillEnemyHand() {
        while(enemyHand.size()<5) {
            if(enemyDraw.isEmpty()) {enemyDraw.addAll(enemyDiscard);enemyDiscard.clear();java.util.Collections.shuffle(enemyDraw);}
            if(enemyDraw.isEmpty()) break;
            enemyHand.add(enemyDraw.remove(enemyDraw.size()-1));
        }
    }
    public int enemyHandCount() {return enemyHand.size();}
    public int enemyDrawCount() {return enemyDraw.size();}
    public int enemyDiscardCount() {return enemyDiscard.size();}
    public Card lastEnemyCard() {return lastEnemyCard;}
    public boolean isOver() { return player.hp() == 0 || enemy.isDefeated(); }
    public List<Card> hand() { return List.copyOf(hand); }
    public int drawCount() { return draw.size(); }
    public int discardCount() { return discard.size(); }
    public int poisonDamage() { return poisons.stream().mapToInt(p -> p[0]).sum(); }
}
