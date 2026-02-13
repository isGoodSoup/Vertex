package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.service.PieceService;

import java.util.List;
import java.util.Objects;

public class Queen extends Piece {

	public Queen(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.QUEEN;
		if(color == Tint.WHITE) {
			sprite = PieceService.getImage("/pieces/queen_white");
			hovered = PieceService.getImage("/pieces/queen_whiteh");
		} else {
			sprite = PieceService.getImage("/pieces/queen_black");
			hovered = PieceService.getImage("/pieces/queen_blackh");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(isWithinBoard(targetCol, targetRow) && !isSameSquare(this, targetCol,
				targetRow)) {
			if(targetCol == getCol() || targetRow == getRow()) {
				if(isPathClear(this, targetCol, targetRow, board)) {
					return true;
				}
			}

			if(Math.abs(targetCol - getCol()) == Math.abs(targetRow - getRow())) {
                return isPathClear(this, targetCol, targetRow, board);
			}
		}
		return false;
	}

	@Override
	public boolean isPathClear(Piece piece, int targetCol, int targetRow,
							   List<Piece> board) {
		int colDiff = targetCol - getCol();
		int rowDiff = targetRow - getRow();

		if(colDiff == 0 || rowDiff == 0) {
			int colStep = Integer.signum(colDiff);
			int rowStep = Integer.signum(rowDiff);

			int c = getCol() + colStep;
			int r = getRow() + rowStep;

			while (c != targetCol || r != targetRow) {
				if(PieceService.getPieceAt(c, r, board) != null) {
					return false;
				}
				c += colStep;
				r += rowStep;
			}
		} else if(Math.abs(colDiff) == Math.abs(rowDiff)) {
			int colStep = Integer.signum(colDiff);
			int rowStep = Integer.signum(rowDiff);

			int c = getCol() + colStep;
			int r = getRow() + rowStep;

			while (c != targetCol && r != targetRow) {
				if(PieceService.getPieceAt(c, r, board) != null) {
					return false;
				}
				c += colStep;
				r += rowStep;
			}
		}

		if(PieceService.getPieceAt(targetCol, targetRow, board) != null &&
				Objects.requireNonNull(PieceService.getPieceAt(targetCol, targetRow, board))
						.getColor() == this.getColor()) {
			return false;
		}
		return true;
	}


	@Override
	public Piece copy() {
		Queen p = new Queen(getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
