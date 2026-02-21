package org.vertex.engine.enums;

import org.vertex.engine.entities.Board;
import org.vertex.engine.interfaces.State;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;

public enum Games implements State {
    CHESS("CHESS", "",
            "A classic strategy duel where every piece moves differently. " +
                    "Outsmart your opponent and deliver checkmate to win") {
        @Override
        public void setup(GameService gameService) {
            gameService.setGame(this);
            gameService.setState(GameState.BOARD);
            if(!gameService.getSaveManager().autosaveExists()) {
                gameService.startNewGame();
            } else {
                gameService.continueGame();
            }
        }

        @Override
        public int getBoardSize(Board board, GameService gameService) {
            return board.getGrids().get(GameService.getGame());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    },
    CHECKERS("CHECKERS", "checker_",
            "A fast-paced tactical game of diagonal moves and jumps. Capture " +
                    "all opponent pieces or block their moves to win") {
        @Override
        public void setup(GameService gameService) {
            gameService.setGame(this);
            gameService.setState(GameState.BOARD);
            if(!gameService.getSaveManager().autosaveExists()) {
                gameService.startNewGame();
            } else {
                gameService.continueGame();
            }
        }

        @Override
        public int getBoardSize(Board board, GameService gameService) {
            return board.getGrids().get(GameService.getGame());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    },
    SHOGI("SHOGI", "shogi_", "Japanese chess on a 9×9 board where " +
            "captured pieces can rejoin the game under your control."){
        @Override
        public void setup(GameService gameService) {
            gameService.setGame(this);
            gameService.setState(GameState.BOARD);
            if(!gameService.getSaveManager().autosaveExists()) {
                gameService.startNewGame();
            } else {
                gameService.continueGame();
            }
        }

        @Override
        public int getBoardSize(Board board, GameService gameService) {
            return board.getGrids().get(GameService.getGame());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    },
    SANDBOX("SANDBOX", "", "Developer tool to test features and piece moves, " +
            "no AI enabled"){
        @Override
        public void setup(GameService gameService) {
            BooleanService.canAIPlay = false;
            BooleanService.canSwitchTurns = false;

            gameService.setGame(this);
            gameService.setState(GameState.BOARD);

            if(!gameService.getSaveManager().autosaveExists()) {
                gameService.startNewGame();
            } else {
                gameService.continueGame();
            }
        }

        @Override
        public int getBoardSize(Board board, GameService gameService) {
            return board.getGrids().get(GameService.getGame());
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    };

    private final String label;
    private final String spritePrefix;
    private final String tooltip;

    Games(String label, String spritePrefix, String tooltip) {
        this.label = label;
        this.spritePrefix = spritePrefix;
        this.tooltip = tooltip;
    }

    public String getLabel() {
        return label;
    }

    public String getSpritePrefix() {
        return spritePrefix;
    }

    public String getTooltip() {
        return tooltip;
    }

    public abstract boolean isEnabled();

    public abstract int getBoardSize(Board board, GameService gameService);

    public abstract void setup(GameService gameService);
}
