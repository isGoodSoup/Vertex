package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.Type;
import org.vertex.engine.service.PieceService;

import java.util.List;

public class Rook extends Piece {

	public Rook(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.ROOK;
		loadSprite(this);
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(isWithinBoard(targetCol, targetRow) && !isSameSquare(this, targetCol,
				targetRow)) {
			if(targetCol == getPreCol() || targetRow == getPreRow()) {
				return isValidSquare(this, targetCol, targetRow, board)
						&& isPathClear(this, targetCol, targetRow, board);
			}
		}
		return false;
	}

	@Override
	public boolean isPathClear(Piece piece, int targetCol, int targetRow,
							   List<Piece> board) {
		int colDiff = targetCol - getCol();
		int rowDiff = targetRow - getRow();

		if(rowDiff == 0) {
			int colStep = Integer.signum(colDiff);
			int c = getCol() + colStep;

			while(c != targetCol) {
				if(PieceService.getPieceAt(c, getRow(), board) != null) {
					return false;
				}
				c += colStep;
			}
		} else if(colDiff == 0) {
			int rowStep = Integer.signum(rowDiff);
			int r = getRow() + rowStep;

			while(r != targetRow) {
				if(PieceService.getPieceAt(getCol(), r, board) != null) {
					return false;
				}
				r += rowStep;
			}
		} else {
			return false;
		}

		return true;  // Path is clear
	}


	@Override
	public Piece copy() {
		Rook p = new Rook(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
