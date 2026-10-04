package prototype.ui.start;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import prototype.ui.Theme;

/** 세 시안에서 사용하는 글자와 버튼. 게임 규칙을 포함하지 않습니다. */
final class StartComponents {
    private StartComponents() { }

    static JLabel text(String value, int size, Color color) {
        JLabel label = new JLabel(value);
        label.setFont(Theme.font(Font.PLAIN, size));
        label.setForeground(color);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    static JButton button(String title, Color background, Color foreground, Runnable action) {
        JButton button = new JButton(title);
        button.setFont(Theme.font(Font.BOLD, 17));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(true);
        button.setFocusPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(background.brighter()), new EmptyBorder(14, 25, 14, 25)));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(310, 54));
        button.addActionListener(event -> action.run());
        return button;
    }

    static JPanel menu(Color foreground, Color muted, Color accent, Color buttonText,
                       Runnable start, Runnable help, Runnable exit) {
        JPanel menu = new JPanel();
        menu.setOpaque(false);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.add(button("게임 시작   →", accent, buttonText, start));
        menu.add(Box.createVerticalStrut(12));
        menu.add(button("플레이 방법", new Color(47, 58, 74), foreground, help));
        menu.add(Box.createVerticalStrut(12));
        menu.add(button("종료", new Color(47, 58, 74), foreground, exit));
        menu.add(Box.createVerticalStrut(16));
        menu.add(text("새 전투를 시작합니다 · 저장 기능 미포함", 12, muted));
        return menu;
    }

    static Graphics2D smooth(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return g;
    }
}
