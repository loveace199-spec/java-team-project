package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.*;
import game.backend.battle.DemoBattle;

/** 전투 UI 구성. 임시 전투 로직과 카드 외형은 별도 파일에서 관리합니다. */
public final class BattleScreenPanel extends JPanel {
    private static final int TURN_SECONDS = 30;
    private enum TurnPhase { PLAYER_INTRO, PLAYER_ACTIVE, ENEMY_INTRO, DEFENSE, RESOLVING, OVER }
    private final ClashPanel clash=new ClashPanel();
    private final JButton skipDefense=button("방어 안 함",()->respondToAttack(-1));

    // [역할 분리] battle: 체력·카드 규칙 계산, board: 인물과 전장 그림, hand: 카드 나열.
    private final DemoBattle battle;
    private final BattleBoardPanel board;
    private final HandPanel hand = new HandPanel();
    private final BattleEffectOverlay effects=new BattleEffectOverlay();
    // 손패 카드에 마우스를 올리면 뜨는 설명 상자
    private final CardInfoPopup cardInfo=new CardInfoPopup();
    private final JTextArea history=new JTextArea();
    private final java.util.Deque<String> recentActions=new java.util.ArrayDeque<>();
    private final int stage;
    private final Runnable onVictory;
    private boolean victoryReported;
    private TurnPhase turnPhase = TurnPhase.PLAYER_INTRO;
    private int secondsLeft = TURN_SECONDS;
    private boolean turnSequenceStarted;
    private final JLabel title = label("", 17);
    private final JLabel message = label("사용할 카드를 클릭하세요.", 13);
    private final JLabel turnBanner = label("MY TURN", 42);
    private final Timer countdownTimer = new Timer(1000, event -> tickCountdown());
    private final Timer phaseTimer = new Timer(3000, event -> finishPhaseIntro());
    private final JButton endTurn = button("턴 종료", this::finishPlayerTurn);
    private final JPanel battleMenu=new JPanel(new GridLayout(2,1,0,5));
    private final JButton gearButton=new BattleMenuButton("",true,()->battleMenu.setVisible(!battleMenu.isVisible()));
    // 패배 시 덮는 GAME OVER 화면 (재도전 / 메인으로)
    private final GameOverPanel gameOver;

    public BattleScreenPanel(Runnable back) {
        this(back, 1, () -> { });
    }

    public BattleScreenPanel(Runnable back, int stage, Runnable onVictory) {
        this(back,stage,onVictory,game.database.CardCatalog.allCards());
    }
    public BattleScreenPanel(Runnable back, int stage, Runnable onVictory, java.util.List<game.backend.model.Card> deck) {
        this(back,stage,onVictory,deck,new game.backend.model.RunUpgrades());
    }
    /** upgrades: 상점에서 산 강화. 전투가 바뀌어도 같은 객체를 넘겨 효과를 유지합니다. */
    public BattleScreenPanel(Runnable back, int stage, Runnable onVictory, java.util.List<game.backend.model.Card> deck, game.backend.model.RunUpgrades upgrades) {
        // 전투 배경 위에 카드와 버튼을 겹쳐 놓기 위해 JLayeredPane을 사용합니다.
        super(new BorderLayout());
        this.stage = stage;
        battle=new DemoBattle(deck, upgrades);
        battle.enableReactions();
        board = new BattleBoardPanel(battle, stage);
        this.onVictory = onVictory;
        setBackground(Theme.BACKGROUND);
        Runnable goMain = ()->leaveToMain(back);
        JButton backButton = button("메인으로 가기", goMain);
        JButton restartButton = button("재시작", this::restartBattle);
        gameOver = new GameOverPanel(this::restartBattle, goMain);
        gameOver.setVisible(false);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        turnBanner.setHorizontalAlignment(SwingConstants.CENTER);
        turnBanner.setOpaque(true);
        turnBanner.setBackground(new Color(6, 12, 18, 205));
        turnBanner.setForeground(new Color(243, 204, 111));
        title.setOpaque(true);
        title.setBackground(new Color(6, 12, 18, 185));
        message.setOpaque(true);
        message.setBackground(new Color(6, 12, 18, 185));
        message.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        hand.setOpaque(false);
        hand.setLayout(null); // 프레임 슬롯과 버튼 위치를 같은 좌표로 맞춥니다.
        hand.setHoverHandlers(this::showCardInfo, cardInfo::hidePopup);
        history.setEditable(false);history.setFocusable(false);history.setLineWrap(true);history.setWrapStyleWord(true);
        history.setFont(Theme.font(Font.PLAIN,12));history.setForeground(Theme.TEXT);
        history.setBackground(new Color(9,15,23));
        history.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Theme.GOLD),"최근 행동",0,0,Theme.font(Font.BOLD,13),Theme.GOLD));

        battleMenu.setBackground(new Color(10,17,25));
        battleMenu.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(146,126,86)),BorderFactory.createEmptyBorder(6,6,6,6)));
        battleMenu.add(restartButton);battleMenu.add(backButton);battleMenu.setVisible(false);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"),"close-menu");
        getActionMap().put("close-menu",new AbstractAction(){@Override public void actionPerformed(java.awt.event.ActionEvent e){battleMenu.setVisible(false);}});
        add(new ArenaLayer(), BorderLayout.CENTER);
        countdownTimer.setCoalesce(true);
        phaseTimer.setRepeats(false);
        board.setTurnTimer(secondsLeft, TURN_SECONDS, false);
        refresh();
    }

    @Override public void addNotify() {
        super.addNotify();
        if (!turnSequenceStarted) {
            turnSequenceStarted = true;
            beginPlayerTurn();
        }
    }

    @Override public void removeNotify() {
        countdownTimer.stop();
        phaseTimer.stop();
        effects.cancel();
        super.removeNotify();
    }

    /** 전투를 멈추고 메인(시작) 화면으로 나갑니다. (전투 메뉴의 메인으로 가기, GAME OVER 의 메인으로) */
    private void leaveToMain(Runnable back) {
        battleMenu.setVisible(false);gameOver.setVisible(false);
        countdownTimer.stop();phaseTimer.stop();effects.cancel();
        back.run();
    }

    /** 같은 단계를 처음부터 다시 시작합니다. (전투 메뉴의 재시작, GAME OVER 의 재도전) */
    private void restartBattle() {
        battleMenu.setVisible(false);
        gameOver.setVisible(false);
        effects.cancel();recentActions.clear();history.setText("");
        battle.reset();
        board.resetEffects(); // 체력이 처음으로 돌아갈 때 회복 연출이 나오지 않게
        clash.show(null,null,"공격 → 방어 → 피해 판정");
        victoryReported = false;
        message.setText("새 전투를 준비합니다.");
        beginPlayerTurn();
    }

    private void beginPlayerTurn() {
        countdownTimer.stop();
        phaseTimer.stop();
        turnPhase = TurnPhase.PLAYER_INTRO;
        secondsLeft = TURN_SECONDS;
        board.setTurnTimer(secondsLeft, TURN_SECONDS, false);
        turnBanner.setText("MY TURN");
        turnBanner.setForeground(new Color(243, 204, 111));
        turnBanner.setVisible(true);
        message.setText("플레이어 턴 준비");
        refresh();
        phaseTimer.setInitialDelay(3000);
        phaseTimer.restart();
    }

    private void finishPhaseIntro() {
        if (turnPhase == TurnPhase.PLAYER_INTRO) {
            turnPhase = TurnPhase.PLAYER_ACTIVE;
            turnBanner.setVisible(false);
            board.setTurnTimer(secondsLeft, TURN_SECONDS, true);
            message.setText("사용할 카드를 클릭하세요.");
            countdownTimer.restart();
            refresh();
            return;
        }
        if (turnPhase == TurnPhase.ENEMY_INTRO) {
            turnBanner.setVisible(false);
            var attack=battle.revealEnemyAttack();
            if(attack==null) {
                recordAction("상대",battle.isOver()?"중독으로 전투 종료":"공격 카드 없음 · 턴 넘김");
                completeEnemyRound();return;
            }
            clash.show(attack,null,"상대 공격 공개 · 아직 피해 없음");
            recordAction("상대",attack.name()+" 공개");
            effects.showAction(attack,"상대 공격 공개","방어 선택 후 피해 적용",true,()->{
                turnPhase=TurnPhase.DEFENSE;secondsLeft=15;
                message.setText("방어 카드를 선택하거나 방어 안 함을 누르세요. (공격과 같거나 높은 등급만 가능)");
                board.setTurnTimer(secondsLeft,15,true);countdownTimer.restart();refresh();
            });
            refresh();
        }
    }

    private void tickCountdown() {
        if(turnPhase==TurnPhase.DEFENSE) {
            secondsLeft--;board.setTurnTimer(secondsLeft,15,true);
            if(secondsLeft<=0) respondToAttack(-1);
            return;
        }
        if (turnPhase != TurnPhase.PLAYER_ACTIVE) return;
        secondsLeft--;
        board.setTurnTimer(secondsLeft, TURN_SECONDS, true);
        if (secondsLeft <= 0) {
            message.setText("제한 시간이 끝났습니다.");
            finishPlayerTurn();
        }
    }

    private void finishPlayerTurn() {
        if (turnPhase != TurnPhase.PLAYER_ACTIVE || battle.isOver() || effects.isPlaying()) return;
        countdownTimer.stop();
        board.setTurnTimer(secondsLeft, TURN_SECONDS, false);
        turnPhase = TurnPhase.ENEMY_INTRO;
        battle.prepareEnemyTurn();
        turnBanner.setText("ENEMY TURN");
        turnBanner.setForeground(new Color(235, 126, 108));
        turnBanner.setVisible(true);
        message.setText("상대가 행동을 준비합니다.");
        refresh();
        phaseTimer.setInitialDelay(1800);
        phaseTimer.restart();
    }

    private void finishBattle(boolean victory) {
        countdownTimer.stop();
        phaseTimer.stop();
        turnPhase = TurnPhase.OVER;
        board.setTurnTimer(secondsLeft, TURN_SECONDS, false);
        turnBanner.setText(victory ? "VICTORY" : "DEFEAT");
        turnBanner.setForeground(victory ? new Color(120, 220, 145) : new Color(235, 126, 108));
        turnBanner.setVisible(true);
        if (!victory) {
            // 패배: GAME OVER 화면을 덮고 '재도전' 버튼에 포커스를 둡니다.
            battleMenu.setVisible(false);
            gameOver.setVisible(true);
            gameOver.focusRetry();
        }
        refresh();
    }

    /** 배경을 창 전체에 그리고 실제 카드를 이미지 속 오른쪽 아래 슬롯 위에 배치합니다. */
    private final class ArenaLayer extends JLayeredPane {
        private ArenaLayer() {
            setOpaque(true);
            setBackground(Theme.BACKGROUND);
            add(board, JLayeredPane.DEFAULT_LAYER);
            add(hand, JLayeredPane.PALETTE_LAYER);
            add(title, JLayeredPane.MODAL_LAYER);
            add(turnBanner, JLayeredPane.POPUP_LAYER);
            add(message, JLayeredPane.MODAL_LAYER);
            add(gearButton, JLayeredPane.MODAL_LAYER);
            add(battleMenu, JLayeredPane.POPUP_LAYER);
            add(endTurn, JLayeredPane.MODAL_LAYER);
            add(history,JLayeredPane.MODAL_LAYER);
            add(effects,JLayeredPane.DRAG_LAYER);
            add(cardInfo,Integer.valueOf(JLayeredPane.DRAG_LAYER+5));
            add(clash,JLayeredPane.PALETTE_LAYER);
            add(skipDefense,JLayeredPane.MODAL_LAYER);
            add(gameOver,Integer.valueOf(JLayeredPane.DRAG_LAYER+10));
        }

        @Override public void doLayout() {
            int w = getWidth();
            int h = getHeight();
            board.setBounds(0, 0, w, h);
            effects.setBounds(0,0,w,h);
            gameOver.setBounds(0,0,w,h);
            clash.setBounds((int)(w*.30),(int)(h*.34),(int)(w*.38),(int)(h*.32));
            skipDefense.setBounds(w-(int)(w*.175)-24,(int)(h*.49),(int)(w*.175),42);
            history.setBounds(18,(int)(h*.63),Math.min(275,w/4),(int)(h*.25));

            // 배경 원본의 오른쪽 아래 카드 패널 비율에 맞춘 좌표입니다.
            Rectangle tray=CardTrayPainter.bounds(w,h,false);
            hand.setBounds(tray);
            int cardCount=hand.getComponentCount();
            for(java.awt.Component c:hand.getComponents()) {
                if(!(c instanceof CardView card)) continue;
                // 5장 이하는 칸에 맞추고, 5장을 넘으면(최대 20장) 겹쳐서 모두 보이게 펼칩니다.
                Rectangle slot=CardTrayPainter.slot(tray.width,tray.height,card.handIndex(),cardCount);
                slot.grow(-3,-3);
                card.setFillSlot(true);
                card.setBounds(slot);
            }


            turnBanner.setBounds((w - 360) / 2, (h - 100) / 2, 360, 100);
            // 첨부 표시 기준: 오른쪽 상단 기둥의 2번 위치에 메뉴를 둡니다.
            int gearX=(int)(w*.95)-27;
            // 설정 버튼 높이: 0.16을 더 작은 값(예: 0.08)으로 바꾸면 위로 이동합니다.
            int gearY=(int)(h*.08)-27;
            gearButton.setBounds(gearX,gearY,54,54);
            // 단계·턴·뽑기·버림 정보: 상대 초상화 오른쪽, 톱니바퀴 버튼 왼쪽에 둡니다.
            int titleWidth=320;
            title.setBounds(Math.max((w+130)/2+20, gearX-titleWidth-14),gearY+10,titleWidth,34);
            battleMenu.setBounds(Math.min(gearX-120,w-210),gearY+60,196,108);
            message.setBounds(18, h - 44, Math.min(430, w / 3), 32);
            // 하스스톤처럼 오른쪽 중앙, 내 카드 보관함 위에 턴 종료 버튼을 배치합니다.
            int endTurnWidth=(int)(w*.175), endTurnHeight=46;
            endTurn.setBounds(w-endTurnWidth-24,(int)(h*.40),endTurnWidth,endTurnHeight);
            hand.doLayout();
        }
    }

    private void refresh() {
        // 카드 사용/턴 종료/재시작 후 최신 데이터를 화면에 다시 반영합니다.
        // 전투 수치는 DemoBattle에서 계산하고 이 메서드는 결과만 표시합니다.
        // 적 체력이 0이 된 경우에만 승리를 보고합니다. 돌아가기/패배는 클리어가 아닙니다.
        if (battle.isOver() && battle.playerWon() && !victoryReported && !effects.isPlaying()) {
            victoryReported = true;
            onVictory.run();
            message.setText(stage + "단계 클리어! 톱니바퀴 메뉴에서 메인으로 돌아가세요.");
            finishBattle(true);
            return;
        }
        // 내 공격의 반격 피해 등으로 체력이 0이 되면 패배 처리합니다.
        if (battle.playerHp() == 0 && turnPhase != TurnPhase.OVER && !effects.isPlaying()) {
            finishBattle(false);
            return;
        }
        title.setText(stage + "단계 · 턴 " + battle.turn()+" · 뽑기 "+battle.drawCount()+" · 버림 "+battle.discardCount());
        hand.showCards(battle.hand(), effects.isPlaying() || battle.isOver() || turnPhase != TurnPhase.PLAYER_ACTIVE, index -> {
            if(turnPhase==TurnPhase.DEFENSE) {respondToAttack(index);return;}
            if(effects.isPlaying() || turnPhase!=TurnPhase.PLAYER_ACTIVE || !battle.canPlay(index)) return;
            var card=battle.hand().get(index);
            if(card.type()==game.backend.model.CardType.ATTACK) {
                battle.play(index);countdownTimer.stop();turnPhase=TurnPhase.RESOLVING;
                board.setTurnTimer(secondsLeft,TURN_SECONDS,false);
                clash.show(card,null,"내 공격 공개 · 상대 방어 대기");recordAction("나",card.name()+" 공개");
                effects.showAction(card,"내 공격 공개","상대 방어 후 피해 적용",false,()->{
                    var defense=battle.chooseEnemyDefense();
                    clash.show(card,defense,defense==null?"상대 방어 없음":"상대 방어 공개");
                    Runnable resolve=()->{
                        String result=battle.resolveAttack(defense);clash.show(card,defense,result);recordAction("판정",result);
                        turnPhase=TurnPhase.PLAYER_ACTIVE;refresh();
                        if(!battle.isOver()) {countdownTimer.start();board.setTurnTimer(secondsLeft,TURN_SECONDS,true);}
                    };
                    if(defense==null) resolve.run();else {
                        recordAction("상대",defense.name()+" 방어");
                        effects.showAction(defense,"상대 방어",defense.description(),true,resolve);refresh();
                    }
                });
                refresh();return;
            }
            int hp=battle.playerHp(), enemyHp=battle.enemyHp(), block=battle.block();
            String result=battle.play(index);
            String detail="피해 "+(enemyHp-battle.enemyHp());
            if(battle.block()>block) detail+=" · 방어도 +"+(battle.block()-block);
            if(card.type()==game.backend.model.CardType.HEAL) detail+=" · 실제 회복 +"+(battle.playerHp()-hp);
            if(card.type()==game.backend.model.CardType.SPELL) detail=card.description();
            recordAction("나",card.name()+" · "+detail);message.setText(result);
            countdownTimer.stop();board.setTurnTimer(secondsLeft,TURN_SECONDS,false);
            effects.showAction(card,"내가 사용한 카드",detail,false,()->{
                refresh();
                if(!battle.isOver()) {countdownTimer.start();board.setTurnTimer(secondsLeft,TURN_SECONDS,true);}
            });
            refresh();
        });
        for(int i=0;i<hand.getComponentCount();i++) if(hand.cardAt(i)!=null) hand.cardAt(i).setEnabled(
            !effects.isPlaying() && (turnPhase==TurnPhase.PLAYER_ACTIVE || turnPhase==TurnPhase.DEFENSE) && battle.canPlay(i));
        skipDefense.setVisible(turnPhase==TurnPhase.DEFENSE);
        skipDefense.setEnabled(!effects.isPlaying());
        // 카드 크기는 ArenaLayer가 배경의 오른쪽 아래 슬롯 크기에 맞춰 자동 계산합니다.
        hand.revalidate();
        if(hand.getParent()!=null) {hand.getParent().doLayout();hand.getParent().repaint();}
        board.repaint();
        endTurn.setEnabled(!effects.isPlaying() && !battle.isOver() && turnPhase == TurnPhase.PLAYER_ACTIVE);
    }

    /** 손패 index 번째 카드 위에 설명 상자를 띄웁니다. 사용할 수 없으면 그 이유도 보여줍니다. */
    private void showCardInfo(CardView view, int index) {
        var cards=battle.hand();
        if(index<0 || index>=cards.size() || view.getParent()==null) return;
        var card=cards.get(index);
        String status=null;
        if(battle.isOver()) status="전투 종료";
        else if(effects.isPlaying()) status="연출 중";
        else if(turnPhase==TurnPhase.DEFENSE) status=card.type()!=game.backend.model.CardType.DEFENSE ? "지금은 방어 카드만 사용"
            : (battle.canPlay(index) ? null : "공격("+(battle.pendingAttack()==null?"?":battle.pendingAttack().cost())+"등급)보다 낮은 등급");
        else if(turnPhase!=TurnPhase.PLAYER_ACTIVE) status="내 턴이 아님";
        else if(card.type()==game.backend.model.CardType.DEFENSE) status="상대 공격 때 사용";
        else if(!battle.canPlay(index)) status=switch(card.type()) {
            case ATTACK -> "이번 턴에 공격 카드를 이미 사용";
            case HEAL -> "이번 턴에 회복 카드를 이미 사용";
            case SPELL -> "이번 턴에 주문 카드 2장을 이미 사용";
            default -> "지금은 사용 불가";
        };
        java.awt.Container layer=cardInfo.getParent();
        if(layer==null) return;
        Rectangle anchor=javax.swing.SwingUtilities.convertRectangle(view.getParent(),view.getBounds(),layer);
        cardInfo.showFor(card,status,anchor,layer.getSize());
    }

    private void recordAction(String actor,String text) {
        recentActions.addFirst(actor+" : "+text);
        while(recentActions.size()>4) recentActions.removeLast();
        history.setText(String.join("\n",recentActions));history.setCaretPosition(0);
    }
    private void respondToAttack(int index) {
        if(turnPhase!=TurnPhase.DEFENSE || effects.isPlaying()) return;
        var attack=battle.pendingAttack();
        var defense=index<0?null:battle.commitDefense(index);
        if(index>=0 && defense==null) return;
        countdownTimer.stop();turnPhase=TurnPhase.RESOLVING;board.setTurnTimer(secondsLeft,15,false);
        clash.show(attack,defense,defense==null?"방어 안 함":"내 방어 공개");
        Runnable resolve=()->{
            String result=battle.resolveAttack(defense);clash.show(attack,defense,result);recordAction("판정",result);
            completeEnemyRound();
        };
        if(defense==null) resolve.run();else {
            recordAction("나",defense.name()+" 방어");
            effects.showAction(defense,"내 방어 카드",defense.description(),false,resolve);refresh();
        }
    }
    private void completeEnemyRound() {
        if(battle.isOver()) finishBattle(battle.playerWon());
        else {battle.startNextRound();beginPlayerTurn();}
    }

    private static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.TEXT);
        label.setFont(Theme.font(Font.PLAIN, size));
        return label;
    }
    private static JButton button(String text, Runnable action) {
        JButton button = new BattleMenuButton(text,false,action);
        button.setPreferredSize(new Dimension(140, 36));
        return button;
    }
}
