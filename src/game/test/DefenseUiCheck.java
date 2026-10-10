package game.test;

import javax.swing.*;
import java.util.List;
import game.backend.model.Card;
import game.database.CardCatalog;
import game.backend.battle.DemoBattle;
import game.frontend.battle.BattleScreenPanel;
import game.frontend.battle.HandPanel;

public final class DefenseUiCheck {
    private static Object field(Object o,String name) throws Exception {return BattleTestAccess.field(o,name);}
    private static void invoke(Object o,String name) throws Exception {var m=o.getClass().getDeclaredMethod(name);m.setAccessible(true);m.invoke(o);}
    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        BattleScreenPanel[] screen={null};
        SwingUtilities.invokeAndWait(()->{
            try {
                var p=new BattleScreenPanel(()->{});screen[0]=p;
                var b=(DemoBattle)field(p,"battle");
                var hand=(List<Card>)field(b,"hand");hand.clear();hand.add(CardCatalog.allCards().get(5));hand.add(CardCatalog.allCards().get(0));
                var enemy=(List<Card>)field(b,"enemyHand");enemy.clear();enemy.add(CardCatalog.allCards().get(0));
                invoke(p,"finishPhaseIntro");invoke(p,"finishPlayerTurn");((Timer)field(p,"phaseTimer")).stop();invoke(p,"finishPhaseIntro");
                if(b.playerHp()!=40) throw new AssertionError("공개 전 피해");
            } catch(Exception ex) {throw new RuntimeException(ex);}
        });
        Thread.sleep(1850);
        SwingUtilities.invokeAndWait(()->{
            try {
                var p=screen[0];var hand=(HandPanel)field(p,"hand");
                if(!((JButton)field(p,"skipDefense")).isVisible() || !hand.cardAt(0).isEnabled() || hand.cardAt(1).isEnabled()) throw new AssertionError("방어 선택 UI");
                hand.cardAt(0).doClick();
                if(((DemoBattle)field(p,"battle")).playerHp()!=40) throw new AssertionError("방어 연출 전 피해");
            } catch(Exception ex) {throw new RuntimeException(ex);}
        });
        Thread.sleep(1850);
        SwingUtilities.invokeAndWait(()->{
            try {
                var p=screen[0];var b=(DemoBattle)field(p,"battle");
                if(b.playerHp()!=38 || b.turn()!=2) throw new AssertionError("방어 판정 및 다음 턴");
                ((Timer)field(p,"phaseTimer")).stop();((Timer)field(p,"countdownTimer")).stop();
            } catch(Exception ex) {throw new RuntimeException(ex);}
        });
        System.out.println("PASS: attack reveal -> defense-only buttons -> animation -> damage -> next turn");
    }
}
