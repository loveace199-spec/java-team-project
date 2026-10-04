package game.database;

import game.backend.model.Card;
import game.backend.model.CardType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * [database] 카드 정의 데이터를 불러옵니다.
 * 카드 이름·비용·수치는 코드가 아니라 src/data/cards.csv 에서 수정합니다.
 * 외부 라이브러리 없이 Java 기본 기능만 사용합니다.
 */
public final class CardCatalog {
    public static final String CARD_FILE = "/data/cards.csv";
    private static List<Card> cache;

    private CardCatalog() { }

    /** 전체 카드 정의 목록 (파일 순서 그대로). 처음 한 번만 파일을 읽습니다. */
    public static synchronized List<Card> all() {
        if (cache == null) {
            try (InputStream in = CardCatalog.class.getResourceAsStream(CARD_FILE)) {
                if (in == null) throw new IllegalStateException("카드 데이터 파일이 없습니다: " + CARD_FILE);
                cache = Collections.unmodifiableList(parse(in));
            } catch (IOException e) {
                throw new IllegalStateException("카드 데이터를 읽지 못했습니다.", e);
            }
        }
        return cache;
    }

    /** 식별자로 카드 찾기. 없으면 예외. */
    public static Card byId(String id) {
        for (Card card : all()) if (card.id().equals(id)) return card;
        throw new IllegalArgumentException("없는 카드 식별자: " + id);
    }

    /** 시제품 손패: 매 턴 같은 카드 5장. 실제 덱/뽑기 구현 시 backend 에서 교체합니다. */
    public static List<Card> sampleHand() {
        return all();
    }

    /** CSV 내용을 카드 목록으로 변환. 잘못된 줄은 몇 번째 줄인지 알려 줍니다. */
    public static List<Card> parse(InputStream in) throws IOException {
        List<Card> cards = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            line = line.replace("﻿", "").trim(); // 메모장 저장 시 붙는 BOM 제거
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] f = line.split(",", -1);
            try {
                if (f.length != 6) throw new IllegalArgumentException("항목 수가 6개가 아닙니다.");
                cards.add(new Card(f[0].trim(), f[1].trim(), CardType.valueOf(f[2].trim()),
                    Integer.parseInt(f[3].trim()), Integer.parseInt(f[4].trim()), f[5].trim()));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(CARD_FILE + " " + lineNo + "번째 줄 오류: " + e.getMessage(), e);
            }
        }
        return cards;
    }
}
