package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.GameState;
import org.chess.enums.Tint;
import org.chess.input.Mouse;
import org.chess.input.MoveManager;
import org.chess.gui.Sound;
import org.chess.records.Move;

import java.util.*;

public class BoardService {
    private static Piece[][] boardState;
    private final Board board;
    private final Sound fx;
    private final List<Move> moves;

    private final PieceService pieceService;
    private final Mouse mouse;
    private final PromotionService promotionService;
    private final ModelService modelService;
    private static MoveManager manager;

    public BoardService(PieceService pieceService, Mouse mouse,
                        PromotionService promotionService,
                        ModelService modelService,
                        MoveManager manager) {
        this.board = new Board();
        this.fx = new Sound();
        this.pieceService = pieceService;
        this.mouse = mouse;
        this.promotionService = promotionService;
        this.modelService = modelService;
        BoardService.manager = manager;
        boardState = new Piece[board.getROW()][board.getCOL()];
        this.moves = new ArrayList<>();
        precomputeSquares();
    }

    public Board getBoard() {
        return board;
    }

    public static Piece[][] getBoardState() {
        return boardState;
    }

    public static MoveManager getManager() {
        return manager;
    }

    public List<Move> getMoves() {
        return moves;
    }

    private void precomputeSquares() {
        for(int row = 0; row < Objects.requireNonNull(board).getROW(); row++) {
            for(int col = 0; col < board.getCOL(); col++) {
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
        pieceService.getPieces().clear();
        if(BooleanService.isTestingToggle) { setPiecesTest(); }
        else if(BooleanService.isChaosActive) { setPiecesChaos(); }
        else{ setPieces(); }
        GameService.setCurrentTurn(Tint.WHITE);
        PieceService.nullThisPiece();
        GameService.setState(GameState.BOARD);
    }

    public void setPieces() {
        List<Piece> pieces = pieceService.getPieces();
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

    public void setPiecesChaos() {
        if(!BooleanService.isChaosActive) {
            return;
        }

        List<Piece> pieces = pieceService.getPieces();
        pieces.clear();
        clearBoardState();

        for(int col = 0; col < 8; col++) {
            pieces.add(pieceService.getRandomPiece(Tint.WHITE, col, 6));
            pieces.add(pieceService.getRandomPiece(Tint.BLACK, col, 1));
        }

        pieces.add(pieceService.getRandomPiece(Tint.WHITE, 0, 7));
        pieces.add(pieceService.getRandomPiece(Tint.BLACK, 7, 7));
        pieces.add(pieceService.getRandomPiece(Tint.WHITE, 0, 0));
        pieces.add(pieceService.getRandomPiece(Tint.BLACK,7, 0));
        pieces.add(pieceService.getRandomPiece(Tint.WHITE, 1, 7));
        pieces.add(pieceService.getRandomPiece(Tint.BLACK,6, 7));
        pieces.add(pieceService.getRandomPiece(Tint.WHITE, 1, 0));
        pieces.add(pieceService.getRandomPiece(Tint.BLACK,6, 0));

        pieces.add(pieceService.getRandomPiece(Tint.WHITE, 2, 7));
        pieces.add(pieceService.getRandomPiece(Tint.BLACK,5, 7));
        pieces.add(pieceService.getRandomPiece(Tint.WHITE, 2, 0));
        pieces.add(pieceService.getRandomPiece(Tint.BLACK,5, 0));

        pieces.add(pieceService.getRandomPiece(Tint.WHITE, 3, 7));
        pieces.add(pieceService.getRandomPiece(Tint.BLACK,3, 0));

        pieces.add(new King(pieceService, Tint.WHITE, 4, 7));
        pieces.add(new King(pieceService, Tint.BLACK, 4, 0));
    }

    private void setPiecesTest() {
        List<Piece> pieces = pieceService.getPieces();
        pieces.clear();
        clearBoardState();

        pieces.add(new Pawn(Tint.WHITE, 0, 2));
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
        if(BooleanService.isDragging && PieceService.getPiece() == null) {
            BooleanService.isDragging = false;
        }

        Piece currentPiece = PieceService.getPiece();
        int boardMouseX = mouse.getX() - GUIService.getEXTRA_WIDTH();
        int hoverCol = boardMouseX / Board.getSquare();
        int hoverRow = mouse.getY() / Board.getSquare();
        checkPiece(currentPiece, hoverCol, hoverRow);
    }

    private void checkPiece(Piece currentPiece, int hoverCol, int hoverRow) {
        if(GameService.getState() != GameState.BOARD) { return; }

        if(BooleanService.isAIPlaying &&
                GameService.getCurrentTurn() == Tint.BLACK) return;

        manager.pickUpPiece(currentPiece, hoverCol, hoverRow);
    }
}
