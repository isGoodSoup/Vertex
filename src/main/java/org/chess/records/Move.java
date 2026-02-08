package org.chess.records;

import org.chess.entities.Piece;

public record Move(Piece piece, int targetCol, int targetRow) {}
