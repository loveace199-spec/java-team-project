package game.backend.model;

import java.util.*;
import game.database.CardCatalog;

/** 전투 사이에 편집하는 덱. 저장은 현재 실행 중에만 유지됩니다. */
public final class PlayerDeck {
    public static final int SIZE=20, MAX_COPIES=2;
    private List<Card> cards=CardCatalog.allCards();
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
        cards=List.copyOf(candidate);
    }
}
