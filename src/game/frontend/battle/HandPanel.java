package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.FlowLayout;
import java.util.function.BiConsumer;
import javax.swing.ToolTipManager;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.JPanel;
import game.backend.model.Card;

/** 카드 목록과 클릭 연결을 담당합니다. */
public final class HandPanel extends JPanel {
    // 카드 위에 마우스를 올리고 내릴 때 알려 줄 곳 (전투 화면의 설명 상자)
    private BiConsumer<CardView, Integer> onHover;
    private Runnable onHoverEnd;

    /** 마우스 올림 설명을 켭니다. 켜면 작은 기본 말풍선 대신 이 콜백으로 설명을 띄웁니다. */
    public void setHoverHandlers(BiConsumer<CardView, Integer> onHover, Runnable onHoverEnd) {
        this.onHover = onHover;
        this.onHoverEnd = onHoverEnd;
    }

    public HandPanel() {
        super(new FlowLayout(FlowLayout.CENTER, 12, 12));
        setBackground(Theme.BACKGROUND);
    }

    public void showCards(List<Card> cards, int energy, boolean finished, IntConsumer onUse) {
        // 이전 카드 컴포넌트를 지우고 현재 손패 목록으로 새 카드 버튼을 만듭니다.
        // 여기서 removeAll은 화면 컴포넌트 제거이며 게임 데이터 삭제가 아닙니다.
        if (onHoverEnd != null) onHoverEnd.run(); // 카드를 새로 만들면 이전 설명은 닫습니다.
        removeAll();
        for (int i = 0; i < cards.size(); i++) {
            // 클릭할 때 사용할 손패 위치를 보관합니다. 같은 종류 카드도 위치로 구분됩니다.
            final int index = i;
            CardView view = new CardView(cards.get(i), () -> onUse.accept(index));
            view.setEnabled(!finished && cards.get(i).cost() <= energy);
            if (onHover != null) {
                ToolTipManager.sharedInstance().unregisterComponent(view); // 기본 말풍선 대신 큰 설명 상자
                view.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override public void mouseEntered(java.awt.event.MouseEvent e) { onHover.accept(view, index); }
                    @Override public void mouseExited(java.awt.event.MouseEvent e) { onHoverEnd.run(); }
                });
            }
            add(view);
        }
        revalidate();
        repaint();
    }
}
