package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.BoardPanel;

import java.util.List;

public class Knight extends Piece {

	public Knight(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.KNIGHT;
		if(color == Tint.WHITE) {
			image = getImage("/pieces/knight");
		} else {
			image = getImage("/pieces/knight-b");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, BoardPanel board) {
	    if(isWithinBoard(targetCol, targetRow)) {
	        if(Math.abs(targetCol - getPreCol()) * Math.abs(targetRow - getPreRow()) == 2) {
                return isValidSquare(targetCol, targetRow, board);
	        }
	    }
	    return false;
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if (!isWithinBoard(targetCol, targetRow)) {
			return false;
		}

		int colDiff = Math.abs(targetCol - getCol());
		int rowDiff = Math.abs(targetRow - getRow());

		if ((colDiff == 2 && rowDiff == 1) || (colDiff == 1 && rowDiff == 2)) {
			return isValidSquare(targetCol, targetRow, board);
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
