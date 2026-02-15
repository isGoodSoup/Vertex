package org.vertex.engine.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.Type;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.service.GUIService;
import org.vertex.engine.service.PieceService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class Piece {
	protected Type id;
	protected transient BufferedImage sprite;
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
		this.sprite = loadSprite(this);
	}

	public BufferedImage loadSprite(Piece piece) {
		String name = piece.getClass().getSimpleName().toLowerCase();
        return getImage("/pieces/" + name + "_" + themeColor);
    }

	public BufferedImage getImage(String path) {
		InputStream stream = getClass().getResourceAsStream(path + ".png");
		if (stream == null) {
			log.error("Resource not found: {}.png", path);
			return null;
		}
		return ImageIO.read(stream);
	}

	public Type getId() {
		return id;
	}

	public void setId(Type id) {
		this.id = id;
	}

	public BufferedImage getSprite() {
		return sprite;
	}

	public BufferedImage getFilteredSprite(BufferedImage image) {
		return Colorblindness.filter(image);
	}

	public void setSprite(BufferedImage sprite) {
		this.sprite = sprite;
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

	public void restoreFromSave(int row, int col, GUIService guiService) {
		this.row = row;
		this.col = col;
		this.preRow = row;
		this.preCol = col;
		this.x = getX(col);
		this.y = getY(row);
		loadSprite(guiService);
	}
}
