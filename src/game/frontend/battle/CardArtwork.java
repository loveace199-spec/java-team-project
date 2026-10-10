package game.frontend.battle;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import game.database.CardCatalog;
import game.backend.model.Card;

/** 원본 20장 시트를 보존하고, 그릴 때만 각 카드 영역을 잘라 사용합니다. */
public final class CardArtwork {
    private static final BufferedImage SHEET=load();
    private static BufferedImage load() {
        try(var input=CardArtwork.class.getResourceAsStream("/assets/crafted-cards-v1.png")) {
            if(input!=null) return ImageIO.read(input);
            var file=new File("src/assets/crafted-cards-v1.png");
            return file.isFile()?ImageIO.read(file):null;
        } catch(IOException ex) { return null; }
    }
    static boolean paint(Graphics2D g, Card card, int w, int h) {
        return paint(g,card,w,h,false);
    }
    static boolean paint(Graphics2D g, Card card, int w, int h, boolean fillSlot) {
        int index=CardCatalog.artworkIndex(card);
        if(SHEET==null || index<0) return false;
        int x1=(index%5)*SHEET.getWidth()/5, x2=(index%5+1)*SHEET.getWidth()/5;
        int y1=(index/5)*SHEET.getHeight()/4, y2=(index/5+1)*SHEET.getHeight()/4;
        double scale=Math.min(w/(double)(x2-x1),h/(double)(y2-y1));
        // 전투 손패는 카드 이름/설명을 자르지 않고 슬롯의 가로·세로에 맞춰 채웁니다.
        int dw=fillSlot?w:(int)((x2-x1)*scale), dh=fillSlot?h:(int)((y2-y1)*scale);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(SHEET,(w-dw)/2,(h-dh)/2,(w+dw)/2,(h+dh)/2,x1,y1,x2,y2,null);
        return true;
    }
}
