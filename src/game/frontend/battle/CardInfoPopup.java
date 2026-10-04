package game.frontend.battle;

import game.backend.model.Card;
import game.backend.model.CardType;
import game.frontend.common.Theme;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 * 손패 카드 위에 마우스를 올렸을 때 카드 위쪽에 뜨는 설명 상자.
 * 이름 · 종류 · 비용 · 효과 · 사용 규칙 · (사용 못 하는 이유)를 보여줍니다.
 */
public final class CardInfoPopup extends JComponent {
    private static final int WIDTH = 280;
    private Card card;
    private String status; // 예: "에너지 부족" (사용 가능하면 null)

    public CardInfoPopup() {
        setOpaque(false);
        setVisible(false);
    }

    /** card 설명을 anchor(카드 위치) 위쪽에 띄웁니다. area 는 화면 전체 크기입니다. */
    public void showFor(Card card, String status, Rectangle anchor, Dimension area) {
        this.card = card;
        this.status = status;
        int height = preferredHeight();
        int x = anchor.x + anchor.width / 2 - WIDTH / 2;
        int y = anchor.y - height - 10;
        if (y < 8) y = anchor.y + anchor.height + 10;          // 위에 공간이 없으면 아래로
        x = Math.max(8, Math.min(x, area.width - WIDTH - 8));    // 화면 밖으로 나가지 않게
        y = Math.max(8, Math.min(y, area.height - height - 8));
        setBounds(x, y, WIDTH, height);
        setVisible(true);
        repaint();
    }

    public void hidePopup() {
        setVisible(false);
        card = null;
    }

    public Card card() { return card; }

    /** 종류별 사용 규칙 (게임 설명의 '카드 사용 규칙'과 같은 내용). */
    static String rule(CardType type) {
        return switch (type) {
            case ATTACK -> "내 턴에 1장 · 상대가 방어 카드로 막을 수 있음";
            case HEAL -> "내 턴에 1장 사용 가능";
            case DEFENSE -> "상대가 공격할 때만 사용 가능";
            case SPELL -> "공격·회복 제한과 관계없이 사용 가능";
        };
    }

    private List<String> wrap(String text, FontMetrics fm, int width) {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String next = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(next) > width && line.length() > 0) { lines.add(line.toString()); line = new StringBuilder(word); }
            else line = new StringBuilder(next);
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    private int preferredHeight() {
        FontMetrics fm = getFontMetrics(Theme.font(Font.PLAIN, 14));
        int lines = card == null ? 1 : wrap(card.description(), fm, WIDTH - 32).size();
        return 104 + lines * 20 + (status != null ? 24 : 0);
    }

    @Override protected void paintComponent(Graphics graphics) {
        if (card == null) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        Color accent = Theme.accent(card.type());
        // 바탕과 금색 테두리
        g.setColor(new Color(8, 12, 18, 238));
        g.fillRoundRect(0, 0, w - 1, h - 1, 16, 16);
        g.setColor(new Color(176, 141, 78));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(1, 1, w - 3, h - 3, 16, 16);
        g.setColor(accent);
        g.fillRoundRect(1, 1, 8, h - 3, 6, 6);

        // 비용 원
        g.setColor(accent);
        g.fillOval(w - 48, 12, 34, 34);
        g.setColor(Color.WHITE);
        g.setFont(Theme.font(Font.BOLD, 18));
        String cost = Integer.toString(card.cost());
        g.drawString(cost, w - 31 - g.getFontMetrics().stringWidth(cost) / 2, 35);

        // 이름 · 종류
        g.setFont(Theme.font(Font.BOLD, 18));
        g.setColor(Theme.GOLD);
        g.drawString(card.name(), 20, 32);
        g.setFont(Theme.font(Font.BOLD, 13));
        g.setColor(accent.brighter());
        g.drawString(card.type().label() + " 카드 · 에너지 " + card.cost(), 20, 52);

        // 효과 설명
        g.setFont(Theme.font(Font.PLAIN, 14));
        g.setColor(Theme.TEXT);
        int y = 78;
        for (String line : wrap(card.description(), g.getFontMetrics(), w - 32)) { g.drawString(line, 20, y); y += 20; }

        // 사용 규칙
        g.setColor(new Color(176, 141, 78, 120));
        g.drawLine(20, y - 6, w - 20, y - 6);
        g.setFont(Theme.font(Font.PLAIN, 12));
        g.setColor(Theme.MUTED);
        g.drawString(rule(card.type()), 20, y + 12);
        if (status != null) {
            g.setFont(Theme.font(Font.BOLD, 13));
            g.setColor(new Color(235, 126, 108));
            g.drawString("지금은 사용 불가 · " + status, 20, y + 34);
        }
        g.dispose();
    }
}
