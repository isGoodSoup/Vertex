package org.lud.engine.render.menu;

import org.lud.engine.entities.Board;
import org.lud.engine.entities.Piece;
import org.lud.engine.enums.GameState;
import org.lud.engine.enums.Games;
import org.lud.engine.enums.TypeID;
import org.lud.engine.input.Mouse;
import org.lud.engine.interfaces.State;
import org.lud.engine.interfaces.UI;
import org.lud.engine.render.RenderContext;
import org.lud.engine.service.BoardService;
import org.lud.engine.service.GameService;
import org.lud.engine.service.PieceService;
import org.lud.engine.service.UIService;

import java.awt.*;

public class TooltipMenu implements UI {
    private static final int ARC = 32;
    private static final int STROKE = 6;

    private final RenderContext render;
    private final PieceService pieceService;
    private final BoardService boardService;
    private final UIService uiService;
    private final Mouse mouse;

    public TooltipMenu(RenderContext render, PieceService pieceService, BoardService boardService,
                       UIService uiService, Mouse mouse) {
        this.render = render;
        this.pieceService = pieceService;
        this.boardService = boardService;
        this.uiService = uiService;
        this.mouse = mouse;
    }

    private int getCenterX(int containerWidth, int elementWidth) {
        return render.getOffsetX() + (containerWidth - elementWidth)/2;
    }

    @Override
    public void drawMenu(Graphics2D g2) {
        draw(g2);
    }

    @Override
    public boolean canDraw(State state) {
        return state == GameState.BOARD;
    }

    public void draw(Graphics2D g2) {
        Piece p = pieceService.getHoveredPiece();

        final int COL = boardService.getBoard().getCol();
        final int SQUARE = render.scale(Board.getSquare());

        int boardSize = SQUARE * COL;
        int boardX = (RenderContext.BASE_WIDTH - boardSize)/2;
        int boardY = (RenderContext.BASE_HEIGHT - boardSize)/2;
        int mouseBoardX = render.unscaleX(mouse.getX()) - boardX;
        int mouseBoardY = render.unscaleY(mouse.getY()) - boardY;
        int mouseCol = mouseBoardX/Board.getSquare();
        int mouseRow = mouseBoardY/Board.getSquare();

        Piece hovered = PieceService.getPieceAt(mouseCol, mouseRow, pieceService.getPieces());
        pieceService.setHoveredPiece(hovered);

        if(p != null) {
            if(mouseCol == p.getCol() && mouseRow == p.getRow()) {
                TypeID id = p.getTypeID();
                TypeID shogiID = p.getShogiID();
                if(shogiID != null && GameService.getGame() == Games.SHOGI) {
                    id = shogiID;
                }
                int padding = 16;
                String square = boardService.getSquareNameAt(p.getCol(), p.getRow());
                String text = id.name() + " " + square.toUpperCase();
                g2.setFont(UIService.getFont(UIService.getMENU_FONT()));
                uiService.drawTooltip(g2, text, padding, ARC);
            }
        }
    }
}