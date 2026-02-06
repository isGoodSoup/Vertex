package org.chess;

import org.chess.gui.ChessFrame;

public class ChessApp {
	private static ChessApp app;
	private final ChessFrame frame;

	public ChessApp() {
		super();
		this.frame = new ChessFrame();
	}
	
	public static void main(String[] args) {
		app = new ChessApp();
	}

	public ChessFrame getFrame() {
		return frame;
	}
}
