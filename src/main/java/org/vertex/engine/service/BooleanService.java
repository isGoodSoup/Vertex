package org.vertex.engine.service;

import org.vertex.engine.entities.*;
import org.vertex.engine.enums.Tint;

import java.util.Random;

public class BooleanService {
    public static boolean isAIMoving;
    public static boolean canMove;
    public static boolean isValidSquare;
    public static boolean isLegal;
    public static boolean isDragging;
    public static boolean isCheckmate;
    public static boolean isStalemate;
    public static boolean isPromotionActive;
    public static boolean isTurn;
    public static boolean isDarkMode;
    public static boolean isFullscreen;
    public static boolean isExitActive;
    public static boolean isAchievementLocked;
    public static boolean isMovesActive;
    public static boolean canType;
    public static boolean canPlayFX;
    public static boolean canZoomIn;
    public static boolean isSandboxEnabled;

    public static boolean canDoMoves;
    public static boolean canUndoMoves;
    public static boolean canDoAchievements;
    public static boolean canPromote;
    public static boolean canSave;
    public static boolean canAIPlay;
    public static boolean canDoSandbox;
    public static boolean canDoChaos;
    public static boolean canDoHard;
    public static boolean canTime;
    public static boolean canStopwatch;
    public static boolean canResetTable;
    public static boolean canBeColorblind;
    public static boolean canTheme;
    public static boolean canShowTick;

    private static final Random random = new Random();

    public static void defaultToggles() {
        canPlayFX = true;
        canDoMoves = true;
        canAIPlay = true;
        canPromote = true;
        canDoAchievements = true;
        canShowTick = true;
        canZoomIn = false;
        canSave = false;
        canResetTable = true;
        canStopwatch = true;
        canDoSandbox = false;
        isSandboxEnabled = false;
        canDoChaos = false;
        canTheme = false;
        isFullscreen = true;
        isDarkMode = false;
        isCheckmate = false;
        isExitActive = false;
        canType = false;
        isDragging = false;
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
