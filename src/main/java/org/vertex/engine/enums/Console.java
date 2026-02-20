package org.vertex.engine.enums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertex.engine.entities.*;
import org.vertex.engine.gui.Colors;
import org.vertex.engine.service.BoardService;
import org.vertex.engine.service.BooleanService;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.ServiceFactory;

public enum Console {
    PLACE("PLACE") {
        @Override
        public void run(ServiceFactory service, String[] args) {
            if(args.length < 3) {
                log.info("Usage: PLACE <color> <piece> <square>");
                return;
            }

            String colorStr = args[0].toUpperCase();
            String pieceStr = args[1].toUpperCase();
            String square = args[2];

            int col, row;
            try {
                col = Character.toUpperCase(square.charAt(0)) - 'A';
                row = service.getBoardService().getBoard().getRow() - Integer.parseInt(square.substring(1));
            } catch (NumberFormatException e) {
                log.warn("Column and row must be integers.");
                return;
            }

            Tint tone;
            switch(colorStr) {
                case "DARK" -> tone = Tint.DARK;
                case "LIGHT" -> tone = Tint.LIGHT;
                default -> {
                    log.error("Invalid color: {}", colorStr);
                    return;
                }
            }

            Piece createdPiece = switch(pieceStr) {
                case "PAWN" -> new Pawn(tone, col, row);
                case "TOKIN" -> new Tokin(tone, col, row);
                case "BISHOP" -> new Bishop(tone, col, row);
                case "KNIGHT" -> new Knight(tone, col, row);
                case "LANCE" -> new Lance(tone, col, row);
                case "SILVER" -> new Silver(tone, col, row);
                case "GOLD" -> new Gold(tone, col, row);
                case "ROOK" -> new Rook(tone, col, row);
                case "QUEEN" -> new Queen(tone, col, row);
                case "KING" -> new King(tone, col, row);
                default -> null;
            };

            if(createdPiece == null) {
                log.info("Invalid piece type: {}", pieceStr);
                return;
            }

            service.getPieceService().getPieces().add(createdPiece);
            BoardService.getBoardState()[row][col] = createdPiece;

            if(GameService.getGame() == Games.SANDBOX) {
                service.getMovesManager().setSelectedPiece(createdPiece);
                service.getPieceService().setHoveredPieceKeyboard(createdPiece);
                service.getPieceService().setHoveredSquare(col, row);
                service.getKeyboardInput().setMoveX(col);
                service.getKeyboardInput().setMoveY(row);
                BooleanService.isLegal = true;
            }

            int squareSize = Board.getSquare();
            createdPiece.setX(col * squareSize);
            createdPiece.setY(row * squareSize);

            log.info("Placed {} {} at ({}, {})", colorStr, pieceStr, col, row);
        }
    },
    REMOVE("REMOVE") {
        @Override
        public void run(ServiceFactory service, String[] args) {
            if(args.length < 3) {
                log.info("Usage: REMOVE <color> <piece> <square>");
                return;
            }

            String colorStr = args[0].toUpperCase();
            String pieceStr = args[1].toUpperCase();
            String square = args[2];

            int col, row;
            try {
                col = Character.toUpperCase(square.charAt(0)) - 'A';
                row = service.getBoardService().getBoard().getRow() - Integer.parseInt(square.substring(1));
            } catch (NumberFormatException e) {
                log.error("Column and row must be integers.");
                return;
            }

            var iterator = service.getPieceService().getPieces().iterator();
            boolean removed = false;
            while (iterator.hasNext()) {
                Piece p = iterator.next();
                if (p.getTypeID().name().equalsIgnoreCase(pieceStr)
                        && p.getColor().name().equalsIgnoreCase(colorStr)
                        && p.getCol() == col
                        && p.getRow() == row) {
                    iterator.remove();
                    BoardService.getBoardState()[row][col] = null;
                    removed = true;
                    log.info("Removed {} {} at ({}, {})", colorStr, pieceStr, col, row);
                    break;
                }
            }

            if (!removed) {
                log.info("No piece found at ({}, {}) to remove.", col, row);
            }
        }
    },
    THEME("THEME") {
        @Override
        public void run(ServiceFactory service, String[] args) {
            if(args.length < 1) {
                log.info("Usage: THEME <GET|NEXT|PREV|LIST> [pos]");
                return;
            }

            String action = args[0].toUpperCase();

            switch(action) {
                case "GET" -> {
                    if (args.length < 2) {
                        log.info("Usage: THEME GET <pos>");
                        return;
                    }
                    try {
                        int pos = Integer.parseInt(args[1]);
                        Theme[] themes = Theme.values();
                        if (pos < 0 || pos >= themes.length) {
                            log.info("Invalid theme index. Must be between 0 and {}", themes.length - 1);
                            return;
                        }
                        Colors.setTheme(themes[pos]);
                        log.info("Theme set to: {}", themes[pos].name());
                    } catch (NumberFormatException e) {
                        log.info("Position must be a number.");
                    }
                }
                case "NEXT" -> {
                    Colors.nextTheme();
                    log.info("Switched to next theme: {}", Colors.getTheme().name());
                }
                case "PREV" -> {
                    Colors.previousTheme();
                    log.info("Switched to previous theme: {}", Colors.getTheme().name());
                }
                default -> log.error("Unknown action: {}. Use GET, NEXT, PREV",
                        action);
            }
        }
    };
    private final String command;
    private static final Logger log = LoggerFactory.getLogger(Console.class);

    Console(String command) {
        this.command = command;
    }

    public abstract void run(ServiceFactory service, String[] args);

    public static Console fromString(String input) {
        for (Console c : values()) {
            if (c.command.equalsIgnoreCase(input)) {
                return c;
            }
        }
        return null;
    }
}
