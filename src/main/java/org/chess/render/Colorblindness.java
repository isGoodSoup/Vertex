package org.chess.render;

import org.chess.enums.ColorblindType;
import org.chess.service.BooleanService;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Colorblindness {

    public static Color apply(Color original, ColorblindType type) {
        int r = original.getRed();
        int g = original.getGreen();
        int b = original.getBlue();

        float newR = r, newG = g, newB = b;

        switch (type) {
            case PROTANOPIA -> {
                newR = 0.567f * r + 0.433f * g;
                newG = 0.558f * r + 0.442f * g;
                newB = b;
            }
            case DEUTERANOPIA -> {
                newR = 0.625f * r + 0.375f * g;
                newG = 0.7f * g + 0.3f * r;
                newB = b;
            }
            case TRITANOPIA -> {
                newR = r;
                newG = 0.95f * g + 0.05f * b;
                newB = 0.433f * g + 0.567f * b;
            }
        }
        newR = Math.min(255, Math.max(0, newR));
        newG = Math.min(255, Math.max(0, newG));
        newB = Math.min(255, Math.max(0, newB));
        return new Color((int)newR, (int)newG, (int)newB);
    }

    public static Color filter(Color c) {
        return BooleanService.canBeColorblind ? apply(c, MenuRender.getCb()) : c;
    }

    public static BufferedImage filter(BufferedImage img) {
        return BooleanService.canBeColorblind ? apply(img, MenuRender.getCb()) : img;
    }

    public static BufferedImage apply(BufferedImage img, ColorblindType type) {
        BufferedImage filtered = new BufferedImage(
                img.getWidth(),
                img.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                Color orig = new Color(img.getRGB(x, y), true);
                Color cb = apply(orig, type);

                Color finalColor = new Color(cb.getRed(), cb.getGreen(),
                        cb.getBlue(), orig.getAlpha());
                filtered.setRGB(x, y, finalColor.getRGB());
            }
        }
        return filtered;
    }
}
