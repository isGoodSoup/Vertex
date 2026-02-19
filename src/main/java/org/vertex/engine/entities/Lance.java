package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.interfaces.GoldGeneral;

import java.util.List;

public class Lance extends Piece implements GoldGeneral {

    public Lance(Tint color, int col, int row) {
        super(color, col, row);
        this.typeID = TypeID.LANCE;
    }

    @Override
    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        if (isPromoted()) {
            return canMoveLikeGold(this, targetCol, targetRow, board);
        }

        int direction = getColor() == Tint.LIGHT ? 1 : -1;
        int rowDiff = targetRow - getRow();
        int colDiff = targetCol - getCol();

        if (colDiff == 0 && rowDiff * direction > 0) {
            return isPathClear(this, targetCol, targetRow, board)
                    && isValidSquare(this, targetCol, targetRow, board);
        }
        return false;
    }

    @Override
    public Piece copy() {
        Lance p = new Lance(getColor(), getCol(), getRow());
        p.setHasMoved(hasMoved());
        return p;
    }
}
