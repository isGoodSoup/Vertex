package org.vertex.engine.input;

import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.GameMenu;
import org.vertex.engine.enums.GameSettings;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.records.Save;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.PieceService;
import org.vertex.engine.service.ServiceFactory;

import java.util.List;

public class KeyboardInput {
    private int moveX = 4;
    private int moveY = 6;
    private int selectedIndexY;
    private int selectedIndexX;
    private int currentPage;
    private static final int ITEMS_PER_PAGE = 6;

    private ServiceFactory service;

    public KeyboardInput() {
        this.selectedIndexY = 0;
        this.selectedIndexX = 0;
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

            if(piece != null && piece.getColor() == GameService.getCurrentTurn()) {
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
            if (hoveredPiece != null && hoveredPiece.getColor() == GameService.getCurrentTurn()) {
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

    public void moveUp() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.max(0, moveY - 1);
            updateKeyboardHover();
        }
    }

    public void moveUp(Object[] options) {
        selectedIndexY--;
        if (selectedIndexY < 0) {
            selectedIndexY = options.length - 1;
        }
    }

    public void moveUp(List<?> list) {
        if(list.isEmpty()) { return; }

        MenuRender menu = service.getRender().getMenuRender();
        int itemsPerPage = ITEMS_PER_PAGE;
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, list.size());

        selectedIndexY--;
        if(selectedIndexY < startIndex) {
            selectedIndexY = endIndex - 1;
        }
    }

    public void moveLeft() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.max(0, moveX - 1);
            updateKeyboardHover();
        }
    }

    public void moveLeft(Object[] options) {
        MenuRender menu = service.getRender().getMenuRender();
        previousPage();

        int itemsPerPage = 8;
        int newPage = currentPage;
        selectedIndexY = (newPage - 1) * itemsPerPage;
    }

    public void moveDown(List<?> list) {
        if(list.isEmpty()) { return; }

        MenuRender menu = service.getRender().getMenuRender();
        int itemsPerPage = ITEMS_PER_PAGE;
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, list.size());

        selectedIndexY++;
        if(selectedIndexY >= endIndex) {
            selectedIndexY = startIndex;
        }
    }

    public void moveDown() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.min(7, moveY + 1);
            updateKeyboardHover();
        }
    }


    public void moveDown(Object[] options) {
        selectedIndexY++;
        if (selectedIndexY >= options.length) {
            selectedIndexY = 0;
        }
    }

    public void moveRight() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.min(7, moveX + 1);
            updateKeyboardHover();
        }
    }

    public void moveRight(Object[] options) {
        MenuRender menu = service.getRender().getMenuRender();
        nextPage(options);

        int itemsPerPage = 8;
        int newPage = currentPage;
        selectedIndexY = (newPage - 1) * itemsPerPage;
    }

    public void activate(String saveName) {
        service.getGameService().continueGame(saveName);
    }

    public void activate(GameState state) {
        switch (state) {
            case MENU -> {
                GameMenu[] options = MenuRender.MENU;
                if(selectedIndexY >= 0 && selectedIndexY < options.length) {
                    GameMenu selected = options[selectedIndexY];
                    if (selected.isEnabled(service.getGameService())) {
                        selected.run(service.getGameService());
                    }
                }
            }
            case SAVES -> {
                List<Save> saves = service.getSaveManager().getSaves();
                if(!saves.isEmpty() && selectedIndexY < saves.size()) {
                    activate(saves.get(selectedIndexY).name());
                }
            }
            case RULES -> {
                int absIndex = currentPage * ITEMS_PER_PAGE + selectedIndexY;
                if(absIndex < MenuRender.SETTINGS_MENU.length) {
                    GameSettings option = MenuRender.SETTINGS_MENU[absIndex];
                    option.toggle();
                }
            }
            case ACHIEVEMENTS -> BooleanService.canZoomIn ^= true;
            case BOARD -> keyboardMove();
        }
    }

    public void previousPage() {
        currentPage = Math.max(0, currentPage - 1);
    }

    public void nextPage() {
        int itemsPerPage = ITEMS_PER_PAGE;
        int totalItems = service.getAchievementService().getAllAchievements().size();
        int totalPages = (totalItems + itemsPerPage - 1)/itemsPerPage;

        if(currentPage < 0) { currentPage = 0; }

        if(currentPage < totalPages - 1) {
            currentPage++;
        }
    }

    public void nextPage(Object[] options) {
        int itemsPerPage = ITEMS_PER_PAGE;
        int totalPages = (options.length + itemsPerPage - 1)/itemsPerPage;

        if(currentPage < 0) { currentPage = 0; }

        if(currentPage < totalPages - 1) {
            currentPage++;
        }
    }
}
