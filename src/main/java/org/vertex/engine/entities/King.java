package org.vertex.engine.entities;

import org.vertex.engine.enums.Games;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.Type;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.PieceService;

import java.util.List;

public class King extends Piece {
	private transient PieceService pieceService;

	public King(PieceService pieceService, Tint color, int col, int row) {
		super(color, col, row);
		this.id = Type.KING;
		this.pieceService = pieceService;
		if(color == Tint.WHITE) {
			if(GameService.getGame() == Games.CHESS) {
				sprite = PieceService.getImage("/pieces/king_white");
				hovered = PieceService.getImage("/pieces/king_whiteh");
			} else if(GameService.getGame() == Games.CHECKERS) {
				sprite = PieceService.getImage("/pieces/king_checkers_white");
				hovered = PieceService.getImage("/pieces/king_checkers_whiteh");
			}
		} else {
			if(GameService.getGame() == Games.CHESS) {
				sprite = PieceService.getImage("/pieces/king_black");
				hovered = PieceService.getImage("/pieces/king_blackh");
			} else if(GameService.getGame() == Games.CHECKERS) {
				sprite = PieceService.getImage("/pieces/king_checkers_black");
				hovered = PieceService.getImage("/pieces/king_checkers_blackh");
			}
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

		if(BooleanService.canDoMoves) {
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
		}
		return false;
	}

	@Override
	public Piece copy() {
		King p = new King(pieceService, getColor(), getCol(), getRow());
		p.setHasMoved(hasMoved());
		return p;
	}

	public void setPieceService(PieceService pieceService) {
		this.pieceService = pieceService;
	}
}
