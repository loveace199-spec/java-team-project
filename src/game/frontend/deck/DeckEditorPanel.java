package game.frontend.deck;

import game.backend.model.Card;
import game.backend.model.PlayerDeck;
import game.database.CardCatalog;
import game.frontend.battle.CardView;
import game.frontend.common.Theme;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicScrollBarUI;

/** 스테이지에서 여는 덱 편집 화면. 초안과 저장된 덱을 분리하여 취소를 지원합니다. */
public final class DeckEditorPanel extends JPanel {

    private final PlayerDeck deck;
    private final List<Card> draft = new ArrayList<>();
    private final JLabel count = new JLabel();
    private final JButton save = new JButton("덱 저장 후 돌아가기");
    private final JPanel gallery = new JPanel(new GridLayout(0, 5, 12, 12));
    private final DefaultListModel<String> entries = new DefaultListModel<>();

    // 게임 UI 색상
    private static final Color BUTTON_BG = new Color(32, 45, 65);
    private static final Color BUTTON_HOVER = new Color(48, 66, 92);
    private static final Color BUTTON_DISABLED = new Color(45, 49, 57);
    private static final Color BORDER = new Color(113, 132, 158);
    private static final Color GOLD = new Color(235, 183, 73);
    private static final Color TEXT = new Color(238, 238, 238);
    private static final Color SCROLL_TRACK = new Color(20, 29, 43);
    private static final Color SCROLL_THUMB = new Color(113, 132, 158);

    public DeckEditorPanel(PlayerDeck deck, Runnable back) {
        super(new BorderLayout(16, 16));
        this.deck = deck;

        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 상단 제목
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("나의 덱 · 20장 / 동일 카드 최대 2장");
        title.setFont(Theme.font(Font.BOLD, 23));
        title.setForeground(Theme.TEXT);
        header.add(title, BorderLayout.WEST);

        JButton cancel = createMenuButton("← 스테이지로 돌아가기");
        cancel.setPreferredSize(new Dimension(180, 38));
        cancel.addActionListener(e -> back.run());
        header.add(cancel, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // 카드 목록
        gallery.setBackground(Theme.BACKGROUND);

        JScrollPane scroll = new JScrollPane(gallery);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 75, 95)));
        scroll.getViewport().setBackground(Theme.BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(28);
        styleScrollPane(scroll);

        add(scroll, BorderLayout.CENTER);

        // 오른쪽 내 덱 영역
        JPanel side = new JPanel(new BorderLayout(8, 12));
        side.setPreferredSize(new Dimension(265, 0));
        side.setBackground(Theme.PANEL);

        count.setForeground(Theme.GOLD);
        count.setFont(Theme.font(Font.BOLD, 22));
        count.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        side.add(count, BorderLayout.NORTH);

        JList<String> list = new JList<>(entries);
        list.setFont(Theme.font(Font.PLAIN, 14));
        list.setBackground(Theme.PANEL);
        list.setForeground(Theme.TEXT);
        list.setSelectionBackground(new Color(55, 72, 95));
        list.setSelectionForeground(Color.WHITE);
        list.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JScrollPane listScroll = new JScrollPane(list);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(75, 91, 112)));
        styleScrollPane(listScroll);
        side.add(listScroll, BorderLayout.CENTER);

        // 오른쪽 설명 + 버튼
        JPanel actions = new JPanel(new GridLayout(0, 1, 8, 8));
        actions.setOpaque(false);

        JLabel note = new JLabel("<html>정확히 20장을 구성해 주세요.<br>처음에는 각 카드가 1장씩 들어 있습니다.<br>저장은 현재 실행 중에만 유지됩니다.<br>중독은 상대 행동 전 발동하며 중첩됩니다.</html>");
        note.setForeground(Theme.TEXT);
        note.setBorder(BorderFactory.createEmptyBorder(5, 3, 8, 3));
        actions.add(note);

        JButton defaults = createMenuButton("기본 덱으로 초기화");
        JButton clear = createMenuButton("모든 카드 빼기");
        styleSaveButton(save);

        defaults.addActionListener(e -> {
            draft.clear();
            draft.addAll(CardCatalog.allCards());
            refresh();
        });

        clear.addActionListener(e -> {
            draft.clear();
            refresh();
        });

        save.addActionListener(e -> {
            deck.save(draft);
            back.run();
        });

        actions.add(defaults);
        actions.add(clear);
        actions.add(save);

        side.add(actions, BorderLayout.SOUTH);
        add(side, BorderLayout.EAST);

        beginEditing();
    }

    public void beginEditing() {
        draft.clear();
        draft.addAll(deck.cards());
        refresh();
    }

    private void refresh() {
        count.setText("내 덱  " + draft.size() + " / 20장");
        save.setEnabled(PlayerDeck.valid(draft));
        entries.clear();
        gallery.removeAll();

        for (Card card : CardCatalog.allCards()) {
            long copies = draft.stream().filter(c -> c.id().equals(card.id())).count();
            if (copies > 0) entries.addElement(card.name() + "  × " + copies);

            JPanel tile = new JPanel(new BorderLayout(2, 7));
            tile.setBackground(Theme.PANEL);
            tile.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(45, 59, 78), 1), BorderFactory.createEmptyBorder(5, 5, 6, 5)));

            CardView art = new CardView(card, () -> JOptionPane.showMessageDialog(this, card.name() + " / 비용 " + card.cost() + "\n" + card.description(), "카드 상세", JOptionPane.INFORMATION_MESSAGE));
            art.setPreferredSize(new Dimension(128, 146));
            tile.add(art, BorderLayout.CENTER);

            // 카드 수량 조절
            JPanel controls = new JPanel(new BorderLayout(6, 0));
            controls.setOpaque(false);

            JButton minus = createRoundControlButton("−");
            JButton plus = createRoundControlButton("+");

            minus.setEnabled(copies > 0);
            plus.setEnabled(copies < 2 && draft.size() < 20);

            minus.addActionListener(e -> {
                draft.remove(card);
                refresh();
            });

            plus.addActionListener(e -> {
                draft.add(card);
                refresh();
            });

            JLabel amount = new JLabel(copies + " / 2", SwingConstants.CENTER);
            amount.setFont(Theme.font(Font.BOLD, 14));
            amount.setForeground(copies == 2 ? GOLD : TEXT);
            amount.setOpaque(true);
            amount.setBackground(new Color(20, 29, 43));
            amount.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(72, 88, 108)), BorderFactory.createEmptyBorder(6, 5, 6, 5)));

            controls.add(minus, BorderLayout.WEST);
            controls.add(amount, BorderLayout.CENTER);
            controls.add(plus, BorderLayout.EAST);

            tile.add(controls, BorderLayout.SOUTH);
            gallery.add(tile);
        }

        gallery.revalidate();
        gallery.repaint();
    }

    /** 카드 아래 + / - 버튼 */
    private JButton createRoundControlButton(String text) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (!isEnabled()) g.setColor(BUTTON_DISABLED);
                else if (getModel().isPressed()) g.setColor(new Color(65, 79, 100));
                else if (getModel().isRollover()) g.setColor(BUTTON_HOVER);
                else g.setColor(BUTTON_BG);

                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                g.setColor(isEnabled() && getModel().isRollover() ? GOLD : BORDER);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                g.dispose();

                super.paintComponent(graphics);
            }
        };

        button.setPreferredSize(new Dimension(38, 32));
        button.setFont(Theme.font(Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    /** 오른쪽 메뉴 버튼 */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) g.setColor(new Color(57, 73, 96));
                else if (getModel().isRollover()) g.setColor(BUTTON_HOVER);
                else g.setColor(BUTTON_BG);

                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                g.setColor(getModel().isRollover() ? GOLD : BORDER);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 12, 12);
                g.dispose();

                super.paintComponent(graphics);
            }
        };

        button.setFont(Theme.font(Font.BOLD, 14));
        button.setForeground(TEXT);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        return button;
    }

    /** 저장 버튼 강조 */
    private void styleSaveButton(JButton button) {
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setFont(Theme.font(Font.BOLD, 15));
        button.setForeground(new Color(30, 25, 15));
        button.setBackground(GOLD);
        button.setFocusPainted(false);

        Border outside = BorderFactory.createLineBorder(new Color(255, 218, 120), 2);
        Border inside = BorderFactory.createEmptyBorder(10, 12, 10, 12);
        button.setBorder(BorderFactory.createCompoundBorder(outside, inside));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /** 세로/가로 스크롤바 디자인 */
    private void styleScrollPane(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUI(createScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(createScrollBarUI());

        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        scroll.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));

        scroll.getVerticalScrollBar().setBackground(SCROLL_TRACK);
        scroll.getHorizontalScrollBar().setBackground(SCROLL_TRACK);
    }

    /** 게임 스타일 스크롤바 */
    private BasicScrollBarUI createScrollBarUI() {
        return new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                trackColor = SCROLL_TRACK;
                thumbColor = SCROLL_THUMB;
            }

            @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(SCROLL_TRACK);
                g.fillRect(r.x, r.y, r.width, r.height);
            }

            @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                if (r.isEmpty() || !scrollbar.isEnabled()) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? GOLD : SCROLL_THUMB);
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
                g2.dispose();
            }

            @Override protected JButton createDecreaseButton(int orientation) {
                return invisibleButton();
            }

            @Override protected JButton createIncreaseButton(int orientation) {
                return invisibleButton();
            }

            private JButton invisibleButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        };
    }
}