package game.database;

import java.util.Map;
import java.util.stream.Collectors;

/** 변경: 단계별 적의 이름·이미지를 STAGE_ENEMY 테이블에서 읽습니다. */
public record StageEnemy(String name, String imagePath) {
    // 추가: 단계 번호와 적 정보를 연결합니다.
    private record Entry(int stage, StageEnemy enemy) { }
    // 변경: 고정 switch 대신 select_stage.sql 조회 결과를 한 번 보관합니다.
    private static final Map<Integer, StageEnemy> ENEMIES =
        SaveStore.select("select_stage", row -> new Entry(row.getInt("stageNo"),
            new StageEnemy(row.getString("enemyName"), row.getString("imagePath"))))
            .stream().collect(Collectors.toMap(Entry::stage, Entry::enemy));

    // 변경: 기존 화면 호출은 유지하고 DB에서 읽은 정보를 반환합니다.
    public static StageEnemy forStage(int stage) {
        StageEnemy enemy = ENEMIES.get(stage);
        if (enemy == null) throw new IllegalArgumentException("전투 단계가 아닙니다: " + stage);
        return enemy;
    }
}
