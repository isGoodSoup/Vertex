package org.vertex.engine.enums;

import org.vertex.engine.service.GameService;

public enum Games {
    CHESS("CHESS") {
        @Override
        public void setup(GameService gameService) {
            GameService.setState(GameState.BOARD);
            gameService.startNewGame();
        }

        @Override
        public int getBoardSize() {
            return 8;
        }
    },
    CHECKERS("CHECKERS") {
        @Override
        public void setup(GameService gameService) {
            GameService.setState(GameState.BOARD);
            gameService.startNewGame(); // TODO
        }

        @Override
        public int getBoardSize() {
            return 8;
        }
    };

    private final String label;

    Games(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public abstract int getBoardSize();

    public boolean isEnabled() {
        return true;
    }

    public abstract void setup(GameService gameService);
}
