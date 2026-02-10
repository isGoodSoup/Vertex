package org.chess.gui;

import org.chess.entities.Piece;

public class MoveAnimation {
    public Piece piece;
    public int startX, startY;
    public int targetX, targetY;
    public double progress;

    public MoveAnimation(Piece piece, int startX, int startY, int targetX, int targetY) {
        this.piece = piece;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.progress = 0.0;
    }

    public void update(double delta) {
        progress = Math.min(1.0, progress + delta);
        piece.setX((int)(startX + (targetX - startX) * progress));
        piece.setY((int)(startY + (targetY - startY) * progress));
    }

    public boolean isFinished() {
        return progress >= 1.0;
    }
}

