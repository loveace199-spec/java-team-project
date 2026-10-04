package game.frontend.common;

import java.awt.*;
import javax.swing.*;

/**
 * 배경 그림 속에 그려진 버튼·카드 위에 겹쳐 놓는 투명 버튼.
 * 그림은 배경이 그리고, 이 버튼은 클릭 영역과 마우스 올림/선택 효과만 담당합니다.
 * 그림 좌표(원본 이미지 픽셀)로 위치를 정하면 창 크기가 바뀌어도 같은 자리에 맞춰집니다.
 */
public final class ImageHotspotButton extends JButton {
    /** 원본 이미지 기준 영역 (x, y, 가로, 세로). */
    private final Rectangle imageArea;
    private String badge; // 예: "구매 완료"

    public ImageHotspotButton(String accessibleName, Rectangle imageArea, Runnable action) {
        this.imageArea = new Rectangle(imageArea);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(accessibleName);
        getAccessibleContext().setAccessibleName(accessibleName);
        addActionListener(e -> action.run());
    }

    /** 그림이 화면에 그려진 사각형(drawn)과 원본 크기(imageW, imageH)로 버튼 위치를 다시 계산합니다. */
    public void place(Rectangle drawn, int imageW, int imageH) {
        double sx = drawn.width / (double) imageW, sy = drawn.height / (double) imageH;
        setBounds(drawn.x + (int) Math.round(imageArea.x * sx), drawn.y + (int) Math.round(imageArea.y * sy),
            (int) Math.round(imageArea.width * sx), (int) Math.round(imageArea.height * sy));
    }

    public void setBadge(String badge) {
        this.badge = badge;
        repaint();
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), arc = Math.max(10, Math.min(w, h) / 8);
        boolean active = isEnabled() && (getModel().isRollover() || hasFocus());
        if (!isEnabled()) {
            // 고를 수 없는 항목은 어둡게 덮습니다.
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRoundRect(0, 0, w, h, arc, arc);
        } else if (getModel().isPressed()) {
            g.setColor(new Color(255, 220, 140, 70));
            g.fillRoundRect(0, 0, w, h, arc, arc);
        } else if (active) {
            g.setColor(new Color(255, 214, 120, 38));
            g.fillRoundRect(0, 0, w, h, arc, arc);
        }
        if (active || getModel().isPressed()) {
            // 금색 테두리 빛
            for (int i = 0; i < 4; i++) {
                g.setColor(new Color(255, 205, 100, 150 - i * 35));
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(i + 1, i + 1, w - 3 - i * 2, h - 3 - i * 2, arc, arc);
            }
        }
        if (badge != null) {
            g.setFont(Theme.font(Font.BOLD, Math.max(14, w / 9)));
            FontMetrics fm = g.getFontMetrics();
            int bw = fm.stringWidth(badge) + 28, bh = fm.getHeight() + 10;
            int bx = (w - bw) / 2, by = (h - bh) / 2;
            g.setColor(new Color(15, 20, 12, 220));
            g.fillRoundRect(bx, by, bw, bh, 14, 14);
            g.setColor(new Color(120, 220, 145));
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(bx, by, bw, bh, 14, 14);
            g.drawString(badge, bx + 14, by + 5 + fm.getAscent());
        }
        g.dispose();
    }
}
