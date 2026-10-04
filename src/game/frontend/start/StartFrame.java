package game.frontend.start;

import game.backend.model.StageProgress;
import game.frontend.battle.BattleScreenPanel;
import game.frontend.stage.StageSelectPanel;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

/** 탑 테마 메인 화면. 기존 3개 시안 클래스는 보존합니다. */
public final class StartFrame extends JFrame {
    // JFrame = 실제 윈도우 창, JPanel = 창 안에 들어가는 화면 조각입니다.
    private final FantasyStartPanel menu;
    private final CardLayout pages = new CardLayout();
    // CardLayout은 여러 화면을 쌓아 놓고 이름으로 한 화면씩 보여주는 배치 방식입니다.
    private final JPanel screens = new JPanel(pages);
    // 전투도 같은 창의 화면 조각으로 관리합니다. 새 JFrame을 만들지 않습니다.
    private BattleScreenPanel battleScreen;
    private final StageProgress progress = new StageProgress();
    private final StageSelectPanel stageScreen;
    public StartFrame() {
        super("게임 타이틀 (미정)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1100, 730);
        setLocationRelativeTo(null);
        // 버튼이 눌렸을 때 실행할 동작을 전달합니다. () -> ... 는 나중에 실행할 코드입니다.
        // 순서: 게임 시작 / 게임 설명 / 설정 / 종료.
        menu = new FantasyStartPanel(() -> pages.show(screens, "stages"),
            () -> new GameGuideDialog(this).setVisible(true),
            () -> new SettingsDialog(this).setVisible(true), () -> System.exit(0));
        // "menu", "stages"는 화면을 찾는 이름입니다. 파일 이름과는 관계없습니다.
        screens.add(menu, "menu");
        stageScreen = new StageSelectPanel(() -> {
            pages.show(screens, "menu");
            menu.focusStart();
        }, progress, this::showBattlePreview);
        screens.add(stageScreen, "stages");
        setContentPane(screens);
        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent event) { menu.focusStart(); }
        });
    }

    private void showBattlePreview(int stage) {
        if (!progress.canEnter(stage)) return;
        // 체험에 다시 들어갈 때는 기존과 같이 새 전투로 시작합니다.
        // 이전 패널을 제거해 중복 화면이 쌓이지 않도록 합니다.
        if (battleScreen != null) screens.remove(battleScreen);
        battleScreen = new BattleScreenPanel(() -> {
            stageScreen.refreshProgress();
            pages.show(screens, "stages");
        }, stage, () -> {
            progress.complete(stage);
            stageScreen.refreshProgress();
        });
        screens.add(battleScreen, "battle");
        // 창의 위치/크기는 그대로 두고 내용만 전투 화면으로 전환합니다.
        pages.show(screens, "battle");
        screens.revalidate();
        screens.repaint();
    }

}
