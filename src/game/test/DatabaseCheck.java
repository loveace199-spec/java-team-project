package game.test;

import game.backend.model.CardType;
import game.database.CardCatalog;
import game.database.SaveStore;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** database 영역 검사: 카드 데이터 파일 읽기와 저장/불러오기. */
public final class DatabaseCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        check(CardCatalog.all().size() == 5, "카드 5장 로드");
        check(CardCatalog.byId("heavy").power() == 11, "강타 수치");
        check(CardCatalog.byId("focus").type() == CardType.SPELL, "집중 종류");

        String broken = "bad,잘못된 카드,ATTACK,1\n";
        try {
            CardCatalog.parse(new ByteArrayInputStream(broken.getBytes(StandardCharsets.UTF_8)));
            throw new AssertionError("잘못된 줄을 거부해야 함");
        } catch (IllegalStateException expected) {
            check(expected.getMessage().contains("1번째 줄"), "오류 줄 번호 안내");
        }

        Path dir = Files.createTempDirectory("save-check");
        Path file = dir.resolve("save.properties");
        SaveStore store = new SaveStore(file);
        check(store.getInt("stage.cleared", 0) == 0, "첫 실행 기본값");
        store.setInt("stage.cleared", 3);
        store.save();
        check(new SaveStore(file).getInt("stage.cleared", 0) == 3, "저장 후 다시 읽기");

        Files.write(file, new byte[]{'a', '=', '\\', 'u', 'Z', 'Z'}); // 손상된 파일
        SaveStore recovered = new SaveStore(file);
        check(recovered.getInt("stage.cleared", 0) == 0, "손상 파일은 기본값");
        check(Files.exists(dir.resolve("save.properties.broken")), "손상 파일 원본 보존");
        System.out.println("PASS: database (card data + save store)");
    }
}
