package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.BoardPanel;

import java.util.List;

public class Pawn extends Piece {

	public Pawn(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.PAWN;
		if(color == Tint.WHITE) {
			image = getImage("/pieces/pawn");
		} else {
			image = getImage("/pieces/pawn-b");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, BoardPanel board) {
		if(isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
			int moveValue = 0;
			if(getColor() == Tint.WHITE) {
				moveValue = -1;
			}
			
			if(getColor() == Tint.BLACK) {
				moveValue = 1;
			}

			setOtherPiece(isColliding(targetCol, targetRow, board));
			if(targetCol == getPreCol() && targetRow == getPreRow() + moveValue 
					&& getOtherPiece() == null) {
				return true;
			}

			if(targetCol == getPreCol() && targetRow == getPreRow() + moveValue * 2 
					&& getOtherPiece() == null
					&& hasMoved() && isPathClear(targetCol, targetRow,
					board.getPieces())) {
				return true;
			}

			if(Math.abs(targetCol - getPreCol()) == 1
					&& targetRow == getPreRow() + moveValue
					&& getOtherPiece() != null
					&& getOtherPiece().getColor() != this.getColor()) {
				return true;
			}
			
			if(Math.abs(targetCol - getPreCol()) == 1 && targetRow == getPreRow() + moveValue) {
				for (Piece p : board.getPieces()) {
					if(p.getCol() == targetCol && p.getRow() == getPreRow()
							&& p.isTwoStepsAhead()) {
						setOtherPiece(p);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if (!isWithinBoard(targetCol, targetRow)) { return false; }
		int direction = (getColor() == Tint.WHITE) ? -1 : 1;
		int startRow = (getColor() == Tint.WHITE) ? 6 : 1;

		if (targetCol == getCol() && targetRow == getRow() + direction) {
			return isValidSquare(targetCol, targetRow, board);
		}

		if (getRow() == startRow && targetCol == getCol()
				&& targetRow == getRow() + 2 * direction) {
			if (isPathClear(targetCol, targetRow, board)) {
				return true;
			}
		}

		if (Math.abs(targetCol - getCol()) == 1
				&& targetRow == getRow() + direction) {
			return isValidSquare(targetCol, targetRow, board);
		}
		return false;
	}

	@Override
	public Piece copy() {
		Pawn p = new Pawn(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		p.setTwoStepsAhead(this.isTwoStepsAhead());
		return p;
	}
}
