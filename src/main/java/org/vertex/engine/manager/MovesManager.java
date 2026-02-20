package org.vertex.engine.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.*;
import org.vertex.engine.enums.GameState;
import org.vertex.engine.enums.Games;
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
        boolean isHumanMove = isHumanTurn(service.getGameService().getCurrentTurn());

        if(isHumanMove) {
            service.getTimerService().pause();
        }

        for(Piece p : service.getPieceService().getPieces()) {
            if(p instanceof Pawn && p.getColor() == service.getGameService().getCurrentTurn()) {
                p.resetEnPassant();
            }
        }

        if (piece instanceof King) {
            int colDiff = targetCol - piece.getCol();
            if (Math.abs(colDiff) > 1) {
                int step = (colDiff > 0) ? 1 : -1;
                for (int c = piece.getCol(); c != targetCol + step; c += step) {
                    if (service.getPieceService()
                            .wouldLeaveKingInCheck(piece, c, piece.getRow())) {
                        PieceService.updatePos(piece);
                        return;
                    }
                }
            }
        }

        boolean isLegalLocal = piece.canMove(targetCol, targetRow,
                service.getPieceService().getPieces())
                && !service.getPieceService().wouldLeaveKingInCheck(
                piece, targetCol, targetRow);

        if(!isLegalLocal) {
            PieceService.updatePos(piece);
            return;
        }

        Piece captured = null;

        if(Math.abs(targetRow - piece.getRow()) == 2 && Math.abs(targetCol - piece.getCol()) == 2) {
            int capturedRow = (piece.getRow() + targetRow)/2;
            int capturedCol = (piece.getCol() + targetCol)/2;
            captured = PieceService.getPieceAt(capturedCol, capturedRow,
                    service.getPieceService().getPieces());
        } else {
            captured = PieceService.getPieceAt(targetCol, targetRow, service.getPieceService().getPieces());
        }

        if(captured != null) {
            service.getPieceService().removePiece(captured);
            eventBus.fire(new CaptureEvent(piece, captured));
        }

        if(piece instanceof Checker && captured != null) {
            boolean areMoreJumpsAvailable = true;
            while(areMoreJumpsAvailable) {
                areMoreJumpsAvailable = false;
                for(int dr = -2; dr <= 2; dr += 4) {
                    for(int dc = -2; dc <= 2; dc += 4) {
                        int newRow = piece.getRow() + dr;
                        int newCol = piece.getCol() + dc;
                        if(piece.canMove(newCol, newRow, service.getPieceService().getPieces())) {
                            int capturedRow = (piece.getRow() + newRow) / 2;
                            int capturedCol = (piece.getCol() + newCol) / 2;
                            Piece nextCaptured = PieceService.getPieceAt(capturedCol, capturedRow,
                                    service.getPieceService().getPieces());

                            if(nextCaptured != null && nextCaptured.getColor() != piece.getColor()) {
                                service.getPieceService().removePiece(nextCaptured);
                                eventBus.fire(new CaptureEvent(piece, nextCaptured));
                                PieceService.movePiece(piece, newCol, newRow);
                                moves.add(new Move(piece,
                                        piece.getRow() - dr,
                                        piece.getCol() - dc,
                                        newCol, newRow,
                                        piece.getColor(),
                                        nextCaptured));

                                areMoreJumpsAvailable = true;
                            }
                        }
                    }
                }
            }
        }

        if(piece instanceof King) {
            executeCastling(piece, targetCol);
        }

        if (piece instanceof Pawn && isEnPassantMove(piece, targetCol,
                targetRow, service.getPieceService().getPieces())) {
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
            Piece promoted = service.getPromotionService().promote(piece);
            service.getPieceService().replacePiece(piece, promoted);
            log.info("Promoted piece");
            service.getKeyboardInput().setMoveX(promoted.getCol());
            service.getKeyboardInput().setMoveY(promoted.getRow());
            if (!(GameService.getGame() == Games.SANDBOX)) {
                selectedPiece = null;
            }
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

        if(isHumanMove) {
            service.getTimerService().resume();
        }
    }

    private boolean isHumanTurn(Tint turn) {
        return turn == Tint.LIGHT;
    }

    private boolean isCheckmate() {
        if(service.getPieceService().isKingInCheck(service.getGameService().getCurrentTurn())) {
            boolean hasEscapeMoves = false;
            for(Piece piece : service.getPieceService().getPieces()) {
                if(piece.getColor() == service.getGameService().getCurrentTurn()) {
                    for(int col = 0; col < service.getBoardService().getBoard().getCol(); col++) {
                        for(int row = 0; row < service.getBoardService().getBoard().getRow(); row++) {
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
                service.getGameService().setState(GameState.CHECKMATE);
                if(selectedPiece != null) {
                    log.info("Checkmate to {}",
                            selectedPiece.getOtherPiece().getColor());
                }
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
                    service.getGameService().setState(GameState.STALEMATE);
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

            selectedPiece = null;
            service.getPieceService().setHoveredPieceKeyboard(null);
            BooleanService.isLegal = true;
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
}
