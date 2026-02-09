package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.service.PieceService;

import java.util.List;

public class Knight extends Piece {

	public Knight(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.KNIGHT;
		if(color == Tint.WHITE) {
			image = PieceService.getImage("/pieces/knight");
		} else {
			image = PieceService.getImage("/pieces/knight-b");
		}
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
