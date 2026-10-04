package game.frontend.legacy;

import game.frontend.common.StartComponents;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import game.frontend.common.Theme;

/** 이전 시안 3(보존용). 현재 시작 화면에서 사용하지 않으며 비교 렌더링 테스트에 사용합니다. */
public final class MinimalStartPanel extends JPanel {
    public MinimalStartPanel(Runnable start, Runnable help, Runnable exit) {
        super(new GridLayout(1, 2, 56, 0));
        setBackground(new Color(242, 239, 230));
        setBorder(new EmptyBorder(48, 64, 42, 64));
        Color ink = new Color(31, 52, 52);
        Color muted = new Color(99, 111, 109);
        JPanel copy = new JPanel();
        copy.setOpaque(false);
        copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        copy.add(StartComponents.text("03 / SIMPLE PLAY", 13, muted));
        copy.add(Box.createVerticalStrut(32));
        copy.add(StartComponents.text("가볍게 시작하는", 34, ink));
        copy.add(StartComponents.text("깊이 있는 선택.", 34, ink));
        copy.add(Box.createVerticalStrut(22));
        copy.add(StartComponents.text("매 턴, 필요한 카드를 선택하세요.", 15, muted));
        copy.add(Box.createVerticalStrut(34));
        copy.add(StartComponents.menu(Theme.TEXT, muted, new Color(38, 106, 90),
            Color.WHITE, start, help, exit));
        add(copy);

        JPanel guide = new JPanel();
        guide.setLayout(new BoxLayout(guide, BoxLayout.Y_AXIS));
        guide.setBackground(new Color(228, 233, 221));
        guide.setBorder(new EmptyBorder(28, 30, 24, 22));
        guide.add(StartComponents.text("HOW TO PLAY", 13, muted));
        guide.add(Box.createVerticalStrut(28));
        String[] titles = {"01   적의 행동 확인", "02   카드 선택", "03   턴 종료"};
        String[] details = {"다가올 공격을 보고 준비하세요.", "에너지 안에서 효과를 조합하세요.", "공격을 버티고 다음 수를 생각하세요."};
        for (int i = 0; i < titles.length; i++) {
            guide.add(StartComponents.text(titles[i], 21, ink));
            guide.add(Box.createVerticalStrut(10));
            guide.add(StartComponents.text(details[i], 13, muted));
            guide.add(Box.createVerticalStrut(34));
        }
        guide.add(Box.createVerticalGlue());
        guide.add(StartComponents.text("화면 시제품 / 게임 이름 미정", 12, muted));
        add(guide);
    }
}
