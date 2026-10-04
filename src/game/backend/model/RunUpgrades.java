package game.backend.model;

/**
 * 한 번의 도전(게임 실행) 동안 유지되는 강화 상태.
 * 상점에서 산 효과가 이후 모든 전투에 적용됩니다. 프로그램을 끄면 초기화됩니다.
 */
public final class RunUpgrades {
    private int attackBonus;
    private int defenseBonus;
    private int bonusHp;

    /** 상점 효과를 적용합니다. */
    public void apply(ShopItem item) {
        switch (item) {
            case ATTACK_UP -> attackBonus++;
            case DEFENSE_UP -> defenseBonus++;
            case HEAL_10 -> bonusHp += 10;
        }
    }

    /** 내 공격 카드 피해에 더해지는 값. */
    public int attackBonus() { return attackBonus; }
    /** 내 방어 카드 방어량에 더해지는 값. */
    public int defenseBonus() { return defenseBonus; }
    /** 전투 시작 체력·최대 체력에 더해지는 값. */
    public int bonusHp() { return bonusHp; }
}
