package game.frontend.legacy;

import game.frontend.common.StartComponents;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import game.frontend.common.Theme;

/** 이전 시안 1(보존용). 현재 시작 화면은 FantasyStartPanel을 수정하세요. */
public final class TowerStartPanel extends JPanel {
    public TowerStartPanel(Runnable start, Runnable help, Runnable exit) {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(44, 64, 32, 48));
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(StartComponents.text("01 / TOWER", 13, Theme.GOLD));
        copy.add(Box.createVerticalStrut(30));
        copy.add(StartComponents.text("한 장의 선택,", 43, Theme.TEXT));
        copy.add(StartComponents.text("다음 층의 운명.", 43, Theme.TEXT));
        copy.add(Box.createVerticalStrut(20));
        copy.add(StartComponents.text("카드를 모으고, 덱을 완성하고, 탑에 도전하세요.", 15, Theme.MUTED));
        copy.add(Box.createVerticalStrut(36));
        copy.add(StartComponents.menu(Theme.TEXT, Theme.MUTED, Theme.GOLD,
            Theme.BACKGROUND, start, help, exit));
        add(copy, BorderLayout.WEST);
        add(StartComponents.text("싱글플레이  /  턴제 카드 전투                         START SCREEN · PROTOTYPE", 12, Theme.MUTED), BorderLayout.SOUTH);
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = StartComponents.smooth(graphics);
        int w = getWidth(), h = getHeight();
        g.setPaint(new GradientPaint(0, 0, new Color(15, 22, 37), w, h, new Color(36, 53, 75)));
        g.fillRect(0, 0, w, h);
        int cx = (int) (w * .77);
        g.setColor(new Color(222, 203, 154, 22));
        g.fillOval(cx - 140, 35, 280, 280);
        g.setColor(new Color(211, 206, 177, 45));
        g.fillOval(cx - 85, 66, 170, 170);
        g.setColor(new Color(11, 17, 29));
        g.fillRect(cx - 90, 160, 180, h);
        g.fillPolygon(new int[]{cx - 120, cx, cx + 120}, new int[]{175, 80, 175}, 3);
        g.fillRect(cx - 145, h / 2, 60, h);
        g.fillRect(cx + 85, h / 2 - 40, 65, h);
        g.setColor(new Color(227, 176, 90));
        for (int y = 220; y < h - 90; y += 64) {
            g.fillRoundRect(cx - 40, y, 14, 25, 8, 8);
            g.fillRoundRect(cx + 26, y, 14, 25, 8, 8);
        }
        g.setColor(new Color(17, 25, 39));
        g.fillPolygon(new int[]{0, w / 3, w * 2 / 3, w, w, 0},
            new int[]{h - 45, h - 90, h - 40, h - 100, h, h}, 6);
        g.dispose();
    }
}
