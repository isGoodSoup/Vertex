package org.chess.render;

import org.chess.manager.MovesManager;

public class RenderContext {
    public static final int BASE_WIDTH = 1920;
    public static final int BASE_HEIGHT = 1080;
    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    private BoardRender boardRender;
    private MenuRender menuRender;
    private MovesRender movesRender;

    private MovesManager movesManager;

    public RenderContext() {
        this.boardRender = new BoardRender(this);
        this.menuRender = new MenuRender(this);
        this.movesRender = new MovesRender(this, movesManager);
    }

    public BoardRender getBoardRender() {
        return boardRender;
    }

    public void setBoardRender(BoardRender boardRender) {
        this.boardRender = boardRender;
    }

    public MenuRender getMenuRender() {
        return menuRender;
    }

    public void setMenuRender(MenuRender menuRender) {
        this.menuRender = menuRender;
    }

    public MovesRender getMovesRender() {
        return movesRender;
    }

    public void setMovesRender(MovesRender movesRender) {
        this.movesRender = movesRender;
    }

    public MovesManager getMovesManager() {
        return movesManager;
    }

    public void setMovesManager(MovesManager movesManager) {
        this.movesManager = movesManager;
    }

    public void updateTransform(int windowWidth, int windowHeight) {
        double scaleX = windowWidth/(double) BASE_WIDTH;
        double scaleY = windowHeight/(double) BASE_HEIGHT;
        scale = Math.min(scaleX, scaleY);

        int drawWidth = (int)(BASE_WIDTH * scale);
        int drawHeight = (int)(BASE_HEIGHT * scale);

        offsetX = (windowWidth - drawWidth)/2;
        offsetY = (windowHeight - drawHeight)/2;
    }

    public void updateScale(int windowWidth, int windowHeight) {
        double scaleX = windowWidth/(double) BASE_WIDTH;
        double scaleY = windowHeight/(double) BASE_HEIGHT;
        scale = Math.min(scaleX, scaleY);
    }

    public double getScale() {
        return scale;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public int scale(int value) {
        return (int) (value * scale);
    }

    public int unscaleX(int rawX) {
        return (int) ((rawX - offsetX)/scale);
    }

    public int unscaleY(int rawY) {
        return (int) ((rawY - offsetY)/scale);
    }
}
