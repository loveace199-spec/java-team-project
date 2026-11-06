package game.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import game.backend.model.Card;
import game.backend.model.CardType;

/** 변경: Java에 적었던 카드 이름·수치를 CARD 테이블에서 읽어 보관합니다. */
public final class CardCatalog {
    private CardCatalog() { }

    public record Effect(int damage, int block, int heal, int poison, int duration) { }
    // 추가: DB 번호와 그림 번호를 함께 보관해 기존 Card 모델을 유지합니다.
    private record Entry(long databaseId, Card card, Effect effect, int artworkIndex) { }
    // 변경: 고정 이름·효과 배열 대신 select_card.sql을 한 번 읽어 캐시합니다.
    private static final List<Entry> ENTRIES = SaveStore.select("select_card", CardCatalog::read);
    private static final Map<String, Entry> BY_CODE = ENTRIES.stream()
        .collect(Collectors.toMap(entry -> entry.card().id(), Function.identity()));
    // 추가: 그림 번호가 음수인 구형 샘플은 게임용 기본 덱에서 제외합니다.
    private static final List<Card> CARDS = ENTRIES.stream()
        .filter(entry -> entry.artworkIndex() >= 0).map(Entry::card).toList();
    // 변경: 구형 시안용 샘플도 DB에서 읽고 초기 입력 순서를 유지합니다.
    private static final List<Card> SAMPLES = ENTRIES.stream()
        .filter(entry -> entry.artworkIndex() < 0)
        .sorted(java.util.Comparator.comparingLong(Entry::databaseId))
        .map(Entry::card).toList();

    // 추가: DB 열을 기존 Card와 Effect 객체로 변환합니다.
    private static Entry read(ResultSet row) throws SQLException {
        var effect = new Effect(row.getInt("damage"), row.getInt("block"),
            row.getInt("heal"), row.getInt("poison"), row.getInt("duration"));
        int artworkIndex = row.getInt("artworkIndex");
        CardType type = CardType.valueOf(row.getString("cardType"));
        // 추가: 구형 샘플의 방어·회복 power 의미도 기존 전투 코드와 맞춥니다.
        int power = artworkIndex >= 0 ? effect.damage() : switch (type) {
            case DEFENSE -> effect.block();
            case HEAL -> effect.heal();
            default -> effect.damage();
        };
        var card = new Card(row.getString("cardCode"), row.getString("cardName"),
            type, row.getInt("cost"), power, row.getString("cardEffect"));
        return new Entry(row.getLong("cardId"), card, effect, artworkIndex);
    }

    // 추가: 덱 JOIN 결과를 cardCode로 찾아 동일한 카드 정의를 사용합니다.
    public static Card byCode(String code) {
        Entry entry = BY_CODE.get(code);
        if (entry == null) throw new IllegalArgumentException("Unknown card: " + code);
        return entry.card();
    }

    // 추가: 덱 저장에 사용할 숫자 PK를 반환합니다.
    public static long databaseId(Card card) {
        return BY_CODE.get(card.id()).databaseId();
    }

    // 변경: 문자열 계산 대신 DB에 등록한 그림 번호를 사용합니다.
    public static int artworkIndex(Card card) {
        Entry entry = BY_CODE.get(card.id());
        return entry == null ? -1 : entry.artworkIndex();
    }

    // 변경: DB에서 읽은 효과를 반환하며 구형 샘플의 처리 방식은 유지합니다.
    public static Effect effect(Card card) {
        Entry entry = BY_CODE.get(card.id());
        return entry == null || entry.artworkIndex() < 0 ? null : entry.effect();
    }

    // 변경: 현재 게임에 필요한 카드 20종을 확인하고 기존 호출부에 반환합니다.
    public static List<Card> allCards() {
        if (CARDS.size() != 20) throw new IllegalStateException("CARD에 게임용 카드 20종을 등록하세요.");
        return CARDS;
    }

    // 변경: 구형 시안/검사에서 쓰는 샘플도 DB 초기 데이터로 공급합니다.
    public static List<Card> sampleHand() {
        if (SAMPLES.size() != 5) throw new IllegalStateException("legacy_sample_cards.sql의 샘플 5종을 등록하세요.");
        return SAMPLES;
    }
}
