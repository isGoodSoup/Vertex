package org.chess.render;

import org.chess.enums.Tint;
import org.chess.records.Move;
import org.chess.service.BoardService;
import org.chess.service.GUIService;

import java.awt.*;

public class MovesRender {

    private final GUIService guiService;

    public MovesRender(GUIService guiService) {
        this.guiService = guiService;
    }

    public void drawMoves(Graphics2D g2) {
        g2.setColor(GUIService.getNewBackground());
        g2.fillRect(0, 0, GUIService.getEXTRA_WIDTH(), GUIService.getHEIGHT());
        g2.fillRect(GUIService.getWIDTH(), 0, GUIService.getEXTRA_WIDTH(), GUIService.getHEIGHT());

        g2.setFont(GUIService.getFontBold(20));

        final int OFFSET_X = 25;
        int lineHeight = g2.getFontMetrics().getHeight() + 8;
        int leftY = lineHeight;
        int rightY = lineHeight;

        int leftX = OFFSET_X;
        int rightX = GUIService.getWIDTH() + OFFSET_X;

        var moves = BoardService.getMoves();
        int startIndex = Math.max(0, moves.size() - GUIService.getMOVES_CAP());

        for (int i = startIndex; i < moves.size(); i++) {
            Move move = moves.get(i);
            boolean isLast = (i == moves.size() - 1);

            String moveText = BoardService.getSquareName(move.fromCol(), move.fromRow()) +
                    " > " +
                    BoardService.getSquareName(move.targetCol(), move.targetRow());

            if (move.color() == Tint.WHITE) {
                g2.setColor(isLast ? Color.YELLOW : Color.BLACK);
                g2.drawString(moveText, rightX, rightY);
                rightY += lineHeight;
            } else {
                g2.setColor(isLast ? Color.CYAN : Color.BLACK);
                g2.drawString(moveText, leftX, leftY);
                leftY += lineHeight;
            }
        }
    }
}