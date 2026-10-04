package game.frontend.stage;

import game.backend.model.RunUpgrades;
import game.backend.model.ShopItem;
import game.frontend.common.ImageHotspotButton;
import game.frontend.common.Images;
import game.frontend.common.Theme;
import game.frontend.start.FantasyMenuButton;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

/**
 * 3단계 상점. 그림(src/assets/shop-v1.png) 속 카드 3장이 각각 버튼입니다.
 * 한 번의 도전에서 하나만 고를 수 있고, 고른 효과는 이후 전투에 계속 적용됩니다.
 */
public final class ShopPanel extends JPanel {
    public static final String IMAGE = "/assets/shop-v1.png";
    // 원본 그림(1869×842)에서 카드 3장이 그려진 영역
    private static final Rectangle[] CARD_AREAS = {
        new Rectangle(822, 112, 290, 513), new Rectangle(1137, 112, 290, 513), new Rectangle(1453, 112, 290, 513)};
    private static final ShopItem[] ITEMS = {ShopItem.ATTACK_UP, ShopItem.DEFENSE_UP, ShopItem.HEAL_10};

    private final BufferedImage image = Images.load(IMAGE, "상점 화면");
    private final RunUpgrades upgrades;
    private final ImageHotspotButton[] cards = new ImageHotspotButton[ITEMS.length];
    private final FantasyMenuButton back;
    private final FantasyMenuButton next;
    private ShopItem purchased;
    private String status = "카드 한 장을 골라 강화하세요. (한 번만 선택 가능)";

    public ShopPanel(RunUpgrades upgrades, Runnable onBack, Runnable onContinue) {
        this.upgrades = upgrades;
        setLayout(null);
        setBackground(Color.BLACK);
        for (int i = 0; i < ITEMS.length; i++) {
            ShopItem item = ITEMS[i];
            cards[i] = new ImageHotspotButton(item.label() + " · " + item.description(), CARD_AREAS[i], () -> buy(item));
            add(cards[i]);
        }
        back = new FantasyMenuButton("← 스테이지", onBack);
        next = new FantasyMenuButton("4단계로 계속 →", onContinue);
        add(back);
        add(next);
        refresh();
    }

    /** 카드 버튼을 눌렀을 때: 효과 적용 → 나머지 카드 잠금. */
    void buy(ShopItem item) {
        if (purchased != null) return;
        purchased = item;
        upgrades.apply(item);
        status = item.label() + " 획득! " + item.description();
        refresh();
    }

    public ShopItem purchased() { return purchased; }

    private void refresh() {
        for (int i = 0; i < ITEMS.length; i++) {
            boolean chosen = ITEMS[i] == purchased;
            cards[i].setEnabled(purchased == null);
            cards[i].setBadge(chosen ? "구매 완료" : null);
        }
        repaint();
    }

    @Override public void doLayout() {
        Rectangle d = drawnArea();
        for (ImageHotspotButton card : cards) card.place(d, image.getWidth(), image.getHeight());
        int w = getWidth(), h = getHeight();
        back.setBounds(24, h - 64, 200, 46);
        next.setBounds(w - 244, h - 64, 220, 46);
    }

    /** 위쪽 제목과 아래쪽 버튼 줄을 뺀 공간에 그림을 비율 유지로 맞춥니다. */
    private Rectangle drawnArea() {
        int top = 64, bottom = 120;
        Rectangle r = Images.fit(image, getWidth(), Math.max(1, getHeight() - top - bottom));
        r.y += top;
        return r;
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Rectangle d = drawnArea();
        g.drawImage(image, d.x, d.y, d.width, d.height, null);
        g.setFont(Theme.font(Font.BOLD, 28));
        center(g, "3단계 · 상점", 44, Theme.GOLD);
        g.setFont(Theme.font(Font.PLAIN, 15));
        center(g, status, getHeight() - 92, purchased == null ? Theme.TEXT : new Color(120, 220, 145));
        g.dispose();
    }

    private void center(Graphics2D g, String text, int y, Color color) {
        g.setColor(color);
        g.drawString(text, (getWidth() - g.getFontMetrics().stringWidth(text)) / 2, y);
    }
}
