package com.arcade.bubbleshooter.model;

import java.awt.Color;

public enum ColorEnum {
    RED(new Color(231, 76, 60), "Red"),
    BLUE(new Color(52, 152, 219), "Blue"),
    GREEN(new Color(46, 204, 113), "Green"),
    YELLOW(new Color(241, 196, 15), "Yellow"),
    PURPLE(new Color(155, 89, 182), "Purple"),
    ORANGE(new Color(230, 126, 34), "Orange");

    private final Color awtColor;
    private final String name;

    ColorEnum(Color awtColor, String name) {
        this.awtColor = awtColor;
        this.name = name;
    }

    public Color getAwtColor() {
        return awtColor;
    }

    public String getName() {
        return name;
    }

    public static ColorEnum randomColor() {
        ColorEnum[] values = values();
        return values[(int) (Math.random() * values.length)];
    }
}
