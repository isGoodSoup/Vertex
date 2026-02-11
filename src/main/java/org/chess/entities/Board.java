package org.chess.entities;

import org.chess.render.ColorRender;
import org.chess.service.BooleanService;

import java.awt.*;

public class Board {
	private final int COL = 8;
	private final int ROW = 8;
	private static final int SQUARE = 64;
	private static final int HALF_SQUARE = SQUARE / 2;
	private static final Color EVEN = new Color(210, 165, 125);
	private static final Color ODD = new Color(175, 115, 70);
	private static final int PADDING = 4;
	private final String[][] squares = new String[ROW][COL];

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

	public static Color getEven() {
		return BooleanService.canBeColorblind || BooleanService.isDarkMode
				? ColorRender.getColor(EVEN, false) : EVEN;
	}

	public static Color getOdd() {
		return BooleanService.canBeColorblind || BooleanService.isDarkMode
				? ColorRender.getColor(ODD, false) : ODD;
	}


	public String[][] getSquares() {
		return squares;
	}
}
