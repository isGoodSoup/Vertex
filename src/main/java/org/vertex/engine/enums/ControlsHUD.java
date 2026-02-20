package org.vertex.engine.enums;

public enum ControlsHUD {

    BACK_TO_MENU(ControlCategory.GLOBAL, "Back to Menu", new String[]{"ctrl", "b"}),
    QUIT_GAME(ControlCategory.GLOBAL, "Quit", new String[]{"ctrl", "q"}),
    CHANGE_THEME(ControlCategory.GLOBAL, "Switch Theme", new String[]{"ctrl", "t"}),
    TOGGLE_FULLSCREEN(ControlCategory.GLOBAL, "Fullscreen", new String[]{"f11"}),

    NAVIGATE_UP(ControlCategory.MENU, "Up", new String[]{"arrow_up"}),
    NAVIGATE_DOWN(ControlCategory.MENU, "Down", new String[]{"arrow_down"}),
    NAVIGATE_LEFT(ControlCategory.MENU, "Left", new String[]{"arrow_left"}),
    NAVIGATE_RIGHT(ControlCategory.MENU, "Right", new String[]{"arrow_right"}),
    CHANGE_GAME(ControlCategory.MENU, "Switch Game", new String[]{"ctrl", "g"}),
    SELECT(ControlCategory.MENU, "Select", new String[]{"enter"}),
    CANCEL(ControlCategory.MENU, "Back", new String[]{"escape"}),

    RESET_BOARD(ControlCategory.BOARD_KEYBOARD, "Reset Board", new String[]{"ctrl", "r"}),
    UNDO_MOVE(ControlCategory.BOARD_KEYBOARD, "Undo Last Move", new String[]{"ctrl", "z"}),
    CANCEL_MOVE(ControlCategory.BOARD_KEYBOARD, "Cancel Move", new String[]{"c"}),
    TOGGLE_MOVES_LIST(ControlCategory.BOARD_KEYBOARD, "Toggle Moves", new String[]{"ctrl", "h"}),

    DRAG_PIECE(ControlCategory.BOARD_MOUSE, "Move Piece", new String[]{"mouse_left"}),

    PROTANOPIA(ControlCategory.ACCESSIBILITY, "Colorblind: Protanopia", new String[]{"1"}),
    DEUTERANOPIA(ControlCategory.ACCESSIBILITY, "Colorblind: Deuteranopia", new String[]{"2"}),
    TRITANOPIA(ControlCategory.ACCESSIBILITY, "Colorblind: Tritanopia", new String[]{"3"}),

    TOGGLE_SANDBOX(ControlCategory.SANDBOX, "Toggle Sandbox", new String[]{"ctrl", "s"}),
    EXECUTE_CONSOLE(ControlCategory.SANDBOX, "Execute", new String[]{"ctrl", "enter"});

    private final ControlCategory category;
    private final String action;
    private final String[] keys;

    ControlsHUD(ControlCategory category, String action, String[] keys) {
        this.category = category;
        this.action = action;
        this.keys = keys;
    }

    public ControlCategory getCategory() {
        return category;
    }

    public String getAction() {
        return action;
    }

    public String[] getKeys() {
        return keys;
    }
}