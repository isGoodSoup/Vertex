package org.chess.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.chess.entities.Bishop;
import org.chess.entities.Board;
import org.chess.entities.King;
import org.chess.entities.Knight;
import org.chess.entities.Pawn;
import org.chess.entities.Piece;
import org.chess.entities.Queen;
import org.chess.entities.Rook;
import org.chess.enums.Tint;
import org.chess.enums.Type;

public class BoardPanel extends JPanel implements Runnable {
	private static final long serialVersionUID = -5189356863277669172L;
	private static final int WIDTH = 512;
	private static final int HEIGHT = 512;
	private final int FPS = 60;
	private Thread thread;

	private final Board board;
	private final Mouse mouse;
	private Tint currentTurn = Tint.WHITE;

	private List<Piece> pieces = new ArrayList<>();
	private List<Piece> promoted = new ArrayList<>();

	private Piece currentPiece;
	private Piece capturedPiece;
	private Piece castlingPiece;
	private Piece promotingPawn;
	private Piece checkingPiece;
	private int hoverCol = -1;
	private int hoverRow = -1;
	private int dragOffsetX;
	private int dragOffsetY;
    private Tint promotionColor;

	private boolean canMove;
	private boolean validSquare;
	private boolean isPromoted;
	private boolean isDragging;
	private boolean isGameOver;

	public BoardPanel() {
		super();
		this.board = new Board();
		this.mouse = new Mouse();
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.BLACK);
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		setPieces();
	}

	public Thread getThread() {
		return thread;
	}

	public int getWidth() {
		return WIDTH;
	}

	public int getHeight() {
		return HEIGHT;
	}

	public int getFPS() {
		return FPS;
	}

	public Tint getCurrentTurn() {
		return currentTurn;
	}

	public void setCurrentTurn(Tint currentTurn) {
		this.currentTurn = currentTurn;
	}

	public List<Piece> getPieces() {
		return pieces;
	}

	public Board getBoard() {
		return board;
	}

	public boolean isCanMove() {
		return canMove;
	}

	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}

	public boolean isValidSquare() {
		return validSquare;
	}

	public void setValidSquare(boolean validSquare) {
		this.validSquare = validSquare;
	}

	public Piece getCastlingPiece() {
		return castlingPiece;
	}

	public void setCastlingPiece(Piece castlingPiece) {
		this.castlingPiece = castlingPiece;
	}

	public List<Piece> getPromoted() {
		return promoted;
	}

	public void setPromoted(List<Piece> promoted) {
		this.promoted = promoted;
	}

	public boolean isPromoted() {
		return isPromoted;
	}

	public void setPromoted(boolean isPromoted) {
		this.isPromoted = isPromoted;
	}

	public Piece getPromotingPawn() {
		return promotingPawn;
	}

	public void setPromotingPawn(Piece promotingPawn) {
		this.promotingPawn = promotingPawn;
	}

	public Tint getPromotionColor() {
		return promotionColor;
	}

	public void setPromotionColor(Tint promotionColor) {
		this.promotionColor = promotionColor;
	}

	public Piece getCheckingPiece() {
		return checkingPiece;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public void launch() {
		thread = new Thread(this);
		thread.start();
	}

	public void setPieces() {
		for(int col = 0; col < 8; col++) {
			pieces.add(new Pawn(Tint.WHITE, col, 6));
			pieces.add(new Pawn(Tint.BLACK, col, 1));
		}
		pieces.add(new Rook(Tint.WHITE, 0, 7));
		pieces.add(new Rook(Tint.WHITE, 7, 7));
		pieces.add(new Rook(Tint.BLACK, 0, 0));
		pieces.add(new Rook(Tint.BLACK, 7, 0));
		pieces.add(new Knight(Tint.WHITE, 1, 7));
		pieces.add(new Knight(Tint.WHITE, 6, 7));
		pieces.add(new Knight(Tint.BLACK, 1, 0));
		pieces.add(new Knight(Tint.BLACK, 6, 0));
		pieces.add(new Bishop(Tint.WHITE, 2, 7));
		pieces.add(new Bishop(Tint.WHITE, 5, 7));
		pieces.add(new Bishop(Tint.BLACK, 2, 0));
		pieces.add(new Bishop(Tint.BLACK, 5, 0));
		pieces.add(new Queen(Tint.WHITE, 3, 7));
		pieces.add(new Queen(Tint.BLACK, 3, 0));
		pieces.add(new King(Tint.WHITE, 4, 7));
		pieces.add(new King(Tint.BLACK, 4, 0));
	}

	@Override
	public void run() {
		double drawInterval = 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;

		while (thread != null) {
			currentTime = System.nanoTime();
			delta += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;

			if(delta >= 1) {
				update();
				repaint();
				delta--;
			}
		}
	}

	private void update() {
		hoverCol = mouse.getX() / Board.getSquare();
		hoverRow = mouse.getY() / Board.getSquare();

		if (isPromoted) {
			promotion();
			mouse.setClicked(false);
			return;
		}

		if (mouse.isPressed() && !isDragging && currentPiece == null) {
			for (Piece p : pieces) {
				if (p.getColor() == currentTurn &&
						p.getCol() == hoverCol &&
						p.getRow() == hoverRow) {
					currentPiece = p;
					isDragging = true;
					dragOffsetX = mouse.getX() - p.getX();
					dragOffsetY = mouse.getY() - p.getY();
					currentPiece.setPreCol(p.getCol());
					currentPiece.setPreRow(p.getRow());
					break;
				}
			}
		}

		if (isDragging && mouse.isPressed() && currentPiece != null) {
			currentPiece.setX(mouse.getX() - dragOffsetX);
			currentPiece.setY(mouse.getY() - dragOffsetY);
		}

		if (isDragging && mouse.isClicked() && currentPiece != null) {
			isDragging = false;

			int targetCol = mouse.getX() / Board.getSquare();
			int targetRow = mouse.getY() / Board.getSquare();

			boolean legal =
					currentPiece.canMove(targetCol, targetRow, this) &&
							!wouldLeaveKingInCheck(currentPiece, targetCol, targetRow);

			if (legal) {
				capturedPiece = null;
				for (Piece p : pieces) {
					if (p != currentPiece &&
							p.getCol() == targetCol &&
							p.getRow() == targetRow) {
						capturedPiece = p;
						break;
					}
				}

				if (capturedPiece != null) {
					pieces.remove(capturedPiece);
				}

				if (currentPiece instanceof King) {
					int colDiff = targetCol - currentPiece.getCol();

					if (Math.abs(colDiff) == 2 && !currentPiece.hasMoved()) {
						int step = (colDiff > 0) ? 1 : -1;
						int rookStartCol = (colDiff > 0) ? 7 : 0;
						int rookTargetCol = (colDiff > 0) ? 5 : 3;

						if (isKingInCheck(currentPiece.getColor()) ||
								wouldLeaveKingInCheck(currentPiece,
										currentPiece.getCol() + step,
										currentPiece.getRow())) {

							currentPiece.updatePos();
							currentPiece = null;
							return;
						}

						boolean pathClear = true;
						for (int c = currentPiece.getCol() + step; c != rookStartCol; c += step) {
							if (boardHasPieceAt(c, currentPiece.getRow())) {
								pathClear = false;
								break;
							}
						}

						if (pathClear) {
							for (Piece p : pieces) {
								if (p instanceof Rook &&
										p.getCol() == rookStartCol &&
										p.getRow() == currentPiece.getRow() &&
										!p.hasMoved()) {

									p.setCol(rookTargetCol);
									p.updatePos();
									p.setHasMoved(true);
									break;
								}
							}
						}
					}
				}

				currentPiece.setCol(targetCol);
				currentPiece.setRow(targetRow);
				currentPiece.updatePos();
				currentPiece.setHasMoved(true);

				if (currentPiece instanceof Pawn) {
					int oldRow = currentPiece.getPreRow();
					int movedSquares = Math.abs(targetRow - oldRow);

					if (capturedPiece == null && Math.abs(targetCol -
							currentPiece.getPreCol()) == 1) {
						int dir = (currentPiece.getColor() == Tint.WHITE) ? -1 : 1;
						if (targetRow - oldRow == dir) {
							for (Piece p : pieces) {
								if (p instanceof Pawn &&
										p.getColor() != currentPiece.getColor() &&
										p.getCol() == targetCol &&
										p.getRow() == oldRow &&
										p.isTwoStepsAhead()) {
									capturedPiece = p;
									pieces.remove(p);
									break;
								}
							}
						}
					}
					currentPiece.setTwoStepsAhead(movedSquares == 2);
				}

				for (Piece p : pieces) {
					if (p instanceof Pawn && p.getColor() != currentPiece.getColor()) {
						p.resetEnPassant();
					}
				}

				if (canPromote()) {
					isPromoted = true;
					promotionColor = currentPiece.getColor();
				} else {
					currentTurn = (currentTurn == Tint.WHITE)
							? Tint.BLACK : Tint.WHITE;
					isKingInCheck(currentTurn);
				}

			} else {
				currentPiece.updatePos();
			}
			currentPiece = null;
		}
	}

	private boolean canPromote() {
		if(currentPiece.getId() == Type.PAWN) {
			if((currentPiece.getColor() == Tint.WHITE && currentPiece.getRow() == 0)
					|| (currentPiece.getColor() == Tint.BLACK && currentPiece.getRow() == 7)) {
				promotingPawn = currentPiece;
				promoted.clear();
				promoted.add(new Rook(currentPiece.getColor(), 9, 2));
				promoted.add(new Knight(currentPiece.getColor(), 9, 3));
				promoted.add(new Bishop(currentPiece.getColor(), 9, 4));
				promoted.add(new Queen(currentPiece.getColor(), 9, 5));
				return true;
			}
		}
		return false;
	}

	private void promotion() {
		if(isPromoted && mouse.isClicked()) {
			int startX = WIDTH - 4 * Board.getSquare();
			int startY = HEIGHT - Board.getSquare();
			int size = Board.getSquare();

			Type[] options = { Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT };

			for(int i = 0; i < options.length; i++) {
				int x0 = startX + i * size;
				int x1 = x0 + size;
				int y0 = startY;
				int y1 = startY + size;

				if(mouse.getX() >= x0 && mouse.getX() <= x1 && mouse.getY() >= y0 && mouse.getY() <= y1) {
					pieces.remove(promotingPawn);
					Piece promotedPiece = switch (options[i]) {
					case QUEEN -> new Queen(promotingPawn.getColor(), promotingPawn.getCol(), promotingPawn.getRow());
					case ROOK -> new Rook(promotingPawn.getColor(), promotingPawn.getCol(), promotingPawn.getRow());
					case BISHOP -> new Bishop(promotingPawn.getColor(), promotingPawn.getCol(), promotingPawn.getRow());
					case KNIGHT -> new Knight(promotingPawn.getColor(), promotingPawn.getCol(), promotingPawn.getRow());
					default -> null;
					};
					pieces.add(promotedPiece);
					promotingPawn = null;
					isPromoted = false;
					currentTurn = (currentTurn == Tint.WHITE) ? Tint.BLACK : Tint.WHITE;
					currentPiece = null;
					break;
				}
			}
			return;
		}
	}

	private boolean isKingInCheck(Tint kingColor) {
		Piece king = getKing(kingColor);

		for(Piece p : pieces) {
			if(p.getColor() != kingColor) {
				if(p.canMove(king.getCol(), king.getRow(), this)) {
					checkingPiece = p;
					return true;
				}
			}
		}

		checkingPiece = null;
		return false;
	}

	private Piece getKing(Tint color) {
		for(Piece p : pieces) {
			if(p instanceof King && p.getColor() == color) {
				return p;
			}
		}
		throw new IllegalStateException("King not found for color: " + color);
	}

	private boolean wouldLeaveKingInCheck(Piece piece, int targetCol, int targetRow) {
		int oldCol = piece.getCol();
		int oldRow = piece.getRow();

		Piece captured = null;
		for(Piece p : pieces) {
			if(p != piece && p.getCol() == targetCol && p.getRow() == targetRow) {
				captured = p;
				break;
			}
		}

		if(captured != null) {
			pieces.remove(captured);
		}

		piece.setCol(targetCol);
		piece.setRow(targetRow);
		piece.updatePos();

		boolean inCheck = isKingInCheck(piece.getColor());

		piece.setCol(oldCol);
		piece.setRow(oldRow);
		piece.updatePos();

		if(captured != null) {
			pieces.add(captured);
		}
		return inCheck;
	}

	public boolean boardHasPieceAt(int col, int row) {
		for(Piece p : pieces) {
			if(p.getCol() == col && p.getRow() == row)
				return true;
		}
		return false;
	}

	private void drawPromotionOptions(Graphics2D g2) {
		if(!isPromoted)
			return;

		int startX = WIDTH - 4 * Board.getSquare();
		int startY = HEIGHT - Board.getSquare();
		int size = Board.getSquare();

		g2.setColor(new Color(0, 0, 0, 200));
		g2.fillRoundRect(startX, startY, size * 4, size, 10, 10);

		Type[] options = { Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT };

		for(int i = 0; i < options.length; i++) {
			Piece temp;
			switch (options[i]) {
			case QUEEN:
				temp = new Queen(promotionColor, startX / size + i, startY / size);
				break;
			case ROOK:
				temp = new Rook(promotionColor, startX / size + i, startY / size);
				break;
			case BISHOP:
				temp = new Bishop(promotionColor, startX / size + i, startY / size);
				break;
			case KNIGHT:
				temp = new Knight(promotionColor, startX / size + i, startY / size);
				break;
			default:
				continue;
			}
			temp.setX(startX + i * size);
			temp.setY(startY);
			temp.draw(g2);
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		board.draw(g2);

		for (Piece p : pieces) {
			if (p != currentPiece) {
				p.draw(g2);
			}
		}

		if (currentPiece != null) {
			currentPiece.draw(g2);
		}

		if (currentPiece != null) {
			boolean canMoveHere = currentPiece.canMove(hoverCol, hoverRow, this) &&
					!wouldLeaveKingInCheck(currentPiece, hoverCol, hoverRow);

			g2.setStroke(new BasicStroke(3));

			if (!canMoveHere) {
				g2.setColor(new Color(255, 0, 0, 150));
				g2.drawRect(hoverCol * Board.getSquare(),
						hoverRow * Board.getSquare(),
						Board.getSquare(), Board.getSquare());
			} else {
				g2.setColor(new Color(0, 255, 0, 150));
				g2.drawRect(hoverCol * Board.getSquare(),
						hoverRow * Board.getSquare(),
						Board.getSquare(), Board.getSquare());
			}
		}
		drawPromotionOptions(g2);
	}
}
