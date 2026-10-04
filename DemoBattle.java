package prototype.demo;

import java.util.List;
import prototype.backend.Battle;
import prototype.model.Card;

/**
 * 기존 프론트와 테스트 코드를 깨뜨리지 않기 위한 호환 클래스입니다.
 *
 * <p>기존 프로젝트에서는 UI가 DemoBattle을 직접 사용하고 있었기 때문에
 * 한 번에 모든 프론트 코드를 수정하면 오류가 생길 가능성이 있습니다.
 * 그래서 DemoBattle의 기존 메서드 이름은 그대로 유지하고,
 * 실제 계산은 새 Battle 클래스에 맡깁니다.</p>
 *
 * <p>즉, 이 클래스 자체는 더 이상 게임 규칙을 계산하지 않습니다.</p>
 */
public final class DemoBattle {
    public static final int PLAYER_MAX_HP = Battle.PLAYER_MAX_HP;
    public static final int ENEMY_MAX_HP = Battle.ENEMY_MAX_HP;

    private final Battle battle = new Battle();

    public DemoBattle() {
        // Battle 생성자가 이미 초기화를 수행합니다.
    }

    public void reset() {
        battle.reset();
    }

    public boolean canPlay(int index) {
        return battle.canPlay(index);
    }

    public String play(int index) {
        return battle.play(index);
    }

    public String endTurn() {
        return battle.endTurn();
    }

    public int playerHp() {
        return battle.playerHp();
    }

    public int enemyHp() {
        return battle.enemyHp();
    }

    public int energy() {
        return battle.energy();
    }

    public int block() {
        return battle.block();
    }

    public int bonus() {
        return battle.bonus();
    }

    public int turn() {
        return battle.turn();
    }

    public int enemyIntent() {
        return battle.enemyIntent();
    }

    public boolean isOver() {
        return battle.isOver();
    }

    public List<Card> hand() {
        return battle.hand();
    }
}
