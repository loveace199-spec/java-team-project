package game.test;

import game.frontend.legacy.CardStartPanel;
import game.frontend.legacy.MinimalStartPanel;
import game.frontend.legacy.TowerStartPanel;
import game.frontend.stage.StageSelectPanel;
import game.frontend.start.FantasyStartPanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/** 창 없이 시안들을 렌더링하여 출력합니다. */
public final class StartScreenCheck {
    public static void main(String[] args) throws Exception {
        File directory = new File(args.length == 0 ? "preview" : args[0]);
        if (!directory.isDirectory() && !directory.mkdirs()) throw new IllegalStateException("출력 폴더 생성 실패");
        SwingUtilities.invokeAndWait(() -> {
            Runnable noop = () -> { };
            JPanel[] screens = {new TowerStartPanel(noop, noop, noop),
                new CardStartPanel(noop, noop, noop), new MinimalStartPanel(noop, noop, noop),
                new FantasyStartPanel(noop, noop, noop, noop), new StageSelectPanel(noop)};
            for (int i = 0; i < screens.length; i++) {
                JPanel panel = screens[i];
                panel.setSize(1080, 620);
                layout(panel);
                BufferedImage image = new BufferedImage(1080, 620, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = image.createGraphics();
                panel.printAll(graphics);
                graphics.dispose();
                try { ImageIO.write(image, "png", new File(directory, "start-" + (i + 1) + ".png")); }
                catch (java.io.IOException e) { throw new IllegalStateException(e); }
            }
        });
        System.out.println("PASS: 5 screens rendered");
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container nested) layout(nested);
        }
    }
}
