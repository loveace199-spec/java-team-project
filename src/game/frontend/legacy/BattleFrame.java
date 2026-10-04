package game.frontend.legacy;

import game.frontend.battle.BattlePreviewFrame;
import game.frontend.battle.HandPanel;
import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import game.backend.battle.DemoBattle;

/** 이전 전투 시안(보존용). 현재 전투는 BattlePreviewFrame/BattleScreenPanel을 수정하세요. */
public final class BattleFrame extends JFrame {
    private final DemoBattle battle = new DemoBattle();
    private final JLabel heading = label("", 19);
    private final JLabel enemy = label("", 18);
    private final JLabel player = label("", 17);
    private final JLabel message = label("카드를 클릭하면 즉시 사용됩니다.", 14);
    private final HandPanel hand = new HandPanel();
    private final JButton endTurn = button("턴 종료");

    public BattleFrame() {
        super("카드 전투 · Swing 화면 시제품");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 800));
        setSize(1200, 880);
        setLocationRelativeTo(null);

        JPanel root = panel(new BorderLayout(0, 10));
        root.setBorder(new EmptyBorder(18, 24, 18, 24));
        setContentPane(root);
        JPanel top = panel(new BorderLayout());
        top.add(heading, BorderLayout.WEST);
        JButton restart = button("처음부터");
        restart.addActionListener(event -> {
            battle.reset();
            message.setText("새 전투 · 카드를 클릭하면 즉시 사용됩니다.");
            refresh();
        });
        top.add(restart, BorderLayout.EAST);
        root.add(top, BorderLayout.NORTH);

        JPanel center = panel(new BorderLayout(0, 8));
        enemy.setHorizontalAlignment(SwingConstants.CENTER);
        center.add(enemy, BorderLayout.NORTH);
        center.add(new BattlefieldPanel(), BorderLayout.CENTER);

        JPanel controls = panel(new BorderLayout(0, 6));
        controls.add(player, BorderLayout.NORTH);
        JScrollPane cards = new JScrollPane(hand,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        cards.setBorder(null);
        cards.setPreferredSize(new Dimension(1000, 282));
        controls.add(cards, BorderLayout.CENTER);
        JLabel help = label("카드 클릭: 사용  |  Tab 이동 + Space 사용  |  흐린 카드: 에너지 부족 또는 전투 종료", 12);
        help.setForeground(Theme.MUTED);
        controls.add(help, BorderLayout.SOUTH);
        center.add(controls, BorderLayout.SOUTH);
        root.add(center, BorderLayout.CENTER);

        JPanel bottom = panel(new BorderLayout(16, 0));
        bottom.add(message, BorderLayout.CENTER);
        endTurn.addActionListener(event -> {
            message.setText(battle.endTurn());
            refresh();
        });
        bottom.add(endTurn, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
        refresh();
    }

    private void refresh() {
        heading.setText("1층 · 전투 시제품     /     턴 " + battle.turn());
        enemy.setText("탑의 파수꾼    체력 " + battle.enemyHp() + "/" + DemoBattle.ENEMY_MAX_HP
            + (battle.isOver() ? "    · 전투 종료" : "    |    다음 행동: 공격 " + battle.enemyIntent()));
        player.setText("플레이어   체력 " + battle.playerHp() + "/" + DemoBattle.PLAYER_MAX_HP
            + "      방어도 " + battle.block() + "      에너지 " + battle.energy() + "/3"
            + "      다음 공격 추가 피해 +" + battle.bonus());
        hand.showCards(battle.hand(), battle.energy(), battle.isOver(), index -> {
            message.setText(battle.play(index));
            refresh();
        });
        endTurn.setEnabled(!battle.isOver());
    }

    private static JPanel panel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Theme.BACKGROUND);
        return panel;
    }

    private static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(Font.PLAIN, size));
        label.setForeground(Theme.TEXT);
        return label;
    }

    private static JButton button(String text) {
        JButton button = new JButton(text);
        button.setFont(Theme.font(Font.BOLD, 15));
        button.setBackground(Theme.GOLD);
        button.setForeground(Theme.BACKGROUND);
        button.setFocusPainted(true);
        button.setPreferredSize(new Dimension(138, 42));
        return button;
    }
}
