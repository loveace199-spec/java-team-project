package game.frontend.battle;

import game.frontend.common.Theme;

import java.awt.*;
import javax.swing.*;
import game.backend.model.Card;

/** 전투 결과를 1.6초 동안 보여주는 투명 레이어. Swing 타이머로 repaint만 요청합니다. */
public final class BattleEffectOverlay extends JComponent {
    private Card card;
    private String owner, result;
    private boolean enemy;
    private long started;
    private Runnable completion;
    private final Timer animation=new Timer(16,e->tick());
    public BattleEffectOverlay() { setOpaque(false);setVisible(false); }
    public boolean isPlaying() { return animation.isRunning(); }
    public void showAction(Card card,String owner,String result,boolean enemy,Runnable completion) {
        cancel();this.card=card;this.owner=owner;this.result=result;this.enemy=enemy;
        this.completion=completion;started=System.nanoTime();setVisible(true);animation.start();repaint();
    }
    public void cancel() { animation.stop();completion=null;setVisible(false); }
    private void tick() {
        if((System.nanoTime()-started)/1_000_000>=1600) {
            Runnable done=completion;cancel();if(done!=null) done.run();
        } else repaint();
    }
    @Override public void removeNotify() { cancel();super.removeNotify(); }
    @Override protected void paintComponent(Graphics graphics) {
        if(card==null) return;
        Graphics2D g=(Graphics2D)graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        double t=Math.min(1,(System.nanoTime()-started)/1_600_000_000.0);
        float opacity=(float)Math.min(1,Math.min(t*8,(1-t)*6));
        g.setComposite(AlphaComposite.SrcOver.derive(Math.max(0,opacity)));
        Color color=enemy?new Color(255,110,95):switch(card.type()) {
            case ATTACK -> new Color(255,100,90);
            case DEFENSE -> new Color(90,190,255);
            case HEAL -> new Color(255,220,105);
            case SPELL -> new Color(115,240,120);
        };
        int cx=getWidth()/2, cy=getHeight()/2;
        // 처음 0.35초는 손패 위치에서 중앙으로 이동합니다. 상대 카드도 앞면으로 공개됩니다.
        if(t<.22) {
            double progress=t/.22;
            progress=1-Math.pow(1-progress,3);
            int sx=(int)(getWidth()*(enemy?.14:.81)),sy=(int)(getHeight()*(enemy?.13:.83));
            int cardW=(int)(70+80*progress),cardH=(int)(90+90*progress);
            int x=(int)(sx+(cx-sx)*progress)-cardW/2,y=(int)(sy+(cy-sy)*progress)-cardH/2;
            g.translate(x,y);CardArtwork.paint(g,card,cardW,cardH);g.dispose();return;
        }
        // 대상 쪽에 퍼지는 원과 광선. 공격은 위로, 상대 공격은 아래로 향합니다.
        int targetY=(int)(getHeight()*(enemy?.83:.15));
        g.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),100));
        g.setStroke(new BasicStroke(5));
        int radius=35+(int)(t*90);
        g.drawOval(cx-radius,targetY-radius,radius*2,radius*2);
        g.drawLine(cx,cy,cx,targetY);
        int boxW=Math.min(580,getWidth()-40), boxH=245;
        g.setColor(new Color(5,10,18,235));g.fillRoundRect(cx-boxW/2,cy-boxH/2,boxW,boxH,24,24);
        g.setColor(color);g.setStroke(new BasicStroke(2));g.drawRoundRect(cx-boxW/2,cy-boxH/2,boxW,boxH,24,24);
        int x=cx-boxW/2+20, y=cy-96;
        Graphics2D art=(Graphics2D)g.create();art.translate(x,y);
        if(!CardArtwork.paint(art,card,150,180)) {
            art.setColor(color);art.setFont(Theme.font(Font.BOLD,58));art.drawString(enemy?"⚔":"✦",40,110);
        }
        art.dispose();
        x+=175;
        g.setColor(color);g.setFont(Theme.font(Font.BOLD,18));g.drawString(owner,x,cy-66);
        g.setColor(Theme.TEXT);g.setFont(Theme.font(Font.BOLD,25));g.drawString(card.name(),x,cy-27);
        g.setFont(Theme.font(Font.PLAIN,16));
        String[] lines=result.split(" · ");
        for(int i=0;i<lines.length;i++) g.drawString(lines[i],x,cy+10+i*26);
        g.dispose();
    }
}
