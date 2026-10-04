package game.frontend.start;

import game.frontend.common.StartComponents;
import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** 게임 설명은 시작 화면과 분리해 수정합니다. */
public final class GameGuideDialog extends JDialog {
    public GameGuideDialog(JFrame owner) {
        super(owner, "게임 설명", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.add(StartComponents.text("카드로 펼치는 턴제 전투", 23, Theme.TEXT), BorderLayout.NORTH);
        // [게임 설명 수정] 아래 문자열을 바꾸면 안내 창 내용이 바뀝니다. \n은 줄바꿈입니다.
        JTextArea guide = new JTextArea(
            "목표\n적의 체력을 0으로 만들면 승리합니다. 내 체력이 0이면 패배합니다.\n\n"
            + "카드 종류\n공격 : 적에게 피해를 줍니다.\n방어 : 방어도를 얻어 적의 공격 피해를 줄입니다.\n"
            + "회복 : 최대 체력 범위 안에서 체력을 회복합니다.\n주문 : 다음 공격 강화 등 보조 효과를 사용합니다.\n\n"
            + "진행 방법\n1. 화면 위쪽에서 적의 다음 행동을 확인합니다.\n"
            + "2. 에너지 3 안에서 카드를 클릭해 사용합니다.\n"
            + "3. 턴 종료를 누르면 적이 공격하고 다음 턴이 시작됩니다.\n\n"
            + "현재 시제품\n매 턴 같은 카드 5장이 제공됩니다. 방어도는 다음 턴에 초기화됩니다.\n"
            + "층 이동·무작위 덱·저장 기능은 아직 포함되지 않았습니다.");
        guide.setEditable(false);
        guide.setLineWrap(true);
        guide.setWrapStyleWord(true);
        guide.setFont(Theme.font(Font.PLAIN, 15));
        guide.setForeground(Theme.TEXT);
        guide.setBackground(Theme.BACKGROUND);
        guide.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(guide);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(540, 430));
        content.add(scroll, BorderLayout.CENTER);
        JButton close = StartComponents.button("확인", Theme.GOLD, Theme.BACKGROUND, this::dispose);
        content.add(close, BorderLayout.SOUTH);
        setContentPane(content);
        getRootPane().setDefaultButton(close);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack();
        setLocationRelativeTo(owner);
    }
}
