package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.Timer;
import game.backend.battle.DemoBattle;
import game.database.StageEnemy;

/** 12시의 상대와 6시의 플레이어. 배경/초상화는 교체 가능한 임시 도형입니다. */
public final class BattleBoardPanel extends JPanel {
    private final DemoBattle battle;
    // 완성한 전투 필드 시안을 보드 크기에 맞춰 배경으로 사용합니다.
    private final BufferedImage battlefield;
    // [상대 이미지 교체] src/assets의 파일과 아래 리소스 이름을 함께 변경하세요.
    private final BufferedImage enemyImage;
    public static final String PLAYER_IMAGE = "/assets/player-hooded-knight-v1.png";
    private final BufferedImage playerImage;
    private final StageEnemy enemy;
    // Swing Timer가 약 60 FPS로 repaint()를 요청합니다. 그림은 paintComponent에서만 갱신합니다.
    private final Timer animationTimer;
    private long animationStartedAt;

    // ── 피격·회복 연출 ──
    // 체력이 바뀌면(맞거나 회복하면) 그 순간을 기록해 두고, 몇 백 ms 동안 흔들림·번쩍임·숫자 떠오름을 그립니다.
    private static final long SHAKE_MS = 380, FLASH_MS = 450, FLOAT_MS = 1000;
    private record HpChange(boolean enemy, int amount, long startedAt) { }
    private final java.util.List<HpChange> hpChanges = new java.util.ArrayList<>();
    private int lastEnemyHp = -1, lastPlayerHp = -1;
    private int turnSeconds = 30;
    private int turnSecondsMax = 30;
    private boolean turnTimerRunning;

    public BattleBoardPanel(DemoBattle battle) {
        this(battle, 1);
    }

    public BattleBoardPanel(DemoBattle battle, int stage) {
        this.battle = battle;
        enemy = StageEnemy.forStage(stage);
        battlefield = loadImage("/assets/battlefield-hidden-opponent-hand-v1.png", "전투 필드 배경");
        enemyImage = loadImage(enemy.imagePath(), enemy.name());
        // 플레이어 초상화: 원본(1254×1254)에서 얼굴·어깨 부분만 잘라 사용합니다.
        BufferedImage player = loadImage(PLAYER_IMAGE, "플레이어 이미지");
        playerImage = player.getSubimage(290, 40, Math.min(675, player.getWidth() - 290), Math.min(515, player.getHeight() - 40));
        animationTimer = new Timer(16, event -> repaint());
        animationTimer.setCoalesce(true);
        setPreferredSize(new Dimension(1000, 360));
    }

    private BufferedImage loadImage(String resourceName, String displayName) {
        // Eclipse가 새 리소스를 bin 폴더에 아직 복사하지 않았으면 src/assets에서 한 번 더 찾습니다.
        InputStream classpathStream = getClass().getResourceAsStream(resourceName);
        Path sourcePath = Path.of("src", resourceName.substring(1));
        if (!Files.isRegularFile(sourcePath)) {
            // 실행 작업 폴더가 프로젝트 밖이어도, 로드된 bin 폴더를 기준으로 src를 찾습니다.
            try {
                Path output = Path.of(getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                Path projectRoot = Files.isDirectory(output) ? output.getParent() : output.getParent().getParent();
                if (projectRoot != null) sourcePath = projectRoot.resolve("src").resolve(resourceName.substring(1));
            } catch (Exception ignored) {
                // 아래의 자세한 파일 없음 메시지에서 최종 경로를 안내합니다.
            }
        }
        try (InputStream stream = classpathStream != null
                ? classpathStream
                : (Files.isRegularFile(sourcePath) ? Files.newInputStream(sourcePath) : null)) {
            if (stream == null) {
                throw new IllegalStateException(displayName + " 파일이 없습니다: " + sourcePath.toAbsolutePath());
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) throw new IllegalStateException(displayName + " 파일을 읽을 수 없습니다.");
            return image;
        } catch (IOException e) {
            throw new IllegalStateException(displayName + " 로드 실패", e);
        }
    }

    @Override public void addNotify() {
        super.addNotify();
        // 화면에 들어올 때 등장 애니메이션을 처음부터 시작합니다.
        animationStartedAt = System.nanoTime();
        animationTimer.start();
    }

    @Override public void removeNotify() {
        // 다른 화면으로 이동했을 때 불필요하게 repaint()가 반복되지 않도록 멈춥니다.
        animationTimer.stop();
        super.removeNotify();
    }

    @Override protected void paintComponent(Graphics graphics) {
        // [배경 수정] src/assets의 완성 이미지를 패널 전체 크기에 맞춰 그립니다.
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth(), h = getHeight(), cx = w / 2;
        g.drawImage(battlefield, 0, 0, w, h, null);
        // 배경의 고정 카드 그림을 불투명 패널로 가리고 실제 손패만 표시합니다.
        paintCardAreas(g,w,h);

        // 배경 왼쪽의 원형 타이머 자리에 남은 시간과 진행 링을 그립니다.
        paintTurnTimer(g, w, h);

        // 중앙 상태 문구는 배경 위에서도 읽히도록 반투명 판을 깔아 줍니다.
        g.setColor(new Color(6, 12, 18, 155));
        g.fillRoundRect(cx - 112, h / 2 - 25, 224, 48, 24, 24);
        g.setFont(Theme.font(Font.BOLD, 15));
        center(g, battle.isOver() ? (battle.playerWon() ? "전투 승리" : "전투 패배") : "내 턴 · 카드 선택", cx, h / 2 + 6, Theme.TEXT);
        // 배경 원본의 빈 캐릭터 프레임 중심 비율을 사용합니다.
        // 고정 픽셀 좌표가 아니므로 창 크기를 바꿔도 두 캐릭터가 각 프레임 안에 유지됩니다.
        int enemyY = (int) (h * 0.105);
        int playerY = (int) (h * 0.845);
        trackHpChanges();
        portrait(g, cx, enemyY, true, battle.enemyHp(), DemoBattle.ENEMY_MAX_HP);
        portrait(g, cx, playerY, false, battle.playerHp(), battle.playerMaxHp());
        // 초상화 옆 이름·상태 글자는 화면을 깔끔하게 하려고 표시하지 않습니다.
        paintFloatingNumbers(g, cx, enemyY, playerY);
        g.dispose();
    }

    public void setTurnTimer(int seconds, int maxSeconds, boolean running) {
        turnSeconds = Math.max(0, seconds);
        turnSecondsMax = Math.max(1, maxSeconds);
        turnTimerRunning = running;
        repaint();
    }

    /** 배경에 박혀 있던 카드 그림을 가리고 현재 손패 수만큼만 뒷면을 그립니다. */
    private void paintCardAreas(Graphics2D g,int w,int h) {
        CardTrayPainter.paint(g,CardTrayPainter.bounds(w,h,true),"상대 카드",battle.enemyHandCount(),battle.enemyDrawCount(),battle.enemyDiscardCount(),true);
        CardTrayPainter.paint(g,CardTrayPainter.bounds(w,h,false),"내 카드",battle.hand().size(),battle.drawCount(),battle.discardCount(),false);
    }

    private void paintTurnTimer(Graphics2D g, int w, int h) {
    	// 타이머 중심 위치
        int x = (int) (w * 0.087);
        int y = (int) (h * 0.435);

        // 타이머 가로·세로 크기
        int timerWidth = 158;
        int timerHeight = 172;

        // 원형 선이 프레임과 겹치지 않도록 안쪽 여백 설정
        int horizontalMargin = 8;
        int verticalMargin = 8;

        int ringWidth = timerWidth - horizontalMargin * 2;
        int ringHeight = timerHeight - verticalMargin * 2;

        // 중심 좌표를 왼쪽 위 시작 좌표로 변환
        int timerX = x - ringWidth / 2;
        int timerY = y - ringHeight / 2;

        double ratio = turnSeconds / (double) turnSecondsMax;

        // 원형 선 두께
        g.setStroke(new BasicStroke(
            5f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND
        ));

        // 타이머 기본 테두리
        g.setColor(new Color(22, 29, 37, 210));
        g.drawOval(
            timerX,
            timerY,
            ringWidth,
            ringHeight
        );

        // 남은 시간에 따라 줄어드는 진행 테두리
        g.setColor(
            turnTimerRunning
                ? new Color(67, 205, 235)
                : new Color(132, 147, 157)
        );

        g.drawArc(
            timerX,
            timerY,
            ringWidth,
            ringHeight,
            90,
            (int) (-360 * ratio)
        );

        // 남은 시간 숫자
        String time = Integer.toString(turnSeconds);

        // 가로와 세로 중 작은 값을 기준으로 글자 크기 계산
        int fontSize = Math.max(
            24,
            Math.min(timerWidth, timerHeight) / 3
        );

        g.setFont(Theme.font(Font.BOLD, fontSize));

        // 숫자를 타이머 중앙에 표시
        center(
            g,
            time,
            x,
            y + g.getFontMetrics().getAscent() / 3,
            Color.WHITE
        );
    }
    private void portrait(Graphics2D g, int x, int y, boolean enemy, int hp, int max) {
        // enemy=true는 상대 색상, false는 플레이어 색상입니다.
        // 체력바 길이는 현재 체력 / 최대 체력의 비율로 계산합니다.
        // 별도 바탕과 둥근 테두리를 그리지 않고 배경 이미지의 초상화 틀을 사용합니다.
        long now = System.nanoTime();
        x += shakeOffset(enemy, now); // 맞으면 좌우로 흔들림
        if (enemy) {
            // 등장 시 페이드인하고, 이후에는 위아래로 천천히 떠 있는 효과를 줍니다.
            double elapsed = Math.max(0, (System.nanoTime() - animationStartedAt) / 1_000_000_000.0);
            float fade = (float) Math.min(1.0, elapsed / 0.75);
            int floatY = (int) Math.round(Math.sin(elapsed * 2.2) * 4.0);
            Composite originalComposite = g.getComposite();

            // 투명 PNG를 비율 유지하여 초상화 안에 표시합니다.
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double scale = Math.min(124.0 / enemyImage.getWidth(), 94.0 / enemyImage.getHeight());
            int width = (int)(enemyImage.getWidth() * scale), height = (int)(enemyImage.getHeight() * scale);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
            g.drawImage(enemyImage, x - width / 2, y - 50 + floatY, width, height, null);
            g.setComposite(originalComposite);
        } else {
            // 플레이어 초상화 (src/assets/player-hooded-knight-v1.png)
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double scale = Math.min(124.0 / playerImage.getWidth(), 94.0 / playerImage.getHeight());
            int width = (int) (playerImage.getWidth() * scale), height = (int) (playerImage.getHeight() * scale);
            g.drawImage(playerImage, x - width / 2, y - 50, width, height, null);
        }
        paintFlash(g, x, y, enemy, now); // 맞으면 빨강, 회복하면 초록으로 번쩍임
        g.setColor(new Color(14, 21, 29));
        g.fillRoundRect(x - 62, y + 35, 124, 23, 12, 12);
        g.setColor(new Color(125, 48, 58));
        g.fillRoundRect(x - 60, y + 37, (int) (120 * hp / (double) max), 19, 10, 10);
        g.setFont(Theme.font(Font.BOLD, 13));
        center(g, "체력 " + hp + " / " + max, x, y + 51, Color.WHITE);
    }

    /** 재시작 등으로 체력이 한 번에 바뀔 때 연출이 나오지 않도록 기록을 지웁니다. */
    public void resetEffects() {
        hpChanges.clear();
        lastEnemyHp = -1;
        lastPlayerHp = -1;
    }

    /** 이전 그림 이후 체력이 바뀌었으면 연출을 하나 추가합니다. */
    private void trackHpChanges() {
        long now = System.nanoTime();
        int enemyHp = battle.enemyHp(), playerHp = battle.playerHp();
        if (lastEnemyHp >= 0 && enemyHp != lastEnemyHp) hpChanges.add(new HpChange(true, enemyHp - lastEnemyHp, now));
        if (lastPlayerHp >= 0 && playerHp != lastPlayerHp) hpChanges.add(new HpChange(false, playerHp - lastPlayerHp, now));
        lastEnemyHp = enemyHp;
        lastPlayerHp = playerHp;
        hpChanges.removeIf(c -> (now - c.startedAt()) / 1_000_000 > FLOAT_MS);
    }

    /** 가장 최근 피격 직후 짧게 좌우로 흔들리는 양(px). 점점 약해집니다. */
    private int shakeOffset(boolean enemy, long now) {
        for (int i = hpChanges.size() - 1; i >= 0; i--) {
            HpChange c = hpChanges.get(i);
            if (c.enemy() != enemy || c.amount() >= 0) continue;
            double ms = (now - c.startedAt()) / 1_000_000.0;
            if (ms > SHAKE_MS) return 0;
            return (int) Math.round(Math.sin(ms / 22.0) * 8 * (1 - ms / SHAKE_MS));
        }
        return 0;
    }

    /** 초상화 위에 빨간(피격)/초록(회복) 빛을 덮습니다. */
    private void paintFlash(Graphics2D g, int x, int y, boolean enemy, long now) {
        for (HpChange c : hpChanges) {
            if (c.enemy() != enemy) continue;
            double ms = (now - c.startedAt()) / 1_000_000.0;
            if (ms > FLASH_MS) continue;
            int alpha = (int) (150 * (1 - ms / FLASH_MS));
            Color color = c.amount() < 0 ? new Color(220, 40, 40, alpha) : new Color(80, 230, 130, alpha);
            Graphics2D f = (Graphics2D) g.create();
            f.setColor(color);
            f.fillRoundRect(x - 64, y - 52, 128, 98, 18, 18);
            if (c.amount() > 0) { // 회복은 바깥으로 퍼지는 고리도 함께
                int r = 50 + (int) (ms / FLASH_MS * 30);
                f.setStroke(new BasicStroke(3f));
                f.setColor(new Color(120, 255, 160, alpha));
                f.drawOval(x - r, y - 5 - r, r * 2, r * 2);
            }
            f.dispose();
        }
    }

    /** -5 / +8 같은 숫자가 초상화 옆에서 위로 떠오르며 사라집니다. */
    private void paintFloatingNumbers(Graphics2D g, int cx, int enemyY, int playerY) {
        long now = System.nanoTime();
        int[] stack = new int[2]; // 같은 쪽에 연속으로 생기면 조금씩 옆으로 비켜 그림
        for (HpChange c : hpChanges) {
            double t = (now - c.startedAt()) / 1_000_000.0 / FLOAT_MS;
            if (t < 0 || t > 1) continue;
            int side = c.enemy() ? 0 : 1;
            int baseY = c.enemy() ? enemyY : playerY;
            int x = cx + 78 + stack[side]++ * 34;
            // 적은 화면 위쪽 끝에 붙어 있어 조금 아래에서 시작합니다.
            int y = baseY + (c.enemy() ? 34 : -10) - (int) (t * 46);
            float alpha = (float) (t < 0.7 ? 1 : 1 - (t - 0.7) / 0.3);
            String text = (c.amount() > 0 ? "+" : "") + c.amount();
            Graphics2D f = (Graphics2D) g.create();
            f.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
            f.setFont(Theme.font(Font.BOLD, 30));
            f.setColor(new Color(0, 0, 0, 200)); // 글자 테두리(그림자)
            for (int dx = -2; dx <= 2; dx += 2) for (int dy = -2; dy <= 2; dy += 2) f.drawString(text, x + dx, y + dy);
            f.setColor(c.amount() < 0 ? new Color(255, 90, 80) : new Color(110, 240, 140));
            f.drawString(text, x, y);
            f.dispose();
        }
    }

    private void center(Graphics2D g, String text, int x, int y, Color color) {
        g.setColor(color);
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y);
    }
}
