package game.frontend.stage;

import game.frontend.common.StartComponents;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import javax.swing.*;
import game.database.StageEnemy;
import game.backend.model.StageProgress;
import game.frontend.common.Theme;

/** 단계별 상태를 색상과 글자로 함께 표시하는 실제 입장 버튼입니다. */
public final class StageNodeButton extends JButton {
    private final int stage;
    private final StageProgress progress;
    private final BufferedImage monster;
    private final String stageName;
    public StageNodeButton(int stage, StageProgress progress, Runnable enter) {
        this.stage = stage;
        this.progress = progress;
        StageEnemy enemy = stage == 3 ? null : StageEnemy.forStage(stage);
        stageName = enemy == null ? "상점" : enemy.name();
        monster = enemy == null ? null : loadPortrait(enemy.imagePath());
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        addActionListener(e -> { if (progress.canEnter(stage)) enter.run(); });
        refreshState();
    }
    public void refreshState() {
        setEnabled(progress.canEnter(stage));
        String state = progress.isCleared(stage) ? "클리어 · 다시 도전" : isEnabled() ? "입장 가능" : "잠김 · 이전 단계 클리어 필요";
        setToolTipText(stage + "단계 · " + stageName + ": " + state);
        getAccessibleContext().setAccessibleName(getToolTipText());
        setCursor(Cursor.getPredefinedCursor(isEnabled() ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        repaint();
    }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = StartComponents.smooth(graphics);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int x = getWidth()/2, y = 57;
        boolean cleared = progress.isCleared(stage), open = progress.canEnter(stage);
        Color accent = cleared ? new Color(102,220,147) : open ? Theme.GOLD : new Color(121,134,144);
        if (open) {
            for (int r=55;r>=45;r-=2) {
                g.setColor(new Color(accent.getRed(),accent.getGreen(),accent.getBlue(),22));
                g.fillOval(x-r,y-r,r*2,r*2);
            }
        }
        g.setColor(cleared ? new Color(27,65,49) : open ? new Color(63,52,30) : new Color(22,29,36));
        g.fillOval(x-43,y-43,86,86);
        // 별도 그래픽에 원형 클립을 적용해 이미지가 원 밖으로 그려지지 않게 합니다.
        Graphics2D portrait = (Graphics2D) g.create();
        portrait.clip(new Ellipse2D.Double(x-43, y-43, 86, 86));
        // 몬스터는 잠긴 단계에서도 보이며, X 표시로 입장 가능 여부를 구분합니다.
        if (monster != null) {
            portrait.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            double scale = Math.min(88.0 / monster.getWidth(), 88.0 / monster.getHeight());
            int width = (int) (monster.getWidth() * scale);
            int height = (int) (monster.getHeight() * scale);
            portrait.drawImage(monster, x - width / 2, y - height / 2, width, height, null);
        } else {
            paintShop(portrait, x, y);
        }
        portrait.dispose();
        if (!open) {
            g.setColor(new Color(7, 15, 22, 85));
            g.fillOval(x-43,y-43,86,86);
            // 사슬 없이 두 선으로만 잠금 X를 표시합니다.
            g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(246, 137, 128));
            g.drawLine(x-17,y-17,x+17,y+17);
            g.drawLine(x+17,y-17,x-17,y+17);
        }
        // 이미지와 잠금 표시를 그린 뒤 테두리를 마지막에 그립니다.
        g.setColor(accent);
        g.setStroke(new BasicStroke(hasFocus() || getModel().isRollover() ? 3 : 2));
        g.drawOval(x-43,y-43,86,86);
        g.setFont(Theme.font(Font.BOLD,14));
        centered(g,stage+"단계 · "+stageName,x,124,accent);
        g.setFont(Theme.font(Font.PLAIN,12));
        centered(g,cleared ? "✓ 클리어" : open ? "입장 가능" : "잠김",x,145,accent);
        g.dispose();
    }

    private static BufferedImage loadPortrait(String resource) {
        // Eclipse의 리소스 복사가 늦어져도 프로젝트 src/assets에서 불러옵니다.
        try (var stream = StageNodeButton.class.getResourceAsStream(resource)) {
            BufferedImage image;
            if (stream != null) {
                image = ImageIO.read(stream);
            } else {
                Path source = Path.of("src", resource.substring(1));
                if (!Files.isRegularFile(source)) {
                    Path output = Path.of(StageNodeButton.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI());
                    source = output.getParent().resolve("src").resolve(resource.substring(1));
                }
                image = ImageIO.read(source.toFile());
            }
            if (image == null) throw new IllegalStateException("이미지 형식을 읽을 수 없습니다: " + resource);
            return image;
        } catch (Exception e) {
            throw new IllegalStateException("스테이지 몬스터 이미지 로드 실패: " + resource, e);
        }
    }

    private void paintShop(Graphics2D g, int x, int y) {
        // 별도 이미지 없이 그리는 작은 상점: 지붕, 차양, 문, 진열창.
        g.setColor(new Color(213, 178, 114));
        g.fillRoundRect(x-29,y-9,58,42,5,5);
        g.setColor(new Color(151, 65, 51));
        g.fillPolygon(new int[]{x-36,x-26,x+26,x+36},
            new int[]{y-9,y-29,y-29,y-9},4);
        for (int i=0;i<6;i++) {
            g.setColor(i%2==0 ? Theme.GOLD : new Color(151,65,51));
            g.fillRoundRect(x-36+i*12,y-9,12,14,4,4);
        }
        g.setColor(new Color(36, 41, 44));
        g.fillRect(x-21,y+10,18,23);
        g.fillRect(x+6,y+10,17,13);
    }
    private void centered(Graphics2D g,String text,int x,int y,Color color) {
        g.setColor(color); g.drawString(text,x-g.getFontMetrics().stringWidth(text)/2,y);
    }
}
