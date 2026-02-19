package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.interfaces.GoldGeneral;
import org.vertex.engine.service.GameService;

import java.util.List;

public class Knight extends Piece implements GoldGeneral {

	public Knight(Tint color, int col, int row) {
		super(color, col, row);
		this.typeID = TypeID.KNIGHT;
		this.shogiID = TypeID.KNIGHT_SHOGI;
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(!isWithinBoard(targetCol, targetRow)) {
			return false;
		}

		switch(GameService.getGames()) {
            case CHESS -> {
				int colDiff = Math.abs(targetCol - getCol());
				int rowDiff = Math.abs(targetRow - getRow());

				if((colDiff == 2 && rowDiff == 1) || (colDiff == 1 && rowDiff == 2)) {
					return isValidSquare(this, targetCol, targetRow, board);
				}
            }
            case SHOGI -> {

				if(isPromoted()) {
					return canMoveLikeGold(this, targetCol, targetRow, board);
				}
            }
        }
		return false;
	}

	@Override
	public Piece copy() {
		Knight p = new Knight(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
