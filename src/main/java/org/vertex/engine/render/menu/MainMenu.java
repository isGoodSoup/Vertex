package org.vertex.engine.render.menu;

import org.vertex.engine.enums.GameMenu;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.input.KeyboardInput;
import org.vertex.engine.input.Mouse;
import org.vertex.engine.interfaces.Clickable;
import org.vertex.engine.interfaces.State;
import org.vertex.engine.interfaces.UI;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.UIService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class MainMenu implements UI {
    private static final int ARC = 32;
    private static final int CENTER_Y = 800;
    private static final int PADDING_X = 25;
    private static final int PADDING_Y = 25;

    private final RenderContext render;
    private final GameService gameService;
    private final UIService uiService;
    private final KeyboardInput keyUI;
    private final Mouse mouse;

    private final Map<Clickable, Rectangle> buttons;

    public MainMenu(RenderContext render, GameService gameService,
                    UIService uiService, KeyboardInput keyUI,
                    Mouse mouse) {
        this.render = render;
        this.gameService = gameService;
        this.uiService = uiService;
        this.keyUI = keyUI;
        this.mouse = mouse;
        this.buttons = new HashMap<>();
    }

    public Map<Clickable, Rectangle> getButtons() {
        return buttons;
    }

    private int getTotalWidth() {
        return render.scale(RenderContext.BASE_WIDTH);
    }

    private int getCenterX(int containerWidth, int elementWidth) {
        return render.getOffsetX() + (containerWidth - elementWidth)/2;
    }

    private void drawLogo(Graphics2D g2) {
        BufferedImage logo = UIService.getLogo();
        if(logo == null) { return; }
        BufferedImage img = Colorblindness.filter(logo);
        int logoWidth = logo.getWidth() * 2;
        int logoHeight = logo.getHeight() * 2;
        int x = getCenterX(getTotalWidth(), logoWidth);
        int y = render.getOffsetY()
                + render.scale(RenderContext.BASE_HEIGHT)/3;
        g2.drawImage(img, x, y, logoWidth, logoHeight, null);
    }

    @Override
    public void drawMenu(Graphics2D g2) {
        draw(g2, MenuRender.MENU);
    }

    @Override
    public boolean canDraw(State state) {
        return state == GameState.MENU;
    }

    public void draw(Graphics2D g2, GameMenu[] options) {
        buttons.clear();
        int totalWidth = getTotalWidth();
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, totalWidth,
                render.scale(RenderContext.BASE_HEIGHT));

        drawLogo(g2);

        Font baseFont = UIService.getFont(UIService.getMENU_FONT());
        Font selectedFont = UIService.getFontBold(UIService.getMENU_FONT());
        int spacing = render.scale(UIService.getMENU_SPACING());
        int centerX = render.getOffsetX() + totalWidth/2;
        int computedTotalWidth = 0;
        int[] buttonWidths = new int[options.length];
        FontMetrics[] metricsArray = new FontMetrics[options.length];
        for(int i = 0; i < options.length; i++) {
            GameMenu op = options[i];
            String label = buildLabel(op);
            boolean isSelected = i == keyUI.getSelectedIndexY();
            Font font = isSelected ? selectedFont : baseFont;
            g2.setFont(font);
            FontMetrics metrics = g2.getFontMetrics();
            metricsArray[i] = metrics;
            int width = metrics.stringWidth(label) + PADDING_X;
            buttonWidths[i] = width;
            computedTotalWidth += width;
            if(i < options.length - 1)
                computedTotalWidth += spacing;
        }

        int startX = centerX - computedTotalWidth/2;
        int currentX = startX;
        GameMenu hoveredOption = null;

        for(int i = 0; i < options.length; i++) {
            GameMenu op = options[i];
            String label = buildLabel(op);
            boolean isSelected = i == keyUI.getSelectedIndexY();
            Font font = isSelected ? selectedFont : baseFont;
            g2.setFont(font);
            FontMetrics metrics = metricsArray[i];
            int textWidth = metrics.stringWidth(label);
            int textHeight = metrics.getHeight();
            int ascent = metrics.getAscent();
            int buttonWidth = buttonWidths[i];
            int buttonHeight = textHeight + PADDING_Y;
            int textX = currentX + (buttonWidth - textWidth)/2;
            int textY = CENTER_Y
                    + (buttonHeight - textHeight)/2
                    + ascent;
            Rectangle hitbox = new Rectangle(
                    currentX - 2,
                    CENTER_Y,
                    buttonWidth,
                    buttonHeight
            );
            buttons.put(op, hitbox);
            boolean hovered = hitbox.contains(mouse.getX(), mouse.getY());
            render.getMenuRender().getButtons().putAll(buttons);

            uiService.drawButton(
                    g2,
                    currentX - 2,
                    CENTER_Y,
                    buttonWidth,
                    buttonHeight,
                    ARC,
                    hovered || isSelected
            );

            Color textColor = isSelected
                    ? Colorblindness.filter(Colors.getHighlight())
                    : Colorblindness.filter(Colors.getBackground());
            g2.setColor(textColor);
            g2.drawString(label, textX, textY);

            if(hovered) {
                hoveredOption = op;
            }
            currentX += buttonWidth + spacing;
        }

        if(hoveredOption != null) {
            String tooltip = resolveTooltip(hoveredOption);
            uiService.drawTooltip(g2, tooltip, 16, ARC/2);
        }
    }

    private String buildLabel(GameMenu op) {
        if(op == GameMenu.PLAY) {
            return op.getLabel() + GameService.getGame().getLabel();
        }
        return op.getLabel();
    }

    private String resolveTooltip(GameMenu op) {
        if(op == GameMenu.PLAY) {
            return gameService.getTooltip(GameService.getGame(),
                    gameService.getSaveManager().autosaveExists()
            );
        }
        return op.getTooltip();
    }
}