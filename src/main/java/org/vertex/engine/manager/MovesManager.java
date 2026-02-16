package org.vertex.engine.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.King;
import org.vertex.engine.entities.Pawn;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.entities.Rook;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.enums.Tint;
import org.vertex.engine.events.*;
import org.vertex.engine.records.Move;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.PieceService;
import org.vertex.engine.service.ServiceFactory;

import java.util.ArrayList;
import java.util.List;

public class MovesManager {
    private Piece selectedPiece;
    private ServiceFactory service;
    private List<Move> moves;

    private int currentPage = 1;

    private EventBus eventBus;
    private static final Logger log =
            LoggerFactory.getLogger(MovesManager.class);

    public MovesManager() {}

    public void init(ServiceFactory service, EventBus eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        this.moves = new ArrayList<>();
    }

    public List<Move> getMoves() {
        return moves;
    }

    public ServiceFactory getService() {
        return service;
    }

    public Piece getSelectedPiece() {
        return selectedPiece;
    }

    public void setSelectedPiece(Piece selectedPiece) {
        this.selectedPiece = selectedPiece;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void attemptMove(Piece piece, int targetCol, int targetRow) {
        if(BooleanService.isCheckmate) { return; }

        for(Piece p : service.getPieceService().getPieces()) {
            if(p instanceof Pawn && p.getColor() == GameService.getCurrentTurn()) {
                p.resetEnPassant();
            }
        }

        BooleanService.isLegal = piece.canMove(targetCol, targetRow,
                service.getPieceService().getPieces())
                && !service.getPieceService().wouldLeaveKingInCheck(
                piece, targetCol, targetRow);

        if(!BooleanService.isLegal) {
            PieceService.updatePos(piece);
            return;
        }

        Piece captured = PieceService.getPieceAt(
                targetCol, targetRow, service.getPieceService().getPieces());

        if(captured != null) {
            service.getPieceService().removePiece(captured);
            if(piece.getColor() != captured.getColor()) {
                eventBus.fire(new CaptureEvent(captured));
            }
        }

        if(piece instanceof King) {
            executeCastling(piece, targetCol);
        }

        if (piece instanceof Pawn && isEnPassantMove(piece, targetCol, targetRow, service.getPieceService().getPieces())) {
            executeEnPassant(piece, captured, targetCol, targetRow);
        } else {
            moves.add(new Move(
                    piece,
                    piece.getRow(),
                    piece.getCol(),
                    targetCol, targetRow,
                    piece.getColor(),
                    captured));
        }

        PieceService.movePiece(piece, targetCol, targetRow);
        piece.setHasMoved(true);

        if(service.getPromotionService().checkPromotion(piece)) {
            BooleanService.isPromotionActive = true;
            service.getPromotionService().setPromotionColor(piece.getColor());
            Piece promoted = service.getPromotionService().autoPromote(piece);
            service.getPieceService().replacePiece(piece, promoted);
            log.info("Promoted piece");
            service.getKeyUI().setMoveX(promoted.getCol());
            service.getKeyUI().setMoveY(promoted.getRow());
            selectedPiece = null;
            service.getPieceService().setHoveredPieceKeyboard(promoted);
        } else {
            service.getPieceService().switchTurns();
        }

        service.getModelService().triggerAIMove();

        if(isCheckmate()) {
            eventBus.fire(new CheckmateEvent(piece,
                    service.getPieceService().getKing(Tint.DARK)));
            eventBus.fire(new TotalMovesEvent(piece));
        }

        if(isCheckmate() && BooleanService.canDoHard) {
            eventBus.fire(new HardEvent(piece));
        }

        if(isStalemate()) {
            eventBus.fire(new StalemateEvent(piece));
        }
    }

    private boolean isCheckmate() {
        if(service.getPieceService().isKingInCheck(GameService.getCurrentTurn())) {
            boolean hasEscapeMoves = false;
            for(Piece piece : service.getPieceService().getPieces()) {
                if(piece.getColor() == GameService.getCurrentTurn()) {
                    for(int col = 0; col < 8; col++) {
                        for(int row = 0; row < 8; row++) {
                            if(piece.canMove(col, row, service.getPieceService().getPieces()) &&
                                    !service.getPieceService().wouldLeaveKingInCheck(piece, col, row)) {
                                hasEscapeMoves = true;
                                break;
                            }
                        }
                        if(hasEscapeMoves) {
                            break;
                        }
                    }
                }
                if(hasEscapeMoves) {
                    break;
                }
            }

            if(!hasEscapeMoves) {
                BooleanService.isCheckmate = true;
                service.getTimerService().stop();
                GameService.setState(GameState.CHECKMATE);
                log.info("Checkmate to {}",
                        selectedPiece.getOtherPiece().getColor());
                return true;
            }
        }
        return false;
    }

    private boolean isStalemate() {
        int kingCounter = 0;
        for(Piece p : service.getPieceService().getPieces()) {
            if(p instanceof King) {
                kingCounter++;
                if(kingCounter == 2 && service.getPieceService().getPieces().size() == 2) {
                    BooleanService.isStalemate = true;
                    service.getTimerService().stop();
                    GameService.setState(GameState.STALEMATE);
                    log.info("Stalemate. Both sides hold just the King");
                }
            }
        }
        return false;
    }

    private void executeCastling(Piece currentPiece, int targetCol) {
        if(!BooleanService.canDoMoves) { return; }
        if(!(currentPiece instanceof King)) { return; }
        int colDiff = targetCol - currentPiece.getCol();

        if(Math.abs(colDiff) == 2 && !currentPiece.hasMoved()) {
            int step = (colDiff > 0) ? 1 : -1;
            int rookStartCol = (colDiff > 0) ? 7 : 0;
            int rookTargetCol = (colDiff > 0) ? 5 : 3;

            if(service.getPieceService().isKingInCheck(currentPiece.getColor()) ||
                    service.getPieceService().wouldLeaveKingInCheck(currentPiece,
                            currentPiece.getCol() + step,
                            currentPiece.getRow())) {
                PieceService.updatePos(currentPiece);
                currentPiece = null;
                return;
            }

            boolean pathClear = true;
            for(int c = currentPiece.getCol() + step; c != rookStartCol; c += step) {
                if(PieceService.getPieceAt(c, currentPiece.getRow(),
                        service.getPieceService().getPieces()) != null) {
                    pathClear = false;
                    break;
                }
            }

            if(pathClear) {
                for(Piece p : service.getPieceService().getPieces()) {
                    if(p instanceof Rook &&
                            p.getCol() == rookStartCol &&
                            p.getRow() == currentPiece.getRow() &&
                            !p.hasMoved()) {

                        p.setCol(rookTargetCol);
                        PieceService.updatePos(p);
                        p.setHasMoved(true);
                        eventBus.fire(new CastlingEvent(currentPiece, p));
                        break;
                    }
                }
            }
        }
    }

    private void executeEnPassant(Piece currentPiece, Piece captured,
                                  int targetCol, int targetRow) {
        if(!BooleanService.canDoMoves) { return; }
        int oldRow = currentPiece.getPreRow();
        int movedSquares = Math.abs(targetRow - oldRow);

        if(captured == null && Math.abs(targetCol - currentPiece.getPreCol()) == 1) {
            int dir = (currentPiece.getColor() == Tint.LIGHT) ? -1 : 1;
            if(targetRow - oldRow == dir) {
                for(Piece p : service.getPieceService().getPieces()) {
                    if(p instanceof Pawn &&
                            p != null &&
                            p.getColor() != currentPiece.getColor() &&
                            p.getCol() == targetCol &&
                            p.getRow() == oldRow &&
                            p.isTwoStepsAhead()) {
                        captured = p;
                        service.getPieceService().removePiece(p);
                        Move newMove = getMoveEvent();
                        moves.add(newMove);
                        break;
                    }
                }
            }
        }

        currentPiece.setTwoStepsAhead(movedSquares == 2);
    }

    private Move getMoveEvent() {
        Move lastMove = moves.getLast();
        Move newMove = new Move(
                lastMove.piece(),
                lastMove.fromRow(),
                lastMove.fromCol(),
                lastMove.targetCol(),
                lastMove.targetRow(),
                lastMove.color(),
                lastMove.captured()
        );
        return newMove;
    }

    private boolean isEnPassantMove(Piece pawn, int targetCol, int targetRow,
                              List<Piece> board) {
        if(!(pawn instanceof Pawn)) return false;
        if(Math.abs(targetCol - pawn.getCol()) == 1 &&
                targetRow != pawn.getRow()) {
            for(Piece p : board) {
                if(p instanceof Pawn &&
                        p.getColor() != pawn.getColor() &&
                        p.getCol() == targetCol &&
                        p.getRow() == pawn.getRow() &&
                        p.isTwoStepsAhead()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void cancelMove() {
        if (selectedPiece != null) {
            selectedPiece.setCol(selectedPiece.getPreCol());
            selectedPiece.setRow(selectedPiece.getPreRow());
            PieceService.updatePos(selectedPiece);
            PieceService.nullThisPiece();
        }
    }

    public void undoLastMove(Piece piece) {
        if(!BooleanService.canUndoMoves) { return; }
        Move lastMove = moves.removeLast();
        Piece movedPiece = lastMove.piece();
        movedPiece.setCol(lastMove.fromCol());
        movedPiece.setRow(lastMove.fromRow());
        PieceService.updatePos(movedPiece);
        movedPiece.setHasMoved(false);

        Piece captured = lastMove.captured();
        if (captured != null) {
            service.getPieceService().getPieces().add(captured);
            PieceService.updatePos(captured);
        }
        service.getPieceService().switchTurns();
    }

    private List<Piece> getSelectablePieces() {
        return service.getPieceService()
                .getPieces()
                .stream()
                .filter(p -> p.getColor() == GameService.getCurrentTurn())
                .toList();
    }
}
