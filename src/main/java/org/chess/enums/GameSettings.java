package org.chess.enums;

import org.chess.gui.Colors;
import org.chess.service.BooleanService;

public enum GameSettings {
    AI_OPPONENT("AI Opponent") {
        public boolean get() {
            return BooleanService.canAIPlay;
        }
        public void toggle() {
            BooleanService.canAIPlay ^= true;
        }
    },

    SAVES("Saves") {
        public boolean get() {
            return BooleanService.canSave;
        }
        public void toggle() {
            BooleanService.canSave ^= true;
        }
    },

    ACHIEVEMENTS("Achievements") {
        public boolean get() {
            return BooleanService.canDoAchievements;
        }
        public void toggle() {
            BooleanService.canDoAchievements ^= true;
        }
    },

    SANDBOX_MODE("Sandbox Mode") {
        public boolean get() {
            return BooleanService.canSandbox;
        }
        public void toggle() {
            BooleanService.canSandbox ^= true;
        }
    },

    CHAOS_MODE("Chaos Mode") {
        public boolean get() {
            return BooleanService.canDoChaos;
        }
        public void toggle() {
            BooleanService.canDoChaos ^= true;
        }
    },

    TRAINING_MODE("Training Mode") {
        public boolean get() {
            return BooleanService.canDoTraining;
        }
        public void toggle() {
            BooleanService.canDoTraining ^= true;
            BooleanService.canUndoMoves = true;
            BooleanService.canShowTick = true;
            BooleanService.canDoAchievements = false;
            BooleanService.canDoChaos = false;
        }
    },

    BASIC_MOVES("Basic Moves") {
        public boolean get() {
            return BooleanService.canDoMoves;
        }
        public void toggle() {
            BooleanService.canDoMoves ^= true;
        }
    },

    PROMOTION("Promotion") {
        public boolean get() {
            return BooleanService.canPromote;
        }
        public void toggle() {
            BooleanService.canPromote ^= true;
        }
    },

    TIMER("Timer") {
        public boolean get() {
            return BooleanService.canTime;
        }
        public void toggle() {
            BooleanService.canTime ^= true;
            BooleanService.canStopwatch = false;
        }
    },

    STOPWATCH("Stopwatch") {
        public boolean get() {
            return BooleanService.canStopwatch;
        }
        public void toggle() {
            BooleanService.canStopwatch ^= true;
            BooleanService.canTime = false;
        }
    },

    UNDO_MOVES("Undo Moves") {
        public boolean get() {
            return BooleanService.canUndoMoves;
        }
        public void toggle() {
            BooleanService.canUndoMoves ^= true;
            BooleanService.canDoAchievements = false;
        }
    },

    RESET_TABLE("Reset Table") {
        public boolean get() {
            return BooleanService.canResetTable;
        }
        public void toggle() {
            BooleanService.canResetTable ^= true;
        }
    },

    DARK_MODE("Dark Mode") {
        public boolean get() {
            return BooleanService.isDarkMode;
        }
        public void toggle() {
            BooleanService.isDarkMode ^= true;
            Colors.toggleDarkTheme();
        }
    },

    THEMES("Themes") {
        public boolean get() {
            return BooleanService.canTheme;
        }
        public void toggle() {
            BooleanService.canTheme ^= true;

            if (!BooleanService.doRuleTogglesUnlock) {
                if (!BooleanService.doRuleToggles) {
                    BooleanService.doRuleToggles = true;
                }
            }
        }
    },

    COLORBLIND_MODE("Colorblind Mode") {
        public boolean get() {
            return BooleanService.canBeColorblind;
        }
        public void toggle() {
            BooleanService.canBeColorblind ^= true;
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
}
