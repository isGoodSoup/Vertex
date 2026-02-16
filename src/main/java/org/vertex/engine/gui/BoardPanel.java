package org.vertex.engine.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.enums.*;
import org.vertex.engine.events.ToggleEvent;
import org.vertex.engine.input.Keyboard;
import org.vertex.engine.input.KeyboardInput;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.records.Save;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.ServiceFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serial;
import java.util.List;

public class BoardPanel extends JPanel implements Runnable {
	@Serial
    private static final long serialVersionUID = -5189356863277669172L;
    private final GameFrame gameFrame;
    private final RenderContext render;
    private final int FPS = 60;
	private Thread thread;
    private long lastUpTime = 0;
    private long lastDownTime = 0;
    private long lastLeftTime = 0;
    private long lastRightTime = 0;
    private final long repeatDelay = 150;

    private static ServiceFactory service;
    private static final Logger log = LoggerFactory.getLogger(BoardPanel.class);

	public BoardPanel(GameFrame gameFrame) {
        super();
        this.gameFrame = gameFrame;
        this.render = new RenderContext();
        service = new ServiceFactory(render);
        GameService.setState(GameState.MENU);
        Colors.setTheme(Theme.OCEAN);
        BooleanService.defaultToggles();
        final int WIDTH = RenderContext.BASE_WIDTH;
        final int HEIGHT = RenderContext.BASE_HEIGHT;
        log.debug("Set size(s): {}, {}", WIDTH, HEIGHT);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Colorblindness.filter(Colors.getBackground()));
        addKeyListener(service.getKeyboard());
        addMouseListener(service.getMouse());
        addMouseMotionListener(service.getMouse());
        setFocusable(true);
	}

    public void launch() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Timer timer = new Timer(1000/FPS, e -> {
            update();
            updateAnimations(1.0/FPS);
            repaint();
        });
        timer.start();
    }

    public void updateAnimations(double delta) {
        service.getAnimationService().update(delta);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        render.updateTransform(getWidth(), getHeight());
        g2.translate(render.getOffsetX(), render.getOffsetY());
        g2.scale(render.getScale(), render.getScale());
        try {
            drawGame(g2);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
        g2.dispose();
    }

    public void drawGame(Graphics2D g2) throws InterruptedException, IOException {
        switch(GameService.getState()) {
            case MENU -> service.getRender().getMenuRender().drawGraphics(g2,
                    MenuRender.MENU);
            case SAVES -> service.getRender().getMenuRender().drawSavesMenu(g2);
            case BOARD -> {
                service.getRender().getBoardRender().drawBoard(g2);
                service.getRender().getMovesRender().drawMoves(g2);
                if(!BooleanService.canDoSandbox) {
                    if(service.getTimerService().isActive()) {
                        service.getGuiService().drawTimer(g2);
                        service.getGuiService().drawTick(g2, BooleanService.isLegal);
                    }
                }
                if(BooleanService.canDoSandbox) {
                    service.getRender().getMenuRender().drawSandboxMenu(g2);
                }
            }
            case RULES -> service.getRender().getMenuRender()
                    .drawOptionsMenu(g2, MenuRender.SETTINGS_MENU);
            case ACHIEVEMENTS -> service.getRender().getMenuRender().drawAchievementsMenu(g2);
            case CHECKMATE -> service.getRender().getMenuRender().drawCheckmate(g2);
            case STALEMATE -> service.getRender().getMenuRender().drawCheckmate(g2);
        }
        render(g2);
    }

    public void render(Graphics2D g2) throws InterruptedException {
        service.getAnimationService().render(g2);
    }

    private void update() {
        checkKeyboardInput();
        service.getMouseInput().update();
        if(!BooleanService.canDoSandbox) {
            service.getTimerService().update();
        }
        PlayState mode = GameService.getMode();
        if(mode != null) {
            switch(mode) {
                case PLAYER -> BooleanService.canAIPlay = false;
                case AI -> BooleanService.canAIPlay = true;
            }
        }
    }

    private void checkKeyboardInput() {
        long now = System.currentTimeMillis();
        KeyboardInput keyboardInput = service.getKeyboardInput();
        Keyboard keyboard = service.getKeyboard();
        MovesManager move = service.getMovesManager();
        GameState state = GameService.getState();
        GameMenu menu = GameService.getGameMenu();

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_B)) {
            GameService.setState(GameState.MENU);
            service.getKeyboardInput().setSelectedIndexY(3);
            service.getSound().playFX(2);
        }

        if(BooleanService.canBeColorblind) {
            if(keyboard.wasOnePressed()) {
                MenuRender.setCb(ColorblindType.PROTANOPIA);
                service.getSound().playFX(2);
            }
            if(keyboard.wasTwoPressed()) {
                MenuRender.setCb(ColorblindType.DEUTERANOPIA);
                service.getSound().playFX(2);
            }
            if(keyboard.wasThreePressed()) {
                MenuRender.setCb(ColorblindType.TRITANOPIA);
                service.getSound().playFX(2);
            }
        }

        if(BooleanService.isSandboxEnabled) {
            if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_ENTER)) {
                String fullInput = keyboard.consumeText().trim();
                if(fullInput.isEmpty()) { return; }
                String[] parts = fullInput.split("\\s+");
                String commandName = parts[0];
                String[] args = java.util.Arrays.copyOfRange(parts, 1, parts.length);
                Console consoleCommand = Console.fromString(commandName);
                if (consoleCommand != null) {
                    consoleCommand.run(service, args);
                } else {
                    System.out.println("Unknown command: " + commandName);
                }
            }
        }

        switch(state) {
            case MENU -> {
                if(keyboard.wasSelectPressed()) {
                    keyboardInput.activate(GameState.MENU);
                    service.getSound().playFX(3);
                }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    keyboardInput.moveUp(MenuRender.MENU);
                    service.getSound().playFX(1);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    keyboardInput.moveDown(MenuRender.MENU);
                    service.getSound().playFX(1);
                    lastDownTime = now;
                }
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_G)) {
                    GameService.nextGame();
                    service.getSound().playFX(0);
                    service.getEventBus().fire(new ToggleEvent());
                }
            }
            case SAVES -> {
                List<Save> saves = service.getSaveManager().getSaves();
                int itemsPerPage = KeyboardInput.getITEMS_PER_PAGE();
                if(!saves.isEmpty()) {
                    service.getKeyboardInput().setSelectedIndexY(0);
                }
                if(keyboard.wasSelectPressed()) {
                    keyboardInput.activate(GameState.SAVES);
                    service.getSound().playFX(3);
                }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    keyboardInput.moveUp(saves);
                    service.getSound().playFX(1);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    keyboardInput.moveDown(saves);
                    service.getSound().playFX(1);
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastUpTime >= repeatDelay) {
                    service.getKeyboardInput().previousPage();
                    service.getSound().playFX(2);
                    service.getKeyboardInput().setSelectedIndexY(
                            (service.getKeyboardInput().getCurrentPage() - 1) * itemsPerPage
                    );
                    lastUpTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    service.getKeyboardInput().nextPage();
                    service.getSound().playFX(2);
                    service.getKeyboardInput().setSelectedIndexY(
                            (service.getKeyboardInput().getCurrentPage() - 1) * itemsPerPage
                    );
                    lastDownTime = now;
                }
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL,
                        KeyEvent.VK_D) && now - lastDownTime >= repeatDelay) {
                    int selected = service.getKeyboardInput().getSelectedIndexY();
                    if(selected >= 0 && selected < saves.size()) {
                        String saveName = saves.get(selected).name();
                        service.getSaveManager().removeSave(saveName);

                        if(selected >= saves.size()) {
                            service.getKeyboardInput().setSelectedIndexY(saves.size() - 1);
                        }
                    }
                    lastDownTime = now;
                }

            }
            case RULES -> {
                if(keyboard.wasSelectPressed()) {
                    keyboardInput.activate(GameState.RULES);
                    service.getSound().playFX(0);
                }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    keyboardInput.moveUp(MenuRender.SETTINGS_MENU);
                    service.getSound().playFX(1);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    keyboardInput.moveDown(MenuRender.SETTINGS_MENU);
                    service.getSound().playFX(1);
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastDownTime >= repeatDelay) {
                    keyboardInput.moveLeft(MenuRender.SETTINGS_MENU);
                    service.getSound().playFX(2);
                    lastDownTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    keyboardInput.moveRight(MenuRender.SETTINGS_MENU);
                    service.getSound().playFX(2);
                    lastDownTime = now;
                }
            }
            case ACHIEVEMENTS -> {
                if(keyboard.wasSelectPressed()) {
                    keyboardInput.activate(GameState.ACHIEVEMENTS);
                    service.getSound().playFX(3);
                }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    keyboardInput.moveUp(service.getAchievementService().getAchievementList());
                    service.getSound().playFX(1);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    keyboardInput.moveDown(service.getAchievementService().getAchievementList());
                    service.getSound().playFX(1);
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastUpTime >= repeatDelay) {
                    service.getKeyboardInput().previousPage();
                    service.getKeyboardInput().setSelectedIndexY(0);
                    service.getSound().playFX(2);
                    lastUpTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    service.getKeyboardInput().nextPage();
                    service.getKeyboardInput().setSelectedIndexY(0);
                    service.getSound().playFX(2);
                    lastDownTime = now;
                }
            }
            case BOARD -> {
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL,
                        KeyEvent.VK_S) && BooleanService.isSandboxEnabled) {
                    BooleanService.canDoSandbox ^= true;
                    BooleanService.canType ^= true;
                    service.getSound().playFX(0);
                }
                if(BooleanService.canDoSandbox) { return; }
                if(keyboard.wasCancelPressed()) {
                    service.getMovesManager().cancelMove();
                    service.getSound().playFX(1);
                }
                if(keyboard.wasSelectPressed()) {
                    keyboardInput.activate(GameState.BOARD);
                    service.getSound().playFX(0);
                }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    keyboardInput.moveUp();
                    service.getSound().playFX(1);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    keyboardInput.moveDown();
                    service.getSound().playFX(1);
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastLeftTime >= repeatDelay) {
                    keyboardInput.moveLeft();
                    service.getSound().playFX(1);
                    lastLeftTime = now;
                }
                if(keyboard.isRightDown() && now - lastRightTime >= repeatDelay) {
                    keyboardInput.moveRight();
                    service.getSound().playFX(1);
                    lastRightTime = now;
                }
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL,
                        KeyEvent.VK_Z) && BooleanService.canUndoMoves) {
                    move.undoLastMove(move.getSelectedPiece());
                    service.getSound().playFX(3);
                    lastRightTime = now;
                }
            }
        }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_Q)) {
            System.exit(0);
        }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_T)
                && BooleanService.canTheme) {
            Colors.nextTheme();
            service.getSound().playFX(0);
        }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_R)) {
            service.getBoardService().resetBoard();
            service.getSound().playFX(0);
        }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_H)) {
            service.getRender().getMovesRender().toggleMoves();
            service.getSound().playFX(3);
        }

        if(keyboard.wasF11Pressed()) {
            gameFrame.toggleFullscreen();
        }
    }
}
