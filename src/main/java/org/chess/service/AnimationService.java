package org.chess.service;

import org.chess.entities.Board;
import org.chess.entities.Piece;
import org.chess.gui.MoveAnimation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnimationService {
    private final List<MoveAnimation> animations = new ArrayList<>();

    public void startMove(Piece piece, int targetCol, int targetRow) {
        animations.add(new MoveAnimation(piece, piece.getX(), piece.getY(),
                targetCol * Board.getSquare(), targetRow * Board.getSquare()));
    }

    public void update() {
        Iterator<MoveAnimation> it = animations.iterator();
        while (it.hasNext()) {
            MoveAnimation anim = it.next();
            anim.update(0.05); // progress per frame
            if (anim.isFinished()) it.remove();
        }
    }

    public boolean isAnimating(Piece piece) {
        return animations.stream().anyMatch(a -> a.piece == piece);
    }
}