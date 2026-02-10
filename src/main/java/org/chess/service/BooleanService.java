package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;

import java.util.Random;

public class BooleanService {
    public static boolean canMove;
    public static boolean validSquare;
    public static boolean isDragging;
    public static boolean isLegal;
    public static boolean isAIPlaying;
    public static boolean isGameOver;
    public static boolean isPromotionPending;
    public static boolean isTurn;

    public static boolean canUndoMoves;
    public static boolean canPromote;
    public static boolean isTestingToggle;
    public static boolean isChaosActive;
    public static boolean isEasyModeActive;
    public static boolean isCastlingActive;
    public static boolean isEnPassantActive;

    private static final Random random = new Random();

    public static void defaultToggles() {
        isCastlingActive = true;
        isEnPassantActive = true;
        canPromote = true;
        isGameOver = false;
        isChaosActive = false;
    }

    public static boolean getBoolean() {
        return random.nextBoolean();
    }

    public static int getRandom(int i, int i1) {
        return random.nextInt(i, i1);
    }

    public static int getRandom(int i) {
        return random.nextInt(i);
    }

    public static Piece getRandomPiece(Piece pawn, Tint color) {
        int index = getRandom(0, 3);
        switch(index) {
            case 0 -> pawn = new Rook(color, pawn.getCol(), pawn.getRow());
            case 1 -> pawn = new Bishop(color, pawn.getCol(), pawn.getRow());
            case 2 -> pawn = new Knight(color, pawn.getCol(), pawn.getRow());
            case 3 -> pawn = new Queen(color, pawn.getCol(), pawn.getRow());
        }
        return pawn;
    }
}
