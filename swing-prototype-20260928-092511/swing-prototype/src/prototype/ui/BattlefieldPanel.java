package prototype.ui;

import java.awt.*;
import javax.swing.JPanel;

/** 이전 BattleFrame 전용 배경. 현재 위아래 전장의 그림은 BattleBoardPanel에 있습니다. */
public final class BattlefieldPanel extends JPanel {
    public BattlefieldPanel() { setPreferredSize(new Dimension(1000, 230)); }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0, 0, Theme.PANEL, 0, getHeight(), Theme.BACKGROUND));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(new Color(49, 62, 82));
        for (int x = 40; x < getWidth(); x += 130) g.fillRoundRect(x, 20, 28, getHeight() - 30, 8, 8);
        int cx = getWidth() / 2, cy = getHeight() / 2;
        g.setColor(new Color(12, 19, 32));
        g.fillOval(cx - 80, cy + 48, 160, 20);
        g.setColor(new Color(108, 118, 141));
        g.fillPolygon(new int[]{cx, cx + 53, cx + 65, cx - 65, cx - 53},
            new int[]{cy - 70, cy - 25, cy + 55, cy + 55, cy - 25}, 5);
        g.setColor(Theme.GOLD);
        g.fillRect(cx - 26, cy - 18, 17, 6);
        g.fillRect(cx + 9, cy - 18, 17, 6);
        g.setColor(Theme.MUTED);
        g.setFont(Theme.font(Font.PLAIN, 12));
        g.drawString("임시 전장 · 배경과 적 이미지를 교체할 수 있습니다", 20, getHeight() - 12);
        g.dispose();
    }
}
