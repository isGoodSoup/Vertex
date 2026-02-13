package org.chess.enums;

public enum Achievements {
    FIRST_MOVE("a01_first_move", "First Move", "Make your first move"),
    RULES_TOGGLES("a02_rules", "Rules? What's That?", "Discover the rule toggles"),
    CHECKMATE("a03_checkmate", "Checkmate!", "Win a game by checkmate"),
    CASTLING_MASTER("a04_castling_master", "Oh My King", "Castle at least 10 times"),
    KING_PROMOTER("a05_king_promoter", "King Promoter", "Promote the same pawn 5 times"),
    QUICK_WIN("a06_quick_win", "Quick Win!", "Win a game in less than 5 moves"),
    CHECK_OVER("a07_check_over", "It's Check And Over", "Check 16 times in the same game"),
    HUNDRED("a08_heavy_crown", "Heavy Is The Crown", "Win a 128 games"),
    ALL_PIECES("a09_good_riddance", "And Good Riddance", "Clear all pieces from the board"),
    HARD_GAME("a10_that_was_easy", "That Was Easy!", "Win a hard game"),
    UNTOUCHABLE("a11_cant_touch_this", "Can't Touch This", "Win a game without getting checked"),
    GRANDMASTER("axx_grandmaster", "Chess Grandmaster", "Complete all achievements");

    private final String file;
    private final String title;
    private final String description;

    Achievements(String file, String title, String description) {
        this.file = file;
        this.title = title;
        this.description = description;
    }

    public String getFile() { return file; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}