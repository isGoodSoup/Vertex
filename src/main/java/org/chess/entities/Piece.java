package org.chess.entities;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.imageio.ImageIO;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.BoardPanel;

public abstract class Piece {
	protected Type id;
	protected BufferedImage image;
	private int x, y;
	private int col, row, preCol, preRow;
    private static final double DEFAULT_SCALE = 1.0;
	private double scale = DEFAULT_SCALE;
	private Tint color;
	private Piece otherPiece;
	private boolean hasMoved;
	private boolean isTwoStepsAhead;

	public Piece(Tint color, int col, int row) {
		super();
		this.col = col;
		this.row = row;
		this.color = color;
		this.x = getX(col);
		this.y = getY(row);
		this.preCol = col;
		this.preRow = row;
	}

	public Type getId() {
		return id;
	}

	public void setId(Type id) {
		this.id = id;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
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
		return !hasMoved;
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

	public void updatePos() {
		if(id == Type.PAWN) {
			if(Math.abs(row - preRow) == 2) {
				isTwoStepsAhead = true;
			}
		}
		x = getX(col);
		y = getY(row);
		preCol = getCol(x);
		preRow = getRow(y);
		hasMoved = true;
	}

	public void resetPos() {
		col = preCol;
		row = preRow;
		x = getX(col);
		y = getY(row);
	}

	public abstract boolean canMove(int targetCol, int targetRow, BoardPanel board);

	public abstract boolean canMove(int targetCol, int targetRow,
									List<Piece> board);

	public abstract Piece copy();

	public boolean isWithinBoard(int targetCol, int targetRow) {
        return targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7;
    }

	public boolean isSameSquare(int targetCol, int targetRow) {
        return targetCol == preCol && targetRow == preRow;
    }

	public Piece isColliding(int col, int row, BoardPanel board) {
	    for (Piece p : board.getPieces()) {
	        if (p.getCol() == col && p.getRow() == row) {
	        	return p;
	        }
	    }
	    return null;
	}

	public boolean isPathClear(int targetCol, int targetRow, List<Piece> board) {
		int colDiff = targetCol - getCol();
		int rowDiff = targetRow - getRow();

		if (Math.abs(colDiff) != Math.abs(rowDiff)) {
			return false;
		}

		int colStep = Integer.signum(colDiff);
		int rowStep = Integer.signum(rowDiff);

		int c = getCol() + colStep;
		int r = getRow() + rowStep;

		while (c != targetCol && r != targetRow) {
			for (Piece p : board) {
				if (p == this) { continue; }
				if (p.getCol() == c && p.getRow() == r) {
					return false;
				}
			}
			c += colStep;
			r += rowStep;
		}
		return true;
	}

	public boolean isValidSquare(int targetCol, int targetRow,
							   List<Piece> board) {
		for(Piece p : board) {
			if(p.getCol() == targetCol && p.getRow() == targetRow) {
				return p.getColor() != this.getColor();
			}
		}
		return true;
	}

	public boolean isValidSquare(int targetCol, int targetRow, BoardPanel board) {
	    for (Piece p : board.getPieces()) {
	        if (p.getCol() == targetCol && p.getRow() == targetRow) {
	            return p.getColor() != this.getColor();
	        }
	    }
	    return true;
	}

	public BufferedImage getImage(String path) {
	    BufferedImage img = null;
	    try {
			img = ImageIO.read(Objects.requireNonNull(
					getClass().getResourceAsStream(path + ".png")));
		} catch (IOException e) {
	        System.err.println(e.getMessage());
	    }
	    return img;
	}

	public void draw(Graphics2D g2) {
		int square = Board.getSquare();

		int drawSize = (int) (square * scale);
		int offset = (square - drawSize) / 2;

		g2.drawImage(
				image,
				x + offset,
				y + offset,
				drawSize,
				drawSize,
				null
		);
	}
}
