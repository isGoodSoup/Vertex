package org.chess.service;

import org.chess.enums.GameState;
import org.chess.enums.PlayState;
import org.chess.enums.Tint;
import org.chess.input.Mouse;
import org.chess.manager.SaveManager;
import org.chess.records.Save;
import org.chess.render.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;

public class GameService {
    private static GameState state;
    private static PlayState mode;
    private static Tint currentTurn;

    private static RenderContext render;
    private static BoardService boardService;
    private static Mouse mouse;

    private static ServiceFactory service;
    public static SaveManager saveManager;

    private static final Logger log =
            LoggerFactory.getLogger(GameService.class);

    public GameService(RenderContext render, BoardService boardService,
                       Mouse mouse) {
        GameService.render = render;
        GameService.boardService = boardService;
        GameService.mouse = mouse;
    }

    public SaveManager getSaveManager() {
        return saveManager;
    }

    public void setSaveManager(SaveManager saveManager) {
        GameService.saveManager = saveManager;
    }

    public static ServiceFactory getServiceFactory() {
        return service;
    }

    public void setServiceFactory(ServiceFactory service) {
        GameService.service = service;
    }

    public static GameState getState() {
        return state;
    }

    public static PlayState getMode() {
        return mode;
    }

    public static void setState(GameState state) {
        GameService.state = state;
    }

    public static Tint getCurrentTurn() {
        return currentTurn;
    }

    public static void setCurrentTurn(Tint tint) {
        currentTurn = tint;
    }

    public static boolean isBlackTurn() {
        return currentTurn == Tint.BLACK;
    }

    public void startNewGame() {
        Save currentSave = service.getSaveManager().getCurrentSave();
        setCurrentTurn(Tint.WHITE);
        service.getMovesManager().setMoves(new ArrayList<>());
        BooleanService.isCheckmate = false;
        BooleanService.isPromotionActive = false;
        service.getBoardService().startBoard();
        setState(GameState.BOARD);

        if(currentSave == null) {
            Save newSave = new Save(
                    LocalDate.now().toString(),
                    getCurrentTurn(),
                    service.getPieceService().getPieces(),
                    service.getAchievementService().getUnlockedAchievements()
            );
            service.getSaveManager().setCurrentSave(newSave);
            service.getSaveManager().saveGame(newSave);
            log.info("New save file created");
        } else {
            service.getSaveManager().saveGame(currentSave);
        }
    }

    public void continueGame(String saveName) {
        Save loaded = saveManager.loadGame(saveName);
        if (loaded != null) {
            boardService.restoreSprites(loaded, service.getGuiService());
            service.getPieceService().getPieces().clear();
            service.getPieceService().getPieces().addAll(loaded.pieces());
            setCurrentTurn(loaded.player());
            service.getTimerService().start();
            log.info("Loaded save: {}", saveName);
        } else {
            log.error("Failed to load save: {}", saveName);
        }
        GameService.setState(GameState.BOARD);
    }

    public void optionsMenu() {
        setState(GameState.RULES);
    }

    public void achievementsMenu() {
        setState(GameState.ACHIEVEMENTS);
    }

    public void loadSaves() {
        setState(GameState.SAVES);
    }
}
