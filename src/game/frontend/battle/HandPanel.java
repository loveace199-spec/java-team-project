package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.FlowLayout;
import java.util.List;
import java.util.function.IntConsumer;
import javax.swing.JPanel;
import game.backend.model.Card;

/** 카드 목록과 클릭 연결을 담당합니다. */
public final class HandPanel extends JPanel {
    public HandPanel() {
        super(new FlowLayout(FlowLayout.CENTER, 12, 12));
        setBackground(Theme.BACKGROUND);
    }

    public void showCards(List<Card> cards, int energy, boolean finished, IntConsumer onUse) {
        // 이전 카드 컴포넌트를 지우고 현재 손패 목록으로 새 카드 버튼을 만듭니다.
        // 여기서 removeAll은 화면 컴포넌트 제거이며 게임 데이터 삭제가 아닙니다.
        removeAll();
        for (int i = 0; i < cards.size(); i++) {
            // 클릭할 때 사용할 손패 위치를 보관합니다. 같은 종류 카드도 위치로 구분됩니다.
            final int index = i;
            CardView view = new CardView(cards.get(i), () -> onUse.accept(index));
            view.setEnabled(!finished && cards.get(i).cost() <= energy);
            add(view);
        }
        revalidate();
        repaint();
    }
}
