package game.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.frontend.battle.BattleScreenPanel;

public final class BattleScreenCheck {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            int stage = args.length > 1 ? Integer.parseInt(args[1]) : 1;
            BattleScreenPanel panel = new BattleScreenPanel(() -> { }, stage, () -> { });
            panel.setSize(1180, 760);
            layout(panel);
            BufferedImage image = new BufferedImage(1180,760,BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            panel.printAll(g);
            g.dispose();
            try { ImageIO.write(image,"png",new File(args[0])); }
            catch (java.io.IOException e) { throw new IllegalStateException(e); }
        });
        System.out.println("PASS: battle screen rendered");
    }
    private static void layout(Container parent) {
        parent.doLayout();
        for(Component child:parent.getComponents()) if(child instanceof Container c) layout(c);
    }
}
