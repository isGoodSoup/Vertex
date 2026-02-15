package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.Type;

import java.util.List;

public class Checker extends Piece {

    public Checker(Tint color, int col, int row) {
        super(color, col, row);
        this.id = Type.CHECKERS;
    }

    @Override
    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        if(!isWithinBoard(targetCol, targetRow)) { return false; }

        int colDiff = Math.abs(targetCol - getCol());
        int rowDiff = Math.abs(targetRow - getRow());

        int direction = (getColor() == Tint.LIGHT) ? -1 : 1;
        if(rowDiff == 1 && (targetRow - getRow()) == direction) {
            return isColliding(targetCol, targetRow, board) == null;
        }

        if(colDiff == 2 && rowDiff == 2) {
            int midCol = (getCol() + targetCol) / 2;
            int midRow = (getRow() + targetRow) / 2;
            Piece middlePiece = isColliding(midCol, midRow, board);
            if(middlePiece != null && middlePiece.getColor() != getColor()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Piece copy() {
        return null;
    }
}
