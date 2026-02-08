package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.BoardPanel;

import java.util.List;

public class King extends Piece {

	public King(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.KING;
		if(color == Tint.WHITE) {
			image = getImage("/pieces/king");
		} else {
			image = getImage("/pieces/king-b");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, BoardPanel board) {
		if(isWithinBoard(targetCol, targetRow)) {
			int colDiff = Math.abs(targetCol - getPreCol());
			int rowDiff = Math.abs(targetRow - getPreRow());

			if((colDiff + rowDiff == 1) || (colDiff * rowDiff == 1)) {
				if(isValidSquare(targetCol, targetRow, board)) {
					return true;
				}
			}

			if(hasMoved()) {
				if(targetCol == getPreCol() + 2 && targetRow == getPreRow()
						&& isPathClear(targetCol, targetRow, board.getPieces())) {
					for(Piece p : board.getPieces()) {
						board.setCastlingPiece(p);
						return true;
					}
				}

				if(targetCol == getPreCol() - 2 && targetRow == getPreRow()
						&& isPathClear(targetCol, targetRow, board.getPieces())) {
					Piece[] ps = new Piece[2];
					for(Piece p : board.getPieces()) {
						if(p.getCol() == getPreCol() - 3 && p.getRow() == targetRow) { 
							ps[0] = p;
						}
						
						if(p.getCol() == getPreCol() - 4 && p.getRow() == targetRow) { 
							ps[1] = p;
						}
						
						if(ps[0] == null && ps[1] != null && ps[1].hasMoved()) {
							board.setCastlingPiece(ps[1]);
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(!isWithinBoard(targetCol, targetRow)) { return false; }
		int colDiff = Math.abs(targetCol - getCol());
		int rowDiff = Math.abs(targetRow - getRow());
		if((colDiff + rowDiff == 1) || (colDiff * rowDiff == 1)) {
			return isValidSquare(targetCol, targetRow, board);
		}
		return false;
	}

	@Override
	public Piece copy() {
		King p = new King(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
