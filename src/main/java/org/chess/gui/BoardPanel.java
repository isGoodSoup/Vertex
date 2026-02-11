package org.chess.gui;

import org.chess.enums.GameState;
import org.chess.enums.PlayState;
import org.chess.input.Keyboard;
import org.chess.input.MoveManager;
import org.chess.render.MenuRender;
import org.chess.service.*;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

public class BoardPanel extends JPanel implements Runnable {
	@Serial
    private static final long serialVersionUID = -5189356863277669172L;
	private final int FPS = 60;
	private Thread thread;
    private static ServiceFactory service;

	public BoardPanel() {
        super();
        service = new ServiceFactory();
        GameService.setState(GameState.MENU);
        BooleanService.defaultToggles();
        final int WIDTH = GUIService.getWIDTH();
        final int HEIGHT = GUIService.getHEIGHT();
        MenuRender.drawRandomBackground(BooleanService.getBoolean());
        setPreferredSize(new Dimension(WIDTH +
                GUIService.getEXTRA_WIDTH(), HEIGHT));
        setBackground(GUIService.getNewBackground());
        addMouseMotionListener(service.getMouseService());
        addMouseListener(service.getMouseService());
        addKeyListener(service.getKeyboard());
        setFocusable(true);
        requestFocusInWindow();
	}

    public void launch() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        double drawInterval = (double) 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while(thread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if(delta >= 1) {
                update();
                service.getAnimationService().update();
                service.getMouseService().update();
                repaint();
                delta--;
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        switch(GameService.getState()) {
            case MENU -> service.getGuiService().getMenuRender().drawGraphics(g2,
                    MenuRender.optionsMenu);
            case MODE -> service.getGuiService().getMenuRender().drawGraphics(g2,
                    MenuRender.optionsMode);
            case RULES -> service.getGuiService().getMenuRender()
                    .drawOptionsMenu(g2, MenuRender.optionsTweaks);
            case BOARD -> {
                service.getGuiService().getBoardRender().drawBoard(g2);
                service.getGuiService().getMovesRender().drawMoves(g2);
            }
        }

        if(service.getTimerService().isActive()) {
            service.getGuiService().drawTimer(g2);
            service.getGuiService().drawTick(g2, BooleanService.isLegal);
        }
    }

    private void update() {
        checkKeyboard();
        service.getTimerService().update();
        service.getBoardService().resetBoard();
        switch(GameService.getState()) {
            case MENU -> {
                service.getGuiService().getMenuRender().handleMenuInput();
                return;
            }
            case MODE -> {
                GameService.setMode();
                return;
            }
            case RULES -> {
                service.getGuiService().getMenuRender().handleOptionsInput();
                return;
            }
            default -> service.getBoardService().getGame();
        }

        PlayState mode = GameService.getMode();
        if(mode != null) {
            switch(mode) {
                case PLAYER -> BooleanService.isAIPlaying = false;
                case AI -> BooleanService.isAIPlaying = true;
            }
        }
    }

    private void checkKeyboard() {
        MoveManager move = BoardService.getManager();
        Keyboard keyboard = service.getKeyboard();
        GameState state = GameService.getState();

        if((state == GameState.RULES || state == GameState.MODE)
                && keyboard.wasBPressed()) {
            GameService.setState(GameState.MENU);
            service.getGuiService().getFx().playFX(3);
        }

        switch(state) {
            case MENU -> {
                if(keyboard.wasUpPressed()) { move.moveUp(MenuRender.optionsMenu); }
                if(keyboard.wasDownPressed()) { move.moveDown(MenuRender.optionsMenu); }
                if(keyboard.wasSelectPressed()) { move.activate(GameState.MENU); }
            }
            case MODE -> {
                if(keyboard.wasUpPressed()) { move.moveUp(MenuRender.optionsMode); }
                if(keyboard.wasDownPressed()) { move.moveDown(MenuRender.optionsMode); }
                if(keyboard.wasSelectPressed()) { move.activate(GameState.MODE); }
            }
            case RULES -> {
                if(keyboard.wasUpPressed()) { move.moveUp(MenuRender.optionsTweaks); }
                if(keyboard.wasLeftPressed()) { move.moveLeft(MenuRender.optionsTweaks); }
                if(keyboard.wasDownPressed()) { move.moveDown(MenuRender.optionsTweaks); }
                if(keyboard.wasRightPressed()) { move.moveRight(MenuRender.optionsTweaks); }
                if(keyboard.wasSelectPressed()) { move.activate(GameState.RULES); }
            }
            case BOARD -> {
                if(keyboard.wasUpPressed()) { move.moveUp(); move.updateKeyboardHover(); }
                if(keyboard.wasLeftPressed()) { move.moveLeft(); move.updateKeyboardHover(); }
                if(keyboard.wasDownPressed()) { move.moveDown(); move.updateKeyboardHover(); }
                if(keyboard.wasRightPressed()) { move.moveRight(); move.updateKeyboardHover(); }
                if(keyboard.wasSelectPressed()) { move.activate(GameState.BOARD); }
                if(keyboard.wasZPressed()) { move.undoLastMove(move.getSelectedPiece()); }
            }
        }
    }
}
