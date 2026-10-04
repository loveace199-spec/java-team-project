package game.frontend.battle;

import game.backend.battle.Battle;
import game.frontend.common.Theme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

/** 12시의 상대와 6시의 플레이어. 배경/초상화는 교체 가능한 임시 도형입니다. */
public final class BattleBoardPanel extends JPanel {
    private final Battle battle;
    // [상대 이미지 교체] src/assets의 파일과 아래 리소스 이름을 함께 변경하세요.
    private final BufferedImage skeleton;
    public BattleBoardPanel(Battle battle) {
        this.battle = battle;
        try (var stream = getClass().getResourceAsStream("/assets/skeleton-soldier-v1.png")) {
            if (stream == null) throw new IllegalStateException("스켈레톤 이미지가 없습니다. 프로젝트를 새로고침하세요.");
            skeleton = ImageIO.read(stream);
            if (skeleton == null) throw new IllegalStateException("스켈레톤 이미지를 읽을 수 없습니다.");
        } catch (IOException e) { throw new IllegalStateException("상대 이미지 로드 실패", e); }
        setPreferredSize(new Dimension(1000, 360));
    }

    @Override protected void paintComponent(Graphics graphics) {
        // [배경 수정] 외부 이미지 없이 그라데이션과 선으로 그린 임시 전장입니다.
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), cx = w / 2;
        g.setPaint(new GradientPaint(0, 0, new Color(57, 49, 47), 0, h, new Color(31, 48, 57)));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(161, 141, 103, 35));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(18, 10, w - 36, h - 20, 42, 42);
        g.drawOval(cx - 105, h / 2 - 53, 210, 106);
        g.drawLine(40, h / 2, cx - 115, h / 2);
        g.drawLine(cx + 115, h / 2, w - 40, h / 2);
        g.setFont(Theme.font(Font.BOLD, 15));
        center(g, battle.isOver() ? (battle.enemyHp() == 0 ? "전투 승리" : "전투 패배") : "내 턴 · 카드 선택", cx, h / 2 + 6, Theme.TEXT);
        // [캐릭터 위치 수정] cx는 화면 가로 중앙. y=70은 위쪽, h-68은 아래쪽.
        portrait(g, cx, 70, true, battle.enemyHp(), Battle.ENEMY_MAX_HP);
        portrait(g, cx, h - 68, false, battle.playerHp(), Battle.PLAYER_MAX_HP);
        g.setFont(Theme.font(Font.BOLD, 16));
        g.setColor(new Color(235, 180, 160));
        g.drawString("상대 · 스켈레톤 병사", cx + 100, 46);
        g.setFont(Theme.font(Font.PLAIN, 14));
        g.drawString(battle.isOver() ? "전투 종료" : "다음 행동  /  공격 " + battle.enemyIntent(), cx + 100, 75);
        g.setColor(new Color(173, 217, 227));
        g.setFont(Theme.font(Font.BOLD, 16));
        g.drawString("나 · 플레이어", cx + 100, h - 92);
        g.setFont(Theme.font(Font.PLAIN, 14));
        g.drawString("방어도 " + battle.block() + "   |   다음 공격 +" + battle.bonus(), cx + 100, h - 63);
        g.setFont(Theme.font(Font.BOLD, 18));
        g.setColor(Theme.GOLD);
        g.drawString("에너지  " + battle.energy() + " / 3", 48, h - 72);
        for (int i = 0; i < 3; i++) {
            g.setColor(i < battle.energy() ? new Color(75, 182, 220) : new Color(61, 73, 83));
            g.fillOval(50 + i * 27, h - 52, 17, 17);
        }
        g.dispose();
    }

    private void portrait(Graphics2D g, int x, int y, boolean enemy, int hp, int max) {
        // enemy=true는 상대 색상, false는 플레이어 색상입니다.
        // 체력바 길이는 현재 체력 / 최대 체력의 비율로 계산합니다.
        Color accent = enemy ? new Color(193, 111, 91) : new Color(90, 167, 193);
        g.setColor(new Color(12, 22, 29));
        g.fillRoundRect(x - 66, y - 52, 132, 108, 32, 32);
        g.setColor(accent);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(x - 66, y - 52, 132, 108, 32, 32);
        if (enemy) {
            // 투명 PNG를 비율 유지하여 초상화 안에 표시합니다. 체력바는 그 위에 별도로 그립니다.
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double scale = Math.min(124.0 / skeleton.getWidth(), 94.0 / skeleton.getHeight());
            int width = (int)(skeleton.getWidth() * scale), height = (int)(skeleton.getHeight() * scale);
            g.drawImage(skeleton, x - width / 2, y - 50, width, height, null);
        } else {
            // 플레이어는 아직 임시 실루엣을 사용합니다.
            g.fillOval(x - 17, y - 35, 34, 34);
            g.fillArc(x - 37, y + 2, 74, 62, 0, 180);
        }
        g.setColor(new Color(14, 21, 29));
        g.fillRoundRect(x - 62, y + 35, 124, 23, 12, 12);
        g.setColor(new Color(125, 48, 58));
        g.fillRoundRect(x - 60, y + 37, (int) (120 * hp / (double) max), 19, 10, 10);
        g.setFont(Theme.font(Font.BOLD, 13));
        center(g, "체력 " + hp + " / " + max, x, y + 51, Color.WHITE);
    }

    private void center(Graphics2D g, String text, int x, int y, Color color) {
        g.setColor(color);
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y);
    }
}
