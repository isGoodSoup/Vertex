package org.chess.gui;

import org.chess.records.Move;
import org.chess.entities.*;
import org.chess.enums.GameState;
import org.chess.enums.PlayState;
import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.records.MoveScore;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class BoardPanel extends JPanel implements Runnable {
	@Serial
    private static final long serialVersionUID = -5189356863277669172L;
	private static int WIDTH;
	private static int HEIGHT;
	private final int FPS = 60;
	private Thread thread;

    private final Piece[][] boardState;
	private final Board board;
	private final Mouse mouse;
    private static final Random random = new Random();
	private Tint turn = Tint.WHITE;

	private GameState state;
	private PlayState mode;

	private final List<Piece> pieces;
	private Piece currentPiece;
    private Piece castlingPiece;
	private Piece promotingPawn;
	private Piece checkingPiece;
    private int dragOffsetX;
	private int dragOffsetY;
    private long promotionStartTime = -1;
    private final long PROMOTION_DELAY = 3000;
    private Tint promotionColor;

    private final BufferedImage logo;
	private final BufferedImage yes;
	private final BufferedImage no;
	private final BufferedImage whitePawn, blackPawn;
	private final BufferedImage whiteRook, blackRook;
	private final BufferedImage whiteKnight, blackKnight;
	private final BufferedImage whiteBishop, blackBishop;
	private final BufferedImage whiteQueen, blackQueen;
	private final BufferedImage whiteKing, blackKing;

	private boolean canMove;
	private boolean validSquare;
    private boolean isPromotionPending;
	private boolean isDragging;
	private boolean isLegal;
    private boolean isAIPlaying;
    private boolean isChaosActive;
	private boolean isGameOver;

	private static final String[] optionsMenu = { "PLAY AGAINST", "EXIT" };
	private static final String[] optionsMode = { "PLAYER", "AI", "CHAOS" };
	private static final int MENU_SPACING = 40;
	private static final int MENU_START_Y = 80;
    private static final int MENU_FONT = 32;
    private static Color background;
    private static Color foreground;

	public BoardPanel() {
		super();
		this.board = new Board();
        this.pieces = new ArrayList<>();
		this.mouse = new Mouse();
        this.boardState = new Piece[board.getCOL()][board.getROW()];
		this.state = GameState.MENU;
        this.mode = null;
		WIDTH = Board.getSquare() * 8;
		HEIGHT = Board.getSquare() * 8;
        drawRandomBackground(getBoolean());
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setBackground(Color.BLACK);
		addMouseMotionListener(mouse);
		addMouseListener(mouse);
		setPieces();

        try {
            logo = getImage("/ui/logo");
			yes = getImage("/ticks/tick_yes");
			no = getImage("/ticks/tick_no");
			whitePawn = getImage("/pieces/pawn-h");
			blackPawn = getImage("/pieces/pawn-bh");
			whiteRook = getImage("/pieces/rook-h");
			blackRook = getImage("/pieces/rook-bh");
			whiteKnight = getImage("/pieces/knight-h");
			blackKnight = getImage("/pieces/knight-bh");
			whiteBishop = getImage("/pieces/bishop-h");
			blackBishop = getImage("/pieces/bishop-bh");
			whiteQueen = getImage("/pieces/queen-h");
			blackQueen = getImage("/pieces/queen-bh");
			whiteKing = getImage("/pieces/king-h");
			blackKing = getImage("/pieces/king-bh");
		} catch (IOException e) {
            throw new RuntimeException(e);
        }
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

	public Tint getTurn() {
		return turn;
	}

	public void setTurn(Tint turn) {
		this.turn = turn;
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

	public Piece getPromotingPawn() {
		return promotingPawn;
	}

	public void setPromotingPawn(Piece promotingPawn) {
		this.promotingPawn = promotingPawn;
	}

	public Tint getPromotionColor() {
		return promotionColor;
	}

	public Piece getCheckingPiece() {
		return checkingPiece;
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public GameState getState() {
		return state;
	}

    private void startBoard() {
        pieces.clear();
        setPieces();
        turn = Tint.WHITE;
        isGameOver = false;
        currentPiece = null;
        state = GameState.BOARD;
    }

    private Rectangle getHitbox(int y) {
        Rectangle hitbox = new Rectangle(
                WIDTH/2 - 100,
                y - 30,
                200,
                40
        );
        return hitbox;
    }

	public void launch() {
		thread = new Thread(this);
		thread.start();
	}

	public void setPieces() {
        pieces.clear();
        clearBoardState();

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

	private Piece getHoveredPiece() {
		int hoverCol = mouse.getX() / Board.getSquare();
		int hoverRow = mouse.getY() / Board.getSquare();

		for(Piece p : pieces) {
			if(p.getCol() == hoverCol && p.getRow() ==
					hoverRow && p != currentPiece) {
                if(isAIPlaying && p.getColor() != Tint.WHITE || isDragging) {
                    return null;
                }
				return p;
			}
		}
		return null;
	}

	private BufferedImage getHoverSprite(Piece p) {
		if(p instanceof Pawn) return (p.getColor() == Tint.WHITE) ?
				whitePawn : blackPawn;
		if(p instanceof Rook) return (p.getColor() == Tint.WHITE) ?
				whiteRook : blackRook;
		if(p instanceof Knight) return (p.getColor() == Tint.WHITE) ?
				whiteKnight : blackKnight;
		if(p instanceof Bishop) return (p.getColor() == Tint.WHITE) ?
				whiteBishop : blackBishop;
		if(p instanceof Queen) return (p.getColor() == Tint.WHITE) ?
				whiteQueen : blackQueen;
		if(p instanceof King) return (p.getColor() == Tint.WHITE) ?
				whiteKing : blackKing;
		return null;
	}

    private void clearBoardState() {
        for(int c = 0; c < 8; c++) {
            for(int r = 0; r < 8; r++) {
                boardState[c][r] = null;
            }
        }
    }

    private void addPiece(Piece p) {
        pieces.add(p);
        boardState[p.getCol()][p.getRow()] = p;
    }

    private void removePiece(Piece p) {
        pieces.remove(p);
        boardState[p.getCol()][p.getRow()] = null;
    }

    private void movePiece(Piece p, int newCol, int newRow) {
        boardState[p.getCol()][p.getRow()] = null;
        p.setCol(newCol);
        p.setRow(newRow);
        p.updatePos();
        boardState[newCol][newRow] = p;
    }

    @Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		switch(state) {
			case MENU -> drawGraphics(g2, optionsMenu);
			case MODE -> drawGraphics(g2, optionsMode);
			case BOARD -> drawBoard(g2);
		}
	}

    private void drawPromotions(Graphics2D g2) {
        if(!isPromotionPending) { return; }

        int size = Board.getSquare();
        int totalWidth = size * 4;
        int startX = (WIDTH - totalWidth) / 2;
        int startY = (HEIGHT - size) / 2;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        Type[] options = { Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT };

        int hoverIndex = -1;
        for(int i = 0; i < options.length; i++) {
            int x0 = startX + i * size;
            int x1 = x0 + size;
            int y1 = startY + size;

            if(mouse.getX() >= x0 && mouse.getX() <= x1 &&
                    mouse.getY() >= startY && mouse.getY() <= y1) {
                hoverIndex = i;
                break;
            }
        }

        for(int i = 0; i < options.length; i++) {
            Piece temp;
            switch (options[i]) {
                case QUEEN -> temp = new Queen(promotionColor, 0, 0);
                case ROOK -> temp = new Rook(promotionColor, 0, 0);
                case BISHOP -> temp = new Bishop(promotionColor, 0, 0);
                case KNIGHT -> temp = new Knight(promotionColor, 0, 0);
                default -> { continue; }
            }

            int x = startX + i * size;

            temp.setX(x);
            temp.setY(startY);

            if(i == hoverIndex) {
                temp.setScale(temp.getScale() + 0.5f);
            } else {
                temp.setScale(temp.getDEFAULT_SCALE());
            }

            temp.draw(g2);
        }
    }

    private boolean getBoolean() {
        return random.nextBoolean();
    }

    public BufferedImage getImage(String path) throws IOException {
        return ImageIO.read(Objects.requireNonNull(
                getClass().getResourceAsStream(path + ".png")));
    }

    private void drawRandomBackground(boolean isColor) {
        background = isColor ? Board.getEven() : Board.getOdd();
        foreground = isColor ? Board.getOdd() : Board.getEven();
    }

    private void drawTick(Graphics2D g2, boolean isLegal) {
        double scale = currentPiece.getScale();
        int size = (int) (Board.getSquare() * scale);
        int x = currentPiece.getX() - size / 2;
        int y = currentPiece.getY() - size / 2;
        BufferedImage image = isLegal ? yes : no;
        g2.drawImage(image, x, y, size, size, null);
    }

    private void drawLogo(Graphics2D g2) {
        g2.drawImage(logo,WIDTH/3 + 5,HEIGHT/7,
                logo.getWidth()/3,logo.getHeight()/3, null);
    }

    private void drawGraphics(Graphics2D g2, String[] options) {
        g2.setColor(background);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setFont(Board.getFont(MENU_FONT));
        g2.setColor(foreground);
        drawLogo(g2);

        int startY = HEIGHT/2 + MENU_START_Y;
        int spacing = MENU_SPACING;

        for(int i = 0; i < options.length; i++) {
            int textWidth = g2.getFontMetrics().stringWidth(options[i]);
            int x = (WIDTH - textWidth)/2;
            int y = startY + i * spacing;
            boolean isHovered = getHitbox(y).contains(mouse.getX(),
                    mouse.getY());
            g2.setColor(isHovered ? Color.WHITE : foreground);
            g2.drawString(options[i], x, y);
        }
    }

    private void drawBoard(Graphics2D g2) {
        board.draw(g2);
        Piece hovered = getHoveredPiece();
        g2.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );
        for(Piece p : pieces) {
            if(p != currentPiece) {
                if(p == hovered) {
                    BufferedImage hoverImage = getHoverSprite(p);
                    int size = (int)(Board.getSquare() * p.getScale());
                    g2.drawImage(hoverImage, p.getX(), p.getY(), size, size, null);
                } else {
                    p.draw(g2);
                }
            }
        }

        if(currentPiece != null) {
            currentPiece.draw(g2);
        }

        if(currentPiece != null && isDragging) {
            drawTick(g2, isLegal);
        }
        drawPromotions(g2);
    }

	@Override
	public void run() {
		double drawInterval = (double) 1000000000 / FPS;
		double delta = 0;
		long lastTime = System.nanoTime();
		long currentTime;

		while (thread != null) {
			currentTime = System.nanoTime();
			delta += (currentTime - lastTime) / drawInterval;
			lastTime = currentTime;

			if(delta >= 1) {
				update();
                if(!isAIPlaying || turn == Tint.WHITE
                        || isPromotionPending) {
                    repaint();
                }
                delta--;
			}
		}
	}

	private void update() {
        switch(state) {
            case MENU -> {
                handleMenuInput();
                return;
            }
            case MODE -> {
                setPlayState();
                return;
            }
            default -> getGame();
        }

        switch(mode) {
            case PLAYER -> isAIPlaying = false;
            case AI -> isAIPlaying = true;
            case CHAOS -> isChaosActive = true;
        }
	}

    private void getGame() {
        if(state != GameState.BOARD) {
            return;
        }

        if(isAIPlaying && turn == Tint.BLACK) {
            return;
        }

        int hoverCol = mouse.getX() / Board.getSquare();
        int hoverRow = mouse.getY() / Board.getSquare();

        if(isPromotionPending) {
            promotion();
            mouse.setClicked(false);
        }

        if(mouse.isPressed() && !isDragging && currentPiece == null) {
            for(Piece p : pieces) {
                if(p.getColor() == turn &&
                        p.getCol() == hoverCol &&
                        p.getRow() == hoverRow) {
                    currentPiece = p;
                    currentPiece.setScale(currentPiece.getDEFAULT_SCALE() + 0.5f);
                    isDragging = true;
                    dragOffsetX = mouse.getX() - p.getX();
                    dragOffsetY = mouse.getY() - p.getY();
                    currentPiece.setPreCol(p.getCol());
                    currentPiece.setPreRow(p.getRow());
                    break;
                }
            }
        }

        if(isDragging && mouse.isPressed() && currentPiece != null) {
            currentPiece.setX(mouse.getX() - dragOffsetX);
            currentPiece.setY(mouse.getY() - dragOffsetY);
            int targetCol = mouse.getX() / Board.getSquare();
            int targetRow = mouse.getY() / Board.getSquare();
            isLegal = currentPiece.canMove(targetCol, targetRow, this) &&
                            !wouldLeaveKingInCheck(currentPiece, targetCol, targetRow);
        }

        if(isDragging && mouse.isClicked() && currentPiece != null) {
            isDragging = false;
            int targetCol = mouse.getX() / Board.getSquare();
            int targetRow = mouse.getY() / Board.getSquare();

            if(isLegal) {
                Piece captured = getPieceAt(targetCol, targetRow);
                if(captured != null) {
                    removePiece(captured);
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
                        for(int c = currentPiece.getCol() + step; c != rookStartCol; c += step) {
                            if(getPieceAt(c, currentPiece.getRow()) != null) {
                                pathClear = false;
                                break;
                            }
                        }

                        if(pathClear) {
                            for(Piece p : pieces) {
                                if(p instanceof Rook &&
                                        p.getCol() == rookStartCol &&
                                        p.getRow() == currentPiece.getRow() &&
                                        p.hasMoved()) {

                                    p.setCol(rookTargetCol);
                                    p.updatePos();
                                    p.setHasMoved(true);
                                    break;
                                }
                            }
                        }
                    }
                }

                movePiece(currentPiece, targetCol, targetRow);
                currentPiece.setHasMoved(true);

                if (currentPiece instanceof Pawn) {
                    int oldRow = currentPiece.getPreRow();
                    int movedSquares = Math.abs(targetRow - oldRow);

                    if (captured == null && Math.abs(targetCol -
                            currentPiece.getPreCol()) == 1) {
                        int dir = (currentPiece.getColor() == Tint.WHITE) ? -1 : 1;
                        if (targetRow - oldRow == dir) {
                            for (Piece p : pieces) {
                                if (p instanceof Pawn &&
                                        p.getColor() != currentPiece.getColor() &&
                                        p.getCol() == targetCol &&
                                        p.getRow() == oldRow &&
                                        p.isTwoStepsAhead()) {
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

                if (checkPromotion(currentPiece)) {
                    isPromotionPending = true;
                    promotionColor = currentPiece.getColor();
                } else {
                    turn = (turn == Tint.WHITE)
                            ? Tint.BLACK : Tint.WHITE;

                    if (currentPiece != null) {
                        currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
                        currentPiece = null;
                    }

                    if (isAIPlaying && !isPromotionPending) {
                        Move move = getAiTurn();
                        if (move != null) {
                            executeMove(move);
                        }
                    }

                    if (isKingInCheck(turn) && getAiTurn() == null) {
                        isGameOver = true;
                    }

                }

            } else {
                currentPiece.updatePos();
            }
            if(currentPiece != null) {
                currentPiece.setScale(currentPiece.getDEFAULT_SCALE());
                currentPiece = null;
            }
        }
    }

    private boolean checkPromotion(Piece p) {
        if(p instanceof Pawn) {
            if((p.getColor() == Tint.WHITE && p.getRow() == 0) ||
                    (p.getColor() == Tint.BLACK && p.getRow() == 7)) {
                isPromotionPending = true;
                promotingPawn = p;
                return true;
            }
        }
        return false;
    }

    private void promotion() {
        if(!isPromotionPending) { return; }

        int size = Board.getSquare();
        int totalWidth = size * 4;
        int startX = (WIDTH - totalWidth) / 2;
        int startY = (HEIGHT - size) / 2;

        Type[] options = {Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT};
        int selectedIndex = -1;

        if (promotionStartTime == -1) {
            promotionStartTime = System.currentTimeMillis();
        }

        for(int i = 0; i < options.length; i++) {
            int x0 = startX + i * size;
            int x1 = x0 + size;

            if(mouse.isClicked() &&
                    mouse.getX() >= x0 && mouse.getX() <= x1 &&
                    mouse.getY() >= startY && mouse.getY() <= startY + size) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex == -1 && System.currentTimeMillis() - promotionStartTime >= PROMOTION_DELAY) {
            selectedIndex = 0; // QUEEN by default
        }

        if(selectedIndex == -1) { selectedIndex = 0; }
        pieces.remove(promotingPawn);
        Piece promotedPiece = switch(options[selectedIndex]) {
            case QUEEN -> new Queen(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            case ROOK -> new Rook(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            case BISHOP -> new Bishop(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            case KNIGHT -> new Knight(promotingPawn.getColor(),
                    promotingPawn.getCol(), promotingPawn.getRow());
            default -> throw new IllegalStateException("Unexpected promotion type");
        };

        pieces.add(promotedPiece);
        boardState[promotedPiece.getCol()][promotedPiece.getRow()] = promotedPiece;
        promotingPawn = null;
        isPromotionPending = false;
        turn = (turn == Tint.WHITE) ? Tint.BLACK : Tint.WHITE;
        mouse.setClicked(false);
    }

    private List<Piece> clonePieces() {
        List<Piece> copy = new ArrayList<>();
        for (Piece p : pieces) {
            copy.add(p.copy());
        }
        return copy;
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
        List<Piece> simPieces = clonePieces();

        Piece simPiece = simPieces.stream()
                .filter(p -> p.getCol() == piece.getCol()
                        && p.getRow() == piece.getRow()
                        && p.getColor() == piece.getColor()
                        && p.getClass() == piece.getClass())
                .findFirst()
                .orElse(null);

        if (simPiece == null) {
            return true;
        }

        simPieces.removeIf(p -> p != simPiece
                && p.getCol() == targetCol
                && p.getRow() == targetRow
                && !(p instanceof King));

        simPiece.setCol(targetCol);
        simPiece.setRow(targetRow);

        Piece king = simPieces.stream()
                .filter(p -> p instanceof King
                        && p.getColor() == piece.getColor())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("King must exist after cloning"));

        for (Piece enemy : simPieces) {
            if (enemy.getColor() != piece.getColor() &&
                    enemy.canMove(king.getCol(), king.getRow(), simPieces)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPieceThreatened(Piece piece) {
        for(Piece enemy : pieces) {
            if(enemy.getColor() == piece.getColor()) { continue; }
            if(enemy.canMove(piece.getCol(), piece.getRow(), this)) {
                return true;
            }
        }
        return false;
    }

    private Move getAiTurn() {
        List<MoveScore> moves = getAllLegalMoves(turn);
        if(moves.isEmpty()) return null;
        moves.sort((a,b) -> Integer.compare(b.score(), a.score()));
        return moves.getFirst().move();
    }

    private List<MoveScore> getAllLegalMoves(Tint color) {
        List<MoveScore> moves = new ArrayList<>();
        for (Piece p : pieces) {
            if (p.getColor() != color) { continue; }
            for (int col = 0; col < 8; col++) {
                for (int row = 0; row < 8; row++) {
                    if (!isLegalMove(p, col, row)) { continue; }
                    Move move = new Move(p, col, row);
                    moves.add(new MoveScore(move, evaluateMove(move)));
                }
            }
        }
        return moves;
    }

    private boolean isLegalMove(Piece p, int col, int row) {
        Piece target = getPieceAt(col, row);
        if (!p.canMove(col, row, this)) {
            return false;
        }

        if (target != null && target.getColor() == p.getColor()) {
            return false;
        }
        return !wouldLeaveKingInCheck(p, col, row);
    }


    public Piece getPieceAt(int col, int row) {
        if(col < 0 || col > 7 || row < 0 || row > 7) {
            return null;
        }
        return boardState[col][row];
    }

    private int evaluateMove(Move move) {
        int score = 0;
        Piece p = move.piece();

        for(Piece enemy : pieces) {
            if(enemy.getCol() == move.targetCol() && enemy.getRow() == move.targetRow()) {
                score += getPieceValue(enemy);
                break;
            }
        }

        int oldCol = p.getCol();
        int oldRow = p.getRow();
        p.setCol(move.targetCol());
        p.setRow(move.targetRow());

        if(isPieceThreatened(p)) {
            score -= getPieceValue(p);
        }

        p.setCol(oldCol);
        p.setRow(oldRow);
        return score;
    }

    private int getPieceValue(Piece p) {
        return switch(p.getId()) {
            case PAWN -> 10;
            case KNIGHT, BISHOP -> 30;
            case ROOK -> 50;
            case QUEEN -> 90;
            case KING -> 900;
        };
    }

    private void executeMove(Move move) {
        Piece p = move.piece();
        p.setPreCol(p.getCol());
        p.setPreRow(p.getRow());

        Piece captured = getPieceAt(move.targetCol(), move.targetRow());
        if (captured != null) {
            removePiece(captured);
        }

        movePiece(p, move.targetCol(), move.targetRow());
        p.setHasMoved(true);

        currentPiece = null;
        isDragging = false;
        isLegal = false;
        turn = Tint.WHITE;
    }

    private void handleMenuInput() {
		if(!mouse.isClicked()) {
			return;
		}
		int startY = HEIGHT/2 + MENU_START_Y;
		int spacing = MENU_SPACING;

        for(int i = 0; i < optionsMenu.length; i++) {
            int y = startY + i * spacing;
            boolean isHovered = getHitbox(y).contains(mouse.getX(),
                    mouse.getY());
            if(isHovered) {
                switch(i) {
                    case 0 -> startNewGame();
                    case 1 -> System.exit(0);
                }
                mouse.setClicked(false);
                break;
            }
        }
	}

	private void startNewGame() {
		this.state = GameState.MODE;
	}

	private void setPlayState() {
        if(!mouse.isClicked()) {
            return;
        }

		if(state != GameState.MODE) {
			return;
		}

		int startY = HEIGHT/2 + MENU_START_Y;
		int spacing = MENU_SPACING;

		for(int i = 0; i < optionsMode.length; i++) {
			int y = startY + i * spacing;
            boolean isHovered = getHitbox(y).contains(mouse.getX(),
                    mouse.getY());
            if(isHovered) {
				switch(i) {
					case 0 -> mode = PlayState.PLAYER;
					case 1 -> mode = PlayState.AI;
				}
				mouse.setClicked(false);
                startBoard();
				return;
			}
		}
	}
}
