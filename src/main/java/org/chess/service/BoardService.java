package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;
import org.chess.input.Mouse;
import org.chess.manager.MovesManager;
import org.chess.gui.Sound;
import org.chess.manager.SaveManager;
import org.chess.records.Save;

import java.util.*;

public class BoardService {
    private static Piece[][] boardState;
    private Board board;
    private transient Sound fx;
    private String[][] squares;
    private final Map<List<Integer>, List<Integer>> columns;

    private final PieceService pieceService;
    private final Mouse mouse;
    private final PromotionService promotionService;
    private final ModelService modelService;
    private static MovesManager movesManager;
    private static SaveManager saveManager;

    private ServiceFactory serviceFactory;

    public BoardService(PieceService pieceService, Mouse mouse,
                        PromotionService promotionService,
                        ModelService modelService,
                        MovesManager movesManager) {
        this.board = new Board();
        this.fx = new Sound();
        this.pieceService = pieceService;
        this.mouse = mouse;
        this.promotionService = promotionService;
        this.modelService = modelService;
        BoardService.movesManager = movesManager;
        boardState = new Piece[board.getROW()][board.getCOL()];
        this.columns = new HashMap<>();
        precomputeSquares();
    }

    public ServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public void setServiceFactory(ServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
    }

    public static Piece[][] getBoardState() {
        return boardState;
    }

    public static MovesManager getMovesManager() {
        return movesManager;
    }

    public Map<List<Integer>, List<Integer>> getColumns() {
        return columns;
    }

    private void precomputeSquares() {
        int rows = board.getROW();
        int cols = board.getCOL();
        squares = new String[rows][cols];

        for(int r = 0; r < rows; r++) {
            for(int c = 0; c < cols; c++) {
                char file = (char) ('a' + c);
                char rank = (char) ('8' - r);
                squares[r][c] = "" + file + rank;
            }
        }
    }

    public void restoreSprites(Save save, GUIService guiService) {
        List<Piece> loadedPieces = save.pieces();
        Piece[][] boardArray = new Piece[board.getROW()][board.getCOL()];
        for (Piece p : loadedPieces) {
            if (p == null) continue;
            int col = p.getCol();
            int row = p.getRow();
            p.setPreCol(p.getPreCol());
            p.setPreRow(p.getPreRow());
            p.setX(col * Board.getSquare());
            p.setY(row * Board.getSquare());
            p.loadSprite(guiService);
            boardArray[row][col] = p;
        }
        board.setPieces(boardArray);
        pieceService.getPieces().clear();
        pieceService.getPieces().addAll(loadedPieces);
    }

    private void clearBoardState() {
        boardState = new Piece[board.getROW()][board.getCOL()];
        pieceService.getPieces().clear();
    }

    public String getSquareNameAt(int row, int col) {
        return squares[row][col];
    }

    public void startBoard() {
        if(BooleanService.canSandbox) { setSandboxPieces(); }
        else if(BooleanService.canDoChaos) { setPiecesChaos(); }
        else { setPieces(); }
        PieceService.nullThisPiece();

        if(BooleanService.canStopwatch) {
            BooleanService.canTime = false;
            getServiceFactory().getTimerService().reset();
            getServiceFactory().getTimerService().start();
        }

        if(BooleanService.canTime) {
            BooleanService.canStopwatch = false;
            getServiceFactory().getTimerService().reset();
            getServiceFactory().getTimerService().start();
        }
    }

    private void getPiecesDebug() {
        System.out.println("Initializing pieces...");
        pieceService.getPieces().forEach(p -> System.out.println(p.getId()));
    }

    public void resetBoard() {
        if(BooleanService.canResetTable
                && getServiceFactory().getKeyboard().wasRPressed()) {
            getServiceFactory().getGameService().startNewGame();
        }
    }

    public void setPieces() {
        List<Piece> pieces = pieceService.getPieces();
        pieces.clear();
        clearBoardState();

        for(int col = 0; col < 8; col++) {
            Pawn whitePawn = new Pawn(Tint.WHITE, col, 6);
            Pawn blackPawn = new Pawn(Tint.BLACK, col, 1);
            pieces.add(whitePawn);
            pieces.add(blackPawn);
        }

        Rook wR1 = new Rook(Tint.WHITE, 0, 7);
        Rook wR2 = new Rook(Tint.WHITE, 7, 7);
        Rook bR1 = new Rook(Tint.BLACK, 0, 0);
        Rook bR2 = new Rook(Tint.BLACK, 7, 0);
        pieces.addAll(List.of(wR1, wR2, bR1, bR2));

        Knight wN1 = new Knight(Tint.WHITE, 1, 7);
        Knight wN2 = new Knight(Tint.WHITE, 6, 7);
        Knight bN1 = new Knight(Tint.BLACK, 1, 0);
        Knight bN2 = new Knight(Tint.BLACK, 6, 0);
        pieces.addAll(List.of(wN1, wN2, bN1, bN2));

        Bishop wB1 = new Bishop(Tint.WHITE, 2, 7);
        Bishop wB2 = new Bishop(Tint.WHITE, 5, 7);
        Bishop bB1 = new Bishop(Tint.BLACK, 2, 0);
        Bishop bB2 = new Bishop(Tint.BLACK, 5, 0);
        pieces.addAll(List.of(wB1, wB2, bB1, bB2));

        Queen wQ = new Queen(Tint.WHITE, 3, 7);
        Queen bQ = new Queen(Tint.BLACK, 3, 0);
        pieces.add(wQ);
        pieces.add(bQ);

        King wK = new King(pieceService, Tint.WHITE, 4, 7);
        King bK = new King(pieceService, Tint.BLACK, 4, 0);
        pieces.add(wK);
        pieces.add(bK);

        for (Piece p : pieces) {
            boardState[p.getRow()][p.getCol()] = p;
            int squareSize = Board.getSquare();
            p.setX(p.getCol() * squareSize);
            p.setY(p.getRow() * squareSize);
        }

        GameService.setCurrentTurn(Tint.WHITE);
        PieceService.nullThisPiece();
    }

    public void setPiecesChaos() {
        if(!BooleanService.canDoChaos) {
            return;
        }

        List<Piece> pieces = pieceService.getPieces();
        columns.clear();
        pieces.clear();
        clearBoardState();

        List<Integer> f = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        List<Integer> b = new ArrayList<>(List.of(0, 1, 2, 3, 4, 5, 6, 7));
        Collections.shuffle(f);
        Collections.shuffle(b);
        columns.put(f, b);

        for(Map.Entry<List<Integer>, List<Integer>> entry :
                columns.entrySet()) {
            List<Integer> front = entry.getKey();
            List<Integer> back = entry.getValue();

            for(int col : front) {
                pieces.add(pieceService.getRandomPiece(Tint.WHITE, col, 6));
                pieces.add(pieceService.getRandomPiece(Tint.BLACK, col, 1));
            }

            for(int col : back) {
                if(col == 4) { continue; }
                pieces.add(pieceService.getRandomPiece(Tint.WHITE, col, 7));
                pieces.add(pieceService.getRandomPiece(Tint.BLACK, col, 0));
            }
        }
        pieces.add(new King(pieceService, Tint.WHITE, 4, 7));
        pieces.add(new King(pieceService, Tint.BLACK, 4, 0));
    }

    private void setSandboxPieces() {
        List<Piece> pieces = pieceService.getPieces();
        pieces.clear();
        clearBoardState();

        // TODO
    }
}
