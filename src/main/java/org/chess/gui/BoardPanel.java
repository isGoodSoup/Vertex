package org.chess.gui;

import org.chess.entities.*;
import org.chess.enums.GameState;
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
        final int WIDTH = GUIService.getWIDTH();
        final int HEIGHT = GUIService.getHEIGHT();
        GUIService.drawRandomBackground(BooleanService.getBoolean());
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addMouseMotionListener(service.getMouseService());
        addMouseListener(service.getMouseService());
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
                AnimationService.animateMove();
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
            case MENU -> service.getGuiService().drawGraphics(g2,
                    GUIService.optionsMenu);
            case MODE -> service.getGuiService().drawGraphics(g2,
                    GUIService.optionsMode);
            case BOARD -> service.getGuiService().drawBoard(g2);
        }
    }

    private void update() {
        switch(GameService.getState()) {
            case MENU -> {
                service.getGuiService().handleMenuInput();
                return;
            }
            case MODE -> {
                GameService.setMode();
                return;
            }
            default -> service.getBoardService().getGame();
        }

        switch(GameService.getMode()) {
            case PLAYER -> BooleanService.isAIPlaying = false;
            case AI -> BooleanService.isAIPlaying = true;
            case CHAOS -> BooleanService.isChaosActive = true;
        }
    }
}
