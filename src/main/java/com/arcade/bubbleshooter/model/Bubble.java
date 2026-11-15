package com.arcade.bubbleshooter.model;

public class Bubble {
    private int row;
    private int col;
    private ColorEnum color;
    private boolean visited;

    public Bubble(ColorEnum color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.visited = false;
    }

    public Bubble(int row, int col, ColorEnum color) {
        this.row = row;
        this.col = col;
        this.color = color;
        this.visited = false;
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

    public ColorEnum getColor() {
        return color;
    }

    public void setColor(ColorEnum color) {
        this.color = color;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
