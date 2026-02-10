package org.chess.service;

import org.chess.entities.Pawn;
import org.chess.entities.Piece;
import org.chess.enums.Tint;
import org.chess.records.Move;
import org.chess.records.MoveScore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModelService {
    private final PieceService pieceService;
    private final AnimationService animationService;
    private final PromotionService promotionService;

    public ModelService(PieceService pieceService,
                        AnimationService animationService,
                        PromotionService promotionService) {
        this.pieceService = pieceService;
        this.animationService = animationService;
        this.promotionService = promotionService;
    }

    public List<Move> getMoves() {
        return BoardService.getMoves();
    }

    public Move getAiTurn() {
        List<MoveScore> moves = getAllLegalMoves(GameService.getCurrentTurn());
        if(moves.isEmpty()) { return null; }
        Collections.shuffle(moves);
        moves.sort(Comparator.comparingInt(MoveScore::score).reversed());
        Move bestMove = moves.getFirst().move();

        Piece p = bestMove.piece();
        if (p.getColor() == Tint.BLACK) {
            AnimationService.startMoveAnimation(p, bestMove.targetCol(),
                    bestMove.targetRow());
        }

        if(p instanceof Pawn) {
            boolean hasReachedEnd =
                    (p.getColor() == Tint.WHITE && p.getRow() == 0)
                            || (p.getColor() == Tint.BLACK && p.getCol() == 8);
            if(hasReachedEnd) { promotionService.autoPromote(p); }
        }
        return bestMove;
    }

    public List<MoveScore> getAllLegalMoves(Tint color) {
        List<MoveScore> moves = new ArrayList<>();
        for(Piece p : pieceService.getPieces()) {
            if(p.getColor() != color) { continue; }
            for(int col = 0; col < 8; col++) {
                for(int row = 0; row < 8; row++) {
                    if(!isLegalMove(p, col, row)) { continue; }
                    Move move = new Move(p, p.getCol(), p.getRow(), col, row,
                            p.getColor());
                    moves.add(new MoveScore(move, evaluateMove(move)));
                }
            }
        }
        return moves;
    }

    private boolean isLegalMove(Piece p, int col, int row) {
        Piece target = PieceService.getPieceAt(col, row,
                pieceService.getPieces());
        if (!p.canMove(col, row, pieceService.getPieces())) {
            return false;
        }

        if (target != null && target.getColor() == p.getColor()) {
            return false;
        }
        return !pieceService.wouldLeaveKingInCheck(p, col, row);
    }

    private int evaluateMove(Move move) {
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

    public void executeMove(Move move) {
        Piece p = move.piece();
        p.setPreCol(p.getCol());
        p.setPreRow(p.getRow());

        Piece captured = PieceService.getPieceAt(move.targetCol(),
                move.targetRow(),
                pieceService.getPieces());
        if (captured != null) {
            pieceService.removePiece(captured);
        }

        if (p.getColor() == Tint.BLACK) {
            AnimationService.startMoveAnimation(p, move.targetCol(),
                    move.targetRow());
        }

        PieceService.movePiece(p, move.targetCol(),
                move.targetRow());
        p.setHasMoved(true);

        getMoves().add(new Move(p, p.getPreCol(), p.getPreRow(),
                p.getCol(),
                p.getRow(), Tint.BLACK));

        PieceService.nullThisPiece();
        BooleanService.isDragging = false;
        BooleanService.isLegal = false;
        GameService.setCurrentTurn(Tint.WHITE);
    }
}
