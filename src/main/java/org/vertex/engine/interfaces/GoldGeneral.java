package org.vertex.engine.interfaces;

import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.service.PieceService;

import java.util.List;

public interface GoldGeneral {
    default boolean canMoveLikeGold(Piece piece, int targetCol, int targetRow, List<Piece> board) {
        int col = piece.getCol();
        int row = piece.getRow();
        int forward = (piece.getColor() == Tint.LIGHT) ? -1 : 1;

        int[][] directions = {
                {0, forward}, {-1, forward}, {1, forward},
                {-1, 0}, {1, 0},
                {0, -forward}
        };

        for(int[] d : directions) {
            int newCol = col + d[0];
            int newRow = row + d[1];
            if(newCol == targetCol && newRow == targetRow) {
                Piece targetPiece = PieceService.getPieceAt(targetCol, targetRow, board);
                return targetPiece == null || targetPiece.getColor() != piece.getColor();
            }
        }
        return false;
    }
}
