package org.chess.render;

import org.chess.entities.Board;
import org.chess.enums.Tint;
import org.chess.manager.MovesManager;
import org.chess.records.Move;
import org.chess.service.BoardService;
import org.chess.service.BooleanService;
import org.chess.service.GUIService;

import java.awt.*;
import java.util.List;

public class MovesRender {
    private RenderContext render;
    private BoardService boardService;
    private GUIService guiService;
    private MovesManager movesManager;

    public MovesRender(RenderContext render, MovesManager movesManager) {
        this.render = render;
        this.movesManager = movesManager;
    }

    public MovesManager getMovesManager() {
        return movesManager;
    }

    public void setMovesManager(MovesManager movesManager) {
        this.movesManager = movesManager;
    }

    public BoardService getBoardService() {
        return boardService;
    }

    public void setBoardService(BoardService boardService) {
        this.boardService = boardService;
    }

    public GUIService getGuiService() {
        return guiService;
    }

    public void setGuiService(GUIService guiService) {
        this.guiService = guiService;
    }

    public RenderContext getRender() {
        return render;
    }

    public void setRender(RenderContext render) {
        this.render = render;
    }

    public void drawMoves(Graphics2D g2) {
        if(!BooleanService.isMovesActive) { return; }
        int boardWidth = render.scale(RenderContext.BASE_WIDTH) - Board.getSquare() * 8;
        int totalHeight = render.scale(RenderContext.BASE_HEIGHT);

        g2.setFont(GUIService.getFontBold(24));
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = render.scale(fm.getHeight() + 8);

        int stroke = 4;
        int boardX = render.getBoardRender().getBoardOriginX();
        int boardY = render.getBoardRender().getBoardOriginY();
        int boardSize = Board.getSquare() * boardService.getBoard().getCOL();
        int arcWidth = 32;
        int arcHeight = 32;
        boolean hasBackground = true;

        int padding = render.scale(GUIService.getPADDING() - 30);
        int innerPadding = render.scale(24);

        int availableWidth = render.scale(RenderContext.BASE_WIDTH) - boardSize - padding * 4;
        int boxWidth = availableWidth/7;
        int boxHeight = boardSize - 20;

        int leftX = boardX - boxWidth - padding + innerPadding;
        int rightX = boardX + boardSize + padding + innerPadding;
        int leftY = boardY + innerPadding + fm.getAscent();
        int rightY = boardY + innerPadding + fm.getAscent();

        GUIService.drawBox(g2, stroke, boardX - boxWidth - padding,
                boardY, boxWidth, boxHeight, arcWidth, arcHeight,
                hasBackground, false);
        GUIService.drawBox(g2, stroke, boardX + boardSize + padding,
                boardY, boxWidth, boxHeight, arcWidth, arcHeight,
                hasBackground, true);

        List<Move> moves = movesManager.getMoves();
        int startIndex = Math.max(0, moves.size() - GUIService.getMOVES_CAP());

        for (int i = startIndex; i < moves.size(); i++) {
            Move move = moves.get(i);
            boolean isLast = (i == moves.size() - 1);

            String moveText = boardService.getSquareNameAt(move.fromRow(),
                    move.fromCol()) + " > " + boardService.getSquareNameAt(move.targetRow(), move.targetCol());

            if (move.color() == Tint.WHITE) {
                g2.setColor(isLast ? Color.YELLOW : Color.WHITE);
                g2.drawString(moveText, rightX, rightY);
                rightY += fm.getHeight() + render.scale(4);
            } else {
                g2.setColor(isLast ? Color.CYAN : Color.WHITE);
                g2.drawString(moveText, leftX, leftY);
                leftY += fm.getHeight() + render.scale(4);
            }
        }
    }

    public void hideMoves() {
        BooleanService.isMovesActive = !BooleanService.isMovesActive;
    }
}