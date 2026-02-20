package org.vertex.engine.enums;

public enum Achievements {
    FIRST_CAPTURE           (1001L, "a01_first_steps",            "First Steps",            "Capture first chess piece"),
    SECRET_TOGGLE           (1002L, "a02_toggles",                "The One To Rule 'Em All","Find the (secret) toggle"),
    CHECKMATE               (1003L, "a03_checkmate",              "Checkmate!",             "Win a game by checkmate"),
    CASTLING_MASTER         (1004L, "a04_castling_master",        "Oh My King",             "Castle at least 10 times"),
    KING_PROMOTER           (1005L, "a05_king_promoter",          "King Promoter",          "Promote a Pawn 4 times"),
    QUICK_WIN               (1006L, "a06_quick_win",              "Quick Win!",             "Win less than 5 moves"),
    CHECK_OVER              (1007L, "a07_check_over",             "It's Check And Over",    "Check 4 times in the same game"),
    HEAVY_CROWN             (1008L, "a08_heavy_crown",            "Heavy Is The Crown",     "Win 128 game"),
    ALL_PIECES              (1009L, "a09_good_riddance",          "And Good Riddance",      "Clear all pieces from the board"),
    HARD_GAME               (1010L, "a10_that_was_easy",          "That Was Easy!",         "Win a hard chess game"),
    UNTOUCHABLE             (1011L, "a11_cant_touch_this",        "Can't Touch This",       "Win a game without getting checked"),
    MASTER_OF_NONE          (1012L, "a12_master_of_none",         "Master of None",         "Complete all Chess achievements"),
    ROUND_CAPTURE           (2001L, "a13_first_capture",          "Wait A Second",          "Capture first checkers piece"),
    QUICK_START             (2002L, "a14_quick_start",            "Quick Start",            "Win in under 8 moves"),
    STRATEGIST              (2003L, "a15_strategist",             "Strategist",             "Win without losing a piece"),
    DOUBLE_JUMP             (2004L, "a16_graysons",               "The Grayson's",          "Double jump in one turn"),
    TRIPLE_THREAT           (2005L, "a17_triple_threat",          "Three's Company",        "Triple jump in one turn"),
    KING_ME                 (2006L, "a18_king_me",                "King Me",                "Promote to King"),
    KINGDOM_BUILDER         (2007L, "a19_kingdom_builder",        "Kingdom Come",           "Promote 4 pieces to Kings"),
    COMEBACK                (2008L, "a20_comeback",               "Comeback",               "Win after losing 4+ pieces"),
    DRAW_MASTER             (2009L, "a21_even_stevens",           "Even Stevens",           "End in a draw"),
    CLEAN_SWEEP             (2010L, "a22_lord_of_checkers",       "The Lord Of Checkers",   "Capture all opponent pieces"),
    STALEMATE_SURVIVOR      (2011L, "a23_stalemate_survivor",     "The Last Of Us",         "Avoid losing with 1 piece"),
    CHAMPION                (2012L, "a24_kingmaker",              "Kingmaker",              "Complete all Checkers achievements"),
    GRANDMASTER             (1024L, "axx_grandmaster",            "Grandmaster",            "Complete all achievements");

    private final long id;
    private final String file;
    private final String title;
    private final String description;

    Achievements(long id, String file, String title, String description) {
        this.id = id;
        this.file = file;
        this.title = title;
        this.description = description;
    }

    public long getId() { return id; }
    public String getFile() { return file; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
