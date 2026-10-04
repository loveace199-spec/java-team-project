package game.frontend.stage;

import java.awt.*;
import javax.swing.*;
import game.frontend.common.Theme;

/** 상점 구현 전 사용하는 3단계 안내 화면입니다. 구매 기능은 추후 연결합니다. */
public final class ShopPlaceholderPanel extends JPanel {
    public ShopPlaceholderPanel(Runnable back, Runnable continueJourney) {
        super(new GridBagLayout());
        setBackground(Theme.BACKGROUND);
        JPanel content = new JPanel(new GridLayout(0, 1, 0, 18));
        content.setOpaque(false);
        JLabel title = new JLabel("3단계 · 상점", SwingConstants.CENTER);
        title.setForeground(Theme.GOLD);
        title.setFont(Theme.font(Font.BOLD, 30));
        content.add(title);
        JLabel hint = new JLabel("상점 준비 중 · 지금은 전투 없이 지나갈 수 있습니다.");
        hint.setForeground(Theme.TEXT);
        content.add(hint);
        JButton next = new JButton("상점 건너뛰고 4단계 열기");
        next.addActionListener(e -> continueJourney.run());
        content.add(next);
        JButton returnButton = new JButton("← 스테이지");
        returnButton.addActionListener(e -> back.run());
        content.add(returnButton);
        add(content);
    }
}
