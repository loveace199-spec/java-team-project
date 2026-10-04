package prototype.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import prototype.demo.DemoBattle;

/** 전투 UI 구성. 임시 전투 로직과 카드 외형은 별도 파일에서 관리합니다. */
public final class BattleScreenPanel extends JPanel {
    // [역할 분리] battle: 체력/에너지 계산, board: 인물과 전장 그림, hand: 카드 나열.
    private final DemoBattle battle = new DemoBattle();
    private final BattleBoardPanel board = new BattleBoardPanel(battle);
    private final HandPanel hand = new HandPanel();
    private final int stage;
    private final Runnable onVictory;
    private boolean victoryReported;
    private final JLabel title = label("", 17);
    private final JLabel message = label("사용할 카드를 클릭하세요.", 13);
    private final JButton endTurn = button("턴 종료", () -> {
        message.setText(battle.endTurn());
        refresh();
    });

    public BattleScreenPanel(Runnable back) {
        this(back, 1, () -> { });
    }

    public BattleScreenPanel(Runnable back, int stage, Runnable onVictory) {
        // BorderLayout: NORTH 위 / CENTER 가운데 / SOUTH 아래 / WEST 왼쪽 / EAST 오른쪽.
        super(new BorderLayout(0, 6));
        this.stage = stage;
        this.onVictory = onVictory;
        setBackground(Theme.BACKGROUND);
        setBorder(new EmptyBorder(10, 18, 10, 18));
        // ① 상단: 돌아가기, 턴 표시, 다시 시작 버튼.
        JPanel header = row();
        header.add(button("← 스테이지", back), BorderLayout.WEST);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.CENTER);
        header.add(button("다시 시작", () -> {
            battle.reset();
            victoryReported = false;
            message.setText("새 전투 · 사용할 카드를 클릭하세요.");
            refresh();
        }), BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ② 중앙: 위아래 초상화를 그리는 보드 + 그 아래 손패.
        JPanel center = row();
        center.add(board, BorderLayout.CENTER);
        JPanel handArea = row();
        JLabel caption = label("내 손패  ·  공격 / 방어 / 회복 / 주문", 13);
        caption.setBorder(new EmptyBorder(4, 12, 0, 0));
        handArea.add(caption, BorderLayout.NORTH);
        // 창이 좁으면 손패를 가로 스크롤합니다. 카드를 잘라 숨기지 않기 위한 처리입니다.
        JScrollPane scroll = new JScrollPane(hand, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(1000, 244));
        handArea.add(scroll, BorderLayout.CENTER);
        center.add(handArea, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        // ③ 하단: 마지막 행동 안내와 턴 종료 버튼.
        JPanel footer = row();
        footer.add(message, BorderLayout.CENTER);
        footer.add(endTurn, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
        refresh();
    }

    private void refresh() {
        // 카드 사용/턴 종료/재시작 후 최신 데이터를 화면에 다시 반영합니다.
        // 전투 수치는 DemoBattle에서 계산하고 이 메서드는 결과만 표시합니다.
        // 적 체력이 0이 된 경우에만 승리를 보고합니다. 돌아가기/패배는 클리어가 아닙니다.
        if (battle.enemyHp() == 0 && !victoryReported) {
            victoryReported = true;
            onVictory.run();
            message.setText(stage + "단계 클리어! 왼쪽 위 스테이지 버튼으로 돌아가세요.");
        }
        title.setText(stage + "단계   /   턴 " + battle.turn() + "   /   임시 전투");
        hand.showCards(battle.hand(), battle.energy(), battle.isOver(), index -> {
            message.setText(battle.play(index));
            refresh();
        });
        // [전투 카드 크기 수정] 가로 160, 세로 212. 그림은 CardView에서 그립니다.
        for (Component card : hand.getComponents()) card.setPreferredSize(new Dimension(160, 212));
        // revalidate: 컴포넌트 배치를 다시 계산. repaint: 그림을 다시 그려 달라고 요청.
        hand.revalidate();
        board.repaint();
        endTurn.setEnabled(!battle.isOver());
    }

    private static JPanel row() {
        JPanel panel = new JPanel(new BorderLayout(12, 4));
        panel.setOpaque(false);
        return panel;
    }
    private static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.TEXT);
        label.setFont(Theme.font(Font.PLAIN, size));
        return label;
    }
    private static JButton button(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(Theme.font(Font.BOLD, 14));
        button.setBackground(Theme.GOLD);
        button.setForeground(Theme.BACKGROUND);
        button.setPreferredSize(new Dimension(140, 36));
        button.addActionListener(e -> action.run());
        return button;
    }
}
