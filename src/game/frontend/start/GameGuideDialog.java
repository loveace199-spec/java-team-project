package game.frontend.start;

import game.frontend.common.StartComponents;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import game.frontend.common.Theme;

/** 게임 설명은 시작 화면과 분리해 수정합니다. */
public final class GameGuideDialog extends JDialog {
    public GameGuideDialog(JFrame owner) {
        super(owner, "게임 설명", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content = new JPanel(new BorderLayout(0, 20));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        content.add(StartComponents.text("전투 규칙 안내", 23, Theme.TEXT), BorderLayout.NORTH);
        // [게임 설명 수정] 아래 문자열을 바꾸면 안내 창 내용이 바뀝니다. \n은 줄바꿈입니다.
        JTextArea guide = new JTextArea(
            "전투 규칙\n\n"
            + "1. 공격 / 회복 카드\n한 턴에 한 장만 사용할 수 있습니다.\n"
            + "현재 정리 기준: 공격과 회복을 합쳐 1장입니다.\n"
            + "예: 공격 카드를 사용한 턴에는 회복 카드를 사용할 수 없습니다.\n\n"
            + "2. 방어 카드\n상대방이 공격 카드를 사용했을 때만 대응하여 사용할 수 있습니다.\n"
            + "공격 공개 → 방어 선택 → 피해 판정 순서로 진행합니다.\n"
            + "방어는 남은 에너지로 1장 사용하며, 선택 시간은 15초입니다.\n"
            + "방어 안 함 또는 시간 초과 시 방어 없이 판정합니다. 상대는 방어 카드가 있으면 자동 대응합니다.\n"
            + "공격 피해에서 방어 수치를 뺀 값(최소 0)을 적용하며, 방어 카드의 반격 피해도 함께 적용합니다.\n\n"
            + "3. 주문 카드\n공격 / 회복의 한 장 제한과 관계없이 사용할 수 있습니다.\n"
            + "상대 턴 사용 여부와 에너지 비용 적용 여부는 추가로 결정합니다.\n\n"
            + "4. 승리 / 패배 조건\n상대방의 체력을 0으로 만들면 승리합니다.\n"
            + "자신의 체력이 0이 되면 패배합니다.\n"
            + "덱을 먼저 소진한 쪽이 패배합니다.\n"
            + "소진 판정 시점(마지막 카드를 뽑을 때 / 다음 카드를 뽑지 못할 때)은 추가로 결정합니다.\n\n"
            + "현재 시제품과의 차이\n공격·회복 합계 1장 제한과 방어 대응은 적용했습니다.\n"
            + "주문은 현재 내 턴에 에너지가 허용하는 만큼 사용합니다. 덱 소진 패배 규칙은 아직 미적용입니다.\n"
            + "뽑기 더미가 비면 버린 카드를 다시 섞습니다.\n"
            + "상대는 공격·방어 카드 20장의 임시 덱을 사용합니다. 상대 턴에 손패를 5장까지 보충하고 공격 1장을 사용합니다.\n"
            + "손패는 현재 들고 있는 카드, 뽑기는 아직 뽑지 않은 카드, 버림은 사용하거나 턴 종료로 버린 카드입니다.\n"
            + "상대 손패는 뒷면으로 숨겨지고, 사용한 카드만 중앙에서 공개됩니다.");
        guide.setEditable(false);
        guide.setLineWrap(true);
        guide.setWrapStyleWord(true);
        guide.setFont(Theme.font(Font.PLAIN, 15));
        guide.setForeground(Theme.TEXT);
        guide.setBackground(Theme.BACKGROUND);
        guide.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(guide);
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(600, 490));
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
