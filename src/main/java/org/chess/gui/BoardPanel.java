package org.chess.gui;

import org.chess.enums.Achievements;
import org.chess.enums.ColorblindType;
import org.chess.enums.GameState;
import org.chess.enums.PlayState;
import org.chess.input.Keyboard;
import org.chess.manager.MovesManager;
import org.chess.records.Save;
import org.chess.render.Colorblindness;
import org.chess.render.MenuRender;
import org.chess.render.RenderContext;
import org.chess.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serial;
import java.util.List;

public class BoardPanel extends JPanel implements Runnable {
	@Serial
    private static final long serialVersionUID = -5189356863277669172L;
    private final ChessFrame frame;
    private final RenderContext render;
    private final int FPS = 60;
	private Thread thread;
    private long lastUpTime = 0;
    private long lastDownTime = 0;
    private long lastLeftTime = 0;
    private long lastRightTime = 0;
    private final long repeatDelay = 150;

    private static ServiceFactory service;

	public BoardPanel(ChessFrame frame) {
        super();
        this.frame = frame;
        this.render = new RenderContext();
        service = new ServiceFactory(render);
        GameService.setState(GameState.MENU);
        BooleanService.defaultToggles();
        final int WIDTH = RenderContext.BASE_WIDTH;
        final int HEIGHT = RenderContext.BASE_HEIGHT;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Colorblindness.filter(Colors.BACKGROUND));
        addMouseListener(service.getMouseService());
        addMouseMotionListener(service.getMouseService());
        addKeyListener(service.getKeyboard());
        setFocusable(true);
	}

    public void launch() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        Timer timer = new Timer(1000 / FPS, e -> {
            try {
                update();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            updateMouse();
            updateAnimations(1.0 / FPS);
            repaint();
        });
        timer.start();
    }

    public void updateMouse() {
        service.getMouseService().update();
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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        g2.dispose();
    }

    public void drawGame(Graphics2D g2) throws InterruptedException {
        switch(GameService.getState()) {
            case MENU -> service.getRender().getMenuRender().drawGraphics(g2,
                    MenuRender.optionsMenu);
            case SAVES -> service.getRender().getMenuRender().drawSavesMenu(g2);
            case BOARD -> {
                service.getRender().getBoardRender().drawBoard(g2);
                service.getRender().getMovesRender().drawMoves(g2);
                if(service.getTimerService().isActive()) {
                    service.getGuiService().drawTimer(g2);
                    service.getGuiService().drawTick(g2, BooleanService.isLegal);
                }
                if(BooleanService.canSandbox) {
                    service.getRender().getMenuRender().drawSandboxMenu(g2);
                }
            }
            case RULES -> service.getRender().getMenuRender()
                    .drawOptionsMenu(g2, MenuRender.optionsTweaks);
            case ACHIEVEMENTS -> service.getRender().getMenuRender().drawAchievementsMenu(g2);
        }
        render(g2);
    }

    public void render(Graphics2D g2) throws InterruptedException {
        service.getAnimationService().render(g2);
    }

    private void update() throws IOException {
        checkKeyboardInput();
        checkMouseInput();
        service.getTimerService().update();
        service.getBoardService().resetBoard();
        checkAchievements();
        PlayState mode = GameService.getMode();
        if(mode != null) {
            switch(mode) {
                case PLAYER -> BooleanService.canAIPlay = false;
                case AI -> BooleanService.canAIPlay = true;
            }
        }
    }

    private void checkMouseInput() {
        switch(GameService.getState()) {
            case MENU -> {
                service.getRender().getMenuRender()
                        .getMenuInput().handleMenuInput(MenuRender.optionsMenu);
                return;
            }
            case SAVES -> service.getRender().getMenuRender().getMenuInput().handleSavesInput();
            case RULES -> {
                service.getRender().getMenuRender()
                        .getMenuInput().handleOptionsInput();
                return;
            }
            case ACHIEVEMENTS -> {}
            default -> {}
        }
    }

    private void checkKeyboardInput() {
        long now = System.currentTimeMillis();
        MovesManager move = BoardService.getMovesManager();
        Keyboard keyboard = service.getKeyboard();
        GameState state = GameService.getState();

        if(keyboard.wasBPressed()) {
            if(GameService.getState() == GameState.BOARD) {
                if(!BooleanService.canSave) { return; }
                service.getSaveManager().autoSave();
            }
            GameService.setState(GameState.MENU);
            service.getMovesManager().setSelectedIndexY(0);
            service.getGuiService().getFx().playFX(0);
        }

        if(BooleanService.canBeColorblind) {
            if(keyboard.wasOnePressed()) { MenuRender.setCb(ColorblindType.PROTANOPIA); }
            if(keyboard.wasTwoPressed()) { MenuRender.setCb(ColorblindType.DEUTERANOPIA); }
            if(keyboard.wasThreePressed()) { MenuRender.setCb(ColorblindType.TRITANOPIA); }
        }

        switch(state) {
            case MENU -> {
                if(keyboard.wasSelectPressed()) { move.activate(GameState.MENU); }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    move.moveUp(MenuRender.optionsMenu);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown(MenuRender.optionsMenu);
                    lastDownTime = now;
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
                    service.getRender().getMenuRender().getMenuInput().previousPage();
                    service.getMovesManager().setSelectedIndexY(
                            (service.getRender().getMenuRender().getCurrentPage() - 1) * itemsPerPage
                    );
                    lastUpTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    service.getGuiService().getFx().playFX(4);
                    service.getRender().getMenuRender().getMenuInput().nextPage();
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
                    move.moveUp(MenuRender.optionsTweaks);
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown(MenuRender.optionsTweaks);
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastDownTime >= repeatDelay) {
                    move.moveLeft(MenuRender.optionsTweaks);
                    lastDownTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    move.moveRight(MenuRender.optionsTweaks);
                    lastDownTime = now;
                }
            }
            case ACHIEVEMENTS -> {
                if(keyboard.isLeftDown() && now - lastUpTime >= repeatDelay) {
                    if(service.getRender().getMenuRender().getCurrentPage() > 0) {
                        service.getGuiService().getFx().playFX(4);
                    }
                    service.getRender().getMenuRender().getMenuInput().previousPage();
                    lastUpTime = now;
                }
                if(keyboard.isRightDown() && now - lastDownTime >= repeatDelay) {
                    service.getGuiService().getFx().playFX(4);
                    service.getRender().getMenuRender().getMenuInput().nextPage();
                    lastDownTime = now;
                }
            }
            case BOARD -> {
                if(keyboard.wasSelectPressed()) { move.activate(GameState.BOARD); }
                if(keyboard.isUpDown() && now - lastUpTime >= repeatDelay) {
                    move.moveUp();
                    move.updateKeyboardHover();
                    lastUpTime = now;
                }
                if(keyboard.isDownDown() && now - lastDownTime >= repeatDelay) {
                    move.moveDown();
                    move.updateKeyboardHover();
                    lastDownTime = now;
                }
                if(keyboard.isLeftDown() && now - lastLeftTime >= repeatDelay) {
                    move.moveLeft();
                    move.updateKeyboardHover();
                    lastLeftTime = now;
                }
                if(keyboard.isRightDown() && now - lastRightTime >= repeatDelay) {
                    move.moveRight();
                    move.updateKeyboardHover();
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
            refreshGraphics();
        }

        if(keyboard.isComboPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_H)) {
            service.getRender().getMovesRender().hideMoves();
        }

        if(keyboard.wasF11Pressed()) {
            frame.toggleFullscreen();
        }
    }

    private void checkAchievements() {
        if(!BooleanService.canDoAchievements) { return; }
        if(BooleanService.doFirstMove) {
            service.getAchievementService().unlock(Achievements.FIRST_MOVE);
            BooleanService.doFirstMove = false;
            BooleanService.doFirstMoveUnlock = true;
            playFX();
        }
        if(BooleanService.doRuleToggles) {
            service.getAchievementService().unlock(Achievements.SECRET_TOGGLES);
            BooleanService.doRuleToggles = false;
            BooleanService.doRuleTogglesUnlock = true;
            playFX();
        }
        if(BooleanService.doMasterCastling) {
            service.getAchievementService().unlock(Achievements.CASTLING_MASTER);
            BooleanService.doMasterCastling = false;
            BooleanService.doMasterCastlingUnlock = true;
            playFX();
        }
        if(BooleanService.doQuickWin) {
            service.getAchievementService().unlock(Achievements.QUICK_WIN);
            BooleanService.doQuickWin = false;
            BooleanService.doQuickWinUnlock = true;
            playFX();
        }
        if(BooleanService.doKingPromoter) {
            service.getAchievementService().unlock(Achievements.KING_PROMOTER);
            BooleanService.doKingPromoter = false;
            BooleanService.doKingPromoterUnlock = true;
            playFX();
        }
    }

    private void playFX() {
        service.getGuiService().getFx().playFX(5);
    }

    public void refreshGraphics() {
        repaint();
    }
}
