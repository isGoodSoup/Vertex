package org.vertex.engine.enums;

import java.awt.*;

public enum Theme {
    DEFAULT(
            "white", "black",
            new Color(255,255,255),
            new Color(20,20,40),
            new Color(20,20,40),
            new Color(255,200,0)
    ),
    BLACK(
            "white", "black",
            new Color(0,0,0),
            new Color(255,255,255),
            new Color(20,20,40),
            new Color(255,255,100)
    ),
    LEGACY(
            "creme", "brown",
            new Color(210,165,125),
            new Color(175,115,70),
            new Color(120,70,40),
            new Color(255,225,0)
    ),
    OCEAN(
            "ocean", "pacific",
            new Color(100,140,200),
            new Color(100,90,200),
            new Color(70,60,170),
            new Color(180,170,255)
    ),
    FOREST(
            "forest", "rainforest",
            new Color(100,180,100),
            new Color(30,120,60),
            new Color(10,100,40),
            new Color(150,240,180)
    ),
    LOGO(
            "logo", "logo2",
            new Color(111,175,77),
            new Color(71,135,37),
            new Color(51,115,17),
            new Color(191,255,157)
    ),
    FAIRY(
            "fairy", "purple",
            new Color(180,140,200),
            new Color(180,70,200),
            new Color(140,40,170),
            new Color(250,140,255)
    );

    private final String lightName;
    private final String darkName;
    private final Color background;
    private final Color foreground;
    private final Color edge;
    private final Color highlight;

    Theme(String lightName,
          String darkName,
          Color background,
          Color foreground,
          Color edge,
          Color highlight) {

        this.lightName = lightName;
        this.darkName = darkName;
        this.background = background;
        this.foreground = foreground;
        this.edge = edge;
        this.highlight = highlight;
    }

    public String getColor(Tint tint) {
        return tint == Tint.LIGHT ? lightName : darkName;
    }

    public Color getBackground() { return background; }
    public Color getForeground() { return foreground; }
    public Color getEdge() { return edge; }
    public Color getHighlight() { return highlight; }
}
