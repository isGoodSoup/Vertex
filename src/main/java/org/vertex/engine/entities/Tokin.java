package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.interfaces.GoldGeneral;

import java.util.List;

public class Tokin extends Piece implements GoldGeneral {

    public Tokin(Tint color, int col, int row) {
        super(color, col, row);
        this.typeID = TypeID.TOKIN;
    }

    @Override
    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        return canMoveLikeGold(this, targetCol, targetRow, board);
    }

    @Override
    public Piece copy() {
        Tokin p = new Tokin(getColor(), getCol(), getRow());
        p.setHasMoved(hasMoved());
        return p;
    }
}
