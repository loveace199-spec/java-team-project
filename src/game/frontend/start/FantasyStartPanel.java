package game.frontend.start;

import game.frontend.common.StartComponents;
import game.frontend.common.Theme;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

/** 배경·로고·버튼은 독립적으로 교체할 수 있습니다. */
public final class FantasyStartPanel extends JPanel {
    public static final String GAME_TITLE = "게임 타이틀"; // 이름 확정 후 변경
    private final BufferedImage background;
    private final FantasyMenuButton[] buttons;
    // 이 클래스는 시작 화면을 그립니다. 전투 규칙은 처리하지 않습니다.
    // Runnable 인수는 버튼 클릭 시 실행할 작업이며 StartFrame에서 전달합니다.
    public FantasyStartPanel(Runnable start, Runnable help, Runnable settings, Runnable exit) {
        // null 레이아웃: doLayout()에서 각 버튼의 좌표와 크기를 직접 지정합니다.
        setLayout(null);
        // /assets는 윈도우 절대 경로가 아니라 실행 클래스 경로 안의 리소스 폴더입니다.
        // 원본 위치: src/assets/tower-menu-v1.png → Eclipse 빌드 후 bin/assets로 복사.
        try (var stream = getClass().getResourceAsStream("/assets/tower-menu-v1.png")) {
            if (stream == null) throw new IllegalStateException("탑 배경 리소스가 없습니다.");
            background = ImageIO.read(stream);
            if (background == null) throw new IllegalStateException("탑 배경을 읽을 수 없습니다.");
        } catch (IOException e) { throw new IllegalStateException("배경 로드 실패",e); }
        // [메뉴 문구 수정] 버튼의 표시 순서는 이 배열의 순서입니다.
        buttons = new FantasyMenuButton[]{new FantasyMenuButton("게임 시작",start),
            new FantasyMenuButton("게임 설명",help), new FantasyMenuButton("설정",settings),
            new FantasyMenuButton("게임 종료",exit)};
        for (int i=0;i<buttons.length;i++) {
            add(buttons[i]);
            final int index=i;
            // 위/아래 방향키로 이전/다음 버튼에 포커스(키보드 선택)를 옮깁니다.
            for (int delta : new int[]{-1,1}) {
                String key=delta<0 ? "UP" : "DOWN";
                buttons[i].getInputMap().put(KeyStroke.getKeyStroke(key),key);
                buttons[i].getActionMap().put(key,new AbstractAction() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        buttons[(index+delta+buttons.length)%buttons.length].requestFocusInWindow();
                    }
                });
            }
        }
    }
    public void focusStart() { buttons[0].requestFocusInWindow(); }
    @Override public void doLayout() {
        // [메뉴 위치 수정] setBounds(x, y, 가로, 세로). 좌표는 이 패널의 왼쪽 위가 (0,0).
        // getHeight() * .57 = 패널 높이의 57% 지점. 버튼 간격은 52픽셀입니다.
        int width=Math.min(350,getWidth()-80), y=Math.min((int)(getHeight()*.57),getHeight()-260);
        for(int i=0;i<buttons.length;i++) buttons[i].setBounds((getWidth()-width)/2,y+i*52,width,46);
    }
    @Override protected void paintComponent(Graphics graphics) {
        // Swing이 화면을 다시 그릴 때 호출합니다. 여기에서는 그림만 그리고 상태는 바꾸지 않습니다.
        super.paintComponent(graphics);
        Graphics2D g=StartComponents.smooth(graphics);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w=getWidth(), h=getHeight();
        // 배경 비율을 유지하며 창을 채웁니다. 창 비율이 다르면 가장자리가 일부 잘립니다.
        double scale=Math.max(w/(double)background.getWidth(),h/(double)background.getHeight());
        int iw=(int)Math.ceil(background.getWidth()*scale), ih=(int)Math.ceil(background.getHeight()*scale);
        g.drawImage(background,(w-iw)/2,(h-ih)/2,iw,ih,null);
        // 배경 아래쪽을 어둡게 덮어 버튼 글자가 읽히도록 합니다. Color의 4번째 값은 불투명도입니다.
        g.setPaint(new GradientPaint(0,h*.45f,new Color(0,0,0,0),0,h,new Color(3,8,13,180)));
        g.fillRect(0,0,w,h);
        // [로고 위치 수정] 탑 문양 → 제목 → 부제 순서로 그립니다.
        int center=w/2, logoY=(int)(h*.16);
        g.setColor(new Color(191,212,210));
        g.setStroke(new BasicStroke(2));
        g.drawPolygon(new int[]{center,center+18,center+12,center-12,center-18},
            new int[]{logoY-27,logoY-6,logoY+19,logoY+19,logoY-6},5);
        g.drawLine(center,logoY-15,center,logoY+11);
        g.drawLine(center-92,logoY,center-33,logoY);
        g.drawLine(center+33,logoY,center+92,logoY);
        g.setFont(new Font("바탕",Font.BOLD,Math.min(68,w/16)));
        int titleY=(int)(h*.32), titleX=(w-g.getFontMetrics().stringWidth(GAME_TITLE))/2;
        g.setColor(new Color(0,0,0,190)); g.drawString(GAME_TITLE,titleX+3,titleY+4);
        g.setColor(new Color(225,238,230)); g.drawString(GAME_TITLE,titleX,titleY);
        g.setFont(Theme.font(Font.PLAIN,13));
        centered(g,"이름 미정 · 시작 화면 시안",titleY+32,new Color(168,192,190));
        centered(g,"카드 한 장으로 시작되는 탑의 여정",titleY+65,new Color(202,211,206));
        g.setFont(Theme.font(Font.PLAIN,12));
        centered(g,"↑ ↓ 메뉴 선택     Enter 확인     마우스 클릭",h-20,new Color(163,178,179));
        g.dispose();
    }
    private void centered(Graphics2D g,String text,int y,Color color) {
        g.setColor(color); g.drawString(text,(getWidth()-g.getFontMetrics().stringWidth(text))/2,y);
    }
}
