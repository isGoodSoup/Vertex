package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.service.PieceService;

import java.util.List;

public class Bishop extends Piece {

	public Bishop(Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.BISHOP;
		if(color == Tint.WHITE) {
			sprite = PieceService.getImage("/pieces/bishop");
			hovered = PieceService.getImage("/pieces/bishop-h");
		} else {
			sprite = PieceService.getImage("/pieces/bishop-b");
			hovered = PieceService.getImage("/pieces/bishop-bh");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if (!isWithinBoard(targetCol, targetRow)
				|| isSameSquare(this, targetCol, targetRow)) {
			return false;
		}

		int colDiff = targetCol - getCol();
		int rowDiff = targetRow - getRow();

		if (Math.abs(colDiff) != Math.abs(rowDiff)) {
			return false;
		}

		if (!isPathClear(this, targetCol, targetRow, board)) {
			return false;
		}

		Piece target = null;
		for (Piece p : board) {
			if (p.getCol() == targetCol && p.getRow() == targetRow) {
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
