package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.*;
import game.backend.model.Card;

/** 공격을 왼쪽, 대응 방어를 오른쪽에 보관하는 중앙 판정 영역. */
public final class ClashPanel extends JPanel {
    private Card attack,defense;
    private String status="공격 → 방어 → 피해 판정";
    ClashPanel() {setOpaque(false);}
    void show(Card attack,Card defense,String status) {this.attack=attack;this.defense=defense;this.status=status;repaint();}
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int width=(getWidth()-30)/2,height=getHeight()-42;
        for(int i=0;i<2;i++) {
            int x=i*(width+30);g.setColor(new Color(8,14,22,230));g.fillRoundRect(x,0,width,height,10,10);
            g.setColor(i==0?new Color(200,115,95):new Color(115,170,210));g.drawRoundRect(x+1,1,width-2,height-2,10,10);
            g.setFont(Theme.font(Font.BOLD,16));g.drawString(i==0?"공격 카드":"방어 카드",x+12,23);
            Card card=i==0?attack:defense;
            if(card!=null) {Graphics2D art=(Graphics2D)g.create();art.translate(x+10,32);CardArtwork.paint(art,card,width-20,height-42);art.dispose();}
            else {g.setFont(Theme.font(Font.PLAIN,13));g.drawString("선택 대기",x+12,height/2);}
        }
        g.setColor(Theme.TEXT);g.setFont(Theme.font(Font.PLAIN,12));g.drawString(status,4,getHeight()-12);g.dispose();
    }
}
