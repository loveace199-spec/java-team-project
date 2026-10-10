package game.backend.model;

import game.database.SaveStore;

/** 변경: 기존 단계 개방 규칙을 유지하고 진행도를 DB와 연결합니다. */
public final class StageProgress {
    public static final int COUNT = 5;
    // 추가: 현재 게임의 저장 객체를 공유합니다.
    private final SaveStore store;
    private int cleared;
    // 추가: 기존 기본 생성자 호출은 유지합니다.
    public StageProgress() { this(new SaveStore()); }
    // 추가: GAME_RUN에서 읽은 진행도로 시작합니다.
    public StageProgress(SaveStore store) {
        this.store = store;
        cleared = store.loadCleared();
    }
    public int clearedCount() { return cleared; }
    public boolean isCleared(int stage) { return stage >= 1 && stage <= cleared; }
    public boolean canEnter(int stage) { return stage >= 1 && stage <= COUNT && stage <= cleared + 1; }
    public void complete(int stage) {
        // 현재 열린 다음 단계만 진행도를 증가시킵니다. 재도전 승리는 중복 집계하지 않습니다.
        // 변경: 다음 단계 완료 시 DB 저장에 성공한 뒤 메모리 값을 증가시킵니다.
        if (stage == cleared + 1 && canEnter(stage)) {
            store.saveCleared(cleared + 1);
            cleared++;
        }
    }
}
