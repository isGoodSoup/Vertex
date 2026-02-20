package org.vertex.engine.enums;

import org.vertex.engine.service.GameService;

public enum GameMenu {
    PLAY("", "Start a match of ", "Continue match of ") {
        @Override
        public void run(GameService gameService) {
            GameService.getGame().setup(gameService);
        }
    },
    SETTINGS("SETTINGS", "Settings, themes, toggles", "") {
        @Override
        public void run(GameService gameService) {
            gameService.setState(GameState.RULES);
        }
    },
    ADVANCEMENTS("ACHIEVEMENTS", "Track your progress", "") {
        @Override
        public void run(GameService gameService) {
            gameService.setState(GameState.ACHIEVEMENTS);
        }
    },
    EXIT("EXIT", "Leave?", "") {
        @Override
        public void run(GameService gameService) {
            System.exit(0);
        }
    };

    private final String label;
    private final String tooltip;
    private final String continueTooltip;

    GameMenu(String label, String tooltip, String continueTooltip) {
        this.label = label;
        this.tooltip = tooltip;
        this.continueTooltip = continueTooltip;
    }

    public String getLabel() {
        return label;
    }

    public String getTooltip() {
        return tooltip;
    }

    public String getContinueTooltip() {
        return continueTooltip;
    }

    public boolean isEnabled(GameService gameService) {
        return true;
    }

    public abstract void run(GameService gameService);
}
