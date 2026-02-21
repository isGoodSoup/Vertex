package org.vertex.engine.render;

import org.vertex.engine.input.Mouse;
import org.vertex.engine.input.MouseInput;
import org.vertex.engine.interfaces.Clickable;
import org.vertex.engine.interfaces.UI;
import org.vertex.engine.manager.MovesManager;

import java.util.ArrayList;
import java.util.List;

public class RenderContext {
    public static final int BASE_WIDTH = 1920;
    public static final int BASE_HEIGHT = 1080;
    private final List<UI> menus;
    private double scale = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    private BoardRender boardRender;
    private MenuRender menuRender;
    private MovesRender movesRender;
    private ControlsRender controlsRender;

    private MovesManager movesManager;
    private Mouse mouse;
    private MouseInput mouseInput;

    public RenderContext() {
        this.boardRender = new BoardRender(this);
        this.menus = new ArrayList<>();
        this.menuRender = new MenuRender(this);
        this.movesRender = new MovesRender(this, movesManager);
        this.controlsRender = new ControlsRender();
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

    public ControlsRender getControlsRender() {
        return controlsRender;
    }

    public void setControlsRender(ControlsRender controlsRender) {
        this.controlsRender = controlsRender;
    }

    public Mouse getMouse() {
        return mouse;
    }

    public void setMouse(Mouse mouse) {
        this.mouse = mouse;
    }

    public MouseInput getMouseInput() {
        return mouseInput;
    }

    public void setMouseInput(MouseInput mouseInput) {
        this.mouseInput = mouseInput;
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

    public boolean isHovered(Clickable param) {
        return menuRender.getButtons().get(param).contains(mouse.getX(), mouse.getY())
                && !mouseInput.isClickingOption(param);
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
