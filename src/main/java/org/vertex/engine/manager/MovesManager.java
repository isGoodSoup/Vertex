package org.vertex.engine.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.King;
import org.vertex.engine.entities.Pawn;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.entities.Rook;
import org.vertex.engine.enums.*;
import org.vertex.engine.events.*;
import org.vertex.engine.gui.Sound;
import org.vertex.engine.records.Move;
import org.vertex.engine.records.Save;
import org.vertex.engine.render.MenuRender;
import org.vertex.engine.service.*;

import java.util.ArrayList;
import java.util.List;

public class MovesManager {
    private Piece selectedPiece;
    private int moveX = 4;
    private int moveY = 6;
    private ServiceFactory service;
    private Sound fx;
    private List<Move> moves;
    private int selectedIndexY;
    private int selectedIndexX;
    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 6;

    private EventBus eventBus;
    private static final Logger log =
            LoggerFactory.getLogger(MovesManager.class);

    public MovesManager() {}

    public void init(ServiceFactory service, EventBus eventBus) {
        this.service = service;
        this.eventBus = eventBus;
        this.fx = service.getGuiService().getFx();
        this.moves = new ArrayList<>();
        this.selectedIndexY = 0;
        this.selectedIndexX = 0;
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

    public int getSelectedIndexX() {
        return selectedIndexX;
    }

    public void setSelectedIndexX(int selectedIndexX) {
        this.selectedIndexX = selectedIndexX;
    }

    public int getSelectedIndexY() {
        return selectedIndexY;
    }

    public void setSelectedIndexY(int selectedIndexY) {
        this.selectedIndexY = selectedIndexY;
    }

    public void setMoves(List<Move> moves) {
        this.moves = moves;
    }

    public void setGuiService(GUIService gui) {
        this.fx = gui.getFx();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public static int getITEMS_PER_PAGE() {
        return ITEMS_PER_PAGE;
    }

    private Sound getFx() {
        if (fx == null) {
            fx = service.getGuiService().getFx();
        }
        return fx;
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
            eventBus.fire(new CaptureEvent(captured));
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
        fx.playFX(0);

        if(service.getPromotionService().checkPromotion(piece)) {
            BooleanService.isPromotionActive = true;
            service.getPromotionService().setPromotionColor(piece.getColor());
            Piece promoted = service.getPromotionService().autoPromote(piece);
            log.info("Promoted piece");
            selectedPiece = promoted;
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
                service.getGuiService().getFx().playFX(6);
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
                    service.getGuiService().getFx().playFX(6);
                    GameService.setState(GameState.STALEMATE);
                    log.info("Stalemate. Both sides hold just the King");
                }
            }
        }
        return false;
    }

    public void keyboardMove() {
        if (selectedPiece == null) {
            for(Piece p : service.getPieceService().getPieces()) {
                if(p.getColor() == GameService.getCurrentTurn()
                        && p.getCol() == moveX
                        && p.getRow() == moveY) {
                    selectedPiece = p;
                    fx.playFX(0);
                    return;
                }
            }
        } else {
            if (!BooleanService.isLegal) {
                return;
            }
            service.getAnimationService().startMove(selectedPiece, moveX, moveY);
            attemptMove(selectedPiece, moveX, moveY);
            if (selectedPiece != null) {
                selectedPiece = null;
            }
        }
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
    public void previousPage() {
        int currentPage = service.getRender().getMenuRender().getCurrentPage();
        if(currentPage > 1) {
            service.getRender().getMenuRender().setCurrentPage(currentPage - 1);
        }
    }

    public void nextPage() {
        int itemsPerPage = MovesManager.getITEMS_PER_PAGE();
        int totalItems =service.getAchievementService().getAllAchievements().size();
        int totalPages = (totalItems + itemsPerPage - 1) / itemsPerPage;

        int current = service.getRender().getMenuRender().getCurrentPage();

        if(current < totalPages) {
            service.getRender().getMenuRender().setCurrentPage(current + 1);
        }
    }

    public void nextPage(Object[] options) {
        int itemsPerPage = 8;
        int totalPages = (options.length + itemsPerPage - 1) / itemsPerPage;

        int currentPage = service.getRender().getMenuRender().getCurrentPage();
        if(currentPage < totalPages) {
            service.getRender().getMenuRender().setCurrentPage(currentPage + 1);
        }
    }

    public void updateKeyboardHover() {
        List<Piece> selectablePieces = service.getPieceService()
                .getPieces()
                .stream()
                .filter(p -> p.getColor() == GameService.getCurrentTurn())
                .toList();

        service.getPieceService().setHoveredSquare(moveX, moveY);
        if(selectedPiece == null) {
            Piece hoveredPiece =
                    PieceService.getPieceAt(moveX, moveY,
                            service.getPieceService().getPieces());

            if(hoveredPiece != null &&
                    hoveredPiece.getColor() == GameService.getCurrentTurn()) {
                service.getPieceService().setHoveredPieceKeyboard(hoveredPiece);
            } else {
                service.getPieceService().setHoveredPieceKeyboard(null);
            }
            BooleanService.isLegal = false;
            return;
        }

        BooleanService.isLegal =
                selectedPiece.canMove(moveX, moveY,
                        service.getPieceService().getPieces())
                        && !service.getPieceService().wouldLeaveKingInCheck(
                        selectedPiece, moveX, moveY);
    }

    private List<Piece> getSelectablePieces() {
        return service.getPieceService()
                .getPieces()
                .stream()
                .filter(p -> p.getColor() == GameService.getCurrentTurn())
                .toList();
    }

    public void moveUp() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.max(0, moveY - 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void moveUp(Object[] options) {
        selectedIndexY--;
        getFx().playFX(BooleanService.getRandom(1, 2));
        if(selectedIndexY < 0) {
            selectedIndexY = options.length - 1;
        }
    }

    public void moveUp(List<Save> saves) {
        if(saves.isEmpty()) { return; }

        MenuRender menu = service.getRender().getMenuRender();
        int itemsPerPage = ITEMS_PER_PAGE;
        int currentPage = menu.getCurrentPage();
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, saves.size());

        selectedIndexY--;
        if(selectedIndexY < startIndex) {
            selectedIndexY = endIndex - 1;
        }

        getFx().playFX(BooleanService.getRandom(1, 2));
    }

    public void moveLeft() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.max(0, moveX - 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void moveLeft(Object[] options) {
        MenuRender menu = service.getRender().getMenuRender();
        previousPage();

        int itemsPerPage = 8;
        int newPage = menu.getCurrentPage();
        selectedIndexY = (newPage - 1) * itemsPerPage;

        getFx().playFX(4);
    }

    public void moveDown(List<Save> saves) {
        if(saves.isEmpty()) { return; }

        MenuRender menu = service.getRender().getMenuRender();
        int itemsPerPage = ITEMS_PER_PAGE;
        int currentPage = menu.getCurrentPage();
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, saves.size());

        selectedIndexY++;
        if(selectedIndexY >= endIndex) {
            selectedIndexY = startIndex;
        }

        getFx().playFX(BooleanService.getRandom(1, 2));
    }

    public void moveDown() {
        GameState state = GameService.getState();
        if(state == GameState.BOARD) {
            moveY = Math.min(7, moveY + 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void moveDown(Object[] options) {
        selectedIndexY++;
        getFx().playFX(BooleanService.getRandom(1, 2));
        if(selectedIndexY < 0) {
            selectedIndexY = 0;
        }
    }

    public void moveRight() {
        if(GameService.getState() == GameState.BOARD) {
            moveX = Math.min(7, moveX + 1);
            updateKeyboardHover();
            getFx().playFX(BooleanService.getRandom(1, 2));
        }
    }

    public void moveRight(Object[] options) {
        MenuRender menu = service.getRender().getMenuRender();
        nextPage(options);

        int itemsPerPage = 8;
        int newPage = menu.getCurrentPage();
        selectedIndexY = (newPage - 1) * itemsPerPage;

        getFx().playFX(4);
    }

    public void activate(String saveName) {
        getFx().playFX(3);
        service.getGameService().continueGame(saveName);
    }

    public void activate(GameState state) {
        switch (state) {
            case MENU -> {
                GameMenu[] options = MenuRender.MENU;
                if(selectedIndexY >= 0 && selectedIndexY < options.length) {
                    GameMenu selected = options[selectedIndexY];
                    if (selected.isEnabled(service.getGameService())) {
                        getFx().playFX(3);
                        selected.run(service.getGameService());
                    }
                }
            }
            case SAVES -> {
                List<Save> saves = service.getSaveManager().getSaves();
                if(!saves.isEmpty() && selectedIndexY < saves.size()) {
                    activate(saves.get(selectedIndexY).name());
                }
            }
            case RULES -> {
                getFx().playFX(0);
                GameSettings option = MenuRender.SETTINGS_MENU[selectedIndexY];
                option.toggle();
            }
            case ACHIEVEMENTS -> {}
            case BOARD -> keyboardMove();
        }
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
}
