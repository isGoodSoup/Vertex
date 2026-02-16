package org.vertex.engine.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.enums.*;
import org.vertex.engine.events.ToggleEvent;
import org.vertex.engine.input.Keyboard;
import org.vertex.engine.manager.MovesManager;
import org.vertex.engine.records.Save;
import org.vertex.engine.render.Colorblindness;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.BoardService;
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
    private final Frame frame;
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

	public BoardPanel(Frame frame) {
        super();
        this.frame = frame;
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
        log.debug("Keyboard listener inserted");
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
        MovesManager move = BoardService.getMovesManager();
        Keyboard keyboard = service.getKeyboard();
        GameState state = GameService.getState();
        GameMenu menu = GameService.getGameMenu();

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_B)) {
            GameService.setState(GameState.MENU);
            service.getMovesManager().setSelectedIndexY(0);
            service.getGuiService().getFx().playFX(2);
        }

        if(BooleanService.canBeColorblind) {
            if(keyboard.wasOnePressed()) { MenuRender.setCb(ColorblindType.PROTANOPIA); }
            if(keyboard.wasTwoPressed()) { MenuRender.setCb(ColorblindType.DEUTERANOPIA); }
            if(keyboard.wasThreePressed()) { MenuRender.setCb(ColorblindType.TRITANOPIA); }
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
                if(keyboard.wasSelectPressed()) { move.activate(GameState.MENU); }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    move.moveUp(MenuRender.MENU);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown(MenuRender.MENU);
                    lastDownTime = now;
                }
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_G)) {
                    GameService.nextGame();
                    service.getEventBus().fire(new ToggleEvent());
                }
            }
            case SAVES -> {
                List<Save> saves = service.getSaveManager().getSaves();
                int itemsPerPage = MovesManager.getITEMS_PER_PAGE();
                if(!saves.isEmpty()) {
                    service.getMovesManager().setSelectedIndexY(0);
                }
                if(keyboard.wasSelectPressed()) { move.activate(GameState.SAVES); }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    move.moveUp(saves);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown(saves);
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastUpTime >= repeatDelay) {
                    if(service.getRender().getMenuRender().getCurrentPage() > 0) {
                        service.getGuiService().getFx().playFX(4);
                    }
                    service.getMovesManager().previousPage();
                    service.getMovesManager().setSelectedIndexY(
                            (service.getRender().getMenuRender().getCurrentPage() - 1) * itemsPerPage
                    );
                    lastUpTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    service.getGuiService().getFx().playFX(4);
                    service.getMovesManager().nextPage();
                    service.getMovesManager().setSelectedIndexY(
                            (service.getRender().getMenuRender().getCurrentPage() - 1) * itemsPerPage
                    );
                    lastDownTime = now;
                }
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL,
                        KeyEvent.VK_D) && now - lastDownTime >= repeatDelay) {
                    int selected = service.getMovesManager().getSelectedIndexY();
                    if(selected >= 0 && selected < saves.size()) {
                        String saveName = saves.get(selected).name();
                        service.getSaveManager().removeSave(saveName);
                        service.getGuiService().getFx().playFX(2);

                        if(selected >= saves.size()) {
                            service.getMovesManager().setSelectedIndexY(saves.size() - 1);
                        }
                    }
                    lastDownTime = now;
                }

            }
            case RULES -> {
                if(keyboard.wasSelectPressed()) { move.activate(GameState.RULES); }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    move.moveUp(MenuRender.SETTINGS_MENU);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown(MenuRender.SETTINGS_MENU);
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastDownTime >= repeatDelay) {
                    move.moveLeft(MenuRender.SETTINGS_MENU);
                    lastDownTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    move.moveRight(MenuRender.SETTINGS_MENU);
                    lastDownTime = now;
                }
            }
            case ACHIEVEMENTS -> {
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    move.moveUp(service.getAchievementService().getAchievementList());
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown(service.getAchievementService().getAchievementList());
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastUpTime >= repeatDelay) {
                    if(service.getRender().getMenuRender().getCurrentPage() > 0) {
                        service.getGuiService().getFx().playFX(4);
                    }
                    service.getMovesManager().previousPage();
                    lastUpTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    service.getGuiService().getFx().playFX(4);
                    service.getMovesManager().nextPage();
                    lastDownTime = now;
                }
            }
            case BOARD -> {
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL,
                        KeyEvent.VK_S) && BooleanService.isSandboxEnabled) {
                    BooleanService.canDoSandbox ^= true;
                    BooleanService.canType ^= true;
                }
                if(BooleanService.canDoSandbox) { return; }
                if(keyboard.wasCancelPressed()) {
                    service.getMovesManager().cancelMove();
                }
                if(keyboard.wasSelectPressed()) { move.activate(GameState.BOARD); }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    move.moveUp();
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown();
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastLeftTime >= repeatDelay) {
                    move.moveLeft();
                    lastLeftTime = now;
                }
                if(keyboard.isRightDown() && now - lastRightTime >= repeatDelay) {
                    move.moveRight();
                    lastRightTime = now;
                }
                if(keyboard.isComboPressed(KeyEvent.VK_CONTROL,
                        KeyEvent.VK_Z) && BooleanService.canUndoMoves) {
                    move.undoLastMove(move.getSelectedPiece());
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
        }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_R)) {
            service.getBoardService().resetBoard();
        }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_H)) {
            service.getRender().getMovesRender().toggleMoves();
        }

        if(keyboard.wasF11Pressed()) {
            frame.toggleFullscreen();
        }
    }

    private void playFX() {
        service.getGuiService().getFx().playFX(5);
    }
}
