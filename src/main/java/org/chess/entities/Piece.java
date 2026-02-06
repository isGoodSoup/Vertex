package org.chess.entities;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.BoardPanel;

public abstract class Piece {
	protected Type id;
	protected BufferedImage image;
	private int x, y;
	private int col, row, preCol, preRow;
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

	public int getIndex() {
		BoardPanel board = new BoardPanel();
		for(int index = 0; index < board.getPieces().size(); index++) {
			if(board.getPieces().get(index) == this) {
				return index;
			}
		}
		return 0;
	}

	public abstract boolean canMove(int targetCol, int targetRow, BoardPanel board);

	public boolean isWithinBoard(int targetCol, int targetRow) {
		if(targetCol >= 0 && targetCol <= 7 && targetRow >= 0 && targetRow <= 7) {
			return true;
		}
		return false;
	}

	public boolean isSameSquare(int targetCol, int targetRow) {
		if(targetCol == preCol && targetRow == preRow) {
			return true;
		}
		return false;
	}

	public Piece isColliding(int col, int row, BoardPanel board) {
	    for (Piece p : board.getPieces()) {
	        if (p.getCol() == col && p.getRow() == row) {
	        	return p;
	        }
	    }
	    return null;
	}
	
	public boolean isValidSquare(int targetCol, int targetRow, BoardPanel board) {
	    for (Piece p : board.getPieces()) {
	        if (p.getCol() == targetCol && p.getRow() == targetRow) {
	            return p.getColor() != this.getColor();
	        }
	    }
	    return true;
	}

	public boolean isPieceOnTheWay(int targetCol, int targetRow) {
		BoardPanel board = new BoardPanel();
		if(getRow() == targetRow) {
			int start = Math.min(getCol(), targetCol) + 1;
			int end = Math.max(getCol(), targetCol);
			for(int c = start; c < end; c++) {
				for(Piece piece : board.getPieces()) {
					if(piece.getCol() == c && piece.getRow() == targetRow) {
						otherPiece = piece;
						return true;
					}
				}
			}
		} else if(getCol() == targetCol) {
			int start = Math.min(getRow(), targetRow) + 1;
			int end = Math.max(getRow(), targetRow);
			for(int r = start; r < end; r++) {
				for(Piece piece : board.getPieces()) {
					if(piece.getCol() == targetCol && piece.getRow() == r) {
						otherPiece = piece;
						return true;
					}
				}
			}
		} else if(Math.abs(targetCol - getCol()) == Math.abs(targetRow - getRow())) {
			int colStep = (targetCol > getCol()) ? 1 : -1;
			int rowStep = (targetRow > getRow()) ? 1 : -1;
			int c = getCol() + colStep;
			int r = getRow() + rowStep;

			while (c != targetCol && r != targetRow) {
				for(Piece piece : board.getPieces()) {
					if(piece.getCol() == c && piece.getRow() == r) {
						otherPiece = piece;
						return true;
					}
				}
				c += colStep;
				r += rowStep;
			}
		}
		return false;
	}
	
	public BufferedImage scaleImage(BufferedImage src, int width, int height) {
	    BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = scaled.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
	    		RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	    g2.drawImage(src, 0, 0, width, height, null);
	    g2.dispose();
	    return scaled;
	}
	
	public BufferedImage getImage(String path) {
	    BufferedImage img = null;
	    try {
	        img = ImageIO.read(getClass().getResourceAsStream(path + ".png"));
	        img = scaleImage(img, Board.getSquare(), Board.getSquare());
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return img;
	}


	public void draw(Graphics2D g2) {
		g2.drawImage(image, x, y, null);
	}
}
