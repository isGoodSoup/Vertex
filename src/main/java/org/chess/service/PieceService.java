package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.input.Mouse;
import org.chess.input.MoveManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PieceService {
    private static Piece currentPiece;
    private final List<Piece> pieces;
    private Piece checkingPiece;
    private Piece hoveredPieceKeyboard;
    private int dragOffsetX;
    private int dragOffsetY;
    private int hoveredSquareX = -1;
    private int hoveredSquareY = -1;

    private MoveManager moveManager;
    private final Mouse mouse;

    public PieceService(Mouse mouse) {
        this.mouse = mouse;
        pieces = new ArrayList<>();
    }

    public MoveManager getMoveManager() {
        return moveManager;
    }

    public void setMoveManager(MoveManager moveManager) {
        this.moveManager = moveManager;
    }

    public static Piece getPiece() {
        return currentPiece;
    }

    public static void setPiece(Piece p) {
        currentPiece = p;
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
        currentPiece = null;
    }

    public List<Piece> getPieces() {
        return pieces;
    }

    public static BufferedImage getImage(String path) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(Objects.requireNonNull(
                    PieceService.class.getResourceAsStream(path + ".png")));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return img;
    }

    public int getPieceValue(Piece p) {
        return switch(p.getId()) {
            case PAWN -> 10;
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
        for(Piece p : pieces) {
            if(p instanceof King && p.getColor() == color) {
                return p;
            }
        }
        throw new IllegalStateException("King not found for color: " + color);
    }

    public void addPiece(Piece p) {
        pieces.add(p);
        BoardService.getBoardState()[p.getCol()][p.getRow()] = p;
    }

    public void removePiece(Piece p) {
        synchronized(pieces) {
            pieces.remove(p);
            BoardService.getBoardState()[p.getCol()][p.getRow()] = null;
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
        String oldPos = BoardService.getSquareName(p.getPreCol(),
                p.getPreRow());
        String newPos = BoardService.getSquareName(newCol, newRow);

        BoardService.getBoardState()[p.getCol()][p.getRow()] = null;
        p.setCol(newCol);
        p.setRow(newRow);
        updatePos(p);

        System.out.println(p.getColor().toString() + " "
                + p.getId().toString() + ": " + oldPos + " -> " + newPos);
        BoardService.getBoardState()[newCol][newRow] = p;
    }

    public static void updatePos(Piece piece) {
        if(piece.getId() == Type.PAWN) {
            if(Math.abs(piece.getRow() - piece.getPreRow()) == 2) {
                piece.setTwoStepsAhead(true);
            }
        }
        int square = Board.getSquare();
        piece.setX(piece.getCol() * square);
        piece.setY(piece.getRow() * square);
        piece.setHasMoved(true);
    }

    public void resetPos(Piece piece) {
        piece.setCol(piece.getPreCol());
        piece.setRow(piece.getPreRow());
        updatePos(piece);
    }

    public Piece getHoveredPiece() {
        int boardMouseX = mouse.getX() - GUIService.getEXTRA_WIDTH();
        int boardMouseY = mouse.getY();

        if (boardMouseX < 0 || boardMouseY < 0) {
            return null;
        }

        int hoverCol = boardMouseX  / Board.getSquare();
        int hoverRow = boardMouseY / Board.getSquare();

        for (Piece p : pieces) {
            if (p.getCol() == hoverCol && p.getRow() == hoverRow && p != currentPiece) {
                if (BooleanService.isAIPlaying && p.getColor() != Tint.WHITE
                        || BooleanService.isDragging) {
                    return null;
                }
                return p;
            }
        }
        return null;
    }

    public void switchTurns() {
        GameService.setCurrentTurn(
                GameService.getCurrentTurn() == Tint.WHITE ? Tint.BLACK : Tint.WHITE
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
        Piece king = getKing(kingColor);

        for(Piece p : pieces) {
            if(p.getColor() != kingColor) {
                if(p.canMove(king.getCol(), king.getRow(), pieces)) {
                    checkingPiece = p;
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
        return false;
    }
}
