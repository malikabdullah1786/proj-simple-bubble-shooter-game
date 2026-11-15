package com.arcade.bubbleshooter.model;

public class Bubble {
    private ColorEnum color;
    private int row;
    private int col;
    private double x;
    private double y;
    private boolean occupied;

    public Bubble(int row, int col, ColorEnum color, double x, double y) {
        this.row = row;
        this.col = col;
        this.color = color;
        this.x = x;
        this.y = y;
        this.occupied = (color != null);
    }

    public ColorEnum getColor() {
        return color;
    }

    public void setColor(ColorEnum color) {
        this.color = color;
        this.occupied = (color != null);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
        if (!occupied) {
            this.color = null;
        }
    }
}
