package game.frontend.start;

import game.frontend.common.StartComponents;

import java.awt.*;
import javax.swing.*;
import game.frontend.common.Theme;

/** 메뉴 외형과 키보드 선택. */
public final class FantasyMenuButton extends JButton {
    public FantasyMenuButton(String text, Runnable action) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setFont(Theme.font(Font.BOLD, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addActionListener(e -> action.run());
        getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "activate");
        getActionMap().put("activate", new AbstractAction() {
            public void actionPerformed(java.awt.event.ActionEvent e) { doClick(); }
        });
    }
    @Override protected void paintComponent(Graphics graphics) {
        // 기본 JButton 모양 대신 직접 그립니다. 마우스 올림/키보드 포커스 시 밝게 표시합니다.
        Graphics2D g = StartComponents.smooth(graphics);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean active = hasFocus() || getModel().isRollover();
        Color base = active ? new Color(110, 130, 139) : new Color(35, 43, 48);
        if (getModel().isPressed()) base = base.darker();
        Polygon shape = new Polygon(new int[]{12,w-12,w-2,w-12,12,2}, new int[]{3,3,h/2,h-3,h-3,h/2},6);
        g.setPaint(new GradientPaint(0,0,base.brighter(),0,h,base.darker()));
        g.fillPolygon(shape);
        g.setColor(active ? new Color(216,203,160) : new Color(110,113,105));
        g.setStroke(new BasicStroke(2));
        g.drawPolygon(shape);
        g.setColor(new Color(225,231,219));
        g.setFont(getFont());
        g.drawString(getText(), (w-g.getFontMetrics().stringWidth(getText()))/2,
            (h-g.getFontMetrics().getHeight())/2+g.getFontMetrics().getAscent());
        if (active) g.fillPolygon(new int[]{21,27,21,15},new int[]{h/2-6,h/2,h/2+6,h/2},4);
        g.dispose();
    }
}
