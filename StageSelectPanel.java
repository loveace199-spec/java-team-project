package prototype.ui.start;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import prototype.ui.Theme;
import prototype.model.StageProgress;
import java.util.function.IntConsumer;

/** 5단계 경로 표시와 전투 시안 열기. 실제 단계 입장·상점·해금 규칙은 없습니다. */
public final class StageSelectPanel extends JPanel {
    private final BufferedImage background;
    private final FantasyMenuButton back;
    private final StageProgress progress;
    private final StageNodeButton[] nodes = new StageNodeButton[StageProgress.COUNT];

    public StageSelectPanel(Runnable onBack) {
        this(onBack, new StageProgress(), stage -> { });
    }

    public StageSelectPanel(Runnable onBack, StageProgress progress, IntConsumer onEnter) {
        this.progress = progress;
        setLayout(null);
        try (var stream = getClass().getResourceAsStream("/assets/tower-menu-v1.png")) {
            if (stream == null) throw new IllegalStateException("탑 배경 리소스가 없습니다.");
            background = ImageIO.read(stream);
            if (background == null) throw new IllegalStateException("배경 이미지를 읽을 수 없습니다.");
        } catch (IOException e) { throw new IllegalStateException("배경 로드 실패", e); }
        back = new FantasyMenuButton("← 시작 화면", onBack);
        add(back);
        for (int i=0;i<nodes.length;i++) {
            final int stage=i+1;
            nodes[i]=new StageNodeButton(stage,progress,()->onEnter.accept(stage));
            add(nodes[i]);
        }
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "back");
        getActionMap().put("back", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) { onBack.run(); }
        });
        getAccessibleContext().setAccessibleName("스테이지 안내: 1단계, 2단계, 3단계, 4단계, 5단계");
    }

    @Override public void doLayout() {
        back.setBounds(30, 24, 180, 46);
        for(int i=0;i<nodes.length;i++) nodes[i].setBounds(
            (int)(getWidth()*(.14+i*.18))-70, (int)(getHeight()*(.64-i*.065))-57,140,155);
    }

    public void refreshProgress() {
        for(StageNodeButton node:nodes) node.refreshState();
        repaint();
    }

    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = StartComponents.smooth(graphics);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth(), h = getHeight();
        double scale = Math.max(w / (double) background.getWidth(), h / (double) background.getHeight());
        int iw = (int) Math.ceil(background.getWidth() * scale);
        int ih = (int) Math.ceil(background.getHeight() * scale);
        g.drawImage(background, (w - iw) / 2, (h - ih) / 2, iw, ih, null);
        g.setColor(new Color(7, 15, 22, 135));
        g.fillRect(0, 0, w, h);
        g.setFont(Theme.font(Font.BOLD, 34));
        centered(g, "탑의 여정", w / 2, 114, Theme.TEXT);
        g.setFont(Theme.font(Font.PLAIN, 15));
        centered(g, "다섯 단계로 이어지는 길", w / 2, 145, Theme.MUTED);

        // [단계 위치 수정] 각 원의 중심 좌표를 저장합니다.
        // x는 오른쪽으로 증가, y는 위쪽으로 감소하여 오르막 형태가 됩니다.
        int[] xs = new int[5], ys = new int[5];
        for (int i = 0; i < 5; i++) {
            xs[i] = (int) (w * (.14 + i * .18));
            ys[i] = (int) (h * (.64 - i * .065));
        }
        // 먼저 점선 길을 그리고 그 위에 단계 원을 그려 선이 원 뒤로 숨게 합니다.
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1, new float[]{7, 8}, 0));
        g.setColor(new Color(176, 193, 188, 180));
        for (int i = 0; i < 4; i++) g.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        // 단계 원과 잠금 그림은 각각 StageNodeButton에서 그립니다.
        g.setFont(Theme.font(Font.PLAIN, 13));
        centered(g, progress.clearedCount()==5 ? "모든 단계를 클리어했습니다!" :
            "금색: 입장 가능  ·  초록색: 클리어  ·  사슬 X: 잠김", w / 2, h - 54, Theme.MUTED);
        centered(g, "Esc  시작 화면으로 돌아가기", w / 2, h - 29, Theme.MUTED);
        g.dispose();
    }

    private void centered(Graphics2D g, String text, int x, int y, Color color) {
        g.setColor(color);
        g.drawString(text, x - g.getFontMetrics().stringWidth(text) / 2, y);
    }
}
