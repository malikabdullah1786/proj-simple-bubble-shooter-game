package com.arcade.bubbleshooter.model;

import java.awt.Color;
import java.util.Random;

public enum ColorEnum {
    RED(new Color(231, 76, 60), "Red"),
    GREEN(new Color(46, 204, 113), "Green"),
    BLUE(new Color(52, 152, 219), "Blue"),
    YELLOW(new Color(241, 196, 15), "Yellow"),
    PURPLE(new Color(155, 89, 182), "Purple"),
    ORANGE(new Color(230, 126, 34), "Orange");

    private final Color color;
    private final String name;
    private static final Random RANDOM = new Random();

    ColorEnum(Color color, String name) {
        this.color = color;
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public static ColorEnum randomColor() {
        ColorEnum[] values = values();
        return values[RANDOM.nextInt(values.length)];
    }
}
