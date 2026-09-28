package prototype.data;

import java.util.List;
import prototype.model.Card;
import prototype.model.CardType;

/** 카드 이름, 비용, 효과 설명을 수정하는 곳입니다. 수치는 모두 임시입니다. */
public final class CardCatalog {
    private CardCatalog() { }

    public static List<Card> sampleHand() {
        // [카드 제작] new Card(식별자, 표시 이름, 종류, 에너지 비용, 효과 수치, 설명).
        // power의 의미는 종류마다 다릅니다: 피해 / 방어도 / 회복량 / 다음 공격 추가 피해.
        // 설명은 수치에서 자동 생성되지 않습니다. 수치를 바꾸면 설명도 함께 수정하세요.
        return List.of(
            new Card("slash", "베기", CardType.ATTACK, 1, 6, "적에게 피해 6"),
            new Card("guard", "방패", CardType.DEFENSE, 1, 5, "방어도 5 획득"),
            new Card("heal", "응급처치", CardType.HEAL, 1, 4, "체력 4 회복"),
            new Card("focus", "집중", CardType.SPELL, 0, 2, "이번 턴 다음 공격 피해 +2"),
            new Card("heavy", "강타", CardType.ATTACK, 2, 11, "적에게 피해 11")
        );
    }
}
