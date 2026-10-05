package game.database;

import java.util.List;
import game.backend.model.Card;
import game.backend.model.CardType;

/** 카드 이름, 비용, 효과 설명을 수정하는 곳입니다. 수치는 모두 임시입니다. */
public final class CardCatalog {
    private CardCatalog() { }

    /** 첨부 이미지의 순서: 공격 / 방어 / 중독 주문 / 회복, 각 5종. */
    public record Effect(int damage, int block, int heal, int poison, int duration) { }
    private static final String[] NAMES = {
        "강철의 일격", "신속한 화살", "낡은 총탄", "연속 베기", "필살의 일격",
        "방패 올리기", "반격의 자세", "철벽 방어", "방패 돌격", "불굴의 수호",
        "독의 칼날", "맹독 화살", "독성 구름", "바이러스 주입", "죽음의 독",
        "치유의 손길", "신성한 일격", "회복의 기도", "생명의 의지", "축복의 심판"
    };
    private static final Effect[] EFFECTS = {
        new Effect(5,0,0,0,0), new Effect(4,0,0,0,0), new Effect(6,0,0,0,0), new Effect(8,0,0,0,0), new Effect(14,0,0,0,0),
        new Effect(2,3,0,0,0), new Effect(3,4,0,0,0), new Effect(4,6,0,0,0), new Effect(7,5,0,0,0), new Effect(8,10,0,0,0),
        new Effect(0,0,0,4,2), new Effect(0,0,0,5,2), new Effect(0,0,0,7,2), new Effect(0,0,0,6,3), new Effect(0,0,0,10,4),
        new Effect(3,0,5,0,0), new Effect(4,0,4,0,0), new Effect(6,0,10,0,0), new Effect(8,0,8,0,0), new Effect(12,0,15,0,0)
    };
    public static int artworkIndex(Card card) {
        for (int i=0;i<NAMES.length;i++) if (card.id().equals("crafted-"+i)) return i;
        return -1;
    }
    public static Effect effect(Card card) {
        int index=artworkIndex(card);
        return index<0 ? null : EFFECTS[index];
    }
    public static List<Card> allCards() {
        var cards=new java.util.ArrayList<Card>();
        for(int i=0;i<20;i++) {
            Effect e=EFFECTS[i];
            CardType type=new CardType[]{CardType.ATTACK,CardType.DEFENSE,CardType.SPELL,CardType.HEAL}[i/5];
            int cost=new int[]{1,1,i<5?1:2,2,3}[i%5];
            String description=e.poison()>0 ? e.duration()+"턴 동안 매 턴 중독 피해 "+e.poison()
                : (e.block()>0 ? "방어도 "+e.block()+" · " : "")+(e.heal()>0 ? "체력 "+e.heal()+" 회복 · " : "")+"피해 "+e.damage();
            cards.add(new Card("crafted-"+i,NAMES[i],type,cost,e.damage(),description));
        }
        return List.copyOf(cards);
    }

    public static List<Card> sampleHand() {
        // [카드 제작] new Card(식별자, 표시 이름, 종류, 등급(왼쪽 위 숫자), 효과 수치, 설명).
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
