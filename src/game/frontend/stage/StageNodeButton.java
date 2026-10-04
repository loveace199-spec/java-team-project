package game.frontend.stage;

import game.backend.model.StageProgress;
import game.frontend.common.StartComponents;
import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.*;

/** 단계별 상태를 색상과 글자로 함께 표시하는 실제 입장 버튼입니다. */
public final class StageNodeButton extends JButton {
    private final int stage;
    private final StageProgress progress;
    public StageNodeButton(int stage, StageProgress progress, Runnable enter) {
        this.stage = stage;
        this.progress = progress;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        addActionListener(e -> { if (progress.canEnter(stage)) enter.run(); });
        refreshState();
    }
    public void refreshState() {
        setEnabled(progress.canEnter(stage));
        String state = progress.isCleared(stage) ? "클리어 · 다시 도전" : isEnabled() ? "입장 가능" : "잠김 · 이전 단계 클리어 필요";
        setToolTipText(stage + "단계: " + state);
        getAccessibleContext().setAccessibleName(stage + "단계: " + state);
        setCursor(Cursor.getPredefinedCursor(isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = StartComponents.smooth(graphics);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int x = getWidth()/2, y = 57;
        boolean cleared = progress.isCleared(stage), open = progress.canEnter(stage);
        Color accent = cleared ? new Color(102,220,147) : open ? Theme.GOLD : new Color(121,134,144);
        if (open) {
            for (int r=55;r>=45;r-=2) {
                g.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),22));
                g.fillOval(x-r,y-r,r*2,r*2);
            }
        }
        g.setColor(cleared ? new Color(27,65,49) : open ? new Color(63,52,30) : new Color(22,29,36));
        g.fillOval(x-43,y-43,86,86);
        g.setColor(accent);
        g.setStroke(new BasicStroke(hasFocus() || getModel().isRollover() ? 3 : 2));
        g.drawOval(x-43,y-43,86,86);
        if (!open) {
            // 두 사슬을 대각선으로 교차해 X 모양의 잠금 표시를 그립니다.
            for (int direction : new int[]{-1,1}) {
                Graphics2D chain=(Graphics2D)g.create();
                chain.translate(x,y);
                chain.rotate(direction*Math.PI/4);
                chain.setStroke(new BasicStroke(3));
                for(int link=-36;link<=24;link+=12) chain.drawRoundRect(link,-7,19,14,9,9);
                chain.dispose();
            }
            g.setColor(new Color(26,32,39));
            g.fillRoundRect(x-14,y-15,28,30,6,6);
            g.setColor(new Color(211,160,150));
            g.drawLine(x-7,y-7,x+7,y+7);
            g.drawLine(x+7,y-7,x-7,y+7);
        } else {
            g.setFont(new Font(Font.SERIF,Font.BOLD,31));
            centered(g,Integer.toString(stage),x,y+10,accent);
        }
        g.setFont(Theme.font(Font.BOLD,16));
        centered(g,stage+"단계",x,124,accent);
        g.setFont(Theme.font(Font.PLAIN,12));
        centered(g,cleared ? "✓ 클리어" : open ? "입장 가능" : "잠김",x,145,accent);
        g.dispose();
    }
    private void centered(Graphics2D g,String text,int x,int y,Color color) {
        g.setColor(color); g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,y);
    }
}
