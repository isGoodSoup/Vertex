package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.service.PieceService;

import java.util.List;

public class Pawn extends Piece {

	public Pawn(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.PAWN;
		if(color == Tint.WHITE) {
			sprite = PieceService.getImage("/pieces/pawn_white");
			hovered = PieceService.getImage("/pieces/pawn_whiteh");
		} else {
			sprite = PieceService.getImage("/pieces/pawn_black");
			hovered = PieceService.getImage("/pieces/pawn_blackh");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(!isWithinBoard(targetCol, targetRow) || isSameSquare(this, targetCol, targetRow))
			return false;

		int direction = (getColor() == Tint.WHITE) ? -1 : 1;

		Piece pieceAtTarget = PieceService.getPieceAt(targetCol, targetRow, board);

		if(targetCol == getCol() && targetRow == getRow() + direction) {
			return pieceAtTarget == null;
		}

		if(targetCol == getCol() && targetRow == getRow() + 2 * direction
				&& !hasMoved() && isPathClear(this, targetCol, targetRow, board)) {
			return pieceAtTarget == null;
		}

		if(Math.abs(targetCol - getCol()) == 1 && targetRow == getRow() + direction) {
			if(pieceAtTarget != null && pieceAtTarget.getColor() != this.getColor()) {
				return true;
			}

			for(Piece p : board) {
				if(p instanceof Pawn && p.getColor() != this.getColor()
						&& p.getCol() == targetCol && p.getRow() == getRow()
						&& p.isTwoStepsAhead()) {
					setOtherPiece(p);
					return true;
				}
			}
		}
		return false;
	}


	@Override
	public boolean isPathClear(Piece piece, int targetCol, int targetRow,
							   List<Piece> board) {
		int colDiff = targetCol - getCol();
		int rowDiff = targetRow - getRow();

		if (colDiff == 0) {
			int rowStep = Integer.signum(rowDiff);
			int r = getRow() + rowStep;

			while (r != targetRow) {
				if (PieceService.getPieceAt(targetCol, r, board) != null) {
					return false;
				}
				r += rowStep;
			}
			return true;
		}
		return false;
	}

	public void movePiece(Piece p, int newCol, int newRow) {
		PieceService.movePiece(p, newCol, newRow);
        setTwoStepsAhead(Math.abs(newRow - getPreRow()) == 2);
		if (Math.abs(newRow - getPreRow()) == 2) {
			setHasMoved(true);
		}
	}

	@Override
	public Piece copy() {
		Pawn p = new Pawn(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		p.setTwoStepsAhead(this.isTwoStepsAhead());
		return p;
	}
}
