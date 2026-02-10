package org.chess.render;

import org.chess.entities.*;
import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.service.BooleanService;
import org.chess.service.GUIService;
import org.chess.service.PromotionService;

import java.awt.*;

public class PromotionRender {
    private final GUIService guiService;
    private final PromotionService promotionService;

    public PromotionRender(GUIService guiService, PromotionService promotionService) {
        this.guiService = guiService;
        this.promotionService = promotionService;
    }

    public void drawPromotions(Graphics2D g2) {
        if(!BooleanService.isPromotionPending) { return; }

        int size = Board.getSquare();
        int totalWidth = size * 4;
        int startX = (GUIService.getWIDTH() - totalWidth)/2 + GUIService.getBOARD_OFFSET_X();
        int startY = (GUIService.getHEIGHT() - size)/2;

        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, GUIService.getWIDTH(), GUIService.getHEIGHT());

        Type[] options = { Type.QUEEN, Type.ROOK, Type.BISHOP, Type.KNIGHT };

        int hoverIndex = -1;
        for(int i = 0; i < options.length; i++) {
            int x0 = startX + i * size;
            int x1 = x0 + size;
            int y1 = startY + size;
            if(guiService.getMouse().getX() >= x0 && guiService.getMouse().getX() <= x1 &&
                    guiService.getMouse().getY() >= startY && guiService.getMouse().getY() <= y1) {
                hoverIndex = i;
                break;
            }
        }

        for(int i = 0; i < options.length; i++) {
            Piece temp;
            Tint promotionColor = promotionService.getPromotionColor();
            switch(options[i]) {
                case QUEEN -> temp = new Queen(promotionColor, 0, 0);
                case ROOK -> temp = new Rook(promotionColor, 0, 0);
                case BISHOP -> temp = new Bishop(promotionColor, 0, 0);
                case KNIGHT -> temp = new Knight(promotionColor, 0, 0);
                default -> { continue; }
            }
            temp.setX(startX + i * size);
            temp.setY(startY);
            temp.setScale(i == hoverIndex ? temp.getScale() + temp.getMORE_SCALE() : temp.getDEFAULT_SCALE());
            guiService.getBoardRender().drawPiece(g2, temp);
        }
    }
}
