package game.test;

import java.util.List;
import javax.swing.*;
import game.backend.battle.DemoBattle;
import game.backend.model.Card;
import game.database.CardCatalog;
import game.frontend.battle.BattleScreenPanel;

/**
 * 에너지 없는 규칙 검사:
 *  - 카드 등급(비용) 합이 3을 넘어도 장수 제한(공격 1 · 회복 1 · 주문 2) 안에서는 모두 사용 가능
 *  - 상대 덱을 모두 소진하면 '승리' (GAME OVER 가 아니라 VICTORY)
 */
public final class NoEnergyRulesCheck {
    private static void check(boolean ok, String text) { if (!ok) throw new AssertionError(text); }

    @SuppressWarnings("unchecked")
    private static void setHand(Object battle, String name, Card... cards) throws Exception {
        var hand = (List<Card>) BattleTestAccess.field(battle, name); hand.clear(); hand.addAll(List.of(cards));
    }

    public static void main(String[] args) throws Exception {
        var c = CardCatalog.allCards();
        Card attack3 = c.get(4), heal3 = c.get(19), spell3 = c.get(14), spell2 = c.get(13), spell1 = c.get(11);
        check(attack3.cost() + heal3.cost() + spell3.cost() + spell2.cost() == 11, "등급 합 11 (예전 에너지 3이면 불가능)");

        DemoBattle b = new DemoBattle(c); b.enableReactions();
        setHand(b, "hand", heal3, spell3, spell2, spell1, attack3);
        b.play(0);                                   // 회복 3등급
        check(b.canPlay(0) && b.canPlay(1), "회복 후에도 주문 사용 가능");
        b.play(0); b.play(0);                        // 주문 3등급 + 주문 2등급
        check(!b.canPlay(0), "주문 3번째는 불가 (턴당 2장)");
        check(b.canPlay(1), "등급 합이 커도 공격 1장 사용 가능");
        b.play(1);
        check(b.pendingAttack() == attack3, "공격 공개");

        // 손패 유지: 쓰지 않은 카드는 다음 턴에도 남고, 쓴 자리만 새로 뽑음
        DemoBattle k = new DemoBattle(c); k.enableReactions();
        var before = new java.util.ArrayList<>(k.hand());
        int healIndex = -1;
        for (int i = 0; i < before.size(); i++) if (k.canPlay(i) && before.get(i).type() != game.backend.model.CardType.ATTACK) { healIndex = i; break; }
        if (healIndex >= 0) {
            Card used = before.get(healIndex);
            k.play(healIndex);
            k.startNextRound();
            var kept = new java.util.ArrayList<>(before); kept.remove(used);
            var now = new java.util.ArrayList<>(k.hand());
            for (Card card : kept) check(now.remove(card), "쓰지 않은 카드 유지: " + card.name());
            check(k.hand().size() == 5, "1장 쓰고 1장 뽑아 5장");
        }

        // 손패는 5장을 넘어도 버리지 않고 최대 20장까지
        DemoBattle g = new DemoBattle(c); g.enableReactions();
        for (int t = 0; t < 30; t++) g.startNextRound();
        check(g.hand().size() == DemoBattle.MAX_HAND && g.drawCount() == 0, "카드를 안 쓰면 20장까지 쌓임, 실제 " + g.hand().size());

        // 상대 덱 소진 → 승리
        DemoBattle d = new DemoBattle(c); d.enableReactions();
        setHand(d, "enemyHand"); setHand(d, "enemyDraw");
        check(d.isOver() && d.playerWon() && d.enemyHp() > 0, "적 체력이 남아도 덱 소진이면 플레이어 승리");

        SwingUtilities.invokeAndWait(() -> {
            try {
                int[] won = {0};
                var panel = new BattleScreenPanel(() -> { }, 1, () -> won[0]++, c);
                var frame = new JFrame(); frame.setContentPane(panel); frame.setSize(1100, 730);
                Object battle = BattleTestAccess.field(panel, "battle");
                setHand(battle, "enemyHand"); setHand(battle, "enemyDraw");
                var m = BattleScreenPanel.class.getDeclaredMethod("completeEnemyRound"); m.setAccessible(true); m.invoke(panel);
                var go = (JComponent) BattleTestAccess.field(panel, "gameOver");
                var banner = (JLabel) BattleTestAccess.field(panel, "turnBanner");
                check(!go.isVisible(), "덱 소진 승리 시 GAME OVER 가 뜨지 않음");
                check("VICTORY".equals(banner.getText()), "VICTORY 표시, 실제 " + banner.getText());
                var r = BattleScreenPanel.class.getDeclaredMethod("refresh"); r.setAccessible(true); r.invoke(panel);
                frame.dispose();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("PASS: no energy, unused cards stay, 1 draw per turn up to 20 cards, enemy deck-out = victory");
        System.exit(0);
    }
}
