package org.chess.input;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Mouse extends MouseAdapter {
	private int x, y;
	private boolean isHeld;
	private boolean prevHeld;

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

	public void update() {
		prevHeld = isHeld;
	}

	public boolean wasPressed() {
		return isHeld && !prevHeld;
	}

	public boolean wasReleased() {
		return !isHeld && prevHeld;
	}

	public Point getMousePosition() {
		return new Point(getX(), getY());
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
}
