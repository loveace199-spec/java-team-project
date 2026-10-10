package game.backend.model;

public enum CardType {
    // enum = 허용되는 종류를 미리 정한 목록. 괄호 안은 화면에 표시할 한국어 이름입니다.
    ATTACK("공격"), DEFENSE("방어"), HEAL("회복"), SPELL("주문");

    private final String label;
    CardType(String label) { this.label = label; }
    public String label() { return label; }
}
