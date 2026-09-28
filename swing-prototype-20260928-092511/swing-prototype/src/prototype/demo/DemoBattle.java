package prototype.demo;

import java.util.ArrayList;
import java.util.List;
import prototype.data.CardCatalog;
import prototype.model.Card;

/** UI 연결을 확인하기 위한 임시 처리. 최종 백엔드로 교체할 대상입니다. */
public final class DemoBattle {
    public static final int PLAYER_MAX_HP = 40;
    public static final int ENEMY_MAX_HP = 35;
    private int playerHp, enemyHp, energy, block, bonus, turn;
    private final List<Card> hand = new ArrayList<>();

    public DemoBattle() { reset(); }

    public void reset() {
        // 전투를 처음 상태로 되돌립니다. 화면 디자인을 바꾸려면 이 파일은 건드리지 않아도 됩니다.
        playerHp = PLAYER_MAX_HP;
        enemyHp = ENEMY_MAX_HP;
        energy = 3;
        block = bonus = 0;
        turn = 1;
        refill();
    }

    private void refill() {
        // 시제품은 무작위 뽑기 대신 매 턴 같은 카드 5장을 줍니다.
        hand.clear();
        hand.addAll(CardCatalog.sampleHand());
    }

    public boolean canPlay(int index) {
        // 잘못된 카드 위치, 전투 종료, 에너지 부족일 때 사용을 막습니다.
        return !isOver() && index >= 0 && index < hand.size()
            && hand.get(index).cost() <= energy;
    }

    public String play(int index) {
        // 카드 한 장 사용: 가능 여부 검사 → 손패 제거 → 비용 지불 → 효과 적용.
        if (!canPlay(index)) return "카드를 사용할 수 없습니다.";
        Card card = hand.remove(index);
        energy -= card.cost();
        String effect;
        // [임시 효과 규칙] 종류에 따라 power를 서로 다르게 적용합니다.
        switch (card.type()) {
            case ATTACK -> {
                int damage = card.power() + bonus;
                enemyHp = Math.max(0, enemyHp - damage);
                bonus = 0;
                effect = "적에게 피해 " + damage;
            }
            case DEFENSE -> {
                block += card.power();
                effect = "방어도 +" + card.power();
            }
            case HEAL -> {
                int before = playerHp;
                playerHp = Math.min(PLAYER_MAX_HP, playerHp + card.power());
                effect = "체력 " + (playerHp - before) + " 회복";
            }
            case SPELL -> {
                bonus += card.power();
                effect = "이번 턴 다음 공격 피해 +" + card.power();
            }
            default -> throw new IllegalStateException();
        }
        return card.name() + " · " + effect + (enemyHp == 0 ? " — 승리!" : "");
    }

    public String endTurn() {
        // 적 공격 - 방어도만큼 피해. 음수 피해는 0으로 제한합니다.
        // 살아 있으면 다음 턴: 에너지 3, 고정 손패로 갱신합니다.
        if (isOver()) return "전투가 끝났습니다.";
        int damage = Math.max(0, enemyIntent() - block);
        playerHp = Math.max(0, playerHp - damage);
        block = bonus = 0;
        if (playerHp == 0) return "적의 공격! 피해 " + damage + " — 패배";
        turn++;
        energy = 3;
        refill();
        return "적의 공격! 피해 " + damage + " · 새 턴 시작";
    }

    public int playerHp() { return playerHp; }
    public int enemyHp() { return enemyHp; }
    public int energy() { return energy; }
    public int block() { return block; }
    public int bonus() { return bonus; }
    public int turn() { return turn; }
    public int enemyIntent() { return turn % 3 == 0 ? 12 : 7; }
    public boolean isOver() { return playerHp == 0 || enemyHp == 0; }
    public List<Card> hand() { return List.copyOf(hand); }
}
