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
        // TODO movement logic for silver

        if(isPromoted()) {
            return canMoveLikeGold(this, targetCol, targetRow, board);
        }
        return false;
    }

    @Override
    public Piece copy() {
        Silver p = new Silver(getColor(), getCol(), getRow());
        p.setHasMoved(hasMoved());
        return p;
    }
}
