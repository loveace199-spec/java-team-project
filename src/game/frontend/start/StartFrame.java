package game.frontend.start;

import game.frontend.stage.ShopPanel;
import game.frontend.stage.StageSelectPanel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;
import game.frontend.battle.BattleScreenPanel;
import game.backend.model.StageProgress;
import game.database.SaveStore;

/** 탑 테마 메인 화면. 기존 3개 시안 클래스는 보존합니다. */
public final class StartFrame extends JFrame {
    private final FantasyStartPanel menu;
    private final CardLayout pages=new CardLayout();
    private final JPanel screens=new JPanel(pages);
    private BattleScreenPanel battleScreen;
    // 추가: 진행도·덱·상점보다 먼저 새 게임의 DB 저장 객체를 준비합니다.
    private final SaveStore store=new SaveStore();
    // 변경: 현재 게임의 진행도를 DB와 연결합니다.
    private final StageProgress progress=new StageProgress(store);
    private final StageSelectPanel stageScreen;
    private final game.backend.model.RunUpgrades upgrades=new game.backend.model.RunUpgrades();
    // 변경: 같은 저장 객체를 전달해 진행도와 덱이 동일한 게임을 사용합니다.
    private final game.backend.model.PlayerDeck deck=new game.backend.model.PlayerDeck(store);

    public StartFrame() {
        super(FantasyStartPanel.GAME_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000,680));
        setSize(1100,730);
        setLocationRelativeTo(null);

        menu=new FantasyStartPanel(()->pages.show(screens,"stages"),
            ()->new GameGuideDialog(this).setVisible(true),
            ()->new SettingsDialog(this).setVisible(true),
            ()->System.exit(0));

        screens.add(menu,"menu");

        var editor=new game.frontend.deck.DeckEditorPanel(deck,()->pages.show(screens,"stages"));
        screens.add(editor,"deck");

        stageScreen=new StageSelectPanel(()->{
            pages.show(screens,"menu");
            menu.focusStart();
        },progress,this::showBattlePreview,()->{
            editor.beginEditing();
            pages.show(screens,"deck");
        });

        screens.add(stageScreen,"stages");

        // 변경: 상점도 현재 게임의 저장 객체를 공유합니다.
        screens.add(new ShopPanel(upgrades,store,()->pages.show(screens,"stages"),()->{
            progress.complete(3);
            stageScreen.refreshProgress();
            pages.show(screens,"stages");
        }),"shop");

        setContentPane(screens);

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent event) {menu.focusStart();}
        });
    }

    /** 전투 화면에서 스테이지 선택 화면으로 돌아갑니다. */
    private void showStages() {
        stageScreen.refreshProgress();
        pages.show(screens,"stages");
        screens.revalidate();
        screens.repaint();
    }

    private void showBattlePreview(int stage) {
        if(!progress.canEnter(stage)) return;

        if(stage==3) {
            if(battleScreen!=null) {
                screens.remove(battleScreen);
                battleScreen=null;
            }
            pages.show(screens,"shop");
            return;
        }

        if(battleScreen!=null) screens.remove(battleScreen);

        battleScreen=new BattleScreenPanel(
            ()->{
                stageScreen.refreshProgress();
                pages.show(screens,"menu");
                menu.focusStart();
            },
            this::showStages,
            stage,
            ()->{
                progress.complete(stage);
                stageScreen.refreshProgress();
            },
            deck.cards(),
            upgrades
        );

        screens.add(battleScreen,"battle");
        pages.show(screens,"battle");
        screens.revalidate();
        screens.repaint();
    }
}
