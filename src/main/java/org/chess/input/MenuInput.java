package org.chess.input;

import org.chess.enums.GameSettings;
import org.chess.enums.GameState;
import org.chess.manager.MovesManager;
import org.chess.records.Save;
import org.chess.render.MenuRender;
import org.chess.render.RenderContext;
import org.chess.service.*;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class MenuInput {
    private final RenderContext render;
    private final GameService gameService;
    private final BoardService boardService;
    private final MovesManager movesManager;
    private final GUIService guiService;
    private final Mouse mouse;
    private final MenuRender menuRender;

    public MenuInput(RenderContext render, MenuRender menuRender,
                     GUIService guiService,
                     GameService gameService, BoardService boardService,
                     MovesManager movesManager, Mouse mouse) {
        this.render = render;
        this.gameService = gameService;
        this.boardService = boardService;
        this.movesManager = movesManager;
        this.guiService = guiService;
        this.mouse = mouse;
        this.menuRender = menuRender;
    }

    public void previousPage() {
        int currentPage = menuRender.getCurrentPage();
        if(currentPage > 1) {
            menuRender.setCurrentPage(currentPage - 1);
        }
    }

    public void nextPage() {
        int itemsPerPage = MovesManager.getITEMS_PER_PAGE();
        int totalItems = boardService.getServiceFactory().getAchievementService().getAllAchievements().size();
        int totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage;

        int current = menuRender.getCurrentPage();

        if(current < totalPages) {
            menuRender.setCurrentPage(current + 1);
        }
    }

    public void nextPage(Object[] options) {
        int itemsPerPage = 8;
        int totalPages = (options.length + itemsPerPage - 1) / itemsPerPage;

        int currentPage = menuRender.getCurrentPage();
        if(currentPage < totalPages) {
            menuRender.setCurrentPage(currentPage + 1);
        }
    }

    public void handleSavesInput() {
        if(!mouse.wasPressed()) { return; }
        List<Save> saves = gameService.getSaveManager().getSaves();

        int itemsPerPage = MovesManager.getITEMS_PER_PAGE();
        int startIndex = (menuRender.getCurrentPage() - 1) * itemsPerPage + 1;
        int endIndex = Math.min(startIndex + itemsPerPage, saves.size());

        FontMetrics fm = menuRender.getFontMetrics();
        int lineHeight = fm.getHeight() + render.scale(10);

        int headerY = render.getOffsetY() + render.scale(MenuRender.getOPTION_Y());

        int stroke = 4;
        int spacing = 25;
        int startY = headerY + spacing * 2;
        int width = RenderContext.BASE_WIDTH/2;
        int height = 100, arcWidth = 32, arcHeight = 32;
        int x = MenuRender.getCenterX(menuRender.getTotalWidth(), width);
        boolean hasBackground = true;

        int gap = render.scale(100);
        int start = (menuRender.getCurrentPage() - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, saves.size());

        for (int i = start; i < end; i++) {
            Save s = saves.get(i);
            Rectangle hitbox = new Rectangle(x, startY, width, height);
            if (hitbox.contains(mouse.getX(), mouse.getY())) {
                guiService.getFx().playFX(3);
                gameService.continueGame(s.name());
                break;
            }
            startY += height + spacing;
        }
    }

    public void handleOptionsInput() {
        if(!mouse.wasPressed()) { return; }

        int itemsPerPage = 8;
        int startIndex = (menuRender.getCurrentPage() - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, MenuRender.optionsTweaks.length);

        FontMetrics fm = menuRender.getFontMetrics();
        int lineHeight = fm.getHeight() + render.scale(10);

        int headerY = render.getOffsetY() + render.scale(MenuRender.getOPTION_Y());
        int startY = headerY + render.scale(90);

        int gap = render.scale(100);
        int maxRowWidth = 0;

        for(int i = startIndex; i < endIndex; i++) {
            String enabledOption = MenuRender.ENABLE + MenuRender.optionsTweaks[i];
            int textWidth = fm.stringWidth(enabledOption.toUpperCase());
            int toggleWidth = render.scale(menuRender.getSprite(0).getWidth() / 2);
            int rowWidth = textWidth + gap + toggleWidth;
            if(rowWidth > maxRowWidth) maxRowWidth = rowWidth;
        }

        for(int i = startIndex; i < endIndex; i++) {
            GameSettings option = MenuRender.optionsTweaks[i];
            String enabledOption = MenuRender.ENABLE + option;

            int textWidth = fm.stringWidth(enabledOption);
            int toggleWidth = render.scale(menuRender.getSprite(0).getWidth()/2);
            int toggleHeight = render.scale(menuRender.getSprite(0).getHeight()/2);

            int blockX = MenuRender.getCenterX(menuRender.getTotalWidth(), maxRowWidth);
            int toggleX = blockX + maxRowWidth - toggleWidth;
            int toggleY = startY - toggleHeight;

            Rectangle toggleHitbox = new Rectangle(
                    toggleX,
                    toggleY,
                    toggleWidth,
                    toggleHeight
            );

            if(toggleHitbox.contains(mouse.getX(), mouse.getY())) {
                guiService.getFx().play(0);
                option.toggle();
                break;
            }
            startY += lineHeight;
        }
    }

    public void handleMenuInput(String[] options) {
        if(!mouse.wasPressed()) { return; }

        int startY = render.scale(RenderContext.BASE_HEIGHT)/2 + render.scale(GUIService.getMENU_START_Y());
        int spacing = render.scale(GUIService.getMENU_SPACING());

        for(int i = 0; i < options.length; i++) {
            String optionText = options[i];
            FontMetrics fm = menuRender.getFontMetrics();
            int textWidth = fm.stringWidth(optionText);

            int x = MenuRender.getCenterX(menuRender.getTotalWidth(), textWidth);
            int y = render.getOffsetY() + startY + i * spacing;

            Rectangle hitbox = new Rectangle(
                    x,
                    y - fm.getAscent(),
                    textWidth,
                    fm.getHeight()
            );

            if(hitbox.contains(mouse.getX(), mouse.getY())) {
                guiService.getFx().play(0);
                if (Objects.requireNonNull(GameService.getState()) == GameState.MENU) {
                    switch (i) {
                        case 0 -> gameService.startNewGame();
                        case 1 -> gameService.loadSaves();
                        case 2 -> gameService.achievementsMenu();
                        case 3 -> gameService.optionsMenu();
                        case 4 -> System.exit(0);
                    }
                }
            }
        }
    }
}
