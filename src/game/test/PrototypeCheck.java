package game.test;

import javax.swing.SwingUtilities;
import java.awt.image.BufferedImage;
import game.backend.battle.DemoBattle;
import game.database.CardCatalog;
import game.frontend.battle.CardView;
import game.frontend.battle.HandPanel;

public final class PrototypeCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        DemoBattle battle = new DemoBattle();
        battle.play(3); // 집중
        battle.play(0); // 베기: 6 + 2
        check(battle.enemyHp() == 27, "공격 + 집중 보너스");
        battle.play(0); // 방패
        battle.endTurn();
        check(battle.playerHp() == 38 && battle.block() == 0, "피해와 방어 초기화");
        battle.play(2); // 회복: 최대 체력 제한
        check(battle.playerHp() == 40, "최대 체력");
        check(!battle.canPlay(9) && battle.play(9).contains("사용할 수 없"), "잘못된 인덱스");
        battle.reset();
        for (int i = 0; i < 3; i++) {
            battle.play(4);
            battle.play(0);
            if (!battle.isOver()) battle.endTurn();
        }
        check(battle.enemyHp() == 0 && !battle.canPlay(0), "승리 후 입력 차단");
        battle.reset();
        while (!battle.isOver()) battle.endTurn();
        check(battle.playerHp() == 0, "패배");
        battle.reset();
        check(battle.hand().size() == 5 && battle.playerHp() == 40, "재시작");
        SwingUtilities.invokeAndWait(() -> {
            HandPanel hand = new HandPanel();
            hand.showCards(CardCatalog.sampleHand(), false, index -> { });
            check(hand.getComponentCount() == 5, "손패 컴포넌트");
            for (var card : CardCatalog.sampleHand()) {
                CardView view = new CardView(card, () -> { });
                view.setSize(180, 246);
                var canvas = new BufferedImage(180, 246, BufferedImage.TYPE_INT_ARGB);
                var graphics = canvas.createGraphics();
                view.paint(graphics);
                graphics.dispose();
            }
        });
        System.out.println("PASS: 임시 전투 로직 및 카드 렌더링 검사");
    }
}
