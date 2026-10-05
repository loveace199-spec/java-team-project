package game.test;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.backend.battle.DemoBattle;
import game.database.CardCatalog;
import game.frontend.battle.BattleScreenPanel;
import game.frontend.battle.CardView;
import game.frontend.battle.HandPanel;

/** 손패가 5장을 넘어도(최대 20장) 모든 카드가 '내 카드' 칸 안에 겹쳐 보이고, 마우스를 올린 카드가 맨 앞에 오는지 검사합니다. */
public final class HandOverflowCheck {
    private static void check(boolean ok, String text) { if (!ok) throw new AssertionError(text); }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length == 0 ? "preview" : args[0]);
        out.mkdirs();
        SwingUtilities.invokeAndWait(() -> {
            try {
                var panel = new BattleScreenPanel(() -> { }, 1, () -> { }, CardCatalog.allCards());
                var frame = new JFrame(); frame.setContentPane(panel); frame.setSize(1100, 730); frame.setVisible(true);
                var battle = (DemoBattle) BattleTestAccess.field(panel, "battle");
                var hand = (HandPanel) BattleTestAccess.field(panel, "hand");
                var refresh = BattleScreenPanel.class.getDeclaredMethod("refresh"); refresh.setAccessible(true);
                int[] targets = {5, 8, 20};
                for (int target : targets) {
                    while (battle.hand().size() < target) battle.startNextRound();
                    refresh.invoke(panel);
                    layout(frame.getContentPane());
                    check(hand.getComponentCount() == target, target + "장 모두 화면에 생성");
                    for (int i = 0; i < target; i++) {
                        CardView v = hand.cardAt(i);
                        check(v != null && v.getX() >= 0 && v.getX() + v.getWidth() <= hand.getWidth(), (i + 1) + "번째 카드가 칸 안에 있음 (" + target + "장)");
                        if (i > 0) check(v.getX() > hand.cardAt(i - 1).getX(), "카드가 왼쪽→오른쪽 순서로 펼쳐짐");
                    }
                    if (target == 8) {
                        CardView mid = hand.cardAt(3);
                        mid.dispatchEvent(new MouseEvent(mid, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, 5, 5, 0, false));
                        check(hand.getComponentZOrder(mid) == 0, "마우스를 올린 카드가 맨 앞");
                        snap(frame.getContentPane(), new File(out, "hand-8-hover.png"));
                        mid.dispatchEvent(new MouseEvent(mid, MouseEvent.MOUSE_EXITED, System.currentTimeMillis(), 0, 5, 5, 0, false));
                        check(hand.getComponentZOrder(hand.cardAt(7)) == 0, "마우스를 내리면 원래 순서(오른쪽 카드가 위)");
                    }
                    snap(frame.getContentPane(), new File(out, "hand-" + target + ".png"));
                }
                check(battle.hand().size() == DemoBattle.MAX_HAND, "최대 20장");
                frame.dispose();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("PASS: hand over 5 cards (up to 20) fans out inside the tray, hovered card comes to front");
        System.exit(0);
    }

    private static void layout(Container c) { c.doLayout(); for (Component x : c.getComponents()) if (x instanceof Container k) layout(k); }
    private static void snap(Container c, File f) {
        BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics(); c.paintAll(g); g.dispose();
        try { ImageIO.write(im, "png", f); } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }
}
