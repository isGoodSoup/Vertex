package org.vertex.engine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.*;
import org.vertex.engine.enums.Games;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.events.CheckEvent;
import org.vertex.engine.manager.EventBus;
import org.vertex.engine.manager.MovesManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PieceService {
    private static Map<String, BufferedImage> cache;
    private static Piece currentPiece;
    private final List<Piece> pieces;
    private Piece checkingPiece;
    private Piece hoveredPieceKeyboard;
    private int dragOffsetX;
    private int dragOffsetY;
    private int hoveredSquareX = -1;
    private int hoveredSquareY = -1;

    private final EventBus eventBus;
    private static MovesManager movesManager;
    private static BoardService boardService;
    private GameService gameService;

    private static final Logger log = LoggerFactory.getLogger(PieceService.class);

    public PieceService(EventBus eventBus) {
        this.eventBus = eventBus;
        this.pieces = new ArrayList<>();
        cache = new HashMap<>();
    }

    public GameService getGameService() {
        return gameService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public BoardService getBoardService() {
        return boardService;
    }

    public void setBoardService(BoardService boardService) {
        PieceService.boardService = boardService;
    }

    public MovesManager getMoveManager() {
        return movesManager;
    }

    public void setMoveManager(MovesManager movesManager) {
        PieceService.movesManager = movesManager;
    }


    public Piece getHeldPiece() {
        return movesManager.getSelectedPiece();
    }

    public void setHeldPiece(Piece piece) {
        movesManager.setSelectedPiece(piece);
    }

    public Piece getHoveredPieceKeyboard() {
        return hoveredPieceKeyboard;
    }

    public void setHoveredPieceKeyboard(Piece hpk) {
        this.hoveredPieceKeyboard = hpk;
    }

    public void setHoveredSquare(int col, int row) {
        this.hoveredSquareX = col;
        this.hoveredSquareY = row;
    }

    public int getHoveredSquareX() {
        return hoveredSquareX;
    }

    public int getHoveredSquareY() {
        return hoveredSquareY;
    }

    public static void nullThisPiece() {
        movesManager.setSelectedPiece(null);
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public static BufferedImage getImage(String path) {
        return cache.computeIfAbsent(path, key -> {
            try(InputStream stream =
                         PieceService.class.getResourceAsStream(key + ".png")) {
                if(stream == null) {
                    throw new IllegalStateException("Missing resource: " + key);
                }
                return ImageIO.read(stream);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load image: " + key, e);
            }
        });
    }

    public static void clearCache() {
        cache.clear();
    }

    public int getPieceValue(Piece p) {
        return switch(p.getTypeID()) {
            case PAWN -> 10;
            case CHECKER -> 20;
            case KNIGHT, BISHOP -> 30;
            case ROOK -> 50;
            case QUEEN -> 90;
            case KING -> 900;
        };
    }

    public static Piece getPieceAt(int col, int row, List<Piece> board) {
        for (Piece p : board) {
            if (p.getCol() == col && p.getRow() == row) {
                return p;
            }
        }
        return null;
    }

    public boolean isPieceThreatened(Piece piece) {
        for(Piece enemy : getPieces()) {
            if(enemy.getColor() == piece.getColor()) { continue; }
            if(enemy.canMove(piece.getCol(), piece.getRow(),
                    getPieces())) {
                return true;
            }
        }
        return false;
    }

    public Piece getRandomPiece(Tint color, int col, int row) {
        int index = BooleanService.getRandom(1, 6);
        return switch(index) {
            case 1 -> new Pawn(color, col, row);
            case 2 -> new Rook(color, col, row);
            case 3 -> new Bishop(color, col, row);
            case 4 -> new Knight(color, col, row);
            case 5 -> new Queen(color, col, row);
            case 6 -> new King(this, color, col, row);
            default -> new Pawn(color, col, row);
        };
    }

    public Piece getKing(Tint color) {
        if(gameService.getGame() != Games.CHESS) { return null; }
        if(BooleanService.isSandboxEnabled) { return null; }
        for(Piece p : pieces) {
            if(p instanceof King && p.getColor() == color) {
                return p;
            }
        }
        throw new IllegalStateException("King not found for color: " + color);
    }

    public void addPiece(Piece p) {
        pieces.add(p);
        BoardService.getBoardState()[p.getRow()][p.getCol()] = p;
    }

    public void replacePiece(Piece p, Piece p2) {
        removePiece(p);
        p2.setCol(p.getCol());
        p2.setRow(p.getRow());
        addPiece(p2);
    }

    public void removePiece(Piece p) {
        synchronized(pieces) {
            pieces.remove(p);
            BoardService.getBoardState()[p.getRow()][p.getCol()] = null;
        }
    }

    public List<Piece> clonePieces() {
        List<Piece> copy = new ArrayList<>();
        synchronized(pieces) {
            for (Piece p : pieces) {
                copy.add(p.copy());
            }
        }
        return copy;
    }

    public static void movePiece(Piece p, int newCol, int newRow) {
        String oldPos = boardService.getSquareNameAt(p.getPreRow(), p.getPreCol());
        String newPos = boardService.getSquareNameAt(newRow, newCol);

        BoardService.getBoardState()[p.getRow()][p.getCol()] = null;
        p.setCol(newCol);
        p.setRow(newRow);
        updatePos(p);

        log.debug("{} {}: {} -> {}", p.getColor().toString(),
                p.getTypeID().toString(), oldPos, newPos);
        BoardService.getBoardState()[newRow][newCol] = p;
    }

    public static void updatePos(Piece piece) {
        if(piece.getTypeID() == TypeID.PAWN) {
            if(Math.abs(piece.getRow() - piece.getPreRow()) == 2) {
                piece.setTwoStepsAhead(true);
            }
        }
        int square = Board.getSquare();
        piece.setX(piece.getCol() * square);
        piece.setY(piece.getRow() * square);
        piece.setHasMoved(true);

        piece.setPreCol(piece.getCol());
        piece.setPreRow(piece.getRow());
    }

    public void resetPos(Piece piece) {
        piece.setCol(piece.getPreCol());
        piece.setRow(piece.getPreRow());
        updatePos(piece);
    }

    public void switchTurns() {
        gameService.setCurrentTurn(
                gameService.getCurrentTurn() == Tint.LIGHT ? Tint.DARK : Tint.LIGHT
        );
    }

    public static boolean isWithinBoard(int targetCol, int targetRow) {
        return targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7;
    }

    public static boolean isSameSquare(Piece piece, int targetCol,
                                       int targetRow) {
        if(piece == null) { return false; }
        return targetCol == piece.getPreCol() && targetRow == piece.getPreRow();
    }

    public static Piece isColliding(int col, int row, List<Piece> board) {
        for (Piece p : board) {
            if (p.getCol() == col && p.getRow() == row) {
                return p;
            }
        }
        return null;
    }

    public static boolean isPathClear(Piece piece, int targetCol, int targetRow,
                                List<Piece> board) {
        int colDiff = targetCol - piece.getCol();
        int rowDiff = targetRow - piece.getRow();

        if (Math.abs(colDiff) != Math.abs(rowDiff)) {
            return false;
        }

        int colStep = Integer.signum(colDiff);
        int rowStep = Integer.signum(rowDiff);

        int c = piece.getCol() + colStep;
        int r = piece.getRow() + rowStep;

        while (c != targetCol && r != targetRow) {
            for (Piece p : board) {
                if (p == currentPiece) { continue; }
                if (p.getCol() == c && p.getRow() == r) {
                    return false;
                }
            }
            c += colStep;
            r += rowStep;
        }
        return true;
    }

    public static boolean isValidSquare(Piece piece, int targetCol,
                                        int targetRow,
                                 List<Piece> board) {
        for(Piece p : board) {
            if(p.getCol() == targetCol && p.getRow() == targetRow) {
                return p.getColor() != piece.getColor();
            }
        }
        return true;
    }

    public boolean isKingInCheck(Tint kingColor) {
        if(gameService.getGame() != Games.CHESS) { return false; }
        if(BooleanService.isSandboxEnabled) { return false; }
        Piece king = getKing(kingColor);

        for(Piece p : pieces) {
            if(p.getColor() != kingColor) {
                if(p.canMove(king.getCol(), king.getRow(), pieces)) {
                    checkingPiece = p;
                    eventBus.fire(new CheckEvent(p, king));
                    return true;
                }
            }
        }
        checkingPiece = null;
        return false;
    }

    public boolean wouldLeaveKingInCheck(Piece piece, int targetCol,
                                     int targetRow) {
        List<Piece> simPieces = clonePieces();

        Piece simPiece = simPieces.stream()
                .filter(p -> p.getCol() == piece.getCol()
                        && p.getRow() == piece.getRow()
                        && p.getColor() == piece.getColor()
                        && p.getClass() == piece.getClass())
                .findFirst()
                .orElse(null);

        if (simPiece == null) {
            return true;
        }

        simPieces.removeIf(p -> p != simPiece
                && p.getCol() == targetCol
                && p.getRow() == targetRow
                && !(p instanceof King));

        simPiece.setCol(targetCol);
        simPiece.setRow(targetRow);

        if(gameService.getGame() == Games.CHESS) {
            if(!BooleanService.isSandboxEnabled && !BooleanService.canType) {
                Piece king = simPieces.stream()
                        .filter(p -> p instanceof King
                                && p.getColor() == piece.getColor())
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("King must exist after cloning"));

                for (Piece enemy : simPieces) {
                    if (enemy.getColor() != piece.getColor() &&
                            enemy.canMove(king.getCol(), king.getRow(), simPieces)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
