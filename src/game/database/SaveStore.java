package game.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

/**
 * [database] 사용자 데이터(진행도·설정) 저장/불러오기.
 * 저장 위치: 사용자 폴더/.memory-tower/save.properties (게임 폴더·Git 저장소와 분리)
 * 손상된 파일은 save.properties.broken 으로 보존하고 기본값으로 시작합니다.
 *
 * 아직 화면과 연결되지 않은 틀입니다. 연결 예:
 *   SaveStore store = new SaveStore();
 *   int cleared = store.getInt("stage.cleared", 0);
 *   store.setInt("stage.cleared", 3); store.save();
 */
public final class SaveStore {
    private final Path file;
    private final Properties values = new Properties();

    public SaveStore() {
        this(Paths.get(System.getProperty("user.home"), ".memory-tower", "save.properties"));
    }

    /** 테스트에서 임시 경로를 쓰기 위한 생성자. */
    public SaveStore(Path file) {
        this.file = file;
        load();
    }

    private void load() {
        if (!Files.exists(file)) return; // 첫 실행: 기본값 사용
        try (InputStream in = Files.newInputStream(file)) {
            values.load(in);
        } catch (IOException | IllegalArgumentException e) {
            values.clear();
            try {
                Files.copy(file, file.resolveSibling(file.getFileName() + ".broken"),
                    StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) { }
        }
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(values.getProperty(key, String.valueOf(defaultValue)).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public void setInt(String key, int value) {
        values.setProperty(key, String.valueOf(value));
    }

    public String getString(String key, String defaultValue) {
        return values.getProperty(key, defaultValue);
    }

    public void setString(String key, String value) {
        values.setProperty(key, value);
    }

    /** 파일에 기록. 오래 걸릴 수 있으므로 화면 스레드(EDT)에서 자주 호출하지 마세요. */
    public void save() throws IOException {
        Files.createDirectories(file.getParent());
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(temp)) {
            values.store(out, "memory-tower save");
        }
        Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
    }
}
