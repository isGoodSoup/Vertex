package org.chess.service;

import org.chess.entities.Board;
import org.chess.entities.King;
import org.chess.entities.Piece;
import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.Mouse;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PieceService {
    private static Piece currentPiece;
    private static List<Piece> pieces;
    private Piece checkingPiece;
    private int dragOffsetX;
    private int dragOffsetY;

    private final Mouse mouse;

    public PieceService(Mouse mouse) {
        this.mouse = mouse;
        pieces = new ArrayList<>();
    }

    public static Piece getPiece() {
        return currentPiece;
    }

    public static void setPiece(Piece p) {
        currentPiece = p;
    }

    public static void nullThisPiece() {
        currentPiece = null;
    }

    public static List<Piece> getPieces() {
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
        for(Piece enemy : PieceService.getPieces()) {
            if(enemy.getColor() == piece.getColor()) { continue; }
            if(enemy.canMove(piece.getCol(), piece.getRow(),
                    PieceService.getPieces())) {
                return true;
            }
        }
        return false;
    }

    private Piece getKing(Tint color) {
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
        pieces.remove(p);
        BoardService.getBoardState()[p.getCol()][p.getRow()] = null;
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
        int hoverCol = mouse.getX() / Board.getSquare();
        int hoverRow = mouse.getY() / Board.getSquare();

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

    public static List<Piece> clonePieces() {
        List<Piece> copy = new ArrayList<>();
        for (Piece p : pieces) {
            copy.add(p.copy());
        }
        return copy;
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
