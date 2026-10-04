package game.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.backend.battle.DemoBattle;
import game.backend.model.Card;
import game.backend.model.RunUpgrades;
import game.backend.model.ShopItem;
import game.database.CardCatalog;
import game.frontend.battle.BattleScreenPanel;
import game.frontend.battle.GameOverPanel;
import game.frontend.common.ImageHotspotButton;
import game.frontend.stage.ShopPanel;

/** 상점 강화 효과, 상점 카드 버튼, GAME OVER 의 재도전/메인으로 버튼을 검사합니다. */
public final class GameOverShopCheck {
    private static void check(boolean ok, String text) { if (!ok) throw new AssertionError(text); }

    @SuppressWarnings("unchecked")
    private static void setHand(DemoBattle b, String name, Card... cards) throws Exception {
        var hand = (List<Card>) BattleTestAccess.field(b, name); hand.clear(); hand.addAll(List.of(cards));
    }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length == 0 ? "preview" : args[0]);
        out.mkdirs();
        var cards = CardCatalog.allCards();

        // ① 강화 없음 (DefenseResponseCheck 와 같은 상황): 적 33, 내 체력 38 → 36
        // ② 공격 +1 / 방어 +1 / 체력 +10 적용 시 수치가 1씩 유리해지는지
        RunUpgrades up = new RunUpgrades();
        up.apply(ShopItem.ATTACK_UP); up.apply(ShopItem.DEFENSE_UP); up.apply(ShopItem.HEAL_10);
        check(up.attackBonus() == 1 && up.defenseBonus() == 1 && up.bonusHp() == 10, "강화 누적");
        DemoBattle b = new DemoBattle(cards, up); b.enableReactions();
        check(b.playerMaxHp() == 50 && b.playerHp() == 50, "체력 +10 으로 시작");
        setHand(b, "hand", cards.get(0), cards.get(15), cards.get(5)); setHand(b, "enemyHand", cards.get(5), cards.get(0));
        b.play(0); b.resolveAttack(b.chooseEnemyDefense());
        check(b.enemyHp() == 32, "공격 +1: 피해 2 → 3 (적 35 → 32), 실제 " + b.enemyHp());
        int hp = b.playerHp();
        b.revealEnemyAttack(); var def = b.commitDefense(1); b.resolveAttack(def);
        check(hp - b.playerHp() == 1, "방어 +1: 받는 피해 2 → 1, 실제 " + (hp - b.playerHp()));
        b.reset(); check(b.playerHp() == 50, "재시작해도 강화 유지");
        DemoBattle plain = new DemoBattle(cards); check(plain.playerMaxHp() == 40, "강화 없으면 체력 40");

        SwingUtilities.invokeAndWait(() -> {
            try {
                // ③ 상점: 카드 버튼 클릭 → 강화 1회만 적용
                RunUpgrades shopUp = new RunUpgrades();
                int[] cont = {0};
                ShopPanel shop = new ShopPanel(shopUp, () -> { }, () -> cont[0]++);
                shop.setSize(1100, 730); shop.doLayout();
                var hot = buttons(shop, ImageHotspotButton.class);
                check(hot.size() == 3 && hot.stream().allMatch(JButton::isEnabled), "상점 카드 버튼 3개");
                check(hot.get(0).getWidth() > 80 && hot.get(0).getHeight() > 150, "카드 버튼 크기가 그림 카드에 맞음");
                hot.get(1).doClick(0);
                check(shopUp.defenseBonus() == 1 && shop.purchased() == ShopItem.DEFENSE_UP, "방어력 +1 구매");
                check(hot.stream().noneMatch(JButton::isEnabled), "구매 후 다른 카드 잠금");
                hot.get(0).doClick(0);
                check(shopUp.attackBonus() == 0, "두 번 구매 불가");
                snap(shop, new File(out, "shop.png"));

                // ④ GAME OVER: 패배 → 재도전 / 메인으로
                int[] back = {0};
                BattleScreenPanel panel = new BattleScreenPanel(() -> back[0]++, 1, () -> { }, cards, new RunUpgrades());
                JFrame frame = new JFrame(); frame.setContentPane(panel); frame.setSize(1100, 730);
                panel.setSize(1100, 730); layout(panel);
                GameOverPanel go = buttons(panel, GameOverPanel.class).get(0);
                check(!go.isVisible(), "처음에는 GAME OVER 숨김");
                DemoBattle battle = (DemoBattle) BattleTestAccess.field(panel, "battle");
                var player = (game.backend.model.Player) BattleTestAccess.field(battle, "player");
                player.takeDamage(999);
                var finish = BattleScreenPanel.class.getDeclaredMethod("finishBattle", boolean.class);
                finish.setAccessible(true); finish.invoke(panel, false);
                layout(panel);
                check(go.isVisible(), "패배 시 GAME OVER 표시");
                var goButtons = buttons(go, ImageHotspotButton.class);
                check(goButtons.size() == 2, "재도전 / 메인으로 버튼");
                snap(panel, new File(out, "game-over.png"));
                goButtons.get(0).doClick(0);
                check(!go.isVisible() && battle.playerHp() == 40 && battle.turn() == 1, "재도전 → 새 전투");
                snap(panel, new File(out, "battle-player.png"));
                player.takeDamage(999); finish.invoke(panel, false);
                check(go.isVisible(), "다시 패배");
                goButtons.get(1).doClick(0);
                check(back[0] == 1 && !go.isVisible(), "메인으로 → 메인 화면 콜백");
                frame.dispose();
            } catch (Exception e) { throw new RuntimeException(e); }
        });
        System.out.println("PASS: shop upgrades (+1 attack/+1 defense/+10 hp), shop card buttons, game over retry/main");
        System.exit(0);
    }

    private static <T> List<T> buttons(Container root, Class<T> type) {
        var list = new java.util.ArrayList<T>();
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) list.add(type.cast(c));
            if (c instanceof Container k) list.addAll(buttons(k, type));
        }
        return list;
    }
    private static void layout(Container c) { c.doLayout(); for (Component x : c.getComponents()) if (x instanceof Container k) layout(k); }
    private static void snap(JComponent c, File f) {
        layout(c);
        BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics(); c.printAll(g); g.dispose();
        try { ImageIO.write(im, "png", f); } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }
}
