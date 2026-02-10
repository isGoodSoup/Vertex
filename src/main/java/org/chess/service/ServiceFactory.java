package org.chess.service;

import org.chess.gui.Keyboard;
import org.chess.gui.Mouse;

public class ServiceFactory {
    private final PieceService piece;
    private final BoardService board;
    private final Mouse mouse;
    private final Keyboard keyboard;
    private final GUIService gui;
    private final GameService gs;
    private final PromotionService promotion;
    private final ModelService model;
    private final AnimationService animation;

    public ServiceFactory() {
        this.mouse = new Mouse();
        this.keyboard = new Keyboard();
        this.animation = new AnimationService();
        this.piece = new PieceService(mouse);
        this.promotion = new PromotionService(piece, mouse);
        this.model = new ModelService(piece, animation, promotion);
        this.board = new BoardService(piece, mouse, promotion, model);
        this.gs = new GameService(board, mouse);
        this.gui = new GUIService(piece, board, gs, promotion, model, mouse);
    }

    public PieceService getPieceService() {
        return piece;
    }

    public BoardService getBoardService() {
        return board;
    }

    public Mouse getMouseService() {
        return mouse;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public GUIService getGuiService() {
        return gui;
    }

    public PromotionService getPromotionService() {
        return promotion;
    }

    public ModelService getModelService() {
        return model;
    }

    public GameService getGameService() {
        return gs;
    }

    public AnimationService getAnimationService() {
        return animation;
    }
}
