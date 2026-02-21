package org.lud.engine.service;

import org.lud.engine.gui.GameFrame;
import org.lud.engine.input.Keyboard;
import org.lud.engine.input.KeyboardInput;
import org.lud.engine.input.Mouse;
import org.lud.engine.input.MouseInput;
import org.lud.engine.interfaces.UI;
import org.lud.engine.manager.EventBus;
import org.lud.engine.manager.MovesManager;
import org.lud.engine.manager.SaveManager;
import org.lud.engine.render.MenuRender;
import org.lud.engine.render.RenderContext;
import org.lud.engine.render.menu.*;
import org.lud.engine.sound.Sound;

import java.util.List;

public class ServiceFactory {
    private final RenderContext render;
    private final PieceService piece;
    private final BoardService board;
    private final Keyboard keyboard;
    private final KeyboardInput key;
    private final Mouse mouse;
    private final MouseInput mouseInput;
    private final Sound sound;
    private final UIService ui;
    private final GameService gs;
    private final PromotionService promotion;
    private final MovesManager movesManager;
    private final SaveManager saveManager;
    private final ModelService model;
    private final AnimationService animation;
    private final TimerService timer;
    private final AchievementService achievement;
    private final EventBus eventBus;

    public ServiceFactory(RenderContext render, GameFrame gameFrame) {
        this.render = render;
        this.eventBus = new EventBus();
        this.keyboard = new Keyboard();
        this.key = new KeyboardInput();
        this.key.setGameFrame(gameFrame);
        this.mouse = new Mouse();
        this.mouseInput = new MouseInput(mouse, this);
        this.render.setMouse(mouse);
        this.render.setMouseInput(mouseInput);
        this.sound = new Sound();
        this.key.setService(this);
        this.animation = new AnimationService();
        this.piece = new PieceService(eventBus);
        this.promotion = new PromotionService(piece, eventBus);
        this.model = new ModelService(piece, animation, promotion);
        this.movesManager = new MovesManager();
        this.piece.setMoveManager(movesManager);
        this.render.setMovesManager(movesManager);
        this.saveManager = new SaveManager();
        this.gs = new GameService(render, null, saveManager);
        this.board = new BoardService(piece, promotion,
                model, movesManager);
        this.piece.setGameService(gs);
        this.board.setGameService(gs);
        this.promotion.setGameService(gs);
        this.key.setBoardService(board);
        this.gs.setBoardService(board);
        this.piece.setBoardService(board);
        this.board.setService(this);
        this.model.setBoardService(board);
        this.gs.setServiceFactory(this);
        this.gs.setSaveManager(saveManager);
        this.timer = new TimerService();
        this.ui = new UIService(render, piece, board, gs, promotion,
                model, movesManager, timer, mouse);
        this.achievement = new AchievementService(eventBus);
        this.achievement.setService(this);
        this.achievement.setAnimationService(animation);
        this.achievement.setSaveManager(saveManager);

        this.render.getMenuRender().setGameService(gs);
        this.render.getMenuRender().init();

        List<UI> menus = render.getMenuRender().getMenus();
        menus.add(new MainMenu(render, gs, ui, key, mouse));
        menus.add(new OptionsMenu(render, ui, gs, key, mouse, mouseInput, MenuRender.OPTION_IMAGES));
        menus.add(new AchievementsMenu(render, ui, key, achievement));
        menus.add(new Checkmate(ui, gs, render, RenderContext.BASE_WIDTH));
        menus.add(new PromotionMenu(render, piece, promotion, ui));
        menus.add(new SandboxMenu(render, board, ui));
        menus.add(new TooltipMenu(render, piece, board, ui, mouse));

        this.render.getBoardRender().setBoardService(board);
        this.render.getBoardRender().setPieceService(piece);
        this.render.getBoardRender().setUIService(ui);
        this.render.getBoardRender().setPromotionService(promotion);
        this.render.getBoardRender().setGameService(gs);
        this.render.getBoardRender().setMouse(mouse);
        this.render.getBoardRender().setMouseInput(mouseInput);

        this.render.getMovesRender().setBoardService(board);
        this.render.getMovesRender().setUIService(ui);
        this.render.getMovesRender().setMovesManager(movesManager);
        this.render.getControlsRender().setService(this);
        this.movesManager.init(this, eventBus);
        this.mouseInput.init();
        this.achievement.init();
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

    public KeyboardInput getKeyboardInput() {
        return key;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public MouseInput getMouseInput() {
        return mouseInput;
    }

    public UIService getGuiService() {
        return ui;
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
