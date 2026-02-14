package org.vertex.engine.gui;

import org.vertex.engine.service.BooleanService;

import java.awt.*;
import java.util.List;

public class Colors {
    public final static Color LOGO_BACKGROUND = new Color(111, 175, 77);
    public final static Color LOGO_FOREGROUND = new Color(71, 135, 37);
    public final static Color LOGO_EDGE = new Color(51, 115, 17);
    public final static Color LOGO_HIGHLIGHT = new Color(191, 255, 157);
    public final static Color DEFAULT_BACKGROUND = new Color(210, 165, 125);
    public final static Color DEFAULT_FOREGROUND = new Color(175, 115, 70);
    public final static Color DEFAULT_EDGE = new Color(120, 70, 40);
    public final static Color DEFAULT_HIGHLIGHT = new Color(255, 225, 0);
    public static final Color DARK_MODE = new Color(20, 20, 40);
    public static final Color BLACK_BACKGROUND = new Color(40, 40, 60);
    public static final Color WHITE_FOREGROUND = new Color(255, 255, 255);
    public static final Color BLACK_EDGE = new Color(20, 20, 40);
    public static final Color YELLOW_HIGHLIGHT = new Color(255, 255, 100);
    public static final Color OCEAN_BACKGROUND = new Color(100, 140, 200);
    public static final Color OCEAN_FOREGROUND = new Color(100, 90, 200);
    public static final Color OCEAN_EDGE = new Color(70, 60, 170);
    public static final Color OCEAN_HIGHLIGHT = new Color(180, 170, 255);
    public static final Color FOREST_BACKGROUND = new Color(100, 180, 100);
    public static final Color FOREST_FOREGROUND = new Color(30, 120, 60);
    public static final Color FOREST_EDGE = new Color(10, 100, 40);
    public static final Color FOREST_HIGHLIGHT = new Color(150, 240, 180);
    public static final Color FAIRY_BACKGROUND = new Color(180, 140, 200);
    public static final Color FAIRY_FOREGROUND = new Color(180, 70, 200);
    public static final Color FAIRY_EDGE = new Color(140, 40, 170);
    public static final Color FAIRY_HIGHLIGHT = new Color(200, 90, 220);

    public static Color BACKGROUND = DEFAULT_BACKGROUND;
    public static Color FOREGROUND = DEFAULT_FOREGROUND;
    public static Color EDGE = DEFAULT_EDGE;
    public static Color HIGHLIGHT = DEFAULT_HIGHLIGHT;

    public static final List<Color[]> THEMES_LIST = List.of(
            new Color[]{DEFAULT_BACKGROUND, DEFAULT_FOREGROUND, DEFAULT_EDGE, DEFAULT_HIGHLIGHT},
            new Color[]{BLACK_BACKGROUND, WHITE_FOREGROUND, BLACK_EDGE, YELLOW_HIGHLIGHT},
            new Color[]{OCEAN_BACKGROUND, OCEAN_FOREGROUND, OCEAN_EDGE, OCEAN_HIGHLIGHT},
            new Color[]{FOREST_BACKGROUND, FOREST_FOREGROUND, FOREST_EDGE, FOREST_HIGHLIGHT},
            new Color[]{LOGO_BACKGROUND, LOGO_FOREGROUND, LOGO_EDGE, LOGO_HIGHLIGHT},
            new Color[]{FAIRY_BACKGROUND, FAIRY_FOREGROUND, FAIRY_EDGE, FAIRY_HIGHLIGHT}
    );
    private static int themeIndex = 0;

    public static Color getHighlight() {
        return HIGHLIGHT;
    }

    public static void nextTheme() {
        themeIndex = (themeIndex + 1) % THEMES_LIST.size();
        Color[] theme = THEMES_LIST.get(themeIndex);
        BACKGROUND = theme[0];
        FOREGROUND = theme[1];
        EDGE = theme[2];
        HIGHLIGHT = theme[3];
    }

    public static void toggleDarkTheme() {
        if (BooleanService.isDarkMode) {
            BACKGROUND = DARK_MODE;
            FOREGROUND = WHITE_FOREGROUND;
            EDGE = DARK_MODE;
            HIGHLIGHT = YELLOW_HIGHLIGHT;
        } else {
            setDefaultTheme();
        }
    }

    public static void setDefaultTheme() {
        BACKGROUND = DEFAULT_BACKGROUND;
        FOREGROUND = DEFAULT_FOREGROUND;
        EDGE = DEFAULT_EDGE;
        HIGHLIGHT = DEFAULT_HIGHLIGHT;
    }

    public static Color getBACKGROUND() {
        return BACKGROUND;
    }

    public static Color getFOREGROUND() {
        return FOREGROUND;
    }

    public static Color getEDGE() {
        return EDGE;
    }

    public static Color getHIGHLIGHT() {
        return HIGHLIGHT;
    }
}
