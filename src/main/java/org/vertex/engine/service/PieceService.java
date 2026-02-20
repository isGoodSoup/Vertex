package org.vertex.engine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.*;
import org.vertex.engine.enums.Games;
import org.vertex.engine.enums.Theme;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.events.CheckEvent;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.manager.EventBus;
import org.vertex.engine.manager.MovesManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PieceService {
    private static Map<String, BufferedImage> cache;
    private final List<Piece> pieces;
    private Piece checkingPiece;
    private Piece hoveredPieceKeyboard;
    private Piece hoveredPiece;
    private Piece lastPiece;
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

    public static Piece getHeldPiece() {
        return movesManager.getSelectedPiece();
    }

    public void setHeldPiece(Piece piece) {
        movesManager.setSelectedPiece(piece);
    }

    public Piece getLastPiece() {
        return lastPiece;
    }

    public void setLastPiece(Piece piece) {
        this.lastPiece = piece;
    }

    public Piece getHoveredPiece() {
        return hoveredPiece;
    }

    public void setHoveredPiece(Piece hoveredPiece) {
        this.hoveredPiece = hoveredPiece;
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

    public BufferedImage getSprite(Piece piece) {
        Games game = GameService.getGame();
        try {
            if(game == Games.SHOGI || game == Games.SANDBOX) {
                return getShogiSprite(piece);
            }

            if(game == Games.CHECKERS && piece.getTypeID() == TypeID.KING) {
                return getKingSprites(piece);
            }
            return getThemedSprite(piece);

        } catch (RuntimeException e) {
            log.warn("Error to load sprite,  {} at {},{}: {}",
                    piece.getClass().getSimpleName(),
                    piece.getCol(),
                    piece.getRow(),
                    e.getMessage());
            return getPlaceholderSprite();
        }
    }

    private BufferedImage getThemedSprite(Piece piece) {
        String pieceName = piece.getClass().getSimpleName().toLowerCase();
        Theme theme = Colors.getTheme();
        String color = theme.getColor(piece.getColor());

        String path = "/pieces/" + pieceName + "/" + pieceName + "_" + color;
        return getImage(path);
    }

    private BufferedImage getShogiSprite(Piece piece) {
        String pieceName = piece.getClass().getSimpleName().toLowerCase();
        String suffix = "";

        if(GameService.getGame() == Games.SANDBOX) {
            return getThemedSprite(piece);
        }

        if(piece instanceof King && piece.getColor() == Tint.DARK) {
            suffix = "_jeweled";
        }
        if(piece.isPromoted()) {
            if(piece instanceof Bishop || piece instanceof Rook) {
                suffix = "_promoted";
            }
        }

        String path = "/pieces/shogi/shogi_" + pieceName + suffix;
        try {
            return getImage(path);
        } catch (IllegalStateException e) {
            log.warn("Missing Shogi sprite: {}. Using placeholder.", path);
            return getPlaceholderSprite();
        }
    }

    public BufferedImage getKingSprites(Piece piece) {
        Theme theme = Colors.getTheme();
        String color = theme.getColor(piece.getColor());
        String path = "/pieces/checker/checker_king_" + color;
        return getImage(path);
    }

    private BufferedImage getPlaceholderSprite() {
        BufferedImage placeholder = new BufferedImage(Board.getSquare(),
                Board.getSquare(), BufferedImage.TYPE_INT_ARGB);
        return placeholder;
    }

    public static void clearCache() {
        cache.clear();
    }

    public int getPieceValue(Piece p) {
        return switch(p.getTypeID()) {
            case PAWN_SHOGI -> 1;
            case PAWN -> 10;
            case LANCE, KNIGHT_SHOGI -> 4;
            case SILVER -> 5;
            case TOKIN -> 6;
            case GOLD -> 6;
            case BISHOP_SHOGI, ROOK_SHOGI -> 8;
            case CHECKER -> 20;
            case KNIGHT, BISHOP -> 30;
            case ROOK -> 50;
            case QUEEN -> 90;
            case KING -> 900;
        };
    }

    public static Piece getPieceAt(int col, int row, List<Piece> board) {
        for (Piece p : board) {
            if(p.getCol() == col && p.getRow() == row) {
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
            case 6 -> new King(color, col, row);
            default -> new Pawn(color, col, row);
        };
    }

    public Piece getKing(Tint color) {
        if(GameService.getGame() == Games.SANDBOX) { return null; }
        if(GameService.getGame() == Games.CHESS || GameService.getGame() == Games.SHOGI) {
            for (Piece p : pieces) {
                if(p instanceof King && p.getColor() == color) {
                    return p;
                }
            }
            return null;
        }
        return null;
    }

    public void addPiece(Piece p) {
        pieces.add(p);
        BoardService.getBoardState()[p.getRow()][p.getCol()] = p;
        lastPiece = p;
    }

    public void replacePiece(Piece p, Piece p2) {
        removePiece(p);
        p2.setCol(p.getCol());
        p2.setRow(p.getRow());
        addPiece(p2);
        lastPiece = p2;
    }

    public void removePiece(Piece p) {
        synchronized(pieces) {
            pieces.remove(p);
            BoardService.getBoardState()[p.getRow()][p.getCol()] = null;
        }
    }

    public List<Piece> clonePieces() {
        List<Piece> copy = new ArrayList<>();
        synchronized (pieces) {
            for (Piece p : pieces) {
                if(p == null) {
                    throw new IllegalStateException(
                            "Null piece found inside pieces list"
                    );
                }
                Piece cloned = p.copy();
                if(cloned == null) {
                    throw new IllegalStateException(
                            "copy() returned null for " + p.getClass().getSimpleName()
                    );
                }
                copy.add(cloned);
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
        if(!(GameService.getGame() == Games.SANDBOX) && BooleanService.canSwitchTurns) {
            gameService.setCurrentTurn(
                    gameService.getCurrentTurn() == Tint.LIGHT ? Tint.DARK : Tint.LIGHT
            );
        }
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
            if(p.getCol() == col && p.getRow() == row) {
                return p;
            }
        }
        return null;
    }

    public static boolean isPathClear(Piece piece, int targetCol, int targetRow,
                                List<Piece> board) {
        int colDiff = targetCol - piece.getCol();
        int rowDiff = targetRow - piece.getRow();

        if(Math.abs(colDiff) != Math.abs(rowDiff)) {
            return false;
        }

        int colStep = Integer.signum(colDiff);
        int rowStep = Integer.signum(rowDiff);

        int c = piece.getCol() + colStep;
        int r = piece.getRow() + rowStep;

        while (c != targetCol && r != targetRow) {
            for (Piece p : board) {
                if(p == getHeldPiece()) { continue; }
                if(p.getCol() == c && p.getRow() == r) {
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
        if(GameService.getGame() == Games.SANDBOX) { return false; }
        if(GameService.getGame() == Games.CHESS || GameService.getGame() == Games.SHOGI) {
            Piece king = getKing(kingColor);
            if(king == null) { return false; }

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
        }
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

        if(simPiece == null) {
            return true;
        }

        simPieces.removeIf(p -> p != simPiece
                && p.getCol() == targetCol
                && p.getRow() == targetRow
                && !(p instanceof King));

        simPiece.setCol(targetCol);
        simPiece.setRow(targetRow);

        if(GameService.getGame() == Games.SANDBOX) {
            return false;
        }

        if(GameService.getGame() == Games.CHESS || GameService.getGame() == Games.SHOGI) {
            Optional<Piece> kingList = simPieces.stream()
                    .filter(p -> p instanceof King && p.getColor() == piece.getColor())
                    .findFirst();

            if(kingList.isEmpty()) {
                return false;
            }

            Piece king = kingList.get();
            for (Piece enemy : simPieces) {
                if(enemy.getColor() != piece.getColor() &&
                        enemy.canMove(king.getCol(), king.getRow(), simPieces)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isInPromotionZone(Tint color, int row) {
        if(row < 0) { throw new IllegalStateException("Promotion zone must be between 0,1,2 and 6,7,8"); }
        return (color == Tint.LIGHT && row >= 6) || (color == Tint.DARK && row <= 2);
    }
}
