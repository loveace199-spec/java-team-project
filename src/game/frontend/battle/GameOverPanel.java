package game.frontend.battle;

import game.frontend.common.ImageHotspotButton;
import game.frontend.common.Images;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * 패배 시 전투 화면 위에 덮는 GAME OVER 화면.
 * 그림(src/assets/game-over-v1.png) 속 '재도전' / '메인으로' 버튼 위치에 실제 클릭 영역을 겹칩니다.
 */
public final class GameOverPanel extends JPanel {
    public static final String IMAGE = "/assets/game-over-v1.png";
    // 원본 그림(1536×1024)에서 두 버튼이 그려진 영역
    private static final Rectangle RETRY_AREA = new Rectangle(140, 598, 575, 170);
    private static final Rectangle MAIN_AREA = new Rectangle(822, 598, 618, 170);

    private final BufferedImage image = Images.load(IMAGE, "게임 오버 화면");
    private final ImageHotspotButton retry;
    private final ImageHotspotButton main;

    public GameOverPanel(Runnable onRetry, Runnable onMain) {
        setLayout(null);
        setOpaque(false);
        retry = new ImageHotspotButton("재도전", RETRY_AREA, onRetry);
        main = new ImageHotspotButton("메인으로", MAIN_AREA, onMain);
        add(retry);
        add(main);
        // 키보드: ← → / Tab 이동, Enter·Space 선택
        for (ImageHotspotButton b : new ImageHotspotButton[]{retry, main}) {
            b.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "press");
            b.getActionMap().put("press", new AbstractAction() {
                @Override public void actionPerformed(java.awt.event.ActionEvent e) { b.doClick(); }
            });
        }
        retry.getInputMap().put(KeyStroke.getKeyStroke("RIGHT"), "next");
        retry.getActionMap().put("next", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { main.requestFocusInWindow(); }
        });
        main.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "prev");
        main.getActionMap().put("prev", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { retry.requestFocusInWindow(); }
        });
        // 뒤의 전투 화면이 클릭되지 않도록 빈 곳의 마우스 입력을 막습니다.
        addMouseListener(new java.awt.event.MouseAdapter() { });
    }

    public void focusRetry() { retry.requestFocusInWindow(); }

    @Override public void doLayout() {
        Rectangle drawn = Images.fit(image, getWidth(), getHeight());
        retry.place(drawn, image.getWidth(), image.getHeight());
        main.place(drawn, image.getWidth(), image.getHeight());
    }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        // 전투 화면을 어둡게 가린 뒤 그림을 가운데에 그립니다.
        g.setColor(new Color(0, 0, 0, 235));
        g.fillRect(0, 0, getWidth(), getHeight());
        Rectangle d = Images.fit(image, getWidth(), getHeight());
        g.drawImage(image, d.x, d.y, d.width, d.height, null);
        g.dispose();
    }
}
