package game.frontend.deck;

import game.backend.model.Card;
import game.backend.model.PlayerDeck;
import game.frontend.battle.CardView;
import game.frontend.common.Theme;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import game.database.CardCatalog;

/** 스테이지에서 여는 덱 편집 화면. 초안과 저장된 덱을 분리하여 취소를 지원합니다. */
public final class DeckEditorPanel extends JPanel {
    private final PlayerDeck deck;
    private final List<Card> draft=new ArrayList<>();
    private final JLabel count=new JLabel();
    private final JButton save=new JButton("덱 저장 후 돌아가기");
    private final JPanel gallery=new JPanel(new GridLayout(0,5,12,12));
    private final DefaultListModel<String> entries=new DefaultListModel<>();
    public DeckEditorPanel(PlayerDeck deck,Runnable back) {
        super(new BorderLayout(16,16)); this.deck=deck;
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20,24,20,24));
        JPanel header=new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title=new JLabel("나의 덱 · 20장 / 동일 카드 최대 2장");
        title.setFont(Theme.font(Font.BOLD,23)); title.setForeground(Theme.TEXT); header.add(title,BorderLayout.WEST);
        JButton cancel=new JButton("취소 / 스테이지"); cancel.addActionListener(e->back.run());
        header.add(cancel,BorderLayout.EAST); add(header,BorderLayout.NORTH);
        gallery.setBackground(Theme.BACKGROUND);
        JScrollPane scroll=new JScrollPane(gallery);
        scroll.getVerticalScrollBar().setUnitIncrement(28); add(scroll,BorderLayout.CENTER);
        JPanel side=new JPanel(new BorderLayout(8,12)); side.setPreferredSize(new Dimension(265,0));
        side.setBackground(Theme.PANEL);count.setForeground(Theme.GOLD);
        count.setFont(Theme.font(Font.BOLD,22)); side.add(count,BorderLayout.NORTH);
        JList<String> list=new JList<>(entries); list.setFont(Theme.font(Font.PLAIN,14));
        list.setBackground(Theme.PANEL);list.setForeground(Theme.TEXT);
        side.add(new JScrollPane(list),BorderLayout.CENTER);
        JPanel actions=new JPanel(new GridLayout(0,1,8,8));
        actions.setOpaque(false);
        JLabel note=new JLabel("<html>정확히 20장을 구성해 주세요.<br>처음에는 각 카드가 1장씩 들어 있습니다.<br>저장은 현재 실행 중에만 유지됩니다.<br>중독은 상대 행동 전 발동하며 중첩됩니다.</html>");
        note.setForeground(Theme.TEXT);actions.add(note);
        JButton defaults=new JButton("기본 덱: 각 1장"); defaults.addActionListener(e->{draft.clear();draft.addAll(CardCatalog.allCards());refresh();});
        JButton clear=new JButton("모두 빼기"); clear.addActionListener(e->{draft.clear();refresh();});
        actions.add(defaults); actions.add(clear); actions.add(save);
        save.addActionListener(e->{deck.save(draft);back.run();});
        side.add(actions,BorderLayout.SOUTH); add(side,BorderLayout.EAST);
        beginEditing();
    }
    public void beginEditing() { draft.clear();draft.addAll(deck.cards());refresh(); }
    private void refresh() {
        count.setText("내 덱  "+draft.size()+" / 20장");
        save.setEnabled(PlayerDeck.valid(draft)); entries.clear(); gallery.removeAll();
        for(Card card:CardCatalog.allCards()) {
            long copies=draft.stream().filter(c->c.id().equals(card.id())).count();
            if(copies>0) entries.addElement(card.name()+"  × "+copies);
            JPanel tile=new JPanel(new BorderLayout(2,4)); tile.setBackground(Theme.PANEL);
            CardView art=new CardView(card,()->JOptionPane.showMessageDialog(this,
                card.name()+" / "+card.cost()+"등급"+"\n"+card.description(),"카드 상세",JOptionPane.INFORMATION_MESSAGE));
            art.setPreferredSize(new Dimension(128,146)); tile.add(art,BorderLayout.CENTER);
            JPanel controls=new JPanel(new BorderLayout());
            JButton minus=new JButton("−"), plus=new JButton("+");
            minus.setEnabled(copies>0); plus.setEnabled(copies<2 && draft.size()<20);
            minus.addActionListener(e->{draft.remove(card);refresh();});
            plus.addActionListener(e->{draft.add(card);refresh();});
            controls.add(minus,BorderLayout.WEST); controls.add(new JLabel(copies+"장",SwingConstants.CENTER)); controls.add(plus,BorderLayout.EAST);
            tile.add(controls,BorderLayout.SOUTH); gallery.add(tile);
        }
        gallery.revalidate();gallery.repaint();
    }
}
