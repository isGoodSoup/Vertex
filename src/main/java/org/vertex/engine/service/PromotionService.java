package org.vertex.engine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.*;
import org.vertex.engine.enums.Games;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.manager.EventBus;
import org.vertex.engine.events.PromotionEvent;

public class PromotionService {
    private Tint promotionColor;
    private Piece promotingPawn;

    private final PieceService pieceService;
    private GameService gameService;
    private final EventBus event;

    private boolean isPromoted;
    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

    public PromotionService(PieceService pieceService, EventBus event) {
        this.pieceService = pieceService;
        this.event = event;
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

    public GameService getGameService() {
        return gameService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public boolean checkPromotion(Piece p) {
        if (p instanceof Pawn) {
            if ((p.getColor() == Tint.LIGHT && p.getRow() == 0) ||
                    (p.getColor() == Tint.DARK && p.getRow() == 7)) {
                return true;
            }
        }

        if (p instanceof Checker) {
            if ((p.getColor() == Tint.LIGHT && p.getRow() == 0) ||
                    (p.getColor() == Tint.DARK && p.getRow() == 7)) {
                return true;
            }
        }
        return false;
    }

    public Piece autoPromote(Piece piece) {
        if(piece == null || !BooleanService.canPromote) { return piece; }
        if(!checkPromotion(piece)) { return piece; }
        log.info("Promotion: {}, {}", piece.getRow(), piece.getCol());

        Piece promotedPiece = getPiece(piece);
        pieceService.getPieces().remove(piece);
        pieceService.getPieces().add(promotedPiece);
        promotedPiece.setX(promotedPiece.getRow() * Board.getSquare());
        promotedPiece.setY(promotedPiece.getCol() * Board.getSquare());

        BoardService.getBoardState()
                [promotedPiece.getRow()][promotedPiece.getCol()] =
                promotedPiece;

        PieceService.nullThisPiece();
        pieceService.setHoveredPieceKeyboard(null);

        BooleanService.isPromotionActive = false;
        if(piece instanceof Pawn pawn) {
            event.fire(new PromotionEvent(pawn));
        }

        if(piece instanceof Checker checker) {
            event.fire(new PromotionEvent(checker));
        }

        if(GameService.getGames() == Games.SHOGI) {
            if(piece instanceof Pawn || piece instanceof Lance || piece instanceof Silver || piece instanceof Knight ||
                    piece instanceof Bishop || piece instanceof Rook) {
                event.fire(new PromotionEvent(piece));
            }
        }
        pieceService.switchTurns();
        return promotedPiece;
    }

    private Piece getPiece(Piece piece) {
        Piece promotedPiece = null;
        if(piece instanceof Pawn) {
            if(GameService.getGames() == Games.SHOGI) {
                promotedPiece = new Tokin(piece.getColor(), piece.getRow(),
                        piece.getCol());
            } else {
                promotedPiece = new Queen(piece.getColor(), piece.getRow(),
                        piece.getCol());
            }
        }
        if(piece instanceof Checker) {
            promotedPiece = new King(pieceService, piece.getColor(),
                    piece.getRow(), piece.getCol());
        }
        if(GameService.getGames() == Games.SHOGI) {
            if(piece instanceof Lance || piece instanceof Silver || piece instanceof Knight ) {
                promotedPiece = new Gold(piece.getColor(), piece.getRow(), piece.getCol());
            }
            if(piece instanceof Bishop || piece instanceof Rook) {
                promotedPiece = piece;
                piece.setPromoted(true);
            }
        }
        return promotedPiece;
    }
}
