package org.lud.engine.render.menu;

import org.lud.engine.entities.Button;
import org.lud.engine.enums.GameSettings;
import org.lud.engine.enums.GameState;
import org.lud.engine.enums.Theme;
import org.lud.engine.gui.Colors;
import org.lud.engine.input.KeyboardInput;
import org.lud.engine.input.Mouse;
import org.lud.engine.input.MouseInput;
import org.lud.engine.interfaces.Clickable;
import org.lud.engine.interfaces.State;
import org.lud.engine.interfaces.UI;
import org.lud.engine.render.Colorblindness;
import org.lud.engine.render.MenuRender;
import org.lud.engine.render.RenderContext;
import org.lud.engine.service.GameService;
import org.lud.engine.service.UIService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class OptionsMenu implements UI {
    private static final String SETTINGS = "SETTINGS";
    private static final String ENABLE = "Enable ";
    private static final int ARC = 32;
    private static final int STROKE = 6;
    private static final int OPTION_Y = 160;

    private final RenderContext render;
    private final UIService uiService;
    private final GameService gameService;
    private final KeyboardInput keyUI;
    private final Mouse mouse;
    private final MouseInput mouseInput;

    private final BufferedImage toggleOn;
    private final BufferedImage toggleOff;
    private final BufferedImage toggleOnHighlighted;
    private final BufferedImage toggleOffHighlighted;
    private final BufferedImage hardModeOn;
    private final BufferedImage hardModeOnHighlighted;
    private final BufferedImage nextPage;
    private final BufferedImage nextPageOn;
    private final BufferedImage previousPage;
    private final BufferedImage previousPageOn;

    private final Map<Clickable, Rectangle> buttons;

    private Button nextButton;
    private Button prevButton;
    private Button backButton;

    public OptionsMenu(RenderContext render, UIService uiService, GameService gs,
                       KeyboardInput keyUI, Mouse mouse, MouseInput mouseInput,
                       BufferedImage... images) {

        this.render = render;
        this.uiService = uiService;
        this.gameService = gs;
        this.keyUI = keyUI;
        this.mouse = mouse;
        this.mouseInput = mouseInput;
        this.buttons = new HashMap<>();

        this.toggleOn = images[0];
        this.toggleOff = images[1];
        this.toggleOnHighlighted = images[2];
        this.toggleOffHighlighted = images[3];
        this.hardModeOn = images[4];
        this.hardModeOnHighlighted = images[5];
        this.nextPage = images[6];
        this.nextPageOn = images[7];
        this.previousPage = images[8];
        this.previousPageOn = images[9];
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

    @Override
    public void drawMenu(Graphics2D g2) {
        draw(g2, MenuRender.SETTINGS_MENU);
    }

    @Override
    public boolean canDraw(State state) {
        return state == GameState.SETTINGS;
    }

    public void draw(Graphics2D g2, GameSettings[] options) {
        buttons.clear();
        int totalWidth = getTotalWidth();
        g2.setColor(Colorblindness.filter(Colors.getBackground()));
        g2.fillRect(0, 0, totalWidth,
                render.scale(RenderContext.BASE_HEIGHT));
        int x = 32;
        int y = 32;

        g2.setFont(UIService.getFont(UIService.getMENU_FONT()));

        int headerY = render.getOffsetY() + render.scale(OPTION_Y);
        int headerWidth = g2.getFontMetrics().stringWidth(SETTINGS);

        g2.setColor(Colorblindness.filter(
                Colors.getTheme() == Theme.DEFAULT
                        ? Color.WHITE
                        : Colors.getForeground()));

        g2.drawString(SETTINGS,
                getCenterX(totalWidth, headerWidth),
                headerY);

        int startY = headerY + render.scale(100);
        int lineHeight = g2.getFontMetrics().getHeight()
                + render.scale(10);

        int itemsPerPage = 8;
        int startIndex = keyUI.getCurrentPage() * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage,
                options.length);

        int gap = render.scale(100);
        int maxRowWidth = 0;

        for(int i = startIndex; i < endIndex; i++) {
            String label = ENABLE + options[i].getLabel();
            int textWidth = g2.getFontMetrics().stringWidth(label);
            int toggleWidth = render.scale(toggleOn.getWidth()/2);
            int rowWidth = textWidth + gap + toggleWidth;
            if(rowWidth > maxRowWidth)
                maxRowWidth = rowWidth;
        }

        for(int i = startIndex; i < endIndex; i++) {
            GameSettings option = options[i];
            int relativeIndex = i - startIndex;
            boolean isSelected =
                    relativeIndex == keyUI.getSelectedIndexY();
            String label = ENABLE + option.getLabel();
            int textWidth = g2.getFontMetrics().stringWidth(label);
            int toggleWidth = render.scale(toggleOn.getWidth()/2);
            int toggleHeight = render.scale(toggleOn.getHeight()/2);
            int blockX = getCenterX(totalWidth, maxRowWidth);
            int textX = blockX;
            int toggleX = blockX + maxRowWidth - toggleWidth;
            int toggleY = startY - toggleHeight;

            g2.drawString(label,
                    textX,
                    render.getOffsetY() + startY);

            Rectangle toggleHitbox = new Rectangle(
                    render.getOffsetX() + toggleX,
                    render.getOffsetY() + toggleY,
                    toggleWidth,
                    toggleHeight
            );

            buttons.put(option, toggleHitbox);

            boolean isEnabled = option.get();
            boolean isHovered = toggleHitbox.contains(
                    mouse.getX(), mouse.getY());

            BufferedImage toggleImage = drawToggle(
                    option, isEnabled, isSelected, isHovered);

            uiService.drawToggle(g2,
                    toggleImage,
                    render.getOffsetX() + toggleX,
                    render.getOffsetY() + toggleY,
                    toggleWidth,
                    toggleHeight);

            startY += lineHeight;
        }

        initButtons(options, totalWidth);
        drawButtons(g2);
    }

    private BufferedImage drawToggle(GameSettings option, boolean isEnabled,
                                     boolean isSelected, boolean isHovered) {
        if(option == GameSettings.HARD_MODE) {
            if(isEnabled) {
                return (isSelected || isHovered)
                        ? Colorblindness.filter(hardModeOnHighlighted)
                        : Colorblindness.filter(hardModeOn);
            } else {
                return (isSelected || isHovered)
                        ? Colorblindness.filter(toggleOffHighlighted)
                        : Colorblindness.filter(toggleOff);
            }
        }

        if(isEnabled) {
            return (isSelected || isHovered)
                    ? Colorblindness.filter(toggleOnHighlighted)
                    : Colorblindness.filter(toggleOn);
        } else {
            return (isSelected || isHovered)
                    ? Colorblindness.filter(toggleOffHighlighted)
                    : Colorblindness.filter(toggleOff);
        }
    }

    private void initButtons(GameSettings[] options, int totalWidth) {
        int baseY = render.scale(RenderContext.BASE_HEIGHT - 115);
        if(nextButton == null) {
            int x = totalWidth/2;
            int y = baseY;

            nextButton = new Button(x, y, nextPage.getWidth(),
                    nextPage.getHeight(), () -> {
                        int totalPages =
                                (options.length
                                        + KeyboardInput.getITEMS_PER_PAGE() - 1)
                                        / KeyboardInput.getITEMS_PER_PAGE() - 1;

                        int page = keyUI.getCurrentPage() + 1;
                        if(page > totalPages)
                            page = totalPages;

                        keyUI.setCurrentPage(page);
                    });
        }

        if(prevButton == null) {
            int x = totalWidth/2 - render.scale(80);
            int y = baseY;

            prevButton = new Button(x, y, previousPage.getWidth(),
                    previousPage.getHeight(), () -> {
                        int page = keyUI.getCurrentPage() - 1;
                        if(page < 0)
                            page = 0;
                        keyUI.setCurrentPage(page);
                    });
        }

        if(backButton == null) {
            int x = 50;
            int y = baseY;

            backButton = new Button(x, y, previousPage.getWidth(),
                    previousPage.getHeight(), () -> gameService.setState(GameState.MENU));
        }

        buttons.put(nextButton, new Rectangle(nextButton.getX(), nextButton.getY(),
                        nextButton.getWidth(), nextButton.getHeight()));

        buttons.put(prevButton, new Rectangle(prevButton.getX(), prevButton.getY(),
                        prevButton.getWidth(), prevButton.getHeight()));

        buttons.put(backButton, new Rectangle(backButton.getX(), backButton.getY(),
                        backButton.getWidth(), backButton.getHeight()));
        render.getMenuRender().getButtons().putAll(buttons);
    }

    private void drawButtons(Graphics2D g2) {
        BufferedImage nextImg = render.isHovered(nextButton)
                        ? Colorblindness.filter(nextPageOn)
                        : Colorblindness.filter(nextPage);

        BufferedImage prevImg = render.isHovered(prevButton)
                        ? Colorblindness.filter(previousPageOn)
                        : Colorblindness.filter(previousPage);

        BufferedImage backImg = render.isHovered(backButton)
                ? Colorblindness.filter(previousPageOn)
                : Colorblindness.filter(previousPage);

        g2.drawImage(nextImg, nextButton.getX(), nextButton.getY(), null);
        g2.drawImage(prevImg, prevButton.getX(), prevButton.getY(), null);
        g2.drawImage(backImg, backButton.getX(), backButton.getY(), null);
    }
}