package org.vertex.engine.gui;

import org.vertex.engine.enums.Theme;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.PieceService;

import java.awt.*;

public class Colors {
    private static final Color DARK_MODE_BACKGROUND = new Color(0, 0, 0);
    private static final Color DARK_MODE_FOREGROUND = Color.WHITE;
    private static final Color DARK_MODE_EDGE = Color.WHITE;
    private static final Color DARK_MODE_HIGHLIGHT = new Color(255, 255, 100);
    public static final Color SETTINGS = new Color(0 ,0 , 0, 180);

    private static Theme currentTheme = Theme.DEFAULT;

    public static void setTheme(Theme theme) {
        currentTheme = theme;
    }

    public static Theme getTheme() {
        return currentTheme;
    }

    public static void toggleDarkMode() {
        BooleanService.isDarkMode = !BooleanService.isDarkMode;
    }

    public static boolean isDarkMode() {
        return BooleanService.isDarkMode;
    }

    public static void nextTheme() {
        Theme[] themes = Theme.values();
        int nextIndex = (currentTheme.ordinal() + 1) % themes.length;
        setTheme(themes[nextIndex]);
        PieceService.clearCache();
    }

    public static void previousTheme() {
        Theme[] themes = Theme.values();
        int beforeIndex = (currentTheme.ordinal() - 1) % themes.length;
        setTheme(themes[beforeIndex]);
        PieceService.clearCache();
    }

    public static Color getBackground() {
        return BooleanService.isDarkMode ? DARK_MODE_BACKGROUND : currentTheme.getBackground();
    }

    public static Color getForeground() {
        return BooleanService.isDarkMode ? DARK_MODE_FOREGROUND : currentTheme.getForeground();
    }

    public static Color getEdge() {
        return BooleanService.isDarkMode ? DARK_MODE_EDGE : currentTheme.getEdge();
    }

    public static Color getHighlight() {
        return BooleanService.isDarkMode ? DARK_MODE_HIGHLIGHT : currentTheme.getHighlight();
    }
}