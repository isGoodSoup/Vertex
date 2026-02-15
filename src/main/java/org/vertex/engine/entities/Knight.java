package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.Type;
import org.vertex.engine.service.PieceService;

import java.util.List;

public class Knight extends Piece {

	public Knight(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.KNIGHT;
		loadSprite(this);
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(!isWithinBoard(targetCol, targetRow)) {
			return false;
		}

		int colDiff = Math.abs(targetCol - getCol());
		int rowDiff = Math.abs(targetRow - getRow());
		
		if((colDiff == 2 && rowDiff == 1) || (colDiff == 1 && rowDiff == 2)) {
			return isValidSquare(this, targetCol, targetRow, board);
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
