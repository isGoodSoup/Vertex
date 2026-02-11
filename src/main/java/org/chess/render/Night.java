package org.chess.render;

import java.awt.*;
import java.awt.image.BufferedImage;
import org.chess.service.BooleanService;

public class Night {

    public static Color apply(Color original) {
        int r = original.getRed();
        int g = original.getGreen();
        int b = original.getBlue();

        float factor = 0.3f;
        int newR = (int)(r * factor);
        int newG = (int)(g * factor);
        int newB = (int)(b * factor);

        return new Color(newR, newG, newB, original.getAlpha());
    }

    public static Color filter(Color c) {
        return BooleanService.isDarkMode ? apply(c) : c;
    }

    public static BufferedImage filter(BufferedImage img) {
        if(!BooleanService.isDarkMode) return img;

        BufferedImage filtered = new BufferedImage(
                img.getWidth(),
                img.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        for(int y = 0; y < img.getHeight(); y++) {
            for(int x = 0; x < img.getWidth(); x++) {
                Color orig = new Color(img.getRGB(x, y), true);
                Color dm = apply(orig);
                filtered.setRGB(x, y, dm.getRGB());
            }
        }
        return filtered;
    }
}