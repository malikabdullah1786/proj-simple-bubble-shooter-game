package com.arcade.bubbleshooter.model;

public class Bubble {
    private ColorEnum color;
    private boolean empty;
    private int row;
    private int col;
    private boolean visited; // Used for BFS cluster traversal

    public Bubble(ColorEnum color, int row, int col) {
        this.color = color;
        this.empty = (color == null);
        this.row = row;
        this.col = col;
        this.visited = false;
    }

    public ColorEnum getColor() {
        return color;
    }

    public void setColor(ColorEnum color) {
        this.color = color;
        this.empty = (color == null);
    }

    public boolean isEmpty() {
        return empty;
    }

    public void setEmpty(boolean empty) {
        this.empty = empty;
        if (empty) {
            this.color = null;
        }
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

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }
}
