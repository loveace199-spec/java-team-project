package game.test;

import java.util.List;
import game.backend.battle.DemoBattle;
import game.database.CardCatalog;
import game.backend.model.Card;

/** 고정 손패로 공격 공개/방어 소비/지연 판정/중복 입력을 검사합니다. */
public final class DefenseResponseCheck {
    private static void check(boolean ok,String text) {if(!ok) throw new AssertionError(text);}
    @SuppressWarnings("unchecked")
    private static void setHand(DemoBattle battle,String name,Card... cards) throws Exception {
        var hand=(List<Card>)BattleTestAccess.field(battle,name);hand.clear();hand.addAll(List.of(cards));
    }
    public static void main(String[] args) throws Exception {
        var cards=CardCatalog.allCards();DemoBattle b=new DemoBattle(cards);b.enableReactions();
        setHand(b,"hand",cards.get(0),cards.get(15),cards.get(5));setHand(b,"enemyHand",cards.get(5),cards.get(0));
        check(!b.canPlay(2),"내 턴 방어 금지");b.play(0);
        check(b.enemyHp()==35 && b.pendingAttack()!=null,"공격 공개시 피해 없음");
        check(!b.canPlay(0),"대응 중 일반 카드 금지");
        Card defense=b.chooseEnemyDefense();check(defense!=null && b.enemyHandCount()==1,"상대 방어 소비");
        b.resolveAttack(defense);check(b.enemyHp()==33 && b.playerHp()==38,"5공격-3방어 및 반격2");
        // 공격과 회복은 같은 턴에 각각 1장씩 사용할 수 있습니다.
        check(b.canPlay(0),"공격 후 회복 카드 사용 가능");
        b.play(0);check(b.playerHp()==40 && b.enemyHp()==30,"회복 카드 사용");
        int before=b.playerHp();b.revealEnemyAttack();check(b.playerHp()==before && b.waitingForDefense(),"상대 공개 후 대기");
        check(b.canPlay(0) && !b.canPlay(1),"방어만 선택 가능");
        defense=b.commitDefense(0);check(b.playerHp()==before,"방어 공개시 피해 없음");
        b.resolveAttack(defense);check(b.playerHp()==38 && b.enemyHp()==28,"플레이어 방어 판정");
        b.resolveAttack(defense);check(b.playerHp()==38,"중복 판정 방지");
        b.startNextRound();check(b.turn()==2 && b.hand().size()==5,"판정 후 다음 턴");
        setHand(b,"enemyHand",cards.get(0));b.revealEnemyAttack();b.resolveAttack(null);check(b.playerHp()==33,"방어 안 함");
        b.reset();check(b.pendingAttack()==null,"재시작 중 대기 공격 제거");
        setHand(b,"hand",cards.get(4));setHand(b,"enemyHand",cards.get(9));b.play(0);b.resolveAttack(b.chooseEnemyDefense());
        check(b.enemyHp()==31 && b.playerHp()==32,"고방어 반격");
        System.out.println("PASS: deferred attack, defense window, costs, counter damage, turn limit, skip, reset");
    }
}
