package org.chess.manager;

import org.chess.entities.*;
import org.chess.enums.GameState;
import org.chess.enums.Tint;
import org.chess.gui.Sound;
import org.chess.input.Mouse;
import org.chess.records.Move;
import org.chess.records.Save;
import org.chess.render.MenuRender;
import org.chess.service.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MovesManager {
    private Piece selectedPiece;
    private int moveX = 4;
    private int moveY = 6;
    private ServiceFactory service;
    private Mouse mouse;
    private Sound fx;
    private List<Move> moves;
    private int selectedIndexY;
    private int selectedIndexX;
    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 6;

    public MovesManager() {}

    public void init(ServiceFactory service) {
        this.service = service;
        this.mouse = service.getMouseService();
        this.fx = service.getGuiService().getFx();
        this.moves = new ArrayList<>();
        this.selectedIndexY = 0;
        this.selectedIndexX = 0;
    }

    public List<Move> getMoves() {
        return moves;
    }

    public ServiceFactory getService() {
        return service;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public int getSelectedIndexX() {
        return selectedIndexX;
    }

    public void setSelectedIndexX(int selectedIndexX) {
        this.selectedIndexX = selectedIndexX;
    }

    public int getSelectedIndexY() {
        return selectedIndexY;
    }

    public void setSelectedIndexY(int selectedIndexY) {
        this.selectedIndexY = selectedIndexY;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public void setGuiService(GUIService gui) {
        this.fx = gui.getFx();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public static int getITEMS_PER_PAGE() {
        return ITEMS_PER_PAGE;
    }

    private Sound getFx() {
        if (fx == null) {
            fx = service.getGuiService().getFx();
        }
        return fx;
    }

    public void attemptMove(Piece piece, int targetCol, int targetRow) {
        BooleanService.isLegal = piece.canMove(targetCol, targetRow,
                service.getPieceService().getPieces())
                && !service.getPieceService().wouldLeaveKingInCheck(
                piece, targetCol, targetRow);

        if(!BooleanService.isLegal) {
            PieceService.updatePos(piece);
            return;
        }

        Piece captured = PieceService.getPieceAt(
                targetCol, targetRow, service.getPieceService().getPieces());

        if(captured != null) {
            service.getPieceService().removePiece(captured);
        }

        if(piece instanceof King) {
            executeCastling(piece, targetCol);
        }

        moves.add(new Move(
                piece,
                piece.getRow(),
                piece.getCol(),
                targetCol,
                targetRow,
                piece.getColor(),
                captured
        ));

        PieceService.movePiece(piece, targetCol, targetRow);
        piece.setHasMoved(true);
        fx.playFX(0);

        if(piece instanceof Pawn) {
            executeEnPassant(piece, captured, targetCol, targetRow);
        }

        for(Piece p : service.getPieceService().getPieces()) {
            if(p instanceof Pawn && p.getColor() != piece.getColor()) {
                p.resetEnPassant();
            }
        }

        if(service.getPromotionService().checkPromotion(piece)) {
            BooleanService.isPromotionPending = true;
            service.getPromotionService().setPromotionColor(piece.getColor());
        } else {
            service.getPieceService().switchTurns();
        }

        if (BooleanService.canAIPlay &&
                GameService.getCurrentTurn() == Tint.BLACK) {

            new Thread(() -> {
                Move aiMove = service.getModelService().getAiTurn();
                if (aiMove != null) {
                    SwingUtilities.invokeLater(() ->
                            service.getModelService().executeMove(aiMove));
                }
            }).start();
        }

        if (service.getPieceService().isKingInCheck(GameService.getCurrentTurn())
                && service.getModelService().getAiTurn() == null) {
            BooleanService.isGameOver = true;
        }
    }

    public void keyboardMove() {
        if (selectedPiece == null) {
            for(Piece p : service.getPieceService().getPieces()) {
                if(p.getColor() == GameService.getCurrentTurn()
                        && p.getCol() == moveX
                        && p.getRow() == moveY) {
                    selectedPiece = p;
                    fx.playFX(0);
                    return;
                }
            }
        } else {
            if (!BooleanService.isLegal) {
                return;
            }
            service.getAnimationService().startMove(selectedPiece, moveX, moveY);
            attemptMove(selectedPiece, moveX, moveY);
            selectedPiece.setScale(selectedPiece.getDEFAULT_SCALE());
            selectedPiece = null;
        }
    }

    private void executeCastling(Piece currentPiece, int targetCol) {
        if(!BooleanService.canDoCastling) { return; }
        int colDiff = targetCol - currentPiece.getCol();

        if(Math.abs(colDiff) == 2 && !currentPiece.hasMoved()) {
            int step = (colDiff > 0) ? 1 : -1;
            int rookStartCol = (colDiff > 0) ? 7 : 0;
            int rookTargetCol = (colDiff > 0) ? 5 : 3;

            if(service.getPieceService().isKingInCheck(currentPiece.getColor()) ||
                    service.getPieceService().wouldLeaveKingInCheck(currentPiece,
                            currentPiece.getCol() + step,
                            currentPiece.getRow())) {
                PieceService.updatePos(currentPiece);
                currentPiece = null;
                return;
            }

            boolean pathClear = true;
            for(int c = currentPiece.getCol() + step; c != rookStartCol; c += step) {
                if(PieceService.getPieceAt(c, currentPiece.getRow(),
                        service.getPieceService().getPieces()) != null) {
                    pathClear = false;
                    break;
                }
            }

            if(pathClear) {
                for(Piece p : service.getPieceService().getPieces()) {
                    if(p instanceof Rook &&
                            p.getCol() == rookStartCol &&
                            p.getRow() == currentPiece.getRow() &&
                            !p.hasMoved()) {

                        p.setCol(rookTargetCol);
                        PieceService.updatePos(p);
                        p.setHasMoved(true);
                        break;
                    }
                }
            }
        }
    }

    private void executeEnPassant(Piece currentPiece, Piece captured,
                                  int targetCol, int targetRow) {
        if(!BooleanService.canDoEnPassant) { return; }
        int oldRow = currentPiece.getPreRow();
        int movedSquares = Math.abs(targetRow - oldRow);

        if(captured == null && Math.abs(targetCol - currentPiece.getPreCol()) == 1) {
            int dir = (currentPiece.getColor() == Tint.WHITE) ? -1 : 1;
            if(targetRow - oldRow == dir) {
                for(Piece p : service.getPieceService().getPieces()) {
                    if(p instanceof Pawn &&
                            p != null &&
                            p.getColor() != currentPiece.getColor() &&
                            p.getCol() == targetCol &&
                            p.getRow() == oldRow &&
                            p.isTwoStepsAhead()) {
                        service.getPieceService().removePiece(p);
                        break;
                    }
                }
            }
        }
        currentPiece.setTwoStepsAhead(movedSquares == 2);
    }

    public void updateKeyboardHover() {
        service.getPieceService().setHoveredSquare(moveX, moveY);
        if(selectedPiece == null) {
            Piece hoveredPiece =
                    PieceService.getPieceAt(moveX, moveY,
                            service.getPieceService().getPieces());

            if(hoveredPiece != null &&
                    hoveredPiece.getColor() == GameService.getCurrentTurn()) {
                service.getPieceService().setHoveredPieceKeyboard(hoveredPiece);
            } else {
                service.getPieceService().setHoveredPieceKeyboard(null);
            }
            BooleanService.isLegal = false;
            return;
        }

        BooleanService.isLegal =
                selectedPiece.canMove(moveX, moveY,
                        service.getPieceService().getPieces())
                        && !service.getPieceService().wouldLeaveKingInCheck(
                        selectedPiece, moveX, moveY);
    }

    public void moveUp(List<Save> saves) {
        if (saves.isEmpty()) return;

        MenuRender menu = service.getRender().getMenuRender();
        int itemsPerPage = ITEMS_PER_PAGE;
        int currentPage = menu.getCurrentPage();
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, saves.size());

        selectedIndexY--;
        if (selectedIndexY < startIndex) {
            selectedIndexY = endIndex - 1;
        }

        getFx().playFX(BooleanService.getRandom(1, 2));
    }

    public void moveUp(String[] options) {
        selectedIndexY--;
        getFx().playFX(BooleanService.getRandom(1, 2));
        if(selectedIndexY < 0) {
            selectedIndexY = options.length - 1;
        }
    }

    public void moveUp() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.max(0, moveY - 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void moveLeft(String[] options) {
        service.getRender().getMenuRender().getMenuInput().previousPage();
        getFx().playFX(4);
    }

    public void moveLeft() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.max(0, moveX - 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void moveDown(List<Save> saves) {
        if (saves.isEmpty()) return;

        MenuRender menu = service.getRender().getMenuRender();
        int itemsPerPage = ITEMS_PER_PAGE;
        int currentPage = menu.getCurrentPage();
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, saves.size());

        selectedIndexY++;
        if (selectedIndexY >= endIndex) {
            selectedIndexY = startIndex;
        }

        getFx().playFX(BooleanService.getRandom(1, 2));
    }

    public void moveDown(String[] options) {
        selectedIndexY++;
        getFx().playFX(BooleanService.getRandom(1, 2));
        if(selectedIndexY < 0) {
            selectedIndexY = 0;
        }
    }

    public void moveDown() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.min(7, moveY + 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void moveRight(String[] options) {
        service.getRender().getMenuRender().getMenuInput().nextPage(options);
        getFx().playFX(4);
    }

    public void moveRight() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.min(7, moveX + 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void activate(String saveName) {
        getFx().playFX(3);
        service.getGameService().continueGame(saveName);
    }

    public void activate(GameState state) {
        switch (state) {
            case MENU -> {
                getFx().playFX(3);
                switch(selectedIndexY) {
                    case 0 -> service.getGameService().startNewGame();
                    case 1 -> service.getGameService().loadSaves();
                    case 2 -> service.getGameService().achievementsMenu();
                    case 3 -> service.getGameService().optionsMenu();
                    case 4 -> System.exit(0);
                }
            }
            case SAVES -> {
                List<Save> saves = service.getSaveManager().getSaves();
                if(!saves.isEmpty() && selectedIndexY < saves.size()) {
                    activate(saves.get(selectedIndexY).name());
                }
            }
            case RULES -> {
                getFx().playFX(3);
                if (selectedIndexY == 0) { return; }
                String option = MenuRender.optionsTweaks[selectedIndexY];
                service.getRender().getMenuRender().toggleOption(option);
            }
            case ACHIEVEMENTS -> {
                getFx().playFX(5);
                if (selectedIndexY == 0) { return; }
            }
            case BOARD -> keyboardMove();
        }
    }

    public void undoLastMove(Piece piece) {
        if(!BooleanService.canUndoMoves) { return; }
        Move lastMove = moves.removeLast();
        Piece movedPiece = lastMove.piece();
        movedPiece.setCol(lastMove.fromCol());
        movedPiece.setRow(lastMove.fromRow());
        PieceService.updatePos(movedPiece);
        movedPiece.setHasMoved(false);

        Piece captured = lastMove.captured();
        if (captured != null) {
            service.getPieceService().getPieces().add(captured);
            PieceService.updatePos(captured);
        }
        service.getPieceService().switchTurns();
    }
}
