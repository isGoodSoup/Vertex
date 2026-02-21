package org.vertex.engine.render;

import org.vertex.engine.entities.Board;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.records.Move;
import org.vertex.engine.service.BoardService;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.UIService;

import java.awt.*;
import java.util.List;

public class MovesRender {
    private RenderContext render;
    private BoardService boardService;
    private UIService uiService;
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

    public UIService getUIService() {
        return uiService;
    }

    public void setUIService(UIService UIService) {
        this.uiService = UIService;
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

        g2.setFont(UIService.getFontBold(24));
        FontMetrics fm = g2.getFontMetrics();
        int lineHeight = render.scale(fm.getHeight() + 8);

        int stroke = 4;
        int boardX = render.getBoardRender().getBoardOriginX();
        int boardY = render.getBoardRender().getBoardOriginY();
        int boardSize = Board.getSquare() * boardService.getBoard().getCol();
        boolean hasBackground = true;

        int padding = render.scale(UIService.getPADDING() - 30);
        int innerPadding = render.scale(24);

        int availableWidth = render.scale(RenderContext.BASE_WIDTH) - boardSize - padding * 4;
        int boxWidth = availableWidth/7;
        int boxHeight = boardSize - 20;

        int leftX = boardX - boxWidth - padding + innerPadding;
        int rightX = boardX + boardSize + padding + innerPadding;
        int leftY = boardY + innerPadding + fm.getAscent();
        int rightY = boardY + innerPadding + fm.getAscent();

        UIService.drawBox(g2, stroke, boardX - boxWidth - padding,
                boardY, boxWidth, boxHeight, MenuRender.getARC(),
                hasBackground, false, 255);
        UIService.drawBox(g2, stroke, boardX + boardSize + padding,
                boardY, boxWidth, boxHeight, MenuRender.getARC(),
                hasBackground, true, 255);

        List<Move> moves = movesManager.getMoves();
        int startIndex = Math.max(0, moves.size() - UIService.getMOVES_CAP());

        for (int i = startIndex; i < moves.size(); i++) {
            Move move = moves.get(i);
            boolean isLast = (i == moves.size() - 1);

            String moveText = boardService.getSquareNameAt(move.fromRow(),
                    move.fromCol()) + " > " + boardService.getSquareNameAt(move.targetCol(), move.targetRow());

            if (move.color() == Tint.LIGHT) {
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

    public void toggleMoves() {
        BooleanService.isMovesActive = !BooleanService.isMovesActive;
    }
}