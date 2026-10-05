package game.frontend.start;

import game.frontend.common.Images;
import game.frontend.common.StartComponents;
import game.frontend.common.Theme;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * 시작 화면의 '게임 설명' 창.
 * 기본으로 설명 그림(src/assets/game-guide-v1.png)을 보여주고,
 * '전투 규칙 자세히' 버튼으로 글로 된 상세 규칙을 볼 수 있습니다.
 */
public final class GameGuideDialog extends JDialog {
    public static final String IMAGE = "/assets/game-guide-v1.png";
    private final CardLayout pages = new CardLayout();
    private final JPanel pageHolder = new JPanel(pages);
    private boolean showingRules;

    public GameGuideDialog(JFrame owner) {
        super(owner, "게임 설명", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        BufferedImage image = Images.load(IMAGE, "게임 설명 그림");

        // ① 설명 그림: 창 크기에 맞춰 비율을 유지하며 그립니다.
        JPanel picture = new JPanel() {
            @Override protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Rectangle d = Images.fit(image, getWidth(), getHeight());
                g.drawImage(image, d.x, d.y, d.width, d.height, null);
                g.dispose();
            }
        };
        picture.setBackground(Color.BLACK);
        picture.getAccessibleContext().setAccessibleName(
            "게임 설명: 20장의 카드로 시작, 체력을 관리하세요, 두 가지 승리 조건");

        // ② 상세 전투 규칙 (기존 글 설명)
        // [게임 설명 수정] 아래 문자열을 바꾸면 상세 규칙 내용이 바뀝니다. \n은 줄바꿈입니다.
        JTextArea guide = new JTextArea(
            "전투 규칙\n\n"
            + "1. 내 턴에 쓸 수 있는 카드\n"
            + "공격 카드 1장, 회복 카드 1장, 주문 카드 최대 2장을 사용할 수 있습니다.\n"
            + "에너지는 없습니다. 카드 왼쪽 위 숫자는 카드의 '등급'입니다.\n\n"
            + "2. 방어 카드\n상대방이 공격 카드를 사용했을 때만 대응하여 사용할 수 있습니다.\n"
            + "공격 공개 → 방어 선택 → 피해 판정 순서로 진행합니다. 선택 시간은 15초입니다.\n"
            + "공격 카드와 같거나 높은 등급의 방어 카드만 사용할 수 있습니다. (예: 1등급 공격 → 2등급 방어 가능)\n"
            + "받는 피해는 방어 카드의 수치만큼 줄어들고, 방어 카드의 반격 피해도 함께 적용합니다.\n"
            + "방어 안 함 또는 시간 초과 시 공격 피해를 그대로 받습니다. 상대도 같은 규칙으로 방어합니다.\n\n"
            + "3. 승리 / 패배\n상대의 체력을 0으로 만들거나, 상대가 덱을 모두 소진하면 승리합니다.\n"
            + "자신의 체력이 0이 되면 패배합니다.\n\n"
            + "4. 덱\n플레이어는 20장의 덱으로 시작하며 매 턴 5장을 뽑습니다.\n"
            + "내 뽑기 더미가 비면 버린 카드를 다시 섞습니다. 상대 덱은 다시 섞지 않습니다.\n"
            + "손패는 현재 들고 있는 카드, 뽑기는 아직 뽑지 않은 카드, 버림은 사용하거나 턴 종료로 버린 카드입니다.\n"
            + "상대 손패는 뒷면으로 숨겨지고, 사용한 카드만 중앙에서 공개됩니다.");
        guide.setEditable(false);
        guide.setLineWrap(true);
        guide.setWrapStyleWord(true);
        guide.setFont(Theme.font(Font.PLAIN, 15));
        guide.setForeground(Theme.TEXT);
        guide.setBackground(Theme.BACKGROUND);
        guide.setBorder(new EmptyBorder(18, 24, 18, 24));
        guide.setCaretPosition(0);
        JScrollPane scroll = new JScrollPane(guide);
        scroll.setBorder(null);

        pageHolder.add(picture, "picture");
        pageHolder.add(scroll, "rules");

        JButton toggle = StartComponents.button("전투 규칙 자세히", Theme.PANEL, Theme.TEXT, () -> { });
        toggle.addActionListener(e -> {
            showingRules = !showingRules;
            pages.show(pageHolder, showingRules ? "rules" : "picture");
            toggle.setText(showingRules ? "그림으로 보기" : "전투 규칙 자세히");
        });
        JButton close = StartComponents.button("확인", Theme.GOLD, Theme.BACKGROUND, this::dispose);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 10));
        buttons.setBackground(Color.BLACK);
        buttons.add(toggle);
        buttons.add(close);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.BLACK);
        content.add(pageHolder, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
        getRootPane().setDefaultButton(close);
        getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        // 화면의 85% 안에서 그림 비율대로 창 크기를 정합니다.
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        int width = Math.min(1180, (int) (screen.width * 0.85));
        int pictureHeight = width * image.getHeight() / image.getWidth();
        if (pictureHeight > screen.height * 0.85 - 70) {
            pictureHeight = (int) (screen.height * 0.85 - 70);
            width = pictureHeight * image.getWidth() / image.getHeight();
        }
        pageHolder.setPreferredSize(new Dimension(width, pictureHeight));
        pack();
        setMinimumSize(new Dimension(640, 420));
        setLocationRelativeTo(owner);
    }
}
