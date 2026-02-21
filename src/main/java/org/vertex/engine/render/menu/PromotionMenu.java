package org.vertex.engine.render.menu;

import org.vertex.engine.entities.Board;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.interfaces.State;
import org.vertex.engine.interfaces.UI;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.PromotionService;
import org.vertex.engine.service.PieceService;
import org.vertex.engine.service.UIService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class PromotionMenu implements UI {
    private static final int ARC = 32;
    private static final int STROKE = 6;

    private final RenderContext render;
    private final PieceService pieceService;
    private final PromotionService promotionService;
    private final UIService uiService;

    public PromotionMenu(RenderContext render,
                         PieceService pieceService,
                         PromotionService promotionService,
                         UIService uiService) {
        this.render = render;
        this.pieceService = pieceService;
        this.promotionService = promotionService;
        this.uiService = uiService;
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
        Piece hp = pieceService.getHoveredPiece();
        if(hp == null || !hp.isPromoted()) { return; }

        List<Piece> promotionOptions = promotionService.getPromotions(hp);

        int squareSize = render.scale(Board.getSquare());
        int menuWidth = promotionOptions.size() * squareSize;
        int menuHeight = squareSize;

        int screenWidth = RenderContext.BASE_WIDTH;
        int screenHeight = RenderContext.BASE_HEIGHT;

        int x = (screenWidth - menuWidth)/2;
        int y = (screenHeight - menuHeight)/2;

        UIService.drawBox(g2, STROKE, x, y, menuWidth, menuHeight,
                ARC, true, false, 180);

        for(int i = 0; i < promotionOptions.size(); i++) {
            Piece p = promotionOptions.get(i);
            int optionX = x + i * squareSize;
            BufferedImage sprite = pieceService.getSprite(p);
            g2.drawImage(sprite, optionX, y, squareSize, squareSize, null);
        }
    }
}