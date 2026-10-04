package prototype.ui;

import java.awt.Color;
import java.awt.Font;
import prototype.model.CardType;

/** 화면 전체 색상과 글꼴을 수정하는 곳입니다. */
public final class Theme {
    public static final Color BACKGROUND = new Color(19, 27, 43);
    public static final Color PANEL = new Color(30, 42, 61);
    public static final Color TEXT = new Color(239, 231, 211);
    public static final Color MUTED = new Color(164, 178, 194);
    public static final Color GOLD = new Color(232, 183, 90);
    private Theme() { }

    public static Font font(int style, int size) {
        return new Font("맑은 고딕", style, size);
    }

    public static Color accent(CardType type) {
        return switch (type) {
            case ATTACK -> new Color(195, 83, 66);
            case DEFENSE -> new Color(62, 118, 178);
            case HEAL -> new Color(57, 145, 106);
            case SPELL -> new Color(131, 93, 175);
        };
    }
}
