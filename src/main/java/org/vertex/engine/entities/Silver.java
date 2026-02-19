package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.interfaces.GoldGeneral;

import java.util.List;

public class Silver extends Piece implements GoldGeneral {

    public Silver(Tint color, int col, int row) {
        super(color, col, row);
        this.typeID = TypeID.SILVER;
    }

    @Override
    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        if (isPromoted()) {
            return canMoveLikeGold(this, targetCol, targetRow, board);
        }

        int direction = getColor() == Tint.LIGHT ? 1 : -1;
        int colDiff = targetCol - getCol();
        int rowDiff = targetRow - getRow();

        boolean validMove =
                (rowDiff == direction && Math.abs(colDiff) <= 1) ||
                        (rowDiff == -direction && Math.abs(colDiff) == 1);
        if (!validMove) return false;

        for (Piece p : board) {
            if (p.getCol() == targetCol && p.getRow() == targetRow) {
                return p.getColor() != getColor();
            }
        }
        return true;
    }

    @Override
    public Piece copy() {
        Silver p = new Silver(getColor(), getCol(), getRow());
        p.setHasMoved(hasMoved());
        return p;
    }
}
