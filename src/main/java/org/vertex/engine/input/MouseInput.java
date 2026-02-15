package org.vertex.engine.input;

import org.vertex.engine.enums.GameMenu;
import org.vertex.engine.enums.GameSettings;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.enums.Games;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.records.Save;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.BoardService;
import org.vertex.engine.service.GUIService;
import org.vertex.engine.service.GameService;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class MouseInput {
    private final RenderContext render;
    private final GameService gameService;
    private final BoardService boardService;
    private final MovesManager movesManager;
    private final GUIService guiService;
    private final Mouse mouse;
    private final MenuRender menuRender;

    public MouseInput(RenderContext render, MenuRender menuRender,
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
        int endIndex = Math.min(startIndex + itemsPerPage, MenuRender.SETTINGS_MENU.length);

        FontMetrics fm = menuRender.getFontMetrics();
        int lineHeight = fm.getHeight() + render.scale(10);

        int headerY = render.getOffsetY() + render.scale(MenuRender.getOPTION_Y());
        int startY = headerY + render.scale(90);

        int gap = render.scale(100);
        int maxRowWidth = 0;

        for(int i = startIndex; i < endIndex; i++) {
            String enabledOption = MenuRender.ENABLE + MenuRender.SETTINGS_MENU[i];
            int textWidth = fm.stringWidth(enabledOption.toUpperCase());
            int toggleWidth = render.scale(menuRender.getSprite(0).getWidth() / 2);
            int rowWidth = textWidth + gap + toggleWidth;
            if(rowWidth > maxRowWidth) maxRowWidth = rowWidth;
        }

        for(int i = startIndex; i < endIndex; i++) {
            GameSettings option = MenuRender.SETTINGS_MENU[i];
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

    public void handleMenuInput(GameMenu[] options) {
        if(!mouse.wasPressed()) { return; }

        int startY = render.scale(RenderContext.BASE_HEIGHT)/2 + render.scale(GUIService.getMENU_START_Y());
        int spacing = render.scale(GUIService.getMENU_SPACING());

        for(int i = 0; i < options.length; i++) {
            GameMenu op = options[i];
            String option = op.getLabel();
            FontMetrics fm = menuRender.getFontMetrics();
            int textWidth = fm.stringWidth(option);

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
                        case 0 -> GameService.setState(GameState.GAMES);
                        case 1 -> GameService.setState(GameState.SAVES);
                        case 2 -> System.exit(0);
                    }
                }
            }
        }
    }

    public void handleGamesMenu(Games[] games) {
        if(!mouse.wasPressed()) { return; }

        int startY = render.scale(RenderContext.BASE_HEIGHT)/2 + render.scale(GUIService.getMENU_START_Y());
        int spacing = render.scale(GUIService.getMENU_SPACING());

        for(int i = 0; i < games.length; i++) {
            Games op = games[i];
            String option = op.getLabel();
            FontMetrics fm = menuRender.getFontMetrics();
            int textWidth = fm.stringWidth(option);

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
                        case 0 -> GameService.setGame(games[0]);
                        case 1 -> GameService.setGame(games[1]);
                    }
                }
            }
        }
    }
}
