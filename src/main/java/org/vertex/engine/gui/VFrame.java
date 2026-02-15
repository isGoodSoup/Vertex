package org.vertex.engine.gui;

import org.vertex.engine.service.BooleanService;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;

public class VFrame extends JFrame {
	@Serial
    private static final long serialVersionUID = -3130387824420425271L;
	private final static String TITLE = "Vertex v0.8";
	private final BoardPanel panel;
	private final GraphicsDevice gd;
	private Rectangle windowedBounds;

    public VFrame() {
		super(TITLE);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setUndecorated(false);
		setResizable(false);
		panel = new BoardPanel(this);
		add(panel);
		pack();
		gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice();
		setLocationRelativeTo(null);
		toggleFullscreen();
		setVisible(true);
		panel.requestFocusInWindow();
		panel.launch();
	}

	public void toggleFullscreen() {
		boolean wasFocused = panel.isFocusOwner();
		dispose();
		if (BooleanService.isFullscreen) {
			windowedBounds = getBounds();
			setUndecorated(true);
			setResizable(false);
			setExtendedState(JFrame.MAXIMIZED_BOTH);
		} else {
			setUndecorated(false);
			setResizable(true);
			setExtendedState(JFrame.NORMAL);
			if (windowedBounds != null) {
				setBounds(windowedBounds);
			}
		}
		BooleanService.isFullscreen = !BooleanService.isFullscreen;
		setVisible(true);
		if(wasFocused) { panel.requestFocus(); }
	}
}
