package org.vertex.engine.service;

import org.vertex.engine.animations.MoveAnimation;
import org.vertex.engine.animations.ToastAnimation;
import org.vertex.engine.entities.Board;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.interfaces.Animation;
import org.vertex.engine.render.RenderContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnimationService {
    private final List<Animation> animations = new ArrayList<>();
    private static final int MAX_TOASTS = 4;

    public void startMove(Piece piece, int targetCol, int targetRow) {
        animations.add(new MoveAnimation(piece, piece.getX(), piece.getY(),
                targetCol * Board.getSquare(), targetRow * Board.getSquare(),
                1200));
    }

    public void add(Animation animation) {
        if (animation instanceof ToastAnimation) {
            long currentToasts = animations.stream().filter(a -> a instanceof ToastAnimation).count();
            if(currentToasts >= MAX_TOASTS) { return; }
        }
        animations.add(animation);
    }

    public void update(double delta) {
        Iterator<Animation> it = animations.iterator();
        while(it.hasNext()) {
            Animation anim = it.next();
            anim.update(delta);
            if(anim.isFinished()) { it.remove(); }
        }
    }

    public void render(Graphics2D g2) {
        int panelHeight = RenderContext.BASE_HEIGHT;
        int toastIndex = 0;

        for(Animation anim : animations) {
            if (anim instanceof ToastAnimation toast) {
                toast.setStackIndex(toastIndex, panelHeight);
                toast.render(g2);
                toastIndex++;
            } else {
                anim.render(g2);
            }
        }

    }

    public boolean isAnimating(Piece piece) {
        return animations.stream().anyMatch(a -> a.affects(piece));
    }

    public List<Animation> getAnimations() {
        return animations;
    }
}