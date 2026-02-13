package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;

import java.util.Random;

public class BooleanService {
    public static boolean canMove;
    public static boolean isValidSquare;
    public static boolean isLegal;
    public static boolean isGameOver;
    public static boolean isPromotionPending;
    public static boolean isTurn;
    public static boolean isDarkMode;
    public static boolean isFullscreen;
    public static boolean isExitActive;
    public static boolean isAchievementLocked;

    public static boolean canUndoMoves;
    public static boolean canDoAchievements;
    public static boolean canPromote;
    public static boolean canSave;
    public static boolean canAIPlay;
    public static boolean canSandbox;
    public static boolean canDoChaos;
    public static boolean canDoCastling;
    public static boolean canDoEnPassant;
    public static boolean canTime;
    public static boolean canStopwatch;
    public static boolean canResetTable;
    public static boolean canBeColorblind;
    public static boolean canTheme;
    public static boolean canToggleMoves;

    public static boolean doFirstMove;
    public static boolean doRuleToggles;
    public static boolean doCheckmate;
    public static boolean doCastling;
    public static boolean doKingPromoter;
    public static boolean doQuickWin;
    public static boolean doCheckOver;
    public static boolean doHundred;
    public static boolean doAllPieces;
    public static boolean doHardGame;
    public static boolean doUntouchable;
    public static boolean doGrandmaster;

    private static final Random random = new Random();

    public static void defaultToggles() {
        canAIPlay = true;
        isFullscreen = true;
        isDarkMode = false;
        canDoCastling = true;
        canDoEnPassant = true;
        canPromote = true;
        canSave = true;
        canResetTable = true;
        canStopwatch = true;
        isGameOver = false;
        isExitActive = false;
        canDoChaos = false;
        canTheme = false;
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
