package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.GameState;
import org.chess.enums.Tint;
import org.chess.gui.Mouse;
import org.chess.gui.Sound;
import org.chess.records.Move;

import java.util.List;
import java.util.Objects;

public class BoardService {
    private static Piece[][] boardState;
    private final Board board;
    private final Sound fx;

    private final PieceService pieceService;
    private final Mouse mouse;
    private final PromotionService promotionService;
    private final ModelService modelService;

    public BoardService(PieceService pieceService, Mouse mouse,
                        PromotionService promotionService,
                        ModelService modelService) {
        this.board = new Board();
        this.fx = new Sound();
        this.pieceService = pieceService;
        this.mouse = mouse;
        this.promotionService = promotionService;
        this.modelService = modelService;
        boardState = new Piece[board.getROW()][board.getCOL()];
        precomputeSquares();
    }

    public Board getBoard() {
        return board;
    }

    public static Piece[][] getBoardState() {
        return boardState;
    }

    private void precomputeSquares() {
        for (int row = 0; row < Objects.requireNonNull(board).getROW(); row++) {
            for (int col = 0; col < board.getCOL(); col++) {
                board.getSquares()[row][col] = getSquareName(col, row);
            }
        }
    }

    public static String getSquareName(int col, int row) {
        char file = (char) ('A' + col);
        int rank = 8 - row;
        return "" + file + rank;
    }

    public String getSquareNameAt(int col, int row) {
        return board.getSquares()[row][col];
    }

    public void startBoard() {
        PieceService.getPieces().clear();
        setPieces();
        pieceService.switchTurns();
        BooleanService.isGameOver = false;
        PieceService.nullThisPiece();
        GameService.setState(GameState.BOARD);
    }

    public void setPieces() {
        List<Piece> pieces = PieceService.getPieces();
        pieces.clear();
        clearBoardState();

        for(int col = 0; col < 8; col++) {
            pieces.add(new Pawn(Tint.WHITE, col, 6));
            pieces.add(new Pawn(Tint.BLACK, col, 1));
        }
        pieces.add(new Rook(Tint.WHITE, 0, 7));
        pieces.add(new Rook(Tint.WHITE, 7, 7));
        pieces.add(new Rook(Tint.BLACK, 0, 0));
        pieces.add(new Rook(Tint.BLACK, 7, 0));
        pieces.add(new Knight(Tint.WHITE, 1, 7));
        pieces.add(new Knight(Tint.WHITE, 6, 7));
        pieces.add(new Knight(Tint.BLACK, 1, 0));
        pieces.add(new Knight(Tint.BLACK, 6, 0));
        pieces.add(new Bishop(Tint.WHITE, 2, 7));
        pieces.add(new Bishop(Tint.WHITE, 5, 7));
        pieces.add(new Bishop(Tint.BLACK, 2, 0));
        pieces.add(new Bishop(Tint.BLACK, 5, 0));
        pieces.add(new Queen(Tint.WHITE, 3, 7));
        pieces.add(new Queen(Tint.BLACK, 3, 0));
        pieces.add(new King(pieceService, Tint.WHITE, 4, 7));
        pieces.add(new King(pieceService, Tint.BLACK, 4, 0));
    }

    private void clearBoardState() {
        for(int c = 0; c < 8; c++) {
            for(int r = 0; r < 8; r++) {
                assert boardState[c] != null;
                boardState[c][r] = null;
            }
        }
    }

    public void getGame() {
        Piece currentPiece = PieceService.getPiece();
        if(GameService.getState() != GameState.BOARD) {
            return;
        }

        if(BooleanService.isAIPlaying &&
                GameService.getCurrentTurn() == Tint.BLACK) {
            return;
        }

        int hoverCol = mouse.getX() / Board.getSquare();
        int hoverRow = mouse.getY() / Board.getSquare();

        if(BooleanService.isPromotionPending) {
            promotionService.promotion();
            mouse.setClicked(false);
        }

        if(mouse.isClicked() && !BooleanService.isDragging && currentPiece == null) {
            for(Piece p : PieceService.getPieces()) {
                if(p.getColor() == GameService.getCurrentTurn() &&
                        p.getCol() == hoverCol &&
                        p.getRow() == hoverRow) {
                    currentPiece = p;
                    currentPiece.setScale(currentPiece.getDEFAULT_SCALE()
                            + currentPiece.getMORE_SCALE());
                    PieceService.getPiece().setDragOffsetX(mouse.getX() - p.getX());
                    PieceService.getPiece().setDragOffsetY(mouse.getY() - p.getY());
                    currentPiece.setPreCol(p.getCol());
                    currentPiece.setPreRow(p.getRow());
                    break;
                }
            }
        }

        if(mouse.isHeld() && !BooleanService.isDragging && currentPiece == null) {
            for(Piece p : PieceService.getPieces()) {
                if(p.getColor() == GameService.getCurrentTurn() &&
                        p.getCol() == hoverCol &&
                        p.getRow() == hoverRow) {
                    currentPiece = p;
                    currentPiece.setScale(currentPiece.getDEFAULT_SCALE()
                            + currentPiece.getMORE_SCALE());
                    BooleanService.isDragging = true;
                    PieceService.setPiece(currentPiece);
                    PieceService.getPiece().setDragOffsetX(mouse.getX() - p.getX());
                    PieceService.getPiece().setDragOffsetY(mouse.getY() - p.getY());
                    currentPiece.setPreCol(p.getCol());
                    currentPiece.setPreRow(p.getRow());
                    break;
                }
            }
        }

        if(BooleanService.isDragging && mouse.isHeld() && currentPiece != null) {
            currentPiece.setX(mouse.getX() - PieceService.getPiece().getDragOffsetX());
            currentPiece.setY(mouse.getY() - PieceService.getPiece().getDragOffsetY());
            int targetCol = mouse.getX() / Board.getSquare();
            int targetRow = mouse.getY() / Board.getSquare();

            BooleanService.isLegal = currentPiece.canMove(targetCol,
                    targetRow, PieceService.getPieces()) &&
                    !pieceService.wouldLeaveKingInCheck(currentPiece,
                            targetCol, targetRow);
        }

        if(BooleanService.isDragging && mouse.isClicked() && currentPiece != null) {
            BooleanService.isDragging = false;
            int targetCol = mouse.getX() / Board.getSquare();
            int targetRow = mouse.getY() / Board.getSquare();

            if(BooleanService.isLegal) {
                Piece captured = PieceService.getPieceAt(targetCol, targetRow,
                        PieceService.getPieces());
                if(captured != null) {
                    pieceService.removePiece(captured);
                }

                if (currentPiece instanceof King) {
                    int colDiff = targetCol - currentPiece.getCol();

                    if (Math.abs(colDiff) == 2 && !currentPiece.hasMoved()) {
                        int step = (colDiff > 0) ? 1 : -1;
                        int rookStartCol = (colDiff > 0) ? 7 : 0;
                        int rookTargetCol = (colDiff > 0) ? 5 : 3;

                        if (pieceService.isKingInCheck(currentPiece.getColor()) ||
                                pieceService.wouldLeaveKingInCheck(currentPiece,
                                        currentPiece.getCol() + step,
                                        currentPiece.getRow())) {
                            PieceService.updatePos(currentPiece);
                            currentPiece = null;
                            return;
                        }

                        boolean pathClear = true;
                        for(int c = currentPiece.getCol() + step; c != rookStartCol; c += step) {
                            if(PieceService.getPieceAt(c, currentPiece.getRow(),
                                    PieceService.getPieces()) != null) {
                                pathClear = false;
                                break;
                            }
                        }

                        if(pathClear) {
                            for(Piece p : PieceService.getPieces()) {
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

                PieceService.movePiece(currentPiece, targetCol, targetRow);
                currentPiece.setHasMoved(true);
                fx.playFX(0);

                if (currentPiece instanceof Pawn) {
                    int oldRow = currentPiece.getPreRow();
                    int movedSquares = Math.abs(targetRow - oldRow);

                    if (captured == null && Math.abs(targetCol - currentPiece.getPreCol()) == 1) {
                        int dir = (currentPiece.getColor() == Tint.WHITE) ? -1 : 1;
                        if (targetRow - oldRow == dir) {
                            for (Piece p : PieceService.getPieces()) {
                                if (p instanceof Pawn &&
                                        p.getColor() != currentPiece.getColor() &&
                                        p.getCol() == targetCol &&
                                        p.getRow() == oldRow &&
                                        p.isTwoStepsAhead()) {
                                    PieceService.getPieces().remove(p);
                                    break;
                                }
                            }
                        }
                    }
                    currentPiece.setTwoStepsAhead(movedSquares == 2);
                }

                for (Piece p : PieceService.getPieces()) {
                    if (p instanceof Pawn && p.getColor() != currentPiece.getColor()) {
                        p.resetEnPassant();
                    }
                }

                if (promotionService.checkPromotion(currentPiece)) {
                    BooleanService.isPromotionPending = true;
                    promotionService.setPromotionColor(currentPiece.getColor());
                } else {
                    pieceService.switchTurns();

                    if (currentPiece != null) {
                        currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
                        currentPiece = null;
                    }

                    if (BooleanService.isAIPlaying && !BooleanService.isPromotionPending) {
                        Move move = modelService.getAiTurn();
                        if (move != null) {
                            modelService.executeMove(move);
                        }
                    }

                    if (pieceService.isKingInCheck(
                            GameService.getCurrentTurn()) &&
                            modelService.getAiTurn() == null) {
                        BooleanService.isGameOver = true;
                    }

                }

            } else {
                PieceService.updatePos(currentPiece);
            }
            if(currentPiece != null) {
                currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
                currentPiece = null;
            }
        }
    }
}
