package game.test;

import game.backend.battle.DemoBattle;
import game.database.CardCatalog;
import game.frontend.battle.BattleScreenPanel;
import game.frontend.battle.HandPanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

/** 실제 데이터와 화면의 카드 수가 함께 줄어드는지 검사합니다. */
public final class HandStateCheck {
    private static void check(boolean value,String message) {if(!value) throw new AssertionError(message);}
    private static Object field(Object owner,String name) throws Exception {
        var f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);
    }
    private static void layout(Container c) {c.doLayout();for(Component child:c.getComponents()) if(child instanceof Container container) layout(container);}
    private static void render(JPanel panel,String name) throws Exception {
        layout(panel);var img=new BufferedImage(1100,690,BufferedImage.TYPE_INT_RGB);
        var g=img.createGraphics();panel.printAll(g);g.dispose();ImageIO.write(img,"png",new File(name));
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(()->{
            try {
                BattleScreenPanel panel=new BattleScreenPanel(()->{});panel.setSize(1100,690);
                DemoBattle battle=(DemoBattle)field(panel,"battle");
                HandPanel hand=(HandPanel)field(panel,"hand");
                check(hand.getComponentCount()==5 && battle.enemyHandCount()==5,"초기 양쪽 5장");
                render(panel,"hand-before.png");
                int attackIndex=-1;
                for(int attempt=0;attempt<100 && attackIndex<0;attempt++) {
                    if(battle.enemyIntent()>0) for(int i=0;i<battle.hand().size();i++)
                        if(battle.hand().get(i).type()==game.backend.model.CardType.ATTACK) {attackIndex=i;break;}
                    if(attackIndex<0) battle.reset();
                }
                check(attackIndex>=0,"공격 테스트 손패");battle.play(attackIndex);battle.resolveAttack(null);
                var refresh=panel.getClass().getDeclaredMethod("refresh");refresh.setAccessible(true);refresh.invoke(panel);
                check(hand.getComponentCount()==4 && battle.hand().size()==4 && battle.discardCount()==1,"플레이어 사용 후 4장");
                render(panel,"hand-player-used.png");
                battle.prepareEnemyTurn();int hp=battle.playerHp(),block=battle.block();int intended=battle.enemyIntent();
                battle.revealEnemyAttack();battle.resolveAttack(null);refresh.invoke(panel);
                check(battle.enemyHandCount()==4 && battle.enemyDiscardCount()==1,"상대 사용 후 4장");
                check(battle.lastEnemyCard()!=null && battle.lastEnemyCard().power()==intended,"공개 카드와 예고 일치");
                check(hp-battle.playerHp()==Math.max(0,intended-block),"상대 카드 피해 일치");
                check(battle.enemyHandCount()+battle.enemyDrawCount()+battle.enemyDiscardCount()==20,"상대 20장 보존");
                render(panel,"hand-enemy-used.png");
                battle.prepareEnemyTurn();check(battle.enemyHandCount()==5 && battle.enemyDrawCount()==14,"다음 상대 턴 보충");
                battle.reset();check(battle.enemyHandCount()==5 && battle.enemyDiscardCount()==0 && battle.lastEnemyCard()==null,"재시작");
            } catch(Exception ex) {throw new RuntimeException(ex);}
        });
        System.out.println("PASS: player/enemy hand counts, damage, discard, draw, reset, UI render");
    }
}
