package org.lud.engine.rulesets;

import org.lud.engine.entities.Piece;
import org.lud.engine.enums.Tint;
import org.lud.engine.interfaces.Ruleset;
import org.lud.engine.records.Move;
import org.lud.engine.records.MoveScore;
import org.lud.engine.service.BoardService;
import org.lud.engine.service.PieceService;

import java.util.ArrayList;
import java.util.List;

public class ChessRuleset implements Ruleset {
    private final PieceService pieceService;
    private final BoardService boardService;

    public ChessRuleset(PieceService pieceService, BoardService boardService) {
        this.pieceService = pieceService;
        this.boardService = boardService;
    }

    @Override
    public List<MoveScore> getAllLegalMoves(Tint color) {
        List<MoveScore> moves = new ArrayList<>();
        for(Piece p : pieceService.getPieces()) {
            if(p.getColor() != color) { continue; }
            for(int col = 0; col < 8; col++) {
                for(int row = 0; row < 8; row++) {
                    if(!isLegalMove(p, col, row)) { continue; }
                    Move move = new Move(p, p.getCol(), p.getRow(), col, row,
                            p.getColor(), null, p.isPromoted(), p.getColor(), p.hasMoved(), p.getPreCol(),
                            p.getPreRow(), p.isTwoStepsAhead());
                    moves.add(new MoveScore(move, evaluateMove(move)));
                }
            }
        }
        return moves;
    }

    @Override
    public boolean isLegalMove(Piece p, int col, int row) {
        Piece target = PieceService.getPieceAt(col, row,
                pieceService.getPieces());
        if(!p.canMove(col, row, pieceService.getPieces())) {
            return false;
        }

        if(target != null && target.getColor() == p.getColor()) {
            return false;
        }
        return !pieceService.wouldLeaveKingInCheck(p, col, row);
    }

    @Override
    public int evaluateMove(Move move) {
        int score = 0;
        Piece p = move.piece();

        for(Piece enemy : pieceService.getPieces()) {
            if(enemy.getCol() == move.targetCol() && enemy.getRow() == move.targetRow()) {
                score += pieceService.getPieceValue(enemy);
                break;
            }
        }

        int oldCol = p.getCol();
        int oldRow = p.getRow();
        p.setCol(move.targetCol());
        p.setRow(move.targetRow());

        if(pieceService.isPieceThreatened(p)) {
            score -= pieceService.getPieceValue(p);
        }

        p.setCol(oldCol);
        p.setRow(oldRow);
        return score;
    }
}