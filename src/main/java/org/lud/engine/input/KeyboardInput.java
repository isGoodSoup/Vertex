package org.lud.engine.input;

import org.lud.engine.enums.*;
import org.lud.engine.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.lud.engine.entities.Piece;
import org.lud.engine.events.ToggleEvent;
import org.lud.engine.gui.Colors;
import org.lud.engine.gui.GameFrame;
import org.lud.engine.manager.MovesManager;
import org.lud.engine.render.MenuRender;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KeyboardInput {
    private static final Logger log = LoggerFactory.getLogger(KeyboardInput.class);
    private int moveX = 4;
    private int moveY = 6;
    private int selectedIndexY;
    private int selectedIndexX;
    private int currentPage;
    private long lastUpTime = 0;
    private long lastDownTime = 0;
    private long lastLeftTime = 0;
    private long lastRightTime = 0;
    private final long repeatDelay = 150;
    private static final int ITEMS_PER_PAGE = 6;

    private ServiceFactory service;
    private GameFrame gameFrame;
    private BoardService boardService;

    public KeyboardInput() {
        this.selectedIndexY = 0;
        this.selectedIndexX = 0;
    }

    public GameFrame getGameFrame() {
        return gameFrame;
    }

    public void setGameFrame(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
    }

    public ServiceFactory getService() {
        return service;
    }

    public void setService(ServiceFactory service) {
        this.service = service;
    }

    public int getMoveX() {
        return moveX;
    }

    public void setMoveX(int moveX) {
        this.moveX = moveX;
    }

    public int getMoveY() {
        return moveY;
    }

    public void setMoveY(int moveY) {
        this.moveY = moveY;
    }

    public int getSelectedIndexY() {
        return selectedIndexY;
    }

    public void setSelectedIndexY(int selectedIndexY) {
        this.selectedIndexY = selectedIndexY;
    }

    public int getSelectedIndexX() {
        return selectedIndexX;
    }

    public void setSelectedIndexX(int selectedIndexX) {
        this.selectedIndexX = selectedIndexX;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public static int getITEMS_PER_PAGE() {
        return ITEMS_PER_PAGE;
    }

    public BoardService getBoardService() {
        return boardService;
    }

    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    public void update() {
        long now = System.currentTimeMillis();
        Keyboard keyboard = service.getKeyboard();
        MovesManager move = service.getMovesManager();

        if(keyboard.isEscapeDown()) {
            service.getGameService().setState(GameState.MENU);
            service.getGameService().autoSave();
            selectedIndexY = 0;
            service.getSound().playFX(2);
        }

        colorblindKeys(keyboard);
        sandboxCommands(keyboard);

        switch(service.getGameService().getState()) {
            case MENU -> menuInput(keyboard, now);
            case SETTINGS -> rulesInput(keyboard, now);
            case ACHIEVEMENTS -> achievementsInput(keyboard, now);
            case BOARD -> boardInput(keyboard, now, move);
        }

        globalShortcuts(keyboard);

        if(keyboard.wasF11Pressed()) {
            gameFrame.toggleFullscreen();
        }
    }

    public void keyboardMove() {
        Piece selectedPiece = service.getMovesManager().getSelectedPiece();
        updateKeyboardHover();

        if(selectedPiece == null) {
            Piece piece = PieceService.getPieceAt(moveX, moveY,
                    service.getPieceService().getPieces());

            boolean isSandbox = GameService.getGame() == Games.SANDBOX;
            boolean canSelect = isSandbox || !BooleanService.canSwitchTurns
                    || Objects.requireNonNull(piece).getColor() == service.getGameService().getCurrentTurn();

            if(canSelect) {
                service.getMovesManager().setSelectedPiece(piece);
            } else {
                service.getSound().playFX(3);
            }
        } else {
            boolean isSandbox = GameService.getGame() == Games.SANDBOX;
            if(selectedPiece != null) {
                if(BooleanService.isLegal || isSandbox) {
                    service.getAnimationService().startMove(selectedPiece, moveX, moveY);
                    service.getMovesManager().attemptMove(selectedPiece, moveX, moveY);
                    service.getPieceService().setLastPiece(selectedPiece);

                    if(!isSandbox) {
                        service.getMovesManager().setSelectedPiece(null);
                        service.getPieceService().setHoveredPieceKeyboard(null);
                    }
                }
            }
        }
    }

    public void updateKeyboardHover() {
        Piece selectedPiece = service.getMovesManager().getSelectedPiece();
        service.getPieceService().setHoveredSquare(moveX, moveY);

        boolean isSandbox = GameService.getGame() == Games.SANDBOX;
        if (selectedPiece != null) {
            BooleanService.isLegal = isSandbox
                    || selectedPiece.canMove(moveX, moveY, service.getPieceService().getPieces())
                    && !service.getPieceService().wouldLeaveKingInCheck(selectedPiece, moveX, moveY);
        } else {
            BooleanService.isLegal = isSandbox;
            service.getPieceService().setHoveredPieceKeyboard(
                    PieceService.getPieceAt(moveX, moveY, service.getPieceService().getPieces())
            );
        }
    }

    public void move(Direction dir) {
        if(service.getGameService().getState() != GameState.BOARD) return;

        switch(dir) {
            case UP -> moveY = Math.max(0, moveY - 1);
            case DOWN -> moveY = Math.min(boardService.getBoard().getRow() - 1, moveY + 1);
            case LEFT -> moveX = Math.max(0, moveX - 1);
            case RIGHT -> moveX = Math.min(boardService.getBoard().getCol() - 1, moveX + 1);
        }
        updateKeyboardHover();
    }

    private <T> void moveUp(List<T> items) {
        if(items.isEmpty()) return;
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        selectedIndexY--;
        if(selectedIndexY < start) selectedIndexY = end - 1;
    }

    private <T> void moveDown(List<T> items) {
        if(items.isEmpty()) return;
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, items.size());

        selectedIndexY++;
        if(selectedIndexY >= end) selectedIndexY = start;
    }

    public void previousPage() {
        currentPage = Math.max(0, currentPage - 1);
    }

    public void nextPage(int totalItems) {
        int totalPages = (totalItems + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        if(currentPage < totalPages - 1) currentPage++;
    }

    public void nextPage(Object[] options) {
        nextPage(options.length);
    }

    public void activate(GameState state) {
        switch (state) {
            case MENU -> activateMenu();
            case SETTINGS -> activateRules();
            case ACHIEVEMENTS -> BooleanService.canZoomIn ^= true;
            case BOARD -> keyboardMove();
        }
    }

    private void activateMenu() {
        if(selectedIndexY >= 0 && selectedIndexY < MenuRender.MENU.length) {
            GameMenu selected = MenuRender.MENU[selectedIndexY];
            if(selected.isEnabled(service.getGameService())) {
                selected.run(service.getGameService());
            }
        }
    }

    private void activateRules() {
        int absIndex = currentPage * ITEMS_PER_PAGE + selectedIndexY;
        if(absIndex < MenuRender.SETTINGS_MENU.length) {
            MenuRender.SETTINGS_MENU[absIndex].toggle();
        }
    }

    private void colorblindKeys(Keyboard keyboard) {
        if(!BooleanService.canBeColorblind) return;

        if(keyboard.wasOnePressed()) MenuRender.setCb(ColorblindType.PROTANOPIA);
        if(keyboard.wasTwoPressed()) MenuRender.setCb(ColorblindType.DEUTERANOPIA);
        if(keyboard.wasThreePressed()) MenuRender.setCb(ColorblindType.TRITANOPIA);
    }

    private void sandboxCommands(Keyboard keyboard) {
        if(GameService.getGame() != Games.SANDBOX) { return; }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_ENTER) && !BooleanService.canType) {
            BooleanService.canType = true;
            log.info("Entered sandbox typing mode");
            return;
        }

        if(BooleanService.canType && keyboard.wasEnterPressed()) {
            String fullInput = keyboard.consumeText().trim();
            if(!fullInput.isEmpty()) {
                String[] parts = fullInput.split("\\s+");
                String commandName = parts[0];
                String[] args = Arrays.copyOfRange(parts, 1, parts.length);
                Console consoleCommand = Console.fromString(commandName);
                if(consoleCommand != null) {
                    consoleCommand.run(service, args);
                } else {
                    log.error("Unknown command: {}", commandName);
                }
            }
            exitTypingMode();
        }
    }

    private void exitTypingMode() {
        BooleanService.canType = false;
    }

    private void menuInput(Keyboard keyboard, long now) {
        if(keyboard.wasSelectPressed()) {
            activate(GameState.MENU);
            service.getSound().playFX(3);
        }
        repeatKeyCheck(keyboard.wasUpPressed(), () -> moveUp(Arrays.asList(MenuRender.MENU)), now, lastUpTime, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> moveDown(Arrays.asList(MenuRender.MENU)), now, lastDownTime, () -> lastDownTime = now);

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_G)) {
            service.getGameService().nextGame();
            service.getSound().playFX(0);
            service.getEventBus().fire(new ToggleEvent());
        }
    }

    private void rulesInput(Keyboard keyboard, long now) {
        if(keyboard.wasSelectPressed()) { activate(GameState.SETTINGS); service.getSound().playFX(0); }

        repeatKeyCheck(keyboard.wasUpPressed(), () -> moveUp(Arrays.asList(MenuRender.SETTINGS_MENU)), now, lastUpTime, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> moveDown(Arrays.asList(MenuRender.SETTINGS_MENU)), now, lastDownTime,  () -> lastDownTime = now);

        if(keyboard.wasLeftPressed()) { previousPage(); service.getSound().playFX(2); lastLeftTime = now; }
        if(keyboard.wasRightPressed()) { nextPage(MenuRender.SETTINGS_MENU); service.getSound().playFX(2); lastRightTime = now; }
    }

    private void achievementsInput(Keyboard keyboard, long now) {
        if(keyboard.wasSelectPressed()) { activate(GameState.ACHIEVEMENTS); service.getSound().playFX(3); }

        List<?> achievements = service.getAchievementService().getAchievementList();
        repeatKeyCheck(keyboard.wasUpPressed(), () -> moveUp(achievements), now, lastUpTime, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> moveDown(achievements), now, lastDownTime, () -> lastDownTime = now);

        if(keyboard.wasLeftPressed()) { previousPage(); selectedIndexY = 0; service.getSound().playFX(2); lastLeftTime = now; }
        if(keyboard.wasRightPressed()) { nextPage(achievements.size()); selectedIndexY = 0; service.getSound().playFX(2); lastRightTime = now; }
    }

    private void boardInput(Keyboard keyboard, long now, MovesManager move) {
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_S)) {
            service.getBoardService().toggleSandboxMode();
            service.getSound().playFX(0);
            return;
        }

        if(BooleanService.canType) { return; }

        if(keyboard.wasCancelPressed()) { move.cancelMove(); service.getSound().playFX(1); }
        if(keyboard.wasSelectPressed()) { activate(GameState.BOARD); service.getSound().playFX(0); }

        repeatKeyCheck(keyboard.wasUpPressed(), () -> move(Direction.UP), now, lastUpTime, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> move(Direction.DOWN), now, lastDownTime, () -> lastDownTime = now);
        repeatKeyCheck(keyboard.wasLeftPressed(), () -> move(Direction.LEFT), now, lastLeftTime, () -> lastLeftTime = now);
        repeatKeyCheck(keyboard.wasRightPressed(), () -> move(Direction.RIGHT), now, lastRightTime, () -> lastRightTime = now);

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Z) && BooleanService.canUndoMoves) {
            move.undoLastMove();
            service.getSound().playFX(0);
            lastRightTime = now;
        }
    }

    private void globalShortcuts(Keyboard keyboard) {
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Q)) System.exit(0);
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_T) && BooleanService.canTheme) { Colors.nextTheme(); service.getSound().playFX(0); }
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_R)) { service.getBoardService().resetBoard(); service.getSound().playFX(0); }
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_H)) { service.getRender().getMovesRender().toggleMoves(); service.getSound().playFX(3); }
    }

    private void repeatKeyCheck(boolean condition, Runnable action, long now, long lastKeyTime, Runnable updateTime) {
        if(condition && now - lastKeyTime >= repeatDelay) { action.run(); updateTime.run(); service.getSound().playFX(1); }
    }
}
