package game.test;

import java.awt.*;
import javax.swing.*;
import game.backend.model.StageProgress;
import game.frontend.battle.BattleScreenPanel;

/** 단계 순서와 엔진의 승리 상태가 UI에서 한 번만 보고되는지 검사합니다. 카드 클릭은 DefenseUiCheck에서 검사합니다. */
public final class StageProgressCheck {
    private static void check(boolean value) { if(!value) throw new AssertionError(); }
    public static void main(String[] args) throws Exception {
        StageProgress progress=new StageProgress();
        check(progress.canEnter(1) && !progress.canEnter(2));
        progress.complete(3);
        check(progress.clearedCount()==0);
        SwingUtilities.invokeAndWait(()->{
            int[] wins={0};
            BattleScreenPanel panel=new BattleScreenPanel(()->{},1,()->{wins[0]++;progress.complete(1);});
            check(wins[0]==0);
            try {
                Object engine=BattleTestAccess.field(panel,"battle");
                ((game.backend.model.Enemy)BattleTestAccess.field(engine,"enemy")).takeDamage(35);
                var refresh=panel.getClass().getDeclaredMethod("refresh");refresh.setAccessible(true);
                refresh.invoke(panel);refresh.invoke(panel);
            } catch(Exception e) {throw new RuntimeException(e);}
            check(wins[0]==1 && progress.isCleared(1) && progress.canEnter(2) && !progress.canEnter(3));
        });
        progress.complete(1);
        check(progress.clearedCount()==1);
        for(int i=2;i<=5;i++) progress.complete(i);
        check(progress.clearedCount()==5 && !progress.canEnter(6));
        System.out.println("PASS: sequential unlock and battle victory callback");
    }
    private static boolean click(Container root,String name,boolean tooltip) {
        for(Component child:root.getComponents()) {
            if(child instanceof JButton button) {
                String text=tooltip ? button.getToolTipText():button.getText();
                if(text!=null && (tooltip ? text.startsWith(name+" ·"):text.equals(name))) {
                    button.doClick(0);return true;
                }
            }
            if(child instanceof Container nested && click(nested,name,tooltip)) return true;
        }
        return false;
    }
}
