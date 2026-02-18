package org.vertex.engine.input;

import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.*;
import org.vertex.engine.events.ToggleEvent;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.gui.GameFrame;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.PieceService;
import org.vertex.engine.service.ServiceFactory;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

public class KeyboardInput {
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

    public void keyboardMove() {
        Piece selectedPiece = service.getMovesManager().getSelectedPiece();
        if(selectedPiece == null) {
            Piece piece = PieceService.getPieceAt(moveX, moveY,
                    service.getPieceService().getPieces());

            if(piece != null && piece.getColor() == service.getGameService().getCurrentTurn()) {
                service.getMovesManager().setSelectedPiece(piece);
            }
        } else {
            updateKeyboardHover();
            if(BooleanService.isLegal) {
                service.getAnimationService().startMove(selectedPiece, moveX, moveY);
                service.getMovesManager().attemptMove(selectedPiece, moveX, moveY);
                service.getMovesManager().setSelectedPiece(null);
            }
        }
    }

    public void updateKeyboardHover() {
        Piece selectedPiece = service.getMovesManager().getSelectedPiece();
        service.getPieceService().setHoveredSquare(moveX, moveY);
        if (selectedPiece == null) {
            Piece hoveredPiece = PieceService.getPieceAt(moveX, moveY,
                    service.getPieceService().getPieces());
            if (hoveredPiece != null && hoveredPiece.getColor() == service.getGameService().getCurrentTurn()) {
                service.getPieceService().setHoveredPieceKeyboard(hoveredPiece);
            } else {
                service.getPieceService().setHoveredPieceKeyboard(null);
            }
            BooleanService.isLegal = false;
        } else {
            BooleanService.isLegal = selectedPiece.canMove(moveX, moveY,
                    service.getPieceService().getPieces())
                    && !service.getPieceService().wouldLeaveKingInCheck(selectedPiece, moveX, moveY);
        }
    }

    public void move(Direction dir) {
        if(service.getGameService().getState() != GameState.BOARD) return;

        switch(dir) {
            case UP -> moveY = Math.max(0, moveY - 1);
            case DOWN -> moveY = Math.min(7, moveY + 1);
            case LEFT -> moveX = Math.max(0, moveX - 1);
            case RIGHT -> moveX = Math.min(7, moveX + 1);
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
            case RULES -> activateRules();
            case ACHIEVEMENTS -> BooleanService.canZoomIn ^= true;
            case BOARD -> keyboardMove();
        }
    }

    private void activateMenu() {
        if(selectedIndexY >= 0 && selectedIndexY < MenuRender.MENU.length) {
            GameMenu selected = MenuRender.MENU[selectedIndexY];
            if(selected.isEnabled(service.getGameService())) selected.run(service.getGameService());
        }
    }

    private void activateRules() {
        int absIndex = currentPage * ITEMS_PER_PAGE + selectedIndexY;
        if(absIndex < MenuRender.SETTINGS_MENU.length) {
            MenuRender.SETTINGS_MENU[absIndex].toggle();
        }
    }

    public void update() {
        long now = System.currentTimeMillis();
        Keyboard keyboard = service.getKeyboard();
        MovesManager move = service.getMovesManager();

        if(keyboard.isEscapeDown()) {
            service.getGameService().setState(GameState.MENU);
            selectedIndexY = 0;
            service.getSound().playFX(2);
        }

        handleColorblindKeys(keyboard);
        sandboxCommands(keyboard);

        switch(service.getGameService().getState()) {
            case MENU -> menuInput(keyboard, now);
            case RULES -> rulesInput(keyboard, now);
            case ACHIEVEMENTS -> achievementsInput(keyboard, now);
            case BOARD -> boardInput(keyboard, now, move);
        }

        globalShortcuts(keyboard);

        if(keyboard.wasF11Pressed()) {
            gameFrame.toggleFullscreen();
        }
    }

    private void handleColorblindKeys(Keyboard keyboard) {
        if(!BooleanService.canBeColorblind) return;

        if(keyboard.wasOnePressed()) MenuRender.setCb(ColorblindType.PROTANOPIA);
        if(keyboard.wasTwoPressed()) MenuRender.setCb(ColorblindType.DEUTERANOPIA);
        if(keyboard.wasThreePressed()) MenuRender.setCb(ColorblindType.TRITANOPIA);
    }

    private void sandboxCommands(Keyboard keyboard) {
        if(!BooleanService.isSandboxEnabled) return;

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_ENTER)) {
            String fullInput = keyboard.consumeText().trim();
            if(fullInput.isEmpty()) return;

            String[] parts = fullInput.split("\\s+");
            String commandName = parts[0];
            String[] args = Arrays.copyOfRange(parts, 1, parts.length);
            Console consoleCommand = Console.fromString(commandName);
            if(consoleCommand != null) consoleCommand.run(service, args);
            else System.out.println("Unknown command: " + commandName);
        }
    }

    private void menuInput(Keyboard keyboard, long now) {
        if(keyboard.wasSelectPressed()) {
            activate(GameState.MENU);
            service.getSound().playFX(3);
        }
        repeatKeyCheck(keyboard.wasUpPressed(), () -> moveUp(Arrays.asList(MenuRender.MENU)), now, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> moveDown(Arrays.asList(MenuRender.MENU)), now, () -> lastDownTime = now);

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_G)) {
            service.getGameService().nextGame();
            service.getSound().playFX(0);
            service.getEventBus().fire(new ToggleEvent());
        }
    }

    private void rulesInput(Keyboard keyboard, long now) {
        if(keyboard.wasSelectPressed()) { activate(GameState.RULES); service.getSound().playFX(0); }

        repeatKeyCheck(keyboard.wasUpPressed(), () -> moveUp(Arrays.asList(MenuRender.SETTINGS_MENU)), now, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> moveDown(Arrays.asList(MenuRender.SETTINGS_MENU)), now, () -> lastDownTime = now);

        if(keyboard.wasLeftPressed()) { previousPage(); service.getSound().playFX(2); lastLeftTime = now; }
        if(keyboard.wasRightPressed()) { nextPage(MenuRender.SETTINGS_MENU); service.getSound().playFX(2); lastRightTime = now; }
    }

    private void achievementsInput(Keyboard keyboard, long now) {
        if(keyboard.wasSelectPressed()) { activate(GameState.ACHIEVEMENTS); service.getSound().playFX(3); }

        List<?> achievements = service.getAchievementService().getAchievementList();
        repeatKeyCheck(keyboard.wasUpPressed(), () -> moveUp(achievements), now, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> moveDown(achievements), now, () -> lastDownTime = now);

        if(keyboard.wasLeftPressed()) { previousPage(); selectedIndexY = 0; service.getSound().playFX(2); lastLeftTime = now; }
        if(keyboard.wasRightPressed()) { nextPage(achievements.size()); selectedIndexY = 0; service.getSound().playFX(2); lastRightTime = now; }
    }

    private void boardInput(Keyboard keyboard, long now, MovesManager move) {
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_S) && BooleanService.isSandboxEnabled) {
            BooleanService.canDoSandbox ^= true;
            BooleanService.canType ^= true;
            service.getSound().playFX(0);
        }
        if(BooleanService.canDoSandbox) return;

        if(keyboard.wasCancelPressed()) { move.cancelMove(); service.getSound().playFX(1); }
        if(keyboard.wasSelectPressed()) { activate(GameState.BOARD); service.getSound().playFX(0); }

        repeatKeyCheck(keyboard.wasUpPressed(), () -> move(Direction.UP), now, () -> lastUpTime = now);
        repeatKeyCheck(keyboard.wasDownPressed(), () -> move(Direction.DOWN), now, () -> lastDownTime = now);
        repeatKeyCheck(keyboard.wasLeftPressed(), () -> move(Direction.LEFT), now, () -> lastLeftTime = now);
        repeatKeyCheck(keyboard.wasRightPressed(), () -> move(Direction.RIGHT), now, () -> lastRightTime = now);

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Z) && BooleanService.canUndoMoves) {
            move.undoLastMove(move.getSelectedPiece());
            service.getSound().playFX(3);
            lastRightTime = now;
        }
    }

    private void globalShortcuts(Keyboard keyboard) {
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Q)) System.exit(0);
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_T) && BooleanService.canTheme) { Colors.nextTheme(); service.getSound().playFX(0); }
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_R)) { service.getBoardService().resetBoard(); service.getSound().playFX(0); }
        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_H)) { service.getRender().getMovesRender().toggleMoves(); service.getSound().playFX(3); }
    }

    private void repeatKeyCheck(boolean condition, Runnable action, long now, Runnable updateTime) {
        if(condition && now - repeatDelay >= 0) { action.run(); updateTime.run(); service.getSound().playFX(1); }
    }
}
