package org.vertex.engine.input;

import org.vertex.engine.entities.Board;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.render.RenderContext;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.ServiceFactory;

public class MouseInput {
    private final Mouse mouse;
    private final ServiceFactory service;
    private Piece piece;
    private int offsetX;
    private int offsetY;

    public MouseInput(Mouse mouse, ServiceFactory service) {
        this.mouse = mouse;
        this.service = service;
    }

    public void init() {
        this.piece = service.getPieceService().getHeldPiece();
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

    public void update() {
        checkPiece();
        pickUpPiece();
        dropPiece();
    }

    private void checkPiece() {
        if(mouse.wasPressed() && piece == null) {
            RenderContext render = service.getRender();
            int logicalMouseX = render.unscaleX(mouse.getX());
            int logicalMouseY = render.unscaleY(mouse.getY());

            int boardSize = Board.getSquare() * 8;
            int boardX = (RenderContext.BASE_WIDTH - boardSize) / 2;
            int boardY = (RenderContext.BASE_HEIGHT - boardSize) / 2;

            int mouseBoardX = logicalMouseX - boardX;
            int mouseBoardY = logicalMouseY - boardY;

            for (Piece p : service.getPieceService().getPieces()) {
                int pieceX = p.getCol() * Board.getSquare();
                int pieceY = p.getRow() * Board.getSquare();

                if (p.getColor() == GameService.getCurrentTurn()
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

            int boardSize = Board.getSquare() * 8;
            int boardX = (RenderContext.BASE_WIDTH - boardSize) / 2;
            int boardY = (RenderContext.BASE_HEIGHT - boardSize) / 2;

            int mouseBoardX = logicalMouseX - boardX;
            int mouseBoardY = logicalMouseY - boardY;

            piece.setX(mouseBoardX - offsetX);
            piece.setY(mouseBoardY - offsetY);
        }
    }

    private void dropPiece() {
        if(!mouse.wasPressed() && piece != null) {
            RenderContext render = service.getRender();
            int boardSize = Board.getSquare() * 8;
            int boardX = (RenderContext.BASE_WIDTH - boardSize) / 2;
            int boardY = (RenderContext.BASE_HEIGHT - boardSize) / 2;
            int mouseBoardX = render.unscaleX(mouse.getX()) - boardX;
            int mouseBoardY = render.unscaleY(mouse.getY()) - boardY;
            int targetCol = mouseBoardX / Board.getSquare();
            int targetRow = mouseBoardY / Board.getSquare();
            targetCol = Math.max(0, Math.min(7, targetCol));
            targetRow = Math.max(0, Math.min(7, targetRow));
            service.getMovesManager().attemptMove(piece, targetCol, targetRow);
            service.getSound().playFX(0);
            piece = null;
        }
    }
}
