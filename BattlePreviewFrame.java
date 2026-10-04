package prototype.ui;

import java.awt.*;
import javax.swing.*;

/** 전투 화면만 실행할 때 이 파일을 Java Application으로 실행하세요. */
public final class BattlePreviewFrame extends JFrame {
    // 전투만 단독 실행하는 테스트용 창입니다. App의 일반 화면 이동에서는 사용하지 않습니다.
    public BattlePreviewFrame() {
        super("전투 시안 · 상대 12시 / 플레이어 6시");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 760));
        setSize(1180, 800);
        // 단독 실행용이므로 돌아가기 버튼은 이 테스트 창을 닫습니다.
        setContentPane(new BattleScreenPanel(this::dispose));
        setLocationRelativeTo(null);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BattlePreviewFrame().setVisible(true));
    }
}
