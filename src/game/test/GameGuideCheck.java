package game.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.frontend.start.StartFrame;

/** 시작 화면의 '게임 설명' 버튼 → 설명 그림 창 표시 → 상세 규칙 전환 → 확인으로 닫기. (화면이 있어야 실행됩니다) */
public final class GameGuideCheck {
    private static void check(boolean ok, String text) { if (!ok) throw new AssertionError(text); }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length == 0 ? "preview" : args[0]);
        out.mkdirs();
        StartFrame[] frame = {null};
        SwingUtilities.invokeAndWait(() -> { frame[0] = new StartFrame(); frame[0].setVisible(true); });
        // 모달 창은 버튼 클릭이 끝날 때까지 멈추므로 나중에 클릭합니다.
        SwingUtilities.invokeLater(() -> find(frame[0].getContentPane(), "게임 설명").doClick(0));
        JDialog dialog = null;
        for (int i = 0; i < 50 && dialog == null; i++) {
            Thread.sleep(100);
            for (Window w : Window.getWindows()) if (w instanceof JDialog d && d.isShowing()) dialog = d;
        }
        check(dialog != null && "게임 설명".equals(dialog.getTitle()), "게임 설명 창 열림");
        JDialog d = dialog;
        Thread.sleep(300);
        SwingUtilities.invokeAndWait(() -> {
            check(d.getWidth() > 600, "그림이 보일 만큼 큰 창");
            snap(d.getContentPane(), new File(out, "guide-picture.png"));
            find(d.getContentPane(), "전투 규칙 자세히").doClick(0);
            snap(d.getContentPane(), new File(out, "guide-rules.png"));
            check(find(d.getContentPane(), "그림으로 보기") != null, "상세 규칙으로 전환");
            find(d.getContentPane(), "확인").doClick(0);
        });
        Thread.sleep(200);
        check(!d.isShowing(), "확인으로 닫힘");
        System.out.println("PASS: guide button opens picture, rules toggle, close");
        System.exit(0);
    }

    private static JButton find(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) return b;
            if (c instanceof Container k) { JButton r = find(k, text); if (r != null) return r; }
        }
        return null;
    }
    private static void snap(Container c, File f) {
        BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics(); c.paintAll(g); g.dispose();
        try { ImageIO.write(im, "png", f); } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }
}
