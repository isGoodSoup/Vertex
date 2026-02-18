package org.vertex.engine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.animations.ToastAnimation;
import org.vertex.engine.enums.*;
import org.vertex.engine.interfaces.Ruleset;
import org.vertex.engine.manager.SaveManager;
import org.vertex.engine.records.Save;
import org.vertex.engine.render.RenderContext;

import java.time.LocalDate;
import java.util.ArrayList;

public class GameService {
    private final Logger log = LoggerFactory.getLogger(GameService.class);
    private GameMenu gameMenu;
    private GameState state;
    private PlayState mode;
    private Games game;
    private Tint currentTurn;
    private RenderContext render;
    private BoardService boardService;
    private ServiceFactory service;
    private SaveManager saveManager;

    public GameService(RenderContext render, BoardService boardService, SaveManager saveManager) {
        this.render = render;
        this.boardService = boardService;
        this.saveManager = saveManager;
        game = Games.CHESS;
    }

    public GameMenu getGameMenu() { return gameMenu; }
    public void setGameMenu(GameMenu menu) { gameMenu = menu; }

    public void setGame(Games g) { game = g; }
    public Games getGame() { return game; }

    public GameState getState() { return state; }
    public void setState(GameState s) { state = s; }

    public PlayState getMode() { return mode; }
    public Tint getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(Tint tint) { currentTurn = tint; }

    public ServiceFactory getServiceFactory() { return service; }
    public void setServiceFactory(ServiceFactory svc) { service = svc; }

    public SaveManager getSaveManager() { return saveManager; }
    public void setSaveManager(SaveManager sm) { saveManager = sm; }

    public void startNewGame() {
        setCurrentTurn(Tint.LIGHT);
        service.getMovesManager().setMoves(new ArrayList<>());
        BooleanService.isCheckmate = false;
        BooleanService.isPromotionActive = false;
        boardService.prepBoard();
        boardService.startBoard();
        Save newSave = new Save(
                getGame(),
                LocalDate.now().toString(),
                getCurrentTurn(),
                service.getPieceService().getPieces(),
                service.getAchievementService().getUnlockedAchievements()
        );
        saveManager.saveGame(newSave);
        log.info("New game started and autosave created.");
        Ruleset rule = service.getModelService().createRuleSet(game);
        service.getModelService().setRule(rule);
        setState(GameState.BOARD);
    }

    public void continueGame() {
        if (!saveManager.autosaveExists()) {
            log.warn("No autosave found. Starting new game.");
            startNewGame();
            return;
        }
        Save loaded = saveManager.loadGame();
        if (loaded == null || loaded.pieces() == null) {
            log.warn("Autosave invalid or empty. Starting new game.");
            startNewGame();
            return;
        }
        boardService.prepBoard();
        boardService.restoreSprites(loaded, service.getGuiService());
        service.getPieceService().getPieces().clear();
        service.getPieceService().getPieces().addAll(loaded.pieces());
        service.getAchievementService().setUnlockedAchievements(loaded.achievements());
        setCurrentTurn(loaded.player());

        Ruleset rule = service.getModelService().createRuleSet(getGame());
        service.getModelService().setRule(rule);

        service.getTimerService().start();
        log.info("Autosave loaded successfully.");
        setState(GameState.BOARD);
    }

    public void autoSave() {
        Save save = new Save(
                getGame(),
                LocalDate.now().toString(),
                getCurrentTurn(),
                service.getPieceService().getPieces(),
                service.getAchievementService().getUnlockedAchievements()
        );
        saveManager.saveGame(save);
        log.debug("Autosave triggered.");
    }

    public void nextGame() {
        Games[] games = Games.values();
        int nextIndex = (game.ordinal() + 1) % games.length;
        setGame(games[nextIndex]);
        service.getAnimationService().add(new ToastAnimation(games[nextIndex].getLabel()));
    }
}