package org.chess.entities;

import java.io.Serializable;

public class Board implements Serializable {
	private final int COL = 8;
	private final int ROW = 8;
	private static final int SQUARE = 64;
	private static final int HALF_SQUARE = SQUARE/2;
	private static final int PADDING = 8;
	private Piece[][] pieces = new Piece[ROW][COL];

	public Board() {}

	public int getCOL() {
		return COL;
	}

	public int getROW() {
		return ROW;
	}

	public static int getSquare() {
		return SQUARE;
	}

	public static int getHalfSquare() {
		return HALF_SQUARE;
	}

	public static int getPadding() {
		return PADDING;
	}

	public Piece[][] getPieces() {
		return pieces;
	}

	public void setPieces(Piece[][] pieces) {
		this.pieces = pieces;
	}
}
