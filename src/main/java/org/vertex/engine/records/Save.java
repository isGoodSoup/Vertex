package org.vertex.engine.records;

import org.vertex.engine.entities.Achievement;
import org.vertex.engine.entities.Piece;
import org.vertex.engine.enums.Games;
import org.vertex.engine.enums.Tint;

import java.io.Serializable;
import java.util.List;

public record Save(Games game, String name, Tint player, List<Piece> pieces,
                   List<Achievement> achievements) implements Serializable {}
