package game.database;

import game.backend.model.Card;
import game.backend.model.ShopItem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 변경: Properties 파일 저장을 외부 SQL 파일 기반 JDBC 저장으로 교체합니다. */
public final class SaveStore {
    // 추가: SELECT 결과 한 행을 기존 Java 객체로 변환합니다.
    @FunctionalInterface
    public interface Row<T> { T read(ResultSet result) throws SQLException; }
    // 추가: 진행도·덱·상점이 같은 게임의 DB 번호를 공유합니다.
    private final long gameId;
    private final long deckId;

    // 추가: 프로젝트 최상위의 .env를 처음 필요할 때 한 번만 읽습니다.
    // IDE의 실행 폴더가 다르면 -Dgame.env.file=파일경로로 위치를 지정할 수 있습니다.
    private static final class EnvFile {
        private static final Map<String, String> VALUES = readEnvFile(
            Path.of(System.getProperty("game.env.file", ".env")));
    }

    // 추가: 외부 라이브러리 없이 UTF-8의 KEY=VALUE 형식을 읽습니다.
    // 빈 줄·#으로 시작한 설명 줄은 건너뛰고, 값 안의 #·=·백슬래시는 그대로 둡니다.
    private static Map<String, String> readEnvFile(Path file) {
        if (Files.notExists(file)) return Map.of();
        try {
            Map<String, String> values = new HashMap<>();
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (i == 0 && line.startsWith("\uFEFF")) line = line.substring(1);
                line = line.strip();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("export ")) line = line.substring(7).stripLeading();
                int separator = line.indexOf('=');
                if (separator < 1) throw invalidEnvLine(file, i + 1);
                String key = line.substring(0, separator).strip();
                if (!key.matches("[A-Za-z_][A-Za-z0-9_]*")) throw invalidEnvLine(file, i + 1);
                String value = line.substring(separator + 1).strip();
                if (value.startsWith("\"") || value.startsWith("'")) {
                    char quote = value.charAt(0);
                    if (value.length() < 2 || value.charAt(value.length() - 1) != quote)
                        throw invalidEnvLine(file, i + 1);
                    value = value.substring(1, value.length() - 1);
                }
                values.put(key, value);
            }
            return Map.copyOf(values);
        } catch (IOException e) {
            throw new IllegalStateException(".env 파일을 읽지 못했습니다: " + file, e);
        }
    }

    // 추가: 잘못된 설정의 위치만 알리고 비밀번호 등 실제 값은 오류에 출력하지 않습니다.
    private static IllegalArgumentException invalidEnvLine(Path file, int line) {
        return new IllegalArgumentException(".env 형식을 확인하세요: " + file + " (" + line + "번째 줄)");
    }

    // 변경: 실행 옵션 > 운영체제 환경 변수 > .env > 기존 기본값 순서로 접속 정보를 찾습니다.
    private static String dbSetting(String property, String key, String fallback) {
        String value = System.getProperty(property);
        if (value != null) return value;
        value = System.getenv(key);
        if (value != null) return value;
        return EnvFile.VALUES.getOrDefault(key, fallback);
    }

    // 변경: 사용자가 작성한 .env의 GAME_DB_* 값으로도 DB에 연결할 수 있습니다.
    private static Connection open() throws SQLException {
        return DriverManager.getConnection(
            dbSetting("game.db.url", "GAME_DB_URL", "jdbc:mariadb://localhost:3306/testdb"),
            dbSetting("game.db.user", "GAME_DB_USER", "root"),
            dbSetting("game.db.password", "GAME_DB_PASSWORD", ""));
    }

    // 추가: database 폴더의 SQL 파일을 UTF-8로 읽고 ?에 값을 순서대로 넣습니다.
    private static PreparedStatement prepare(Connection connection, String name, Object... values)
            throws SQLException {
        String sql;
        try (var input = SaveStore.class.getResourceAsStream("/game/database/" + name + ".sql")) {
            if (input == null) throw new IOException("SQL file not found: " + name);
            sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Cannot read SQL: " + name, e);
        }
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        try {
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        } catch (SQLException e) {
            statement.close();
            throw e;
        }
    }

    // 추가: 조회 결과를 객체로 반환하고 사용한 JDBC 자원을 닫습니다.
    public static <T> List<T> select(String name, Row<T> mapper, Object... values) {
        try (Connection connection = open();
             PreparedStatement statement = prepare(connection, name, values);
             ResultSet result = statement.executeQuery()) {
            List<T> rows = new ArrayList<>();
            while (result.next()) rows.add(mapper.read(result));
            return List.copyOf(rows);
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed: " + name, e);
        }
    }

    // 추가: 진행도·증강의 단건 저장은 한 행이 반영됐는지도 확인합니다.
    private static void update(String name, Object... values) {
        try (Connection connection = open();
             PreparedStatement statement = prepare(connection, name, values)) {
            if (statement.executeUpdate() != 1) throw new SQLException("Expected one changed row: " + name);
        } catch (SQLException e) {
            throw new IllegalStateException("Database save failed: " + name, e);
        }
    }

    // 추가: AUTO_INCREMENT로 생성된 gameId와 deckId를 받아옵니다.
    private static long insertId(Connection connection, String name, Object... values)
            throws SQLException {
        try (PreparedStatement statement = prepare(connection, name, values)) {
            statement.executeUpdate();
            try (ResultSet result = statement.getGeneratedKeys()) {
                if (!result.next()) throw new SQLException("Generated ID missing: " + name);
                return result.getLong(1);
            }
        }
    }

    // 변경: 실행마다 새 게임을 만들고 게임·덱·20장 입력을 한 트랜잭션으로 저장합니다.
    public SaveStore() {
        // 추가: 카드 조회에 실패하면 게임 행을 만들기 전에 중단합니다.
        List<Card> initialCards = CardCatalog.allCards();
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                gameId = insertId(connection, "new_game");
                deckId = insertId(connection, "ingame_deck", gameId);
                insertCards(connection, initialCards);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot create game", e);
        }
    }

    // 추가: 현재 게임의 완료 단계 수를 조회합니다.
    public int loadCleared() {
        return select("select_stage_connect", row -> row.getInt("clearedStage"), gameId).getFirst();
    }

    // 추가: connect_game.sql에 완료 단계, gameId 순서로 전달합니다.
    public void saveCleared(int cleared) {
        update("connect_game", cleared, gameId);
    }

    // 추가: JOIN 결과를 기존 Card 객체에 연결하며 같은 카드 두 장도 유지합니다.
    public List<Card> loadDeck() {
        return select("select_join_gamedeck", row -> CardCatalog.byCode(row.getString("cardCode")), gameId);
    }

    // 추가: crafted-N 코드 대신 숫자 cardId로 카드 한 장마다 행을 넣습니다.
    private void insertCards(Connection connection, List<Card> cards) throws SQLException {
        try (PreparedStatement statement = prepare(connection, "game_card", deckId, 0L)) {
            for (Card card : cards) {
                statement.setLong(2, CardCatalog.databaseId(card));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    // 추가: 기존 덱 삭제와 새 카드 입력은 함께 성공하거나 함께 취소됩니다.
    public void saveDeck(List<Card> cards) {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = prepare(connection, "deck_modify_card", deckId)) {
                statement.executeUpdate();
                insertCards(connection, cards);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot save deck", e);
        }
    }

    // 추가: 선택한 증강이 없으면 null을 반환합니다.
    public ShopItem loadUpgrade() {
        var items = select("select_reinforce_game",
            row -> ShopItem.valueOf(row.getString("reinforceCode")), gameId);
        return items.isEmpty() ? null : items.getFirst();
    }

    // 추가: choose_reinforce.sql에는 숫자 reinforceId와 gameId를 전달합니다.
    public void saveUpgrade(ShopItem item) {
        update("choose_reinforce", item.databaseId(), gameId);
    }
}
