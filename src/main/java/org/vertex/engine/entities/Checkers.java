package org.vertex.engine.entities;

import org.vertex.engine.enums.Tint;
import org.vertex.engine.enums.Type;
import org.vertex.engine.service.PieceService;

import java.util.List;

public class Checkers extends Piece {

    public Checkers(Tint color, int col, int row) {
        super(color, col, row);
        this.id = Type.PAWN;
        if(color == Tint.WHITE) {
            sprite = PieceService.getImage("/pieces/checker_white");
            hovered = PieceService.getImage("/pieces/checker_whiteh");
        } else {
            sprite = PieceService.getImage("/pieces/checker_black");
            hovered = PieceService.getImage("/pieces/checker_blackh");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow, List<Piece> board) {
        return false;
    }

    @Override
    public Piece copy() {
        return null;
    }
}
