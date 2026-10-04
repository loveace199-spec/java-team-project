package game.test;

import game.backend.battle.DemoBattle;

/** 테스트에서만 호환 계층 내부 엔진과 고정 손패에 접근합니다. 제품 API에는 노출하지 않습니다. */
public final class BattleTestAccess {
    static Object field(Object owner,String name) throws Exception {
        if(owner instanceof DemoBattle && !name.equals("battle")) owner=field(owner,"battle");
        var f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);
    }
}
