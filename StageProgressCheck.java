package prototype.test;

import java.awt.*;
import javax.swing.*;
import prototype.model.StageProgress;
import prototype.ui.BattleScreenPanel;

/** 단계 순서와 실제 카드 클릭 후 승리 보고를 함께 검사합니다. */
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
            for(int turn=0;turn<3;turn++) {
                click(panel,"강타",true);
                click(panel,"베기",true);
                if(turn<2) click(panel,"턴 종료",false);
            }
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
