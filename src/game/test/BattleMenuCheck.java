package game.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.frontend.battle.BattleScreenPanel;

public final class BattleMenuCheck {
    private static Object field(Object o,String name) throws Exception {var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
    private static void layout(Container c) {c.doLayout();for(Component child:c.getComponents()) if(child instanceof Container d) layout(d);}
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try {
                int[] returns={0};var screen=new BattleScreenPanel(()->returns[0]++);screen.setSize(1100,690);layout(screen);
                JButton gear=(JButton)field(screen,"gearButton");JPanel menu=(JPanel)field(screen,"battleMenu");
                if(menu.isVisible()) throw new AssertionError("메뉴 기본 닫힘");
                gear.doClick();if(!menu.isVisible()) throw new AssertionError("메뉴 열기");
                layout(screen);var img=new BufferedImage(1100,690,BufferedImage.TYPE_INT_RGB);var g=img.createGraphics();screen.printAll(g);g.dispose();ImageIO.write(img,"png",new File("battle-menu-preview.png"));
                gear.doClick();if(menu.isVisible()) throw new AssertionError("메뉴 닫기");
                gear.doClick();((JButton)menu.getComponent(0)).doClick();
                if(menu.isVisible()) throw new AssertionError("재시작 후 메뉴 닫힘");
                gear.doClick();((JButton)menu.getComponent(1)).doClick();
                if(returns[0]!=1 || ((Timer)field(screen,"phaseTimer")).isRunning() || ((Timer)field(screen,"countdownTimer")).isRunning()) throw new AssertionError("메인 복귀와 타이머 종료");
            } catch(Exception e) {throw new RuntimeException(e);}
        });
        System.out.println("PASS: gear menu toggle, restart, main callback, timer cleanup, render");
    }
}
