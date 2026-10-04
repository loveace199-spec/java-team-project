package game.backend.model;

/** 카드 정의. 전투 상태와 Swing 컴포넌트에 의존하지 않습니다. */
// record는 카드 정보를 묶어 보관하는 변경 불가능한 데이터 구조입니다.
// id: 내부 구분용 / name: 화면 이름 / type: 종류 / cost: 비용 / power: 효과 수치.
public record Card(String id, String name, CardType type, int cost, int power,
                   String description) {
    public Card {
        if (id == null || name == null || type == null || description == null
                || cost < 0 || power < 0) {
            throw new IllegalArgumentException("카드 정보를 확인하세요.");
        }
    }
}
