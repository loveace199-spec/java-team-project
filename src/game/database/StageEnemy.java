package game.database;

/** 단계별 상대 표시 정보. 전투 수치와 이미지 설정을 분리합니다. */
public record StageEnemy(String name, String imagePath) {
    public static StageEnemy forStage(int stage) {
        return switch (stage) {
            case 1 -> new StageEnemy("스켈레톤", "/assets/skeleton-warrior-animated-v1.png");
            case 2 -> new StageEnemy("미노타우르스", "/assets/enemy-stage2-minotaur-v1.png");
            case 4 -> new StageEnemy("슬라임", "/assets/enemy-stage4-slime-v1.png");
            case 5 -> new StageEnemy("역병의사", "/assets/enemy-stage5-plague-doctor-v1.png");
            default -> throw new IllegalArgumentException("전투 단계가 아닙니다: " + stage);
        };
    }
}
