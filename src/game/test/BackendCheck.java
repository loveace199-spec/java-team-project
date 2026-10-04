package game.test;

import game.backend.model.Enemy;
import game.backend.model.Player;

/** backend 영역 검사: Player / Enemy 상태 계산 (창 없이 실행). */
public final class BackendCheck {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Player p = new Player();
        p.addBlock(5);
        check(p.takeDamage(7) == 2 && p.hp() == 38 && p.block() == 0, "방어도가 먼저 피해 흡수");
        p.addBlock(10);
        check(p.takeDamage(4) == 0 && p.block() == 6, "방어도가 남으면 체력 유지");
        check(p.heal(100) == 2 && p.hp() == Player.MAX_HP, "최대 체력 초과 회복 불가");
        p.addBonusDamage(2);
        check(p.consumeBonusDamage() == 2 && p.bonusDamage() == 0, "추가 피해는 한 번만 사용");
        p.spendEnergy(3);
        check(p.energy() == 0, "에너지 소비");
        try { p.spendEnergy(1); throw new AssertionError("에너지 부족 시 거부"); }
        catch (IllegalArgumentException expected) { }
        p.refillEnergy();
        check(p.energy() == Player.MAX_ENERGY, "에너지 회복");

        Enemy e = new Enemy();
        e.takeDamage(100);
        check(e.hp() == 0 && e.isDefeated(), "적 체력 0 이하로 내려가지 않음");
        e.reset();
        check(e.hp() == Enemy.MAX_HP && !e.isDefeated(), "적 초기화");
        System.out.println("PASS: backend (Player / Enemy)");
    }
}
