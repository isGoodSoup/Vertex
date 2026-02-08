package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.BoardPanel;

import java.util.List;

public class Rook extends Piece {

	public Rook(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.ROOK;
		if(color == Tint.WHITE) {
			image = getImage("/pieces/rook");
		} else {
			image = getImage("/pieces/rook-b");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, BoardPanel board) {
	    if(isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
	        if(targetCol == getPreCol() || targetRow == getPreRow()) {
                return isValidSquare(targetCol, targetRow, board)
                        && isPathClear(targetCol, targetRow, board.getPieces());
	        }
	    }
	    return false;
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(!isWithinBoard(targetCol, targetRow)) {
			return false;
		}

		if(targetCol == getCol() || targetRow == getRow()) {
			return isPathClear(targetCol, targetRow, board);
		}

		return false;
	}

	@Override
	public Piece copy() {
		Rook p = new Rook(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
