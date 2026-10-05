package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.JButton;
import game.backend.model.Card;

/** 카드 한 장의 외형만 담당합니다. 효과 계산은 하지 않습니다. */
public final class CardView extends JButton {
    // JButton을 상속하므로 카드 전체가 클릭 가능한 버튼입니다.
    // 이 클래스는 외형만 담당하며, 카드 효과는 onUse로 외부에 전달합니다.
    private final Card card;
    private boolean fillSlot;
    public void setFillSlot(boolean fillSlot) { this.fillSlot=fillSlot; }

    public CardView(Card card, Runnable onUse) {
        this.card = card;
        setPreferredSize(new Dimension(180, 246));
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(card.name() + " · " + card.cost() + "등급 · " + card.description());
        getAccessibleContext().setAccessibleName(card.name() + ", " + card.description());
        addActionListener(event -> onUse.run());
    }

    @Override protected void paintComponent(Graphics graphics) {
        // [카드 디자인 수정] 아래 코드는 카드 테두리 → 등급/이름 → 아이콘 → 설명 순서입니다.
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        if(!isEnabled()) g.setComposite(AlphaComposite.SrcOver.derive(0.42f));
        if(CardArtwork.paint(g,card,getWidth(),getHeight(),fillSlot)) {
            if(isFocusOwner() || getModel().isRollover()) {
                g.setColor(Theme.GOLD); g.drawRect(1,1,getWidth()-3,getHeight()-3);
            }
            g.dispose(); return;
        }
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // 기준 도화지는 180×246입니다. 실제 카드 크기가 달라도 비례해서 그립니다.
        g.scale(getWidth() / 180.0, getHeight() / 246.0);
        if (!isEnabled()) g.setComposite(AlphaComposite.SrcOver.derive(0.42f));
        Color accent = Theme.accent(card.type());
        g.setColor(new Color(236, 225, 202));
        g.fillRoundRect(4, 4, 172, 238, 20, 20);
        g.setColor(isFocusOwner() || getModel().isRollover() ? Theme.GOLD : accent);
        g.setStroke(new BasicStroke(3));
        g.drawRoundRect(4, 4, 172, 238, 20, 20);
        g.setColor(accent);
        g.fillOval(13, 14, 32, 32);
        g.setColor(Color.WHITE);
        g.setFont(Theme.font(Font.BOLD, 18));
        g.drawString(Integer.toString(card.cost()), 23, 37);
        g.setColor(new Color(45, 42, 43));
        g.setFont(Theme.font(Font.BOLD, 18));
        g.drawString(card.name(), 52, 37);
        g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 30));
        g.fillRoundRect(17, 57, 146, 108, 12, 12);
        paintSymbol(g, accent);
        g.setFont(Theme.font(Font.BOLD, 14));
        g.setColor(accent);
        centered(g, card.type().label(), 187);
        g.setColor(new Color(45, 42, 43));
        g.setFont(Theme.font(Font.PLAIN, 12));
        // 긴 설명은 카드 안에서 두 줄로 표시합니다.
        String text = card.description();
        if (g.getFontMetrics().stringWidth(text) > 154) {
            int split = text.lastIndexOf(' ', text.length() / 2 + 3);
            if (split < 1) split = text.length() / 2;
            centered(g, text.substring(0, split), 210);
            centered(g, text.substring(split).trim(), 228);
        } else centered(g, text, 216);
        g.dispose();
    }

    private void centered(Graphics2D g, String text, int y) {
        g.drawString(text, (180 - g.getFontMetrics().stringWidth(text)) / 2, y);
    }

    private void paintSymbol(Graphics2D g, Color color) {
        // [카드 그림 수정] 종류별 임시 아이콘입니다. 추후 이미지 그리기로 교체 가능합니다.
        g.setColor(color);
        g.setStroke(new BasicStroke(7, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        switch (card.type()) {
            case ATTACK -> {
                g.drawLine(69, 137, 115, 79);
                g.drawLine(64, 114, 91, 136);
                g.fillPolygon(new int[]{106, 121, 118}, new int[]{84, 73, 95}, 3);
            }
            case DEFENSE -> {
                g.fillPolygon(new int[]{90, 119, 114, 90, 66, 61},
                    new int[]{76, 89, 121, 146, 121, 89}, 6);
                g.setColor(new Color(236, 225, 202));
                g.drawLine(90, 89, 90, 129);
            }
            case HEAL -> {
                g.fillRoundRect(80, 77, 20, 68, 5, 5);
                g.fillRoundRect(56, 101, 68, 20, 5, 5);
            }
            case SPELL -> {
                g.fillPolygon(new int[]{96, 66, 88, 79, 119, 95},
                    new int[]{75, 115, 115, 149, 102, 102}, 6);
            }
        }
    }
}
