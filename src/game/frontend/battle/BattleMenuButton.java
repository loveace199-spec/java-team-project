package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.JButton;

/** 전장과 어울리는 어두운 금속 버튼. 톱니바퀴도 이미지 없이 직접 그립니다. */
public final class BattleMenuButton extends JButton {
    private final boolean gear;
    BattleMenuButton(String text,boolean gear,Runnable action) {
        super(text);this.gear=gear;
        setContentAreaFilled(false);setBorderPainted(false);setFocusPainted(false);
        setFont(Theme.font(Font.BOLD,15));setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setToolTipText(gear?"전투 메뉴: 재시작 / 메인으로 가기":text);
        getAccessibleContext().setAccessibleName(gear?"전투 메뉴":text);
        addActionListener(e->action.run());
    }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        int w=getWidth(),h=getHeight();boolean lit=isEnabled()&&(getModel().isRollover()||isFocusOwner());
        Color gold=isEnabled()?new Color(182,157,108):new Color(103,100,90);
        g.setPaint(new GradientPaint(0,0,lit?new Color(69,75,78):new Color(42,49,56),0,h,new Color(9,15,22)));
        Polygon frame=new Polygon(new int[]{9,w-10,w-2,w-2,w-10,9,2,2},new int[]{2,2,10,h-11,h-3,h-3,h-11,10},8);
        g.fillPolygon(frame);g.setColor(gold);g.setStroke(new BasicStroke(2));g.drawPolygon(frame);
        g.setColor(new Color(89,92,91));g.drawRoundRect(6,6,w-13,h-13,5,5);
        if(getModel().isPressed()) g.translate(0,1);
        if(gear) {
            int cx=w/2,cy=h/2;double radius=Math.min(w,h)*.29;
            Polygon teeth=new Polygon();
            for(int i=0;i<48;i++) {double a=i*Math.PI/24;double r=radius*((i%6==0||i%6==5)?.76:1);teeth.addPoint(cx+(int)(Math.cos(a)*r),cy+(int)(Math.sin(a)*r));}
            g.setColor(gold);g.fillPolygon(teeth);g.setColor(new Color(17,26,37));g.fillOval(cx-7,cy-7,14,14);
            g.setColor(new Color(116,153,192));g.drawOval(cx-7,cy-7,14,14);
        } else {
            g.setFont(getFont());g.setColor(isEnabled()?new Color(231,217,180):new Color(135,133,123));
            FontMetrics metrics=g.getFontMetrics();g.drawString(getText(),(w-metrics.stringWidth(getText()))/2,(h+metrics.getAscent()-metrics.getDescent())/2);
            g.setColor(gold);g.fillOval(11,h/2-2,4,4);g.fillOval(w-15,h/2-2,4,4);
        }
        g.dispose();
    }
}
