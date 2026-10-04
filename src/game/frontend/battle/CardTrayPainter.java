package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.*;

/** 카드 보관함의 금속 프레임과 슬롯. 배경 그림과 버튼이 같은 좌표를 사용합니다. */
public final class CardTrayPainter {
    private static final Color GOLD=new Color(168,145,101), LIGHT=new Color(222,204,155);
    static Rectangle bounds(int w,int h,boolean enemy) {
        return enemy?new Rectangle((int)(w*.008),4,(int)(w*.278),(int)(h*.235))
            :new Rectangle((int)(w*.63),(int)(h*.69),(int)(w*.365),(int)(h*.255));
    }
    static Rectangle slot(int w,int h,int index) {
        int margin=22,gap=5, width=(w-2*margin-gap*4)/5;
        return new Rectangle(margin+index*(width+gap),40,width,h-65);
    }
    static void paint(Graphics2D original,Rectangle area,String title,int count,int draw,int discard,boolean enemy) {
        Graphics2D g=(Graphics2D)original.create();g.translate(area.x,area.y);
        int w=area.width,h=area.height;
        // 불투명 바탕으로 배경에 그려진 고정 카드를 가립니다.
        g.setColor(new Color(8,12,17));g.fillRect(0,0,w,h);
        g.setPaint(new GradientPaint(0,0,new Color(47,50,52),0,h,new Color(14,18,24)));
        g.fillRoundRect(3,8,w-6,h-11,10,10);
        g.setColor(new Color(4,8,13));g.fillRoundRect(12,16,w-24,h-27,8,8);
        // 어두운 강철 테두리에 가느다란 고금색 이중선을 겹칩니다.
        g.setColor(new Color(80,78,70));g.setStroke(new BasicStroke(5));g.drawRoundRect(7,11,w-14,h-18,9,9);
        g.setStroke(new BasicStroke(1));g.setColor(GOLD);g.drawRoundRect(5,9,w-10,h-14,10,10);
        g.setColor(new Color(110,105,86));g.drawRoundRect(12,16,w-24,h-28,6,6);
        g.setPaint(new GradientPaint(0,18,new Color(24,36,48),0,34,new Color(5,11,17)));
        g.fillRect(17,17,w-34,18);g.setColor(GOLD);g.drawLine(17,35,w-17,35);
        g.setFont(Theme.font(Font.BOLD,Math.max(12,Math.min(15,w/25))));g.setColor(Theme.TEXT);
        g.drawString(title,22,30);
        int badgeX=w-64;
        g.setColor(new Color(24,35,54));g.fillRoundRect(badgeX,17,40,17,5,5);
        g.setColor(new Color(112,133,165));g.drawRoundRect(badgeX,17,40,17,5,5);
        g.setColor(new Color(203,221,245));g.drawString(count+"장",badgeX+8,30);
        for(int i=0;i<5;i++) {
            Rectangle s=slot(w,h,i);
            g.setPaint(new GradientPaint(s.x,s.y,new Color(22,25,27),s.x,s.y+s.height,new Color(5,8,12)));
            g.fillRoundRect(s.x,s.y,s.width,s.height,8,8);
            g.setColor(new Color(105,96,74));g.drawRoundRect(s.x,s.y,s.width,s.height,8,8);
            g.setColor(LIGHT);g.drawLine(s.x+3,s.y+1,s.x+12,s.y+1);g.drawLine(s.x+1,s.y+3,s.x+1,s.y+11);
            g.drawLine(s.x+s.width-12,s.y+s.height-1,s.x+s.width-3,s.y+s.height-1);
            if(enemy && i<count) paintBack(g,s);
        }
        // 네 모서리의 금속 장식과 중앙의 푸른 보석입니다.
        ornament(g,10,15,8);ornament(g,w-10,15,8);ornament(g,10,h-13,8);ornament(g,w-10,h-13,8);
        ornament(g,w/2,13,12);
        g.setFont(Theme.font(Font.PLAIN,11));g.setColor(new Color(166,165,154));
        String text="뽑기 "+draw+"  ·  버림 "+discard;
        g.drawString(text,(w-g.getFontMetrics().stringWidth(text))/2,h-10);
        g.dispose();
    }
    private static void ornament(Graphics2D g,int x,int y,int size) {
        g.setColor(new Color(24,31,39));
        Polygon star=new Polygon(new int[]{x,x+3,x+size,x+3,x,x-3,x-size,x-3},
            new int[]{y-size,y-3,y,y+3,y+size,y+3,y,y-3},8);
        g.fillPolygon(star);g.setColor(GOLD);g.drawPolygon(star);
        g.setColor(new Color(86,132,188));g.fillPolygon(new int[]{x,x+3,x,x-3},new int[]{y-5,y,y+5,y},4);
        g.setColor(LIGHT);g.drawLine(x-1,y-4,x-1,y);
    }
    private static void paintBack(Graphics2D g,Rectangle s) {
        int x=s.x+3,y=s.y+3,w=s.width-6,h=s.height-6,cx=x+w/2,cy=y+h/2;
        g.setPaint(new GradientPaint(x,y,new Color(40,27,34),x+w,y+h,new Color(10,14,23)));
        g.fillRoundRect(x,y,w,h,6,6);g.setColor(GOLD);g.drawRoundRect(x,y,w,h,6,6);
        g.setColor(new Color(144,69,83));
        for(int i=0;i<4;i++) {double a=i*Math.PI/4;int dx=(int)(Math.cos(a)*w*.32),dy=(int)(Math.sin(a)*h*.32);g.drawLine(cx-dx,cy-dy,cx+dx,cy+dy);}
        g.drawOval(cx-w/5,cy-w/5,w*2/5,w*2/5);
    }
}
