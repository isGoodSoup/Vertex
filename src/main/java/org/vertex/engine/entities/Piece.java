package org.vertex.engine.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.enums.Theme;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.TypeID;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.service.PieceService;

import java.awt.image.BufferedImage;
import java.util.List;

public abstract class Piece {
	protected final long id;
	private static long NEXT_ID = 1;
	protected TypeID typeID;
	private int x, y;
	private int col, row, preCol, preRow;
	private static final double DEFAULT_SCALE = 1.0;
	private static final float MORE_SCALE = 0.5f;
	private double scale = DEFAULT_SCALE;
	private Tint color;
	private Piece otherPiece;
	private boolean hasMoved;
	private boolean isTwoStepsAhead;

	private static final Logger log = LoggerFactory.getLogger(Piece.class);

	public Piece(Tint color, int col, int row) {
		super();
		this.col = col;
		this.row = row;
		this.color = color;
		this.x = getX(col);
		this.y = getY(row);
		this.preCol = col;
		this.preRow = row;
		this.id = NEXT_ID++;
		getSprite();
		getKingSprites();
	}

	public BufferedImage getSprite() {
		String pieceName = getClass().getSimpleName().toLowerCase();
		Theme theme = Colors.getTheme();
		String color = theme.getColor(this.color);
		String suffix = "";
		String path ="/pieces/" + pieceName + "/" + pieceName + "_" + color;
		return PieceService.getImage(path);
	}

	public BufferedImage getKingSprites() {
		Theme theme = Colors.getTheme();
		String color = theme.getColor(this.color);
		String path = "/pieces/checker/checker_king_" + color;
		return PieceService.getImage(path);
	}

	public BufferedImage getFilteredSprite(BufferedImage image) {
		return Colorblindness.filter(image);
	}

	public long getID() {
		return id;
	}

	public TypeID getTypeID() {
		return typeID;
	}

	public void setTypeID(TypeID type) {
		this.typeID = type;
	}

	public int getPreCol() {
		return preCol;
	}

	public void setPreCol(int preCol) {
		this.preCol = preCol;
	}

	public int getPreRow() {
		return preRow;
	}

	public void setPreRow(int preRow) {
		this.preRow = preRow;
	}

    public double getDEFAULT_SCALE() {
        return DEFAULT_SCALE;
    }

	public double getScale() {
		return scale;
	}

    public void setScale(double scale) {
        this.scale = scale;
    }

	public float getMORE_SCALE() { return MORE_SCALE; }

	public Tint getColor() {
		return color;
	}

	public void setColor(Tint color) {
		this.color = color;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getCol() {
		return col;
	}

	public int getRow() {
		return row;
	}

	public int getX(int col) {
		return col * Board.getSquare();
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY(int row) {
		return row * Board.getSquare();
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCol(int x) {
		return (x + Board.getHalfSquare()) / Board.getSquare();
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getRow(int y) {
		return (y + Board.getHalfSquare()) / Board.getSquare();
	}

	public void setRow(int row) {
		this.row = row;
	}

	public Piece getOtherPiece() {
		return otherPiece;
	}

	public void setOtherPiece(Piece otherPiece) {
		this.otherPiece = otherPiece;
	}

	public boolean hasMoved() {
		return hasMoved;
	}

	public void setHasMoved(boolean hasMoved) {
		this.hasMoved = hasMoved;
	}

	public boolean isTwoStepsAhead() {
		return isTwoStepsAhead;
	}

	public void setTwoStepsAhead(boolean twoStepsAhead) {
		this.isTwoStepsAhead = twoStepsAhead;
	}

	public void resetEnPassant() {
		isTwoStepsAhead = false;
	}

	public abstract boolean canMove(int targetCol, int targetRow,
									List<Piece> board);

	public abstract Piece copy();

	public boolean isWithinBoard(int targetCol, int targetRow) {
        return PieceService.isWithinBoard(targetCol, targetRow);
    }

	public boolean isSameSquare(Piece piece, int targetCol, int targetRow) {
        return PieceService.isSameSquare(piece, targetCol, targetRow);
    }

	public Piece isColliding(int col, int row, List<Piece> board) {
	    return PieceService.isColliding(col, row, board);
	}

	public boolean isPathClear(Piece piece, int targetCol, int targetRow,
							   List<Piece> board) {
		return PieceService.isPathClear(piece, targetCol, targetRow, board);
	}

	public boolean isValidSquare(Piece piece, int targetCol, int targetRow,
							   List<Piece> board) {
		return PieceService.isValidSquare(piece, targetCol, targetRow, board);
	}
}
