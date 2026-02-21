package org.vertex.engine.render.menu;

import org.vertex.engine.entities.Button;
import org.vertex.engine.enums.GameSettings;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.enums.Theme;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.input.KeyboardInput;
import org.vertex.engine.input.Mouse;
import org.vertex.engine.input.MouseInput;
import org.vertex.engine.interfaces.Clickable;
import org.vertex.engine.interfaces.State;
import org.vertex.engine.interfaces.UI;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.UIService;

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

    public OptionsMenu(RenderContext render, UIService uiService,
                       KeyboardInput keyUI, Mouse mouse, MouseInput mouseInput,
                       BufferedImage... images) {

        if (images.length < 10) {
            throw new IllegalArgumentException("OptionsMenu requires 11 images in order: " +
                    "toggleOn, toggleOff, toggleOnHighlighted, toggleOffHighlighted, " +
                    "hardModeOn, hardModeOnHighlighted, nextPage, nextPageOn, previousPage, previousPageOn");
        }

        this.render = render;
        this.uiService = uiService;
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
        return render.getOffsetX() + (containerWidth - elementWidth) / 2;
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
        UIService.drawBox(g2, STROKE, x, y,
                render.scale(RenderContext.BASE_WIDTH - x * 2),
                render.scale(RenderContext.BASE_HEIGHT - y * 2),
                ARC, true, false, 255);

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
            int toggleWidth = render.scale(toggleOn.getWidth() / 2);
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
            int toggleWidth = render.scale(toggleOn.getWidth() / 2);
            int toggleHeight = render.scale(toggleOn.getHeight() / 2);
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
            boolean hovered = toggleHitbox.contains(
                    mouse.getX(), mouse.getY());

            BufferedImage toggleImage = resolveToggleImage(
                    option, isEnabled, isSelected, hovered);

            uiService.drawToggle(g2,
                    toggleImage,
                    render.getOffsetX() + toggleX,
                    render.getOffsetY() + toggleY,
                    toggleWidth,
                    toggleHeight);

            startY += lineHeight;
        }

        initButtons(options, totalWidth);
        drawPagination(g2);
    }

    private BufferedImage resolveToggleImage(GameSettings option,
                                             boolean enabled,
                                             boolean selected,
                                             boolean hovered) {
        if(option == GameSettings.HARD_MODE) {
            if(enabled)
                return selected
                        ? Colorblindness.filter(hardModeOnHighlighted)
                        : Colorblindness.filter(hardModeOn);
            return hovered
                    ? Colorblindness.filter(toggleOffHighlighted)
                    : Colorblindness.filter(toggleOff);
        }

        if(enabled)
            return (selected || hovered)
                    ? Colorblindness.filter(toggleOnHighlighted)
                    : Colorblindness.filter(toggleOn);

        return (selected || hovered)
                ? Colorblindness.filter(toggleOffHighlighted)
                : Colorblindness.filter(toggleOff);
    }

    private void initButtons(GameSettings[] options,
                             int totalWidth) {
        if(nextButton == null) {
            int x = totalWidth / 2;
            int y = render.scale(500)
                    + (UIService.getFont(
                    UIService.getMENU_FONT()).getSize() + 10)
                    * KeyboardInput.getITEMS_PER_PAGE();

            nextButton = new Button(x, y, nextPage.getWidth(), nextPage.getHeight(), () -> {
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
            int x = totalWidth / 2 - render.scale(100);
            int y = render.scale(500)
                    + (UIService.getFont(
                    UIService.getMENU_FONT()).getSize() + 10)
                    * KeyboardInput.getITEMS_PER_PAGE();

            prevButton = new Button(x, y, previousPage.getWidth(), previousPage.getHeight(), () -> {
                        int page = keyUI.getCurrentPage() - 1;
                        if(page < 0)
                            page = 0;
                        keyUI.setCurrentPage(page);
                    });
        }

        buttons.put(nextButton, new Rectangle(nextButton.getX(), nextButton.getY(),
                        nextButton.getWidth(), nextButton.getHeight()));

        buttons.put(prevButton, new Rectangle(prevButton.getX(), prevButton.getY(),
                        prevButton.getWidth(), prevButton.getHeight()));
        render.getMenuRender().getButtons().putAll(buttons);
    }

    private void drawPagination(Graphics2D g2) {
        boolean nextHovered = buttons.get(nextButton)
                        .contains(mouse.getX(), mouse.getY())
                        && !mouseInput.isClickingOption(nextButton);

        boolean prevHovered = buttons.get(prevButton)
                        .contains(mouse.getX(), mouse.getY())
                        && !mouseInput.isClickingOption(prevButton);

        BufferedImage nextImg = nextHovered
                        ? Colorblindness.filter(nextPageOn)
                        : Colorblindness.filter(nextPage);

        BufferedImage prevImg = prevHovered
                        ? Colorblindness.filter(previousPageOn)
                        : Colorblindness.filter(previousPage);

        g2.drawImage(nextImg, nextButton.getX(), nextButton.getY(), null);
        g2.drawImage(prevImg, prevButton.getX(), prevButton.getY(), null);
    }
}