package game.backend.model;

/**
 * 플레이어의 전투 상태를 관리하는 클래스입니다.
 *
 * <p>중요한 역할 분리:</p>
 * <ul>
 *   <li>Swing 화면을 전혀 알지 않습니다.</li>
 *   <li>카드가 어떻게 그려지는지도 알지 않습니다.</li>
 *   <li>플레이어의 HP, 에너지, 방어도처럼 "플레이어에게 속한 값"만 관리합니다.</li>
 * </ul>
 *
 * <p>즉, 프론트(UI)는 이 객체의 값을 보여주고,
 * Battle은 이 객체의 값을 변경하는 구조입니다.</p>
 */
public final class Player {
    /** 전투 시작 시 플레이어 최대 체력 */
    public static final int MAX_HP = 40;

    /** 턴마다 회복되는 기본 에너지 */
    public static final int MAX_ENERGY = 3;

    private int maxHp = MAX_HP;
    private int hp;
    private int energy;
    private int block;
    private int bonusDamage;

    public Player() {
        reset();
    }

    /** 플레이어를 전투 시작 상태로 되돌립니다. */
    public void reset() {
        hp = maxHp;
        energy = MAX_ENERGY;
        block = 0;
        bonusDamage = 0;
    }

    /** 이번 전투의 최대 체력 (기본 40 + 상점 강화). */
    public int maxHp() {
        return maxHp;
    }

    /** 최대 체력을 바꿉니다. 다음 reset() 부터 이 값으로 시작합니다. */
    public void setMaxHp(int maxHp) {
        if (maxHp <= 0) throw new IllegalArgumentException("최대 체력은 1 이상이어야 합니다.");
        this.maxHp = maxHp;
    }

    public int hp() {
        return hp;
    }

    public int energy() {
        return energy;
    }

    public int block() {
        return block;
    }

    /** 현재 턴에 다음 공격에 추가할 피해량입니다. */
    public int bonusDamage() {
        return bonusDamage;
    }

    /** 카드 사용 시 에너지를 차감합니다. */
    public void spendEnergy(int amount) {
        if (amount < 0 || amount > energy) {
            throw new IllegalArgumentException("사용할 수 있는 에너지보다 큰 비용입니다.");
        }
        energy -= amount;
    }

    /** 턴 시작 시 에너지를 최대치로 회복합니다. */
    public void refillEnergy() {
        energy = MAX_ENERGY;
    }

    /** 방어도를 증가시킵니다. */
    public void addBlock(int amount) {
        if (amount < 0) throw new IllegalArgumentException("방어도는 음수가 될 수 없습니다.");
        block += amount;
    }

    /** 현재 방어도를 모두 제거합니다. */
    public void clearBlock() {
        block = 0;
    }

    /** 다음 공격에 추가 피해를 부여합니다. */
    public void addBonusDamage(int amount) {
        if (amount < 0) throw new IllegalArgumentException("추가 피해는 음수가 될 수 없습니다.");
        bonusDamage += amount;
    }

    /** 공격에 사용한 추가 피해 보너스를 소비합니다. */
    public int consumeBonusDamage() {
        int result = bonusDamage;
        bonusDamage = 0;
        return result;
    }

    /** HP를 회복합니다. 최대 HP를 넘지 않도록 제한합니다. */
    public int heal(int amount) {
        if (amount < 0) throw new IllegalArgumentException("회복량은 음수가 될 수 없습니다.");
        int before = hp;
        hp = Math.min(maxHp, hp + amount);
        return hp - before;
    }

    /**
     * 적의 공격을 받습니다.
     * 방어도가 먼저 피해를 흡수하고, 남은 피해만 HP에 적용됩니다.
     *
     * @param incomingDamage 적이 주는 원래 피해량
     * @return 실제로 HP가 감소한 피해량
     */
    public int takeDamage(int incomingDamage) {
        if (incomingDamage < 0) throw new IllegalArgumentException("피해량은 음수가 될 수 없습니다.");

        int blocked = Math.min(block, incomingDamage);
        block -= blocked;

        int actualDamage = incomingDamage - blocked;
        hp = Math.max(0, hp - actualDamage);
        return actualDamage;
    }
}
