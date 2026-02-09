package org.chess.service;

import java.util.Random;

public class BooleanService {
    public static boolean canMove;
    public static boolean validSquare;
    public static boolean isDragging;
    public static boolean isLegal;
    public static boolean isAIPlaying;
    public static boolean isChaosActive;
    public static boolean isGameOver;
    public static boolean isPromotionPending;
    public static boolean isTurn;
    private static final Random random = new Random();

    public static boolean getBoolean() {
        return random.nextBoolean();
    }

    public static int getRandom(int i, int i1) {
        return random.nextInt(i, i1);
    }
}
