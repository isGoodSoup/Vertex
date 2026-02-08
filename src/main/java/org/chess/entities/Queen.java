package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.BoardPanel;

import java.util.List;

public class Queen extends Piece {

	public Queen(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.QUEEN;
		if(color == Tint.WHITE) {
			image = getImage("/pieces/queen");
		} else {
			image = getImage("/pieces/queen-b");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, BoardPanel board) {
	    if(isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
	    	 if(targetCol == getCol() || targetRow == getRow()) {
	    		 if(isValidSquare(targetCol, targetRow, board)
						 && isPathClear(targetCol, targetRow, board.getPieces())) {
	    			 return true;
	    		 }
	    	 }
			if(Math.abs(targetCol - getCol()) == Math.abs(targetRow - getRow())) {
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

		int colDiff = Math.abs(targetCol - getCol());
		int rowDiff = Math.abs(targetRow - getRow());
		if(colDiff == rowDiff || targetCol == getCol() || targetRow == getRow()) {
			return isPathClear(targetCol, targetRow, board);
		}
		return false;
	}

	@Override
	public Piece copy() {
		Queen p = new Queen(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
