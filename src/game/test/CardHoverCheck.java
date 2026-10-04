package game.test;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.database.CardCatalog;
import game.frontend.battle.BattleScreenPanel;
import game.frontend.battle.CardInfoPopup;
import game.frontend.battle.CardView;

/** 손패 카드에 마우스를 올리면 설명 상자가 뜨고, 내리면 사라지는지 검사합니다. (화면이 있어야 실행됩니다) */
public final class CardHoverCheck {
    private static void check(boolean ok, String text) { if (!ok) throw new AssertionError(text); }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length == 0 ? "preview" : args[0]);
        out.mkdirs();
        JFrame[] frame = {null};
        BattleScreenPanel[] panel = {null};
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new BattleScreenPanel(() -> { }, 1, () -> { }, CardCatalog.allCards());
            frame[0] = new JFrame(); frame[0].setContentPane(panel[0]); frame[0].setSize(1100, 730); frame[0].setVisible(true);
        });
        Thread.sleep(3400); // MY TURN 안내(3초) 후 내 턴 시작
        SwingUtilities.invokeAndWait(() -> {
            CardView card = find(panel[0], CardView.class);
            CardInfoPopup popup = find(panel[0], CardInfoPopup.class);
            check(card != null && popup != null && !popup.isVisible(), "처음에는 설명 숨김");
            card.dispatchEvent(new MouseEvent(card, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, 10, 10, 0, false));
            check(popup.isVisible() && popup.card() != null, "마우스 올림 → 설명 표시");
            check(popup.getY() + popup.getHeight() <= SwingUtilities.convertPoint(card, 0, 0, popup.getParent()).y,
                "설명 상자가 카드 위쪽에 표시");
            check(ToolTipManager.sharedInstance() != null && card.getToolTipText() != null, "카드 정보 유지");
            snap(frame[0].getContentPane(), new File(out, "card-hover.png"));
            card.dispatchEvent(new MouseEvent(card, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0, 10, 10, 0, false));
            check(!popup.isVisible(), "마우스 내림 → 설명 숨김");
            frame[0].dispose();
        });
        System.out.println("PASS: hand card hover shows description popup above the card, hides on exit");
        System.exit(0);
    }

    private static <T> T find(Container root, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c) && c.isShowing() || type.isInstance(c) && type == CardInfoPopup.class) return type.cast(c);
            if (c instanceof Container k) { T r = find(k, type); if (r != null) return r; }
        }
        return null;
    }
    private static void snap(Container c, File f) {
        BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics(); c.paintAll(g); g.dispose();
        try { ImageIO.write(im, "png", f); } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }
}
