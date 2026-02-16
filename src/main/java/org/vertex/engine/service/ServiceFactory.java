package org.vertex.engine.service;

import org.vertex.engine.sound.Sound;
import org.vertex.engine.input.Keyboard;
import org.vertex.engine.input.KeyboardUI;
import org.vertex.engine.manager.EventBus;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.manager.SaveManager;
import org.vertex.engine.render.RenderContext;

public class ServiceFactory {
    private final RenderContext render;
    private final PieceService piece;
    private final BoardService board;
    private final Keyboard keyboard;
    private final KeyboardUI keyUI;
    private final Sound sound;
    private final GUIService gui;
    private final GameService gs;
    private final PromotionService promotion;
    private final MovesManager movesManager;
    private final SaveManager saveManager;
    private final ModelService model;
    private final AnimationService animation;
    private final TimerService timer;
    private final AchievementService achievement;
    private final EventBus eventBus;

    public ServiceFactory(RenderContext render) {
        this.render = render;
        this.eventBus = new EventBus();
        this.keyboard = new Keyboard();
        this.keyUI = new KeyboardUI();
        this.sound = new Sound();
        this.keyUI.setService(this);
        this.animation = new AnimationService();
        this.piece = new PieceService(eventBus);
        this.promotion = new PromotionService(piece, eventBus);
        this.model = new ModelService(piece, animation, promotion);
        this.movesManager = new MovesManager();
        this.piece.setMoveManager(movesManager);
        this.render.setMovesManager(movesManager);
        this.board = new BoardService(piece, promotion,
                model, movesManager);
        this.piece.setBoardService(board);
        this.board.setServiceFactory(this);
        this.model.setBoardService(board);
        this.gs = new GameService(render, board);
        this.saveManager = new SaveManager();
        this.saveManager.setServiceFactory(this);
        this.gs.setServiceFactory(this);
        this.gs.setSaveManager(saveManager);
        this.timer = new TimerService();
        this.gui = new GUIService(render, piece, board, gs, promotion,
                model, movesManager, timer);
        this.achievement = new AchievementService(eventBus);
        this.achievement.setService(this);
        this.achievement.setAnimationService(animation);
        this.achievement.setSaveManager(saveManager);
        this.render.getBoardRender().setBoardService(board);
        this.render.getBoardRender().setPieceService(piece);
        this.render.getBoardRender().setGuiService(gui);
        this.render.getBoardRender().setPromotionService(promotion);
        this.render.getMenuRender().setBoardService(board);
        this.render.getMenuRender().setGuiService(gui);
        this.render.getMenuRender().setGameService(gs);
        this.render.getMenuRender().setMoveManager(movesManager);
        this.render.getMenuRender().setKeyUI(keyUI);
        this.render.getMenuRender().setAnimationService(animation);
        this.render.getMovesRender().setBoardService(board);
        this.render.getMovesRender().setGuiService(gui);
        this.render.getMovesRender().setMovesManager(movesManager);
        this.movesManager.init(this, eventBus);
        this.render.getMenuRender().init();
    }

    public RenderContext getRender() {
        return render;
    }

    public PieceService getPieceService() {
        return piece;
    }

    public BoardService getBoardService() {
        return board;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public KeyboardUI getKeyUI() {
        return keyUI;
    }

    public GUIService getGuiService() {
        return gui;
    }

    public Sound getSound() {
        return sound;
    }

    public PromotionService getPromotionService() {
        return promotion;
    }

    public MovesManager getMovesManager() {
        return movesManager;
    }

    public SaveManager getSaveManager() {
        return saveManager;
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

    public TimerService getTimerService() { return timer; }

    public AchievementService getAchievementService() {
        return achievement;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
