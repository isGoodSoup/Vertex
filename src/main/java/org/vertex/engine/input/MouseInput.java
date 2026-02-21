package org.vertex.engine.input;

import org.vertex.engine.entities.Board;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.Games;
import org.vertex.engine.interfaces.Clickable;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.PieceService;
import org.vertex.engine.service.ServiceFactory;

import java.awt.*;
import java.util.Map;

public class MouseInput {
    private final Mouse mouse;
    private final ServiceFactory service;
    private Piece piece;
    private int offsetX;
    private int offsetY;

    public boolean isClicking = false;
    private Clickable click = null;

    public MouseInput(Mouse mouse, ServiceFactory service) {
        this.mouse = mouse;
        this.service = service;
    }

    public void init() {
        this.piece = PieceService.getHeldPiece();
    }

    public Mouse getMouse() {
        return mouse;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public boolean isClickingOption(Clickable option) {
        return isClicking && click == option;
    }

    public Clickable getClick() {
        return click;
    }

    public void setClick(Clickable click) {
        this.click = click;
    }

    public void update() {
        Map<Clickable, Rectangle> buttons = service.getRender().getMenuRender().getButtons();
        switch(service.getGameService().getState()) {
            case MENU, SETTINGS -> updateMenus(buttons);
            case BOARD -> {
                updateBoard();
                updateMenus(buttons);
            }
        }
    }

    private void updateBoard() {
        checkPiece();
        pickUpPiece();
        dropPiece();
    }

    private void updateMenus(Map<Clickable, Rectangle> buttons) {
        for(Map.Entry<Clickable, Rectangle> entry : buttons.entrySet()) {
            if(entry.getValue().contains(mouse.getX(), mouse.getY()) && mouse.wasPressed()) {
                if (!isClickingOption(entry.getKey())) {
                    service.getSound().playFX(0);
                    entry.getKey().onClick(service.getGameService());
                    isClicking = true;
                    click = entry.getKey();
                }
                break;
            }
        }

        if(!mouse.wasPressed()) {
            isClicking = false;
            click = null;
        }
    }

    private void checkPiece() {
        if(mouse.wasPressed() && piece == null) {
            RenderContext render = service.getRender();
            int logicalMouseX = render.unscaleX(mouse.getX());
            int logicalMouseY = render.unscaleY(mouse.getY());

            int boardSize = Board.getSquare() *
                    service.getBoardService().getBoard().getGrids().get(GameService.getGame());
            int boardX = (RenderContext.BASE_WIDTH - boardSize)/2;
            int boardY = (RenderContext.BASE_HEIGHT - boardSize)/2;

            int mouseBoardX = logicalMouseX - boardX;
            int mouseBoardY = logicalMouseY - boardY;

            for (Piece p : service.getPieceService().getPieces()) {
                int pieceX = p.getCol() * Board.getSquare();
                int pieceY = p.getRow() * Board.getSquare();

                boolean isSandbox = GameService.getGame() == Games.SANDBOX;
                if((isSandbox ||
                        p.getColor() == service.getGameService().getCurrentTurn())
                        && mouseBoardX >= pieceX
                        && mouseBoardX < pieceX + Board.getSquare()
                        && mouseBoardY >= pieceY
                        && mouseBoardY < pieceY + Board.getSquare()) {
                    this.piece = p;
                    offsetX = mouseBoardX - pieceX;
                    offsetY = mouseBoardY - pieceY;
                    break;
                }
            }
        }
    }

    private void pickUpPiece() {
        if(piece != null) {
            RenderContext render = service.getRender();
            int logicalMouseX = render.unscaleX(mouse.getX());
            int logicalMouseY = render.unscaleY(mouse.getY());

            int boardSize = Board.getSquare() *
                    service.getBoardService().getBoard().getGrids().get(GameService.getGame());
            int boardX = (RenderContext.BASE_WIDTH - boardSize)/2;
            int boardY = (RenderContext.BASE_HEIGHT - boardSize)/2;

            int mouseBoardX = logicalMouseX - boardX;
            int mouseBoardY = logicalMouseY - boardY;

            piece.setX(mouseBoardX - offsetX);
            piece.setY(mouseBoardY - offsetY);
        }
    }

    private void dropPiece() {
        if(!mouse.wasPressed() && piece != null) {
            RenderContext render = service.getRender();
            int boardSize = Board.getSquare() *
                    service.getBoardService().getBoard().getGrids().get(GameService.getGame());
            int boardX = (RenderContext.BASE_WIDTH - boardSize)/2;
            int boardY = (RenderContext.BASE_HEIGHT - boardSize)/2;
            int mouseBoardX = render.unscaleX(mouse.getX()) - boardX;
            int mouseBoardY = render.unscaleY(mouse.getY()) - boardY;
            int targetCol = mouseBoardX / Board.getSquare();
            int targetRow = mouseBoardY / Board.getSquare();
            int maxIndex = (GameService.getGame() == Games.SHOGI) ? 8 : 7;
            targetCol = Math.max(0, Math.min(maxIndex, targetCol));
            targetRow = Math.max(0, Math.min(maxIndex, targetRow));
            service.getMovesManager().attemptMove(piece, targetCol, targetRow);
            service.getSound().playFX(0);
            piece = null;
        }
    }
}
