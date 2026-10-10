package game.backend.model;

import game.database.SaveStore;
import java.util.Map;
import java.util.stream.Collectors;

/** 변경: 강화 종류는 유지하고 이름·설명·DB 번호는 REINFORCE에서 읽습니다. */
public enum ShopItem {
    ATTACK_UP, DEFENSE_UP, HEAL_10;

    // 추가: DB의 숫자 PK와 Java의 enum 코드를 구분해서 보관합니다.
    private record Text(long databaseId, String code, String label, String description) { }
    // 변경: 고정 이름·설명을 select_reinforce.sql 조회 결과로 교체합니다.
    private static final Map<String, Text> TEXTS =
        SaveStore.select("select_reinforce", row -> new Text(row.getLong("reinforceId"), row.getString("reinforceCode"),
            row.getString("reinforceName"), row.getString("reinforceEffect")))
            .stream().collect(Collectors.toMap(Text::code, text -> text));

    // 추가: INVENTORY 저장에 사용할 숫자 reinforceId를 반환합니다.
    public long databaseId() { return TEXTS.get(name()).databaseId(); }
    // 변경: 화면의 기존 호출은 유지하고 DB에 등록된 글자를 반환합니다.
    public String label() { return TEXTS.get(name()).label(); }
    public String description() { return TEXTS.get(name()).description(); }
}
