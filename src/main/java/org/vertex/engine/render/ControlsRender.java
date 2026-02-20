package org.vertex.engine.render;

import org.vertex.engine.enums.ControlCategory;
import org.vertex.engine.enums.ControlsHUD;
import org.vertex.engine.enums.Games;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.ServiceFactory;
import org.vertex.engine.service.UIService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ControlsRender {
    private ServiceFactory service;

    private static final int LINE_HEIGHT = 15;
    private static final int PADDING = 10;

    public ControlsRender() {}

    public ServiceFactory getService() {
        return service;
    }

    public void setService(ServiceFactory service) {
        this.service = service;
    }

    public void drawControlsHUD(Graphics2D g2) {
        int startX = 25;
        int y = 400;
        int lineHeight = 32;
        g2.setColor(Colorblindness.filter(Colors.getForeground()));
        g2.setFont(UIService.getFont(UIService.getMENU_FONT()));

        ControlCategory[] categories = switch(service.getGameService().getState()) {
            case BOARD -> new ControlCategory[]{ControlCategory.BOARD_KEYBOARD, ControlCategory.BOARD_MOUSE};
            case MENU -> new ControlCategory[]{ControlCategory.MENU};
            default -> new ControlCategory[]{};
        };

        for(ControlCategory category : categories) {
            if(category == ControlCategory.SANDBOX
                    && !(GameService.getGame() == Games.SANDBOX)) { continue; }

            for(ControlsHUD control : ControlsHUD.values()) {
                if(control.getCategory() != category) { continue; }

                int x = startX;
                int spriteHeight = 0;
                for(String keyName : control.getKeys()) {
                    BufferedImage sprite = mapKeyNameToSprite(keyName);
                    g2.drawImage(sprite, x, y, null);
                    x += sprite.getWidth() + 4;
                    spriteHeight = sprite.getHeight();
                }
                FontMetrics fm = g2.getFontMetrics();
                int textHeight = fm.getAscent() - fm.getDescent();
                int textY = y + (spriteHeight / 2) + (textHeight / 2) - 2;
                g2.drawString(control.getAction(), x + 10, textY);
                y += lineHeight * 2;
            }
        }
    }

    private BufferedImage mapKeyNameToSprite(String key) {
        try {
            return switch (key) {
                case "arrow_up", "arrow_down", "arrow_left", "arrow_right" ->
                        UIService.getImage("/ui/keys/" + key);
                case "ctrl" -> UIService.getImage("/ui/keys/ctrl");
                case "q", "t", "g", "r", "z", "c", "h" ->
                        UIService.getImage("/ui/keys/" + key);
                case "1", "2", "3" -> UIService.getImage("/ui/keys/" + key);
                case "enter" -> UIService.getImage("/ui/keys/enter");
                case "escape" -> UIService.getImage("/ui/keys/escape");
                case "f11" -> UIService.getImage("/ui/keys/f11");
                case "mouse_left" -> UIService.getImage("/ui/mouse/mouse_left");
                default -> UIService.getImage("/ui/keys/blank");
            };
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
