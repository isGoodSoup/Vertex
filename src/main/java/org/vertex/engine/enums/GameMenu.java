package org.vertex.engine.enums;

import org.vertex.engine.service.GameService;

public enum GameMenu {
    NEW_GAME("NEW GAME") {
        @Override
        public void run(GameService gameService) {
            GameService.setState(GameState.GAMES);
        }
    },
    LOAD_GAME("LOAD GAME") {
        @Override
        public void run(GameService gameService) {
            if(!gameService.getSaveManager().getSaves().isEmpty()) {
                GameService.setState(GameState.SAVES);
            }
        }
    },
    SETTINGS("SETTINGS") {
        @Override
        public void run(GameService gameService) {
            GameService.setState(GameState.RULES);
        }
    },
    ADVANCEMENTS("ACHIEVEMENTS") {
        @Override
        public void run(GameService gameService) {
            GameService.setState(GameState.ACHIEVEMENTS);
        }
    },
    EXIT("EXIT") {
        @Override
        public void run(GameService gameService) {
            System.exit(0);
        }
    };

    private final String label;

    GameMenu(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isEnabled(GameService gameService) {
        return true;
    }

    public abstract void run(GameService gameService);
}
