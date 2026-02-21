package org.vertex.engine.render;

import org.vertex.engine.entities.Button;
import org.vertex.engine.enums.ColorblindType;
import org.vertex.engine.enums.GameMenu;
import org.vertex.engine.enums.GameSettings;
import org.vertex.engine.enums.Games;
import org.vertex.engine.interfaces.Clickable;
import org.vertex.engine.interfaces.UI;
import org.vertex.engine.service.GameService;
import org.vertex.engine.service.UIService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class MenuRender {
    public static final GameMenu[] MENU = GameMenu.values();
    public static final Games[] GAMES = Games.values();
    public static final GameSettings[] SETTINGS_MENU = GameSettings.values();
    public static BufferedImage[] OPTION_IMAGES;
    public static String ENABLE = "Enable ";
    private static final String SETTINGS = "SETTINGS";
    private static final String ACHIEVEMENTS = "ACHIEVEMENTS";
    private static final String CHECKMATE = "Checkmate!";
    private static final String STALEMATE = "Stalemate";
    private static final int OPTION_X = 100;
    private static final int OPTION_Y = 160;
    private static final float SCALE = 1.5f;
    private static final int ARC = 32;
    private static final int STROKE = 6;

    private final Map<Clickable, Rectangle> buttons;
    private final Map<Button, Boolean> buttonsClicked;
    private final List<UI> menus;

    private transient BufferedImage TOGGLE_ON;
    private transient BufferedImage TOGGLE_OFF;
    private transient BufferedImage TOGGLE_ON_HIGHLIGHTED;
    private transient BufferedImage TOGGLE_OFF_HIGHLIGHTED;
    private transient BufferedImage HARD_MODE_ON;
    private transient BufferedImage HARD_MODE_ON_HIGHLIGHTED;
    private transient BufferedImage NEXT_PAGE;
    private transient BufferedImage NEXT_PAGE_ON;
    private transient BufferedImage PREVIOUS_PAGE;
    private transient BufferedImage PREVIOUS_PAGE_ON;

    private static ColorblindType cb;
    private int lastHoveredIndex = -1;
    private int scrollOffset = 0;
    private static int totalWidth;

    private RenderContext render;
    private GameService gameService;
    private AchievementSprites sprites;

    public MenuRender(RenderContext render, UI... menus) {
        this.buttons = new HashMap<>();
        this.buttonsClicked = new HashMap<>();
        this.menus = new ArrayList<>();
        Collections.addAll(this.menus, menus);
        this.render = render;
        cb = ColorblindType.PROTANOPIA;
    }

    public Map<Clickable, Rectangle> getButtons() {
        return buttons;
    }

    public List<UI> getMenus() {
        return menus;
    }

    public static ColorblindType getCb() {
        return cb;
    }

    public static void setCb(ColorblindType cb) {
        MenuRender.cb = cb;
    }

    public static int getARC() {
        return ARC;
    }

    public GameService getGameService() {
        return gameService;
    }

    public void setGameService(GameService gameService) {
        this.gameService = gameService;
    }

    public void draw(Graphics2D g2) {
        for (UI menu : menus) {
            if(menu.canDraw(gameService.getState())) {
                menu.drawMenu(g2);
            }
        }
    }

    public BufferedImage getPREVIOUS_PAGE() {
        return PREVIOUS_PAGE;
    }

    public BufferedImage getPREVIOUS_PAGE_ON() {
        return PREVIOUS_PAGE_ON;
    }

    public void init() {
        this.sprites = new AchievementSprites();
        try {
            TOGGLE_ON = UIService.getImage("/ui/toggle_on");
            TOGGLE_OFF = UIService.getImage("/ui/toggle_off");
            TOGGLE_ON_HIGHLIGHTED = UIService.getImage("/ui/toggle_onh");
            TOGGLE_OFF_HIGHLIGHTED = UIService.getImage("/ui/toggle_offh");
            HARD_MODE_ON = UIService.getImage("/ui/hardmode_on");
            HARD_MODE_ON_HIGHLIGHTED = UIService.getImage("/ui/hardmode_onh");
            NEXT_PAGE = UIService.getImage("/ui/next_page");
            NEXT_PAGE_ON = UIService.getImage("/ui/next_page_highlighted");
            PREVIOUS_PAGE = UIService.getImage("/ui/previous_page");
            PREVIOUS_PAGE_ON = UIService.getImage("/ui/previous_page_highlighted");

            OPTION_IMAGES = new BufferedImage[]{
                    TOGGLE_ON, TOGGLE_OFF, TOGGLE_ON_HIGHLIGHTED, TOGGLE_OFF_HIGHLIGHTED,
                    HARD_MODE_ON, HARD_MODE_ON_HIGHLIGHTED, NEXT_PAGE, NEXT_PAGE_ON,
                    PREVIOUS_PAGE, PREVIOUS_PAGE_ON};
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BufferedImage getColorblindSprite(BufferedImage img) {
        if(img == TOGGLE_ON) return Colorblindness.filter(TOGGLE_ON);
        if(img == TOGGLE_OFF) return Colorblindness.filter(TOGGLE_OFF);
        if(img == TOGGLE_ON_HIGHLIGHTED) return Colorblindness.filter(TOGGLE_ON_HIGHLIGHTED);
        if(img == TOGGLE_OFF_HIGHLIGHTED) return Colorblindness.filter(TOGGLE_OFF_HIGHLIGHTED);
        if(img == HARD_MODE_ON) return Colorblindness.filter(HARD_MODE_ON);
        if(img == HARD_MODE_ON_HIGHLIGHTED) return Colorblindness.filter(HARD_MODE_ON_HIGHLIGHTED);
        if(img == NEXT_PAGE) return Colorblindness.filter(NEXT_PAGE);
        if(img == NEXT_PAGE_ON) return Colorblindness.filter(NEXT_PAGE_ON);
        if(img == PREVIOUS_PAGE) return Colorblindness.filter(PREVIOUS_PAGE);
        if(img == PREVIOUS_PAGE_ON) return Colorblindness.filter(PREVIOUS_PAGE_ON);
        return img;
    }
}