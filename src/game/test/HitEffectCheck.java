package game.test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.backend.battle.DemoBattle;
import game.database.CardCatalog;
import game.frontend.battle.BattleBoardPanel;
import game.frontend.battle.BattleScreenPanel;

/** 체력이 줄면 피격 연출(흔들림·빨간 빛·-숫자), 늘면 회복 연출(초록 빛·+숫자)이 생기고 사라지는지 검사합니다. */
public final class HitEffectCheck {
    private static void check(boolean ok, String text) { if (!ok) throw new AssertionError(text); }

    public static void main(String[] args) throws Exception {
        File out = new File(args.length == 0 ? "preview" : args[0]);
        out.mkdirs();
        JFrame[] frame = {null};
        BattleScreenPanel[] panel = {null};
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new BattleScreenPanel(() -> { }, 1, () -> { }, CardCatalog.allCards());
            frame[0] = new JFrame(); frame[0].setContentPane(panel[0]); frame[0].setSize(1100, 730); frame[0].setVisible(true);
        });
        Thread.sleep(500);
        DemoBattle battle = (DemoBattle) BattleTestAccess.field(panel[0], "battle");
        BattleBoardPanel board = (BattleBoardPanel) BattleTestAccess.field(panel[0], "board");
        var enemy = (game.backend.model.Enemy) BattleTestAccess.field(battle, "enemy");
        var player = (game.backend.model.Player) BattleTestAccess.field(battle, "player");

        // 피격: 적 -7, 플레이어 -5
        SwingUtilities.invokeAndWait(() -> { enemy.takeDamage(7); player.takeDamage(5); board.repaint(); });
        Thread.sleep(120);
        SwingUtilities.invokeAndWait(() -> snap(frame[0].getContentPane(), new File(out, "hit.png")));
        check(changes(board) == 2, "피격 연출 2개(적·플레이어) 생성, 실제 " + changes(board));

        // 회복: 플레이어 +4
        Thread.sleep(1100);
        check(changes(board) == 0, "1초 뒤 연출 사라짐");
        SwingUtilities.invokeAndWait(() -> { player.heal(4); board.repaint(); });
        Thread.sleep(150);
        SwingUtilities.invokeAndWait(() -> snap(frame[0].getContentPane(), new File(out, "heal.png")));
        check(changes(board) == 1, "회복 연출 생성");

        // 재시작: 체력이 원래대로 돌아가도 '+숫자' 연출이 나오지 않아야 함
        Thread.sleep(1100);
        var restart = BattleScreenPanel.class.getDeclaredMethod("restartBattle");
        restart.setAccessible(true);
        SwingUtilities.invokeAndWait(() -> { try { restart.invoke(panel[0]); } catch (Exception e) { throw new RuntimeException(e); } board.repaint(); });
        Thread.sleep(150);
        check(changes(board) == 0, "재시작 시 회복 연출 없음");
        SwingUtilities.invokeAndWait(() -> frame[0].dispose());
        System.out.println("PASS: hit (shake/red/-N) and heal (green/+N) effects appear and fade; none on restart");
        System.exit(0);
    }

    private static int changes(BattleBoardPanel board) {
        try { return ((List<?>) BattleTestAccess.field(board, "hpChanges")).size(); } catch (Exception e) { throw new RuntimeException(e); }
    }
    private static void snap(Container c, File f) {
        BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics(); c.paintAll(g); g.dispose();
        try { ImageIO.write(im, "png", f); } catch (java.io.IOException e) { throw new RuntimeException(e); }
    }
}
