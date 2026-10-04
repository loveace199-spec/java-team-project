package game.frontend.legacy;

import game.backend.battle.Battle;
import game.frontend.battle.CardView;
import game.frontend.common.StartComponents;
import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** 이전 시안 2(보존용). 현재 시작 화면에서 사용하지 않으며 비교 렌더링 테스트에 사용합니다. */
public final class CardStartPanel extends JPanel {
    public CardStartPanel(Runnable start, Runnable help, Runnable exit) {
        super(new BorderLayout(0, 20));
        setBackground(new Color(32, 30, 44));
        setBorder(new EmptyBorder(30, 38, 26, 38));
        JPanel title = new JPanel(new GridLayout(3, 1, 0, 6));
        title.setOpaque(false);
        title.add(center("02 / BUILD YOUR DECK", 13, Theme.GOLD));
        title.add(center("당신의 전략을 펼치세요", 34, Theme.TEXT));
        title.add(center("공격 · 방어 · 회복 · 주문, 네 가지 선택으로 만드는 전투", 15, Theme.MUTED));
        add(title, BorderLayout.NORTH);

        JPanel gallery = new JPanel(new GridBagLayout());
        gallery.setOpaque(false);
        GridBagConstraints cell = new GridBagConstraints();
        cell.insets = new Insets(0, 10, 0, 10);
        var cards = new Battle().hand();
        for (int i = 0; i < 4; i++) {
            var card = cards.get(i);
            CardView view = new CardView(card, () -> JOptionPane.showMessageDialog(this,
                card.description() + "\n에너지 비용: " + card.cost(), card.name(), JOptionPane.INFORMATION_MESSAGE));
            view.setPreferredSize(new Dimension(180, 230));
            view.setMinimumSize(new Dimension(180, 230));
            gallery.add(view, cell);
        }
        add(gallery, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1, 0, 12));
        bottom.setOpaque(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        buttons.setOpaque(false);
        buttons.add(StartComponents.button("플레이 방법", Theme.PANEL, Theme.TEXT, help));
        buttons.add(StartComponents.button("게임 시작   →", Theme.GOLD, Theme.BACKGROUND, start));
        buttons.add(StartComponents.button("종료", Theme.PANEL, Theme.TEXT, exit));
        bottom.add(buttons);
        bottom.add(center("카드를 클릭하면 효과를 확인할 수 있습니다 · 모든 수치는 임시입니다", 12, Theme.MUTED));
        add(bottom, BorderLayout.SOUTH);
    }

    private JLabel center(String text, int size, Color color) {
        JLabel label = StartComponents.text(text, size, color);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }
}
