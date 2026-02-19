package org.vertex.engine.rulesets;

import org.vertex.engine.entities.*;
import org.vertex.engine.enums.Games;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.interfaces.Ruleset;
import org.vertex.engine.records.Move;
import org.vertex.engine.records.MoveScore;
import org.vertex.engine.service.BoardService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.PieceService;

import java.util.ArrayList;
import java.util.List;

public class ShogiRuleset implements Ruleset {
    private final PieceService pieceService;
    private final BoardService boardService;

    public ShogiRuleset(PieceService pieceService, BoardService boardService) {
        this.pieceService = pieceService;
        this.boardService = boardService;
    }

    @Override
    public List<MoveScore> getAllLegalMoves(Tint color) {
        List<MoveScore> moves = new ArrayList<>();
        for(Piece p : pieceService.getPieces()) {
            if(p.getColor() != color) { continue; }
            for(int col = 0; col < 9; col++) {
                for(int row = 0; row < 9; row++) {
                    if(!isLegalMove(p, col, row)) { continue; }
                    Move move = new Move(p, p.getCol(), p.getRow(), col, row,
                            p.getColor(), null);
                    moves.add(new MoveScore(move, evaluateMove(move)));
                }
            }
        }
        return moves;
    }

    @Override
    public boolean isLegalMove(Piece p, int col, int row) {
        Piece target = PieceService.getPieceAt(col, row, pieceService.getPieces());
        if(col < 0 || col >= 9 || row < 0 || row >= 9) { return false; }
        if(target != null && target.getColor() == p.getColor()) { return false; }
        return p.canMove(col, row, pieceService.getPieces());
    }


    @Override
    public int evaluateMove(Move move) {
        int score = 0;
        Piece p = move.piece();

        Piece target = PieceService.getPieceAt(move.targetCol(), move.targetRow(),
                pieceService.getPieces());
        if(target != null && target.getColor() != p.getColor()) {
            score += pieceService.getPieceValue(target);
        }

        int oldCol = p.getCol();
        int oldRow = p.getRow();

        p.setCol(move.targetCol());
        p.setRow(move.targetRow());

        if (p.isInPromotionZone(p.getColor(), move.targetRow())) {
            if (!p.isPromoted()) {
                int promotedValue = pieceService.getPieceValue(getPromotedPieceForEvaluation(p));
                score += promotedValue - pieceService.getPieceValue(p);
            }
        }

        if(pieceService.isPieceThreatened(p)) {
            score -= pieceService.getPieceValue(p)/2;
        }

        p.setCol(oldCol);
        p.setRow(oldRow);

        int centerCol = 4, centerRow = 4;
        int dist = Math.abs(move.targetCol() - centerCol) + Math.abs(move.targetRow() - centerRow);
        score += (8 - dist);
        return score;
    }

    private Piece getPromotedPieceForEvaluation(Piece p) {
        if(GameService.getGames() == Games.SHOGI) {
            if(p instanceof Pawn) return new Tokin(p.getColor(), p.getRow(), p.getCol());
            if(p instanceof Lance || p instanceof Silver || p instanceof Knight) {
                p.setPromoted(true);
                return new Gold(p.getColor(), p.getRow(), p.getCol());
            }
            if(p instanceof Bishop || p instanceof Rook) {
                p.setPromoted(true);
                return p;
            }
            return p;
        }
        if(p instanceof Pawn) return new Queen(p.getColor(), p.getRow(), p.getCol());
        if(p instanceof Checker) return new King(pieceService, p.getColor(), p.getRow(), p.getCol());
        return p;
    }

}
