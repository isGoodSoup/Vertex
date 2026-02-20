package org.vertex.engine.enums;

import org.vertex.engine.interfaces.Clickable;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;

public enum GameSettings implements Clickable {

    AI_OPPONENT("AI Opponent") {
        public boolean get() { return BooleanService.canAIPlay; }
        public void toggle() { BooleanService.canAIPlay ^= true; }
    },

    HELP("Help") {
        public boolean get() { return BooleanService.canToggleHelp; }
        public void toggle() { BooleanService.canToggleHelp ^= true; }
    },

    SAVES("Saves") {
        public boolean get() { return BooleanService.canSave; }
        public void toggle() { BooleanService.canSave ^= true; }
    },

    ACHIEVEMENTS("Achievements") {
        public boolean get() { return BooleanService.canDoAchievements; }
        public void toggle() { BooleanService.canDoAchievements ^= true; }
    },

    CHAOS_MODE("Chaos Mode") {
        public boolean get() { return BooleanService.canDoChaos; }
        public void toggle() {
            BooleanService.canDoChaos ^= true;
            BooleanService.canUndoMoves = true;
            BooleanService.canDoAchievements = false;
        }
    },

    BASIC_MOVES("Basic Moves") {
        public boolean get() { return BooleanService.canDoMoves; }
        public void toggle() { BooleanService.canDoMoves ^= true; }
    },

    PROMOTION("Promotion") {
        public boolean get() { return BooleanService.canPromote; }
        public void toggle() { BooleanService.canPromote ^= true; }
    },

    TIMER("Timer") {
        public boolean get() { return BooleanService.canTime; }
        public void toggle() {
            BooleanService.canTime ^= true;
            BooleanService.canStopwatch = false;
        }
    },

    STOPWATCH("Stopwatch") {
        public boolean get() { return BooleanService.canStopwatch; }
        public void toggle() {
            BooleanService.canStopwatch ^= true;
            BooleanService.canTime = false;
        }
    },

    UNDO_MOVES("Undo Moves") {
        public boolean get() { return BooleanService.canUndoMoves; }
        public void toggle() {
            BooleanService.canUndoMoves ^= true;
            BooleanService.canDoAchievements = false;
        }
    },

    RESET_TABLE("Reset Table") {
        public boolean get() { return BooleanService.canResetTable; }
        public void toggle() { BooleanService.canResetTable ^= true; }
    },

    SHOW_TICK("Tick") {
        public boolean get() { return BooleanService.canShowTick; }
        public void toggle() { BooleanService.canShowTick ^= true; }
    },

    THEMES("Themes") {
        public boolean get() { return BooleanService.canTheme; }
        public void toggle() { BooleanService.canTheme ^= true; }
    },

    COLORBLIND_MODE("Colorblind Mode") {
        public boolean get() { return BooleanService.canBeColorblind; }
        public void toggle() { BooleanService.canBeColorblind ^= true; }
    },

    HARD_MODE("Hard Mode") {
        public boolean get() { return BooleanService.canDoHard; }
        public void toggle() {
            BooleanService.canDoHard ^= true;
            BooleanService.canTime = true;
            BooleanService.canStopwatch = false;
            BooleanService.canDoAchievements = true;
            BooleanService.canSave = false;
            BooleanService.canShowTick = false;
            BooleanService.canUndoMoves = false;
            BooleanService.canResetTable = false;
        }
    };

    private final String label;

    GameSettings(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract boolean get();
    public abstract void toggle();

    @Override
    public void onClick(GameService gameService) {
        toggle();
    }
}