package game.test;

import game.backend.model.Card;
import game.backend.model.PlayerDeck;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.database.CardCatalog;
import game.backend.battle.DemoBattle;
import game.frontend.deck.DeckEditorPanel;

/** 외부 테스트 라이브러리 없이 실행하는 덱/전투/화면 회귀 검사. */
public final class DeckCheck {
    private static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        PlayerDeck deck=new PlayerDeck();
        check(deck.cards().size()==20,"초기 20장");
        var invalid=new ArrayList<>(deck.cards());invalid.remove(0);
        check(!PlayerDeck.valid(invalid),"19장 거부");
        invalid.add(deck.cards().get(1));invalid.set(2,deck.cards().get(1));
        check(!PlayerDeck.valid(invalid),"동일 카드 3장 거부");
        var custom=new ArrayList<>(deck.cards());custom.set(0,deck.cards().get(1));deck.save(custom);
        custom.clear();check(deck.cards().size()==20,"저장 복사");
        DemoBattle battle=new DemoBattle(deck.cards());
        check(battle.hand().size()==5 && battle.drawCount()==15,"5장 드로우");
        for(int i=0;i<4;i++) {
            restoreTestHealth(battle);
            battle.endTurn();
            check(battle.hand().size()+battle.drawCount()+battle.discardCount()==20,"카드 보존");
        }
        // 새 규칙: 턴마다 1장만 뽑고(5 → 9장), 손패를 버리지 않으며 버린 카드를 다시 섞지 않습니다.
        check(battle.hand().size()==9 && battle.drawCount()==11 && battle.discardCount()==0,"턴마다 1장 드로우 · 손패 유지");
        // 각 카드가 손에 들어올 때까지 새 전투를 생성해 원본 데이터의 효과를 검사합니다.
        for(Card target:CardCatalog.allCards()) {
            boolean tested=false;
            for(int attempt=0;attempt<200 && !tested;attempt++) {
                battle=new DemoBattle(CardCatalog.allCards());
                int index=battle.hand().indexOf(target);if(index<0) continue;
                var effect=CardCatalog.effect(target);battle.play(index);
                check(battle.enemyHp()==35-effect.damage(),"직접 피해: "+target.name());
                check(battle.block()==effect.block(),"방어: "+target.name());
                check(battle.discardCount()==1 && battle.hand().size()==4,"사용 카드 버리기");
                if(effect.poison()>0) {
                    for(int turn=0;turn<effect.duration() && !battle.isOver();turn++) {restoreTestHealth(battle);battle.endTurn();}
                    check(battle.enemyHp()==Math.max(0,35-effect.poison()*effect.duration()),"중독 지속");
                    check(battle.poisonDamage()==0,"중독 만료");
                }
                tested=true;
            }
            check(tested,"카드 검사 누락");
        }
        SwingUtilities.invokeAndWait(()->{
            DeckEditorPanel editor=new DeckEditorPanel(deck,()->{});
            editor.setSize(1100,690);layout(editor);
            var image=new BufferedImage(1100,690,BufferedImage.TYPE_INT_RGB);
            var g=image.createGraphics();editor.printAll(g);g.dispose();
            try { ImageIO.write(image,"png",new File("deck-editor-preview.png")); }
            catch(Exception ex) { throw new RuntimeException(ex); }
        });
        System.out.println("PASS: deck validation, draw/discard, 20 card effects, editor rendering");
    }
    private static void layout(Container parent) {
        parent.doLayout();for(Component c:parent.getComponents()) if(c instanceof Container container) layout(container);
    }
    // 무작위 상대 공격으로 드로우/중독 검사 도중 조기 패배하지 않도록 테스트 체력만 복원합니다.
    private static void restoreTestHealth(DemoBattle battle) throws Exception {
        ((game.backend.model.Player)BattleTestAccess.field(battle,"player")).heal(DemoBattle.PLAYER_MAX_HP);
    }
}
