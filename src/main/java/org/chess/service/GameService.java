package org.chess.service;

import org.chess.enums.GameState;
import org.chess.enums.PlayState;
import org.chess.enums.Tint;
import org.chess.gui.BoardPanel;
import org.chess.gui.Mouse;
import org.chess.render.MenuRender;

public class GameService {
    private static GameState state;
    private static PlayState mode;
    private static Tint currentTurn;

    private static BoardService boardService;
    private static Mouse mouse;

    public GameService(BoardService boardService, Mouse mouse) {
        GameService.boardService = boardService;
        GameService.mouse = mouse;
    }

    public static GameState getState() {
        return state;
    }

    public static PlayState getMode() {
        return mode;
    }

    public static void setState(GameState state) {
        GameService.state = state;
    }

    public static void setMode() {
        if(!mouse.wasPressed()) {
            return;
        }

        if(state != GameState.MODE) {
            return;
        }

        int startY = GUIService.getHEIGHT()/2 + GUIService.getMENU_START_Y();
        int spacing = GUIService.getMENU_SPACING();

        for(int i = 0; i < MenuRender.optionsMode.length; i++) {
            int y = startY + i * spacing;
            boolean isHovered = GUIService.getHITBOX(y).contains(mouse.getX(),
                    mouse.getY());
            if(isHovered) {
                switch(i) {
                    case 0 -> mode = PlayState.PLAYER;
                    case 1 -> mode = PlayState.AI;
                }
                boardService.startBoard();
                return;
            }
        }
    }

    public static Tint getCurrentTurn() {
        return currentTurn;
    }

    public static void setCurrentTurn(Tint tint) {
        currentTurn = tint;
    }

    public static boolean isBlackTurn() {
        return currentTurn == Tint.BLACK;
    }

    public void startNewGame() {
        setState(GameState.MODE);
    }

    public void optionsMenu() {
        setState(GameState.OPTIONS);
    }
}
