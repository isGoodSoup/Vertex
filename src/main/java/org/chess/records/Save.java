package org.chess.records;

import org.chess.entities.Piece;
import org.chess.enums.Tint;

import java.io.Serializable;
import java.util.List;

public record Save(String name, Tint player, List<Piece> pieces) implements Serializable {}
