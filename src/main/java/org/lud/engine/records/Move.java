package org.lud.engine.records;

import org.lud.engine.entities.Piece;
import org.lud.engine.enums.Tint;

public record Move(Piece piece, int fromCol, int fromRow, int targetCol, int targetRow,
                   Tint color, Piece captured, boolean wasPromoted, Tint currentTurn,
                   boolean hasMoved, int preCol, int preRow, boolean isTwoStepsAhead) {}