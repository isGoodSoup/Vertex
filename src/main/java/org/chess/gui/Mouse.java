package org.chess.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {
	private int x, y;
	private boolean isHeld;
	private boolean isClicked;

	public Mouse() {}

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

	public boolean isHeld() {
		return isHeld;
	}

	public void setHeld(boolean isPressed) {
		this.isHeld = isPressed;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		isHeld = true;
		x = e.getX();
		y = e.getY();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		isHeld = false;
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
