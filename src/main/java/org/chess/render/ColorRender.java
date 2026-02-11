package org.chess.render;

import org.chess.service.BooleanService;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorRender {
    public static Color getColor(Color c, boolean canExclude) {
        Color cf = c;
        if(BooleanService.isDarkMode) {
            if(canExclude) {
                return cf;
            }
            cf = Night.filter(cf);
        }
        if(BooleanService.canBeColorblind) {
            cf = Colorblindness.filter(cf);
        }
        return cf;
    }

    public static BufferedImage getSprite(BufferedImage img,
                                          boolean canExclude) {
        BufferedImage imgf = img;
        if(BooleanService.isDarkMode) {
            if(canExclude) {
                return imgf;
            }
            imgf = Night.filter(img);
        }
        if(BooleanService.canBeColorblind) {
            imgf = Colorblindness.filter(img);
        }
        return imgf;
    }
}
