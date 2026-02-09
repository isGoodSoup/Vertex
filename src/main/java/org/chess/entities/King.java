package org.chess.entities;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.service.PieceService;

import java.util.List;

public class King extends Piece {
	private final PieceService pieceService;

	public King(PieceService pieceService, Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.KING;
		this.pieceService = pieceService;
		if(color == Tint.WHITE) {
			image = PieceService.getImage("/pieces/king");
		} else {
			image = PieceService.getImage("/pieces/king-b");
		}
	}

	@Override
	public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
		if(!isWithinBoard(targetCol, targetRow)) { return false; }
		int colDiff = Math.abs(targetCol - getCol());
		int rowDiff = Math.abs(targetRow - getRow());

		if((colDiff + rowDiff == 1) || (colDiff * rowDiff == 1)) {
			return isValidSquare(this, targetCol, targetRow, board);
		}

		if (rowDiff == 0 && colDiff == 2 && !hasMoved()) {
			int rookCol = (targetCol > getCol()) ? 7 : 0;
			Piece rook = PieceService.getPieceAt(rookCol, getRow(), board);
			if (rook instanceof Rook && !rook.hasMoved()) {
				int step = (targetCol > getCol()) ? 1 : -1;
				for (int c = getCol() + step; c != rookCol; c += step) {
					if (PieceService.getPieceAt(c, getRow(), board) != null) {
						return false;
					}
				}
				for (int c = getCol(); c != targetCol + step; c += step) {
					if (pieceService.wouldLeaveKingInCheck(this, c, getRow())) {
						return false;
					}
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public Piece copy() {
		King p = new King(pieceService, getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}
}
