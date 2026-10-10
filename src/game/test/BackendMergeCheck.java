package game.test;

import game.backend.model.Player;
import game.backend.model.Enemy;
import game.backend.battle.Battle;
import game.backend.battle.DemoBattle;

/** 첨부 백엔드의 상태 모델과 최신 UI 호환 계층을 함께 검사합니다. */
public final class BackendMergeCheck {
    private static void check(boolean ok,String label) {if(!ok) throw new AssertionError(label);}
    public static void main(String[] args) throws Exception {
        Player player=new Player();player.addBlock(5);
        check(player.takeDamage(3)==0 && player.block()==2,"방어 소모");
        check(player.takeDamage(7)==5 && player.hp()==35,"방어 후 피해");
        check(player.heal(20)==5 && player.hp()==40,"최대 체력");
        player.addBonusDamage(2);check(player.consumeBonusDamage()==2 && player.bonusDamage()==0,"보너스 소비");
        Enemy enemy=new Enemy();enemy.takeDamage(100);check(enemy.hp()==0 && enemy.isDefeated(),"체력 하한");
        Battle engine=new Battle();DemoBattle facade=new DemoBattle();
        for(int index:new int[]{3,0,0}) {check(engine.play(index).equals(facade.play(index)),"호환 결과");}
        check(engine.endTurn().equals(facade.endTurn()),"호환 턴 처리");
        check(engine.playerHp()==facade.playerHp() && engine.enemyHp()==facade.enemyHp(),"호환 상태");
        check(BattleTestAccess.field(facade,"battle") instanceof Battle,"실제 백엔드 위임");
        System.out.println("PASS: backend models and facade integration");
    }
}
