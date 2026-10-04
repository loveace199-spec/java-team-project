package game.backend.model;

/**
 * 적 한 명의 전투 상태를 담당합니다.
 * 현재 프로토타입의 스켈레톤 병사를 기준으로 만들었지만,
 * 나중에 스테이지별 적 데이터를 넣어 확장할 수 있도록 분리했습니다.
 */
public final class Enemy {
    public static final int MAX_HP = 35;

    private int hp;

    public Enemy() {
        reset();
    }

    /** 적을 전투 시작 상태로 되돌립니다. */
    public void reset() {
        hp = MAX_HP;
    }

    public int hp() {
        return hp;
    }

    /** 적에게 피해를 줍니다. HP가 0 아래로 내려가지 않습니다. */
    public void takeDamage(int damage) {
        if (damage < 0) throw new IllegalArgumentException("피해량은 음수가 될 수 없습니다.");
        hp = Math.max(0, hp - damage);
    }

    public boolean isDefeated() {
        return hp == 0;
    }
}
