package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.Type;

import java.util.List;

public class Bishop extends Piece {

	public Bishop(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.BISHOP;
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(!isWithinBoard(targetCol, targetRow)
				|| isSameSquare(this, targetCol, targetRow)) {
			return false;
		}

		int colDiff = targetCol - getCol();
		int rowDiff = targetRow - getRow();

		if(Math.abs(colDiff) != Math.abs(rowDiff)) {
			return false;
		}

		if(!isPathClear(this, targetCol, targetRow, board)) {
			return false;
		}

		Piece target = null;
		for(Piece p : board) {
			if(p.getCol() == targetCol && p.getRow() == targetRow) {
				target = p;
				break;
			}
		}
		return target == null || target.getColor() != getColor();
	}

	@Override
	public Piece copy() {
		Bishop p = new Bishop(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
