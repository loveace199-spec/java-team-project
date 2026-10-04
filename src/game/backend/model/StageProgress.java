package game.backend.model;

/** 실행 중의 단계 진행도. 파일 저장은 하지 않으며 재실행 시 초기화됩니다. */
public final class StageProgress {
    public static final int COUNT = 5;
    private int cleared;
    public int clearedCount() { return cleared; }
    public boolean isCleared(int stage) { return stage >= 1 && stage <= cleared; }
    public boolean canEnter(int stage) { return stage >= 1 && stage <= COUNT && stage <= cleared + 1; }
    public void complete(int stage) {
        // 현재 열린 다음 단계만 진행도를 증가시킵니다. 재도전 승리는 중복 집계하지 않습니다.
        if (stage == cleared + 1 && canEnter(stage)) cleared++;
    }
}
