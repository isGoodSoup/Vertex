package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.interfaces.GoldGeneral;

import java.util.List;

public class Gold extends Piece implements GoldGeneral {

    public Gold(Tint color, int col, int row) {
        super(color, col, row);
        this.typeID = TypeID.GOLD;
    }

    @Override
    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        return false;
    }

    @Override
    public Piece copy() {
        Gold p = new Gold(getColor(), getCol(), getRow());
        p.setHasMoved(hasMoved());
        return p;
    }
}
