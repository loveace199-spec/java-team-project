package game.backend.model;

/** 상점에서 고를 수 있는 강화 3종. 이름은 상점 그림의 문구와 같습니다. */
public enum ShopItem {
    ATTACK_UP("공격력 카드 +1", "내 공격 카드의 피해가 1 증가합니다."),
    DEFENSE_UP("방어력 +1", "내 방어 카드로 막는 양이 1 증가합니다."),
    HEAL_10("즉시 회복 +10", "다음 전투부터 체력이 10 더 많은 상태로 시작합니다.");

    private final String label;
    private final String description;

    ShopItem(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String label() { return label; }
    public String description() { return description; }
}
