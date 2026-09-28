package prototype.ui.start;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import prototype.ui.Theme;

/** 설정은 현재 실행 중에만 적용. 오디오는 미구현 상태입니다. */
public final class SettingsDialog extends JDialog {
    // JDialog는 부모 창 위에 띄우는 보조 창입니다. true이면 닫기 전까지 부모 입력을 막습니다.
    // 실제 제공 기능은 창 크기 변경과 조작 안내입니다. 소리/키 변경은 아직 없습니다.
    public SettingsDialog(JFrame owner) {
        super(owner,"환경 설정",true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel content=new JPanel();
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS));
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(26,30,26,30));
        setContentPane(content);
        content.add(StartComponents.text("환경 설정",25,Theme.TEXT));
        content.add(Box.createVerticalStrut(24));
        content.add(StartComponents.text("화면 크기 (시작 화면에 적용)",15,Theme.TEXT));
        content.add(Box.createVerticalStrut(8));
        JComboBox<String> size=new JComboBox<>(new String[]{"1100 × 730","1280 × 800","최대화"});
        size.setFont(Theme.font(Font.PLAIN,15));
        size.setAlignmentX(LEFT_ALIGNMENT); size.setMaximumSize(new Dimension(400,34));
        size.setSelectedIndex((owner.getExtendedState()&JFrame.MAXIMIZED_BOTH)!=0 ? 2 : owner.getWidth()>=1280 ? 1 : 0);
        content.add(size);
        content.add(Box.createVerticalStrut(22));
        content.add(StartComponents.text("소리 크기 · 음원 연결 전",15,Theme.TEXT));
        JSlider volume=new JSlider(0,100,50);
        volume.setEnabled(false); volume.setOpaque(false); volume.setAlignmentX(LEFT_ALIGNMENT);
        volume.setMaximumSize(new Dimension(400,35)); content.add(volume);
        content.add(StartComponents.text("현재 시제품에는 배경음과 효과음이 없습니다.",12,Theme.MUTED));
        content.add(Box.createVerticalStrut(24));
        content.add(StartComponents.text("조작 안내",16,Theme.TEXT));
        content.add(Box.createVerticalStrut(10));
        content.add(StartComponents.text("시작 메뉴: ↑ ↓ / Tab 이동, Enter 또는 Space 선택",13,Theme.MUTED));
        content.add(Box.createVerticalStrut(6));
        content.add(StartComponents.text("전투: 카드 클릭으로 사용, 턴 종료 버튼으로 진행",13,Theme.MUTED));
        content.add(Box.createVerticalStrut(26));
        content.add(StartComponents.button("적용하고 닫기",Theme.GOLD,Theme.BACKGROUND,()->{
            if(size.getSelectedIndex()==2) owner.setExtendedState(JFrame.MAXIMIZED_BOTH);
            else {
                owner.setExtendedState(JFrame.NORMAL);
                Rectangle usable=GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
                owner.setSize(Math.min(size.getSelectedIndex()==0?1100:1280,usable.width),
                    Math.min(size.getSelectedIndex()==0?730:800,usable.height));
                owner.setLocationRelativeTo(null);
            }
            dispose();
        }));
        getRootPane().registerKeyboardAction(e->dispose(),KeyStroke.getKeyStroke("ESCAPE"),JComponent.WHEN_IN_FOCUSED_WINDOW);
        pack(); setResizable(false); setLocationRelativeTo(owner);
    }
}
