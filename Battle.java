package prototype.backend;

import java.util.ArrayList;
import java.util.List;
import prototype.data.CardCatalog;
import prototype.model.Card;
import prototype.model.CardType;
import prototype.model.Enemy;
import prototype.model.Player;

/**
 * 실제 전투 규칙을 담당하는 핵심 백엔드 클래스입니다.
 *
 * <p>UI는 이 클래스가 "어떻게 계산하는지" 알 필요가 없습니다.
 * UI는 play(), endTurn(), playerHp(), enemyHp() 등의 공개 메서드만 사용하면 됩니다.</p>
 *
 * <p>현재 구현한 게임 규칙:</p>
 * <ol>
 *   <li>플레이어는 HP 40, 에너지 3으로 시작합니다.</li>
 *   <li>전투 시작 시 카드 5장을 손에 받습니다.</li>
 *   <li>카드 사용 시 카드 비용만큼 에너지가 감소합니다.</li>
 *   <li>공격 카드는 적 HP를 감소시킵니다.</li>
 *   <li>방어 카드는 방어도를 올립니다.</li>
 *   <li>회복 카드는 HP를 회복합니다.</li>
 *   <li>주문 카드는 이번 턴 다음 공격의 피해를 증가시킵니다.</li>
 *   <li>턴 종료 시 적이 공격하고, 남은 방어도는 다음 턴 전에 제거됩니다.</li>
 *   <li>적을 먼저 쓰러뜨리면 승리, 플레이어 HP가 0이 되면 패배입니다.</li>
 * </ol>
 */
public final class Battle {
    public static final int PLAYER_MAX_HP = Player.MAX_HP;
    public static final int ENEMY_MAX_HP = Enemy.MAX_HP;
    public static final int MAX_ENERGY = Player.MAX_ENERGY;

    private final Player player = new Player();
    private final Enemy enemy = new Enemy();
    private final List<Card> hand = new ArrayList<>();

    /** 현재 턴 번호. 첫 턴은 1입니다. */
    private int turn;

    public Battle() {
        reset();
    }

    /**
     * 전투 전체를 초기 상태로 되돌립니다.
     * 다시 시작 버튼을 눌렀을 때도 이 메서드를 사용합니다.
     */
    public void reset() {
        player.reset();
        enemy.reset();
        turn = 1;
        refillHand();
    }

    /**
     * 손패를 다시 채웁니다.
     * 현재 프론트 프로토타입의 규칙을 유지하기 위해 매 턴 동일한 5장의 카드를 줍니다.
     * 나중에 실제 덱/드로우 시스템을 만들 때 이 부분만 교체하면 됩니다.
     */
    private void refillHand() {
        hand.clear();
        hand.addAll(CardCatalog.sampleHand());
    }

    /**
     * 지정한 위치의 카드를 지금 사용할 수 있는지 검사합니다.
     */
    public boolean canPlay(int index) {
        return !isOver()
                && index >= 0
                && index < hand.size()
                && hand.get(index).cost() <= player.energy();
    }

    /**
     * 손패의 카드를 한 장 사용합니다.
     *
     * <p>처리 순서가 중요합니다:</p>
     * <pre>
     * 사용 가능 여부 확인
     * → 카드 가져오기
     * → 손패에서 제거
     * → 에너지 지불
     * → 카드 효과 적용
     * → 결과 메시지 반환
     * </pre>
     *
     * @param index 화면에서 클릭한 카드의 손패 인덱스
     * @return 프론트 화면에 보여줄 결과 메시지
     */
    public String play(int index) {
        if (!canPlay(index)) {
            return "카드를 사용할 수 없습니다.";
        }

        // 카드를 손패에서 꺼내고 사용 비용을 지불합니다.
        Card card = hand.remove(index);
        player.spendEnergy(card.cost());

        String effect;

        // 카드 종류에 따라 실제 게임 효과를 적용합니다.
        switch (card.type()) {
            case ATTACK -> effect = useAttackCard(card);
            case DEFENSE -> effect = useDefenseCard(card);
            case HEAL -> effect = useHealCard(card);
            case SPELL -> effect = useSpellCard(card);
            default -> throw new IllegalStateException("처리하지 않은 카드 종류입니다.");
        }

        return card.name() + " · " + effect + (enemy.isDefeated() ? " — 승리!" : "");
    }

    /** 공격 카드 처리 */
    private String useAttackCard(Card card) {
        // 집중 같은 SPELL 카드가 저장한 추가 피해를 현재 공격에 적용합니다.
        int bonus = player.consumeBonusDamage();
        int damage = card.power() + bonus;

        enemy.takeDamage(damage);
        return "적에게 피해 " + damage;
    }

    /** 방어 카드 처리 */
    private String useDefenseCard(Card card) {
        player.addBlock(card.power());
        return "방어도 +" + card.power();
    }

    /** 회복 카드 처리 */
    private String useHealCard(Card card) {
        int healed = player.heal(card.power());
        return "체력 " + healed + " 회복";
    }

    /** 주문 카드 처리 */
    private String useSpellCard(Card card) {
        player.addBonusDamage(card.power());
        return "이번 턴 다음 공격 피해 +" + card.power();
    }

    /**
     * 턴을 종료합니다.
     *
     * <p>현재 프로토타입의 규칙에 맞춰 적이 먼저 공격하고,
     * 플레이어가 살아 있으면 다음 턴으로 넘어갑니다.</p>
     */
    public String endTurn() {
        if (isOver()) {
            return "전투가 끝났습니다.";
        }

        // 현재 턴에 표시하고 있던 적의 공격력을 그대로 사용합니다.
        int incomingDamage = enemyIntent();
        int actualDamage = player.takeDamage(incomingDamage);

        // 방어도와 다음 공격 보너스는 턴이 끝나면 초기화합니다.
        player.clearBlock();
        player.consumeBonusDamage();

        if (player.hp() == 0) {
            return "적의 공격! 피해 " + actualDamage + " — 패배";
        }

        // 다음 턴 시작
        turn++;
        player.refillEnergy();
        refillHand();

        return "적의 공격! 피해 " + actualDamage + " · 새 턴 시작";
    }

    /**
     * 적이 이번 턴에 사용할 공격력을 반환합니다.
     * 기존 프론트/테스트의 동작을 그대로 유지합니다.
     *
     * 3의 배수 턴: 12 피해
     * 그 외: 7 피해
     */
    public int enemyIntent() {
        return turn % 3 == 0 ? 12 : 7;
    }

    public int playerHp() {
        return player.hp();
    }

    public int enemyHp() {
        return enemy.hp();
    }

    public int energy() {
        return player.energy();
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

    public boolean isOver() {
        return player.hp() == 0 || enemy.isDefeated();
    }

    /**
     * UI에는 내부 ArrayList 자체를 넘기지 않습니다.
     * List.copyOf()를 사용하여 화면이 게임 내부 손패를 직접 수정하지 못하게 합니다.
     */
    public List<Card> hand() {
        return List.copyOf(hand);
    }
}
