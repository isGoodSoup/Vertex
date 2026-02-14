package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;

import java.util.Random;

public class BooleanService {
    public static boolean canMove;
    public static boolean isValidSquare;
    public static boolean isLegal;
    public static boolean isCheckmate;
    public static boolean isPromotionActive;
    public static boolean isTurn;
    public static boolean isDarkMode;
    public static boolean isFullscreen;
    public static boolean isExitActive;
    public static boolean isAchievementLocked;
    public static boolean isMovesActive;

    public static boolean canDoMoves;
    public static boolean canUndoMoves;
    public static boolean canDoAchievements;
    public static boolean canPromote;
    public static boolean canSave;
    public static boolean canAIPlay;
    public static boolean canDoTraining;
    public static boolean canDoSandbox;
    public static boolean canDoChaos;
    public static boolean canDoHard;
    public static boolean canTime;
    public static boolean canStopwatch;
    public static boolean canResetTable;
    public static boolean canBeColorblind;
    public static boolean canTheme;
    public static boolean canShowTick;

    public static boolean doFirstMove;
    public static boolean doFirstMoveUnlock;
    public static boolean doRuleToggles;
    public static boolean doRuleTogglesUnlock;
    public static boolean doCheckmate;
    public static boolean doCheckmateUnlock;
    public static boolean doMasterCastling;
    public static boolean doMasterCastlingUnlock;
    public static boolean doKingPromoter;
    public static boolean doKingPromoterUnlock;
    public static boolean doQuickWin;
    public static boolean doQuickWinUnlock;
    public static boolean doCheckOver;
    public static boolean doCheckOverUnlock;
    public static boolean doHundred;
    public static boolean doHundredUnlock;
    public static boolean doAllPieces;
    public static boolean doAllPiecesUnlock;
    public static boolean doHardGame;
    public static boolean doHardGameUnlock;
    public static boolean doUntouchable;
    public static boolean doUntouchableUnlock;
    public static boolean doGrandmaster;
    public static boolean doGrandmasterUnlock;

    private static final Random random = new Random();

    public static void defaultToggles() {
        canDoMoves = true;
        canAIPlay = true;
        canPromote = true;
        canDoAchievements = true;
        canShowTick = true;
        canSave = true;
        canResetTable = true;
        canStopwatch = true;
        canDoChaos = false;
        canDoTraining = false;
        canTheme = false;
        isFullscreen = true;
        isDarkMode = false;
        isCheckmate = false;
        isExitActive = false;
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
