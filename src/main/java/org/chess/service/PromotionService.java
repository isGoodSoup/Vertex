package org.chess.service;

import org.chess.entities.*;
import org.chess.enums.Tint;
import org.chess.enums.Type;
import org.chess.gui.Mouse;

public class PromotionService {
    private Tint promotionColor;
    private Piece promotingPawn;

    private static Mouse mouse;
    private final PieceService pieceService;

    public PromotionService(PieceService pieceService, Mouse mouse) {
        this.pieceService = pieceService;
        PromotionService.mouse = mouse;
    }

    public Tint getPromotionColor() {
        return promotionColor;
    }

    public void setPromotionColor(Tint promotionColor) {
        this.promotionColor = promotionColor;
    }

    public Piece getPromotingPawn() {
        return promotingPawn;
    }

    public void setPromotingPawn(Piece promotingPawn) {
        this.promotingPawn = promotingPawn;
    }

    public boolean checkPromotion(Piece p) {
        if(p instanceof Pawn) {
            if((p.getColor() == Tint.WHITE && p.getRow() == 0) ||
                    (p.getColor() == Tint.BLACK && p.getRow() == 7)) {
                BooleanService.isPromotionPending = true;
                promotingPawn = p;
                return true;
            }
        }
        return false;
    }

    public void autoPromote(Piece pawn) {
        if(!BooleanService.canPromote) { return; }
        if(!(pawn instanceof Pawn)) { return; }
        if(pawn == null) { return; }

        Tint side = pawn.getColor();
        Piece promotedPiece = BooleanService.getRandomPiece(pawn, pawn.getColor());
        Piece promotingPawn = pawn;

        pieceService.getPieces().remove(pawn);
        pieceService.getPieces().add(promotedPiece);
        BoardService.getBoardState()
                [promotedPiece.getCol()][promotedPiece.getRow()] = promotedPiece;
        this.promotingPawn = null;

        if (PieceService.getPiece() == pawn) {
            PieceService.nullThisPiece();
        }

        if(promotingPawn == pawn) { promotingPawn = null; }
        BooleanService.isPromotionPending = false;
        pieceService.switchTurns();
    }
}
