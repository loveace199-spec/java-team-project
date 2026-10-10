package game.frontend.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * src/assets 의 이미지를 불러옵니다.
 * Eclipse 가 새 이미지를 bin 폴더에 아직 복사하지 않았어도 src/assets 에서 한 번 더 찾습니다.
 */
public final class Images {
    private Images() { }

    public static BufferedImage load(String resourceName, String displayName) {
        InputStream stream = Images.class.getResourceAsStream(resourceName);
        Path sourcePath = Path.of("src", resourceName.substring(1));
        try {
            if (stream == null && !Files.isRegularFile(sourcePath)) {
                Path output = Path.of(Images.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                Path root = Files.isDirectory(output) ? output.getParent() : output.getParent().getParent();
                if (root != null) sourcePath = root.resolve("src").resolve(resourceName.substring(1));
            }
        } catch (Exception ignored) {
            // 아래에서 파일 없음 메시지로 안내합니다.
        }
        try (InputStream in = stream != null ? stream
                : (Files.isRegularFile(sourcePath) ? Files.newInputStream(sourcePath) : null)) {
            if (in == null) throw new IllegalStateException(displayName + " 파일이 없습니다: " + sourcePath.toAbsolutePath());
            BufferedImage image = ImageIO.read(in);
            if (image == null) throw new IllegalStateException(displayName + " 파일을 읽을 수 없습니다.");
            return image;
        } catch (IOException e) {
            throw new IllegalStateException(displayName + " 로드 실패", e);
        }
    }

    /** 그림 비율을 유지하며 area 안에 가장 크게 들어가는 사각형 (남는 곳은 여백). */
    public static java.awt.Rectangle fit(BufferedImage image, int areaW, int areaH) {
        double scale = Math.min(areaW / (double) image.getWidth(), areaH / (double) image.getHeight());
        int w = (int) Math.round(image.getWidth() * scale), h = (int) Math.round(image.getHeight() * scale);
        return new java.awt.Rectangle((areaW - w) / 2, (areaH - h) / 2, w, h);
    }
}
