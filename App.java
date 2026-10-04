package prototype;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import prototype.ui.start.StartFrame;

public final class App {
    // [실행 시작점] Eclipse에서 이 파일을 Run As > Java Application으로 실행합니다.
    // 한 창 안의 화면 흐름: StartFrame → StageSelectPanel → BattleScreenPanel.
    public static void main(String[] args) {
        // Swing 화면 생성/변경은 전용 UI 스레드(EDT)에서 실행해야 합니다.
        SwingUtilities.invokeLater(() -> {
            UIManager.put("ToolTip.font", new java.awt.Font("맑은 고딕", 0, 14));
            new StartFrame().setVisible(true);
        });
    }
}
