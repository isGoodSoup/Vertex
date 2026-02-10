package org.chess.input;

import org.chess.entities.*;
import org.chess.enums.GameState;
import org.chess.enums.Tint;
import org.chess.gui.Sound;
import org.chess.records.Move;
import org.chess.render.MenuRender;
import org.chess.service.*;

import javax.swing.*;
import java.util.List;

public class MoveManager {
    private Piece selectedPiece;
    private int moveX = 4;
    private int moveY = 6;
    private ServiceFactory service;
    private Mouse mouse;
    private Sound fx;
    private List<Move> moves;
    private int selectedIndex;

    public MoveManager() {}

    public void init(ServiceFactory service) {
        this.service = service;
        this.mouse = service.getMouseService();
        this.fx = service.getGuiService().getFx();
        this.moves = service.getBoardService().getMoves();
        this.selectedIndex = service.getManager().getSelectedIndex();
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public void setGuiService(GUIService gui) {
        this.fx = gui.getFx();
    }

    private Sound getFx() {
        if (fx == null) {
            fx = service.getGuiService().getFx();
        }
        return fx;
    }

    public Piece pickUpPiece(Piece currentPiece, int hoverCol, int hoverRow) {
        if(mouse.wasPressed()
                && !BooleanService.isDragging
                && currentPiece == null) {

            for(Piece p : service.getPieceService().getPieces()) {
                if(p.getColor() == GameService.getCurrentTurn()
                        && p.getCol() == hoverCol
                        && p.getRow() == hoverRow) {

                    p.setPreCol(p.getCol());
                    p.setPreRow(p.getRow());
                    p.setScale(p.getDEFAULT_SCALE() + p.getMORE_SCALE());

                    BooleanService.isDragging = true;
                    PieceService.setPiece(p);
                    break;
                }
            }
        }

        dragPiece(PieceService.getPiece());
        return PieceService.getPiece();
    }

    private void dragPiece(Piece currentPiece) {
        if(!BooleanService.isDragging || currentPiece == null) { return; }

        int boardMouseX = mouse.getX() - GUIService.getEXTRA_WIDTH();
        currentPiece.setX(boardMouseX - currentPiece.getDragOffsetX());
        currentPiece.setY(mouse.getY() - currentPiece.getDragOffsetY());
        int targetCol = boardMouseX / Board.getSquare();
        int targetRow = mouse.getY() / Board.getSquare();

        BooleanService.isLegal =
                currentPiece.canMove(targetCol, targetRow, service.getPieceService().getPieces())
                        && !service.getPieceService().wouldLeaveKingInCheck(
                        currentPiece, targetCol, targetRow);

        dropPiece(currentPiece);
    }

    public void attemptMove(Piece piece, int targetCol, int targetRow) {
        BooleanService.isLegal = piece.canMove(targetCol, targetRow,
                service.getPieceService().getPieces()) && !service.getPieceService().wouldLeaveKingInCheck(
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
                piece.getCol(),
                piece.getRow(),
                targetCol,
                targetRow,
                piece.getColor()
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

        if (BooleanService.isAIPlaying &&
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

    private Piece dropPiece(Piece currentPiece) {
        int boardMouseX = mouse.getX() - GUIService.getEXTRA_WIDTH();

        if(BooleanService.isDragging && mouse.wasReleased()
                && currentPiece != null) {

            BooleanService.isDragging = false;
            if(boardMouseX < 0 || boardMouseX >= Board.getSquare() * 8) {
                PieceService.updatePos(currentPiece);
                return currentPiece;
            }

            int targetCol = boardMouseX / Board.getSquare();
            int targetRow = mouse.getY() / Board.getSquare();

            attemptMove(currentPiece, targetCol, targetRow);

            currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
            PieceService.nullThisPiece();
        }
        return currentPiece;
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
            attemptMove(selectedPiece, moveX, moveY);
            selectedPiece.setScale(selectedPiece.getDEFAULT_SCALE());
            selectedPiece = null;
        }
    }

    private void executeCastling(Piece currentPiece, int targetCol) {
        if(!BooleanService.isCastlingActive) { return; }
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
        if(!BooleanService.isEnPassantActive) { return; }
        int oldRow = currentPiece.getPreRow();
        int movedSquares = Math.abs(targetRow - oldRow);

        if(captured == null && Math.abs(targetCol - currentPiece.getPreCol()) == 1) {
            int dir = (currentPiece.getColor() == Tint.WHITE) ? -1 : 1;
            if(targetRow - oldRow == dir) {
                for(Piece p : service.getPieceService().getPieces()) {
                    if(p instanceof Pawn &&
                            p.getColor() != currentPiece.getColor() &&
                            p.getCol() == targetCol &&
                            p.getRow() == oldRow &&
                            p.isTwoStepsAhead()) {
                        service.getPieceService().getPieces().remove(p);
                        break;
                    }
                }
            }
        }
        currentPiece.setTwoStepsAhead(movedSquares == 2);
    }

    public void updateKeyboardHover() {
        service.getPieceService().setHoveredSquare(moveX, moveY);
        Piece p = PieceService.getPieceAt(moveX, moveY, service.getPieceService().getPieces());
        if (p != null && p.getColor() == GameService.getCurrentTurn()) {
            service.getPieceService().setHoveredPieceKeyboard(p);
        } else {
            service.getPieceService().setHoveredPieceKeyboard(null);
        }
    }

    public void moveUp() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.max(0, moveY - 1);
            updateKeyboardHover();
        }
    }

    public void moveUp(String[] options) {
        selectedIndex--;
        service.getGuiService().getFx().play(BooleanService.getRandom(1, 2));
        if(selectedIndex < 0) {
            selectedIndex = options.length - 1;
        }
    }

    public void moveLeft() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.max(0, moveX - 1);
            updateKeyboardHover();
        }
    }

    public void moveDown() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.min(7, moveY + 1);
            updateKeyboardHover();
        }
    }

    public void moveDown(String[] options) {
        selectedIndex++;
        service.getGuiService().getFx().play(BooleanService.getRandom(1, 2));
        if(selectedIndex >= options.length) {
            selectedIndex = 0;
        }
    }

    public void moveRight() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.min(7, moveX + 1);
            updateKeyboardHover();
        }
    }

    public void activate(GameState state) {
        service.getGuiService().getFx().play(0);
        switch (state) {
            case MENU -> {
                switch (selectedIndex) {
                    case 0 -> service.getGameService().startNewGame();
                    case 1 -> service.getGameService().optionsMenu();
                    case 2 -> System.exit(0);
                }
            }
            case MODE -> {
                switch (selectedIndex) {
                    case 0 -> service.getBoardService().startBoard();
                    case 1 -> {
                        BooleanService.isAIPlaying = true;
                        service.getBoardService().startBoard();
                    }
                }
            }
            case RULES -> {
                if (selectedIndex == 0) { return; }
                String option = MenuRender.optionsTweaks[selectedIndex];
                service.getGuiService().getFx().play(0);
                service.getGuiService().getMenuRender().toggleOption(option);
            }
            case BOARD -> keyboardMove();
        }
    }
}
