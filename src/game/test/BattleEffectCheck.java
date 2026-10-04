package game.test;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import game.frontend.battle.BattleEffectOverlay;
import game.database.CardCatalog;

/** 연출 종료 콜백, 취소, 이미지 렌더링을 확인합니다. */
public final class BattleEffectCheck {
    public static void main(String[] args) throws Exception {
        AtomicInteger completed=new AtomicInteger();
        BattleEffectOverlay[] layer=new BattleEffectOverlay[1];
        SwingUtilities.invokeAndWait(()->{
            layer[0]=new BattleEffectOverlay();layer[0].setSize(1100,690);
            layer[0].showAction(CardCatalog.allCards().get(5),"내가 사용한 카드","피해 2 · 방어도 +3",false,completed::incrementAndGet);
        });
        Thread.sleep(550);
        SwingUtilities.invokeAndWait(()->{
            var image=new BufferedImage(1100,690,BufferedImage.TYPE_INT_ARGB);
            var g=image.createGraphics();layer[0].paint(g);g.dispose();
            try { ImageIO.write(image,"png",new File("battle-effect-preview.png")); }
            catch(Exception ex) { throw new RuntimeException(ex); }
        });
        Thread.sleep(1300);
        SwingUtilities.invokeAndWait(()->{
            if(completed.get()!=1 || layer[0].isPlaying() || layer[0].isVisible()) throw new AssertionError("연출 완료");
            layer[0].showAction(CardCatalog.allCards().get(0),"나","피해 5",false,completed::incrementAndGet);
            layer[0].cancel();
        });
        Thread.sleep(1750);
        SwingUtilities.invokeAndWait(()->{if(completed.get()!=1) throw new AssertionError("취소 콜백 실행됨");});
        System.out.println("PASS: effect render, completion and cancellation");
    }
}
