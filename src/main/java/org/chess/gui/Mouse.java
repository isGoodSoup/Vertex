package org.chess.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {
	private int x, y;
	private boolean isPressed;
	private boolean isClicked;
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isPressed() {
		return isPressed;
	}

	public void setPressed(boolean isPressed) {
		this.isPressed = isPressed;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		isPressed = true;
		x = e.getX();
		y = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		isPressed = false;
		isClicked = true;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		x = e.getX();
		y = e.getY();
	}
	
	public void setClicked(boolean isClicked) {
		this.isClicked = isClicked;
	}

	public boolean isClicked() {
	    if (isClicked) {
	    	isClicked = false;
	        return true;
	    }
	    return false;
	}
}
