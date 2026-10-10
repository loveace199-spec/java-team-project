package game.backend.model;

import java.util.*;
import game.database.CardCatalog;
import game.database.SaveStore;

/** 변경: 기존 덱 구성 규칙은 유지하고 조회·저장을 DB에 연결합니다. */
public final class PlayerDeck {
    public static final int SIZE=20, MAX_COPIES=2;
    // 추가: 시작 화면에서 전달한 현재 게임의 저장 객체를 공유합니다.
    private final SaveStore store;
    private List<Card> cards;
    // 추가: 기존 시안·검사의 기본 생성자 호출도 유지합니다.
    public PlayerDeck() { this(new SaveStore()); }
    // 변경: 기본 카드 목록 대신 현재 게임의 덱을 DB에서 읽습니다.
    public PlayerDeck(SaveStore store) {
        this.store=store;
        cards=store.loadDeck();
        if(!valid(cards)) throw new IllegalStateException("DB의 덱 구성을 확인하세요.");
    }
    public List<Card> cards() { return cards; }
    public static boolean valid(List<Card> candidate) {
        if(candidate==null || candidate.size()!=SIZE) return false;
        Map<String,Integer> counts=new HashMap<>();
        for(Card card:candidate) {
            if(!CardCatalog.allCards().contains(card)) return false;
            if(counts.merge(card.id(),1,Integer::sum)>MAX_COPIES) return false;
        }
        return true;
    }
    public void save(List<Card> candidate) {
        if(!valid(candidate)) throw new IllegalArgumentException("덱은 정확히 20장, 같은 카드는 최대 2장입니다.");
        // 추가: DB 저장 성공 후에만 현재 실행 중의 덱도 바꿉니다.
        store.saveDeck(candidate);
        cards=List.copyOf(candidate);
    }
}
