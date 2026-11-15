package com.arcade.bubbleshooter.model;

public class Bubble {
    private final ColorEnum color;
    private int row;
    private int col;
    private boolean visited; // Used for BFS clustering

    public Bubble(ColorEnum color) {
        this.color = color;
        this.row = -1;
        this.col = -1;
        this.visited = false;
    }

    public Bubble(ColorEnum color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
        this.visited = false;
    }

    public ColorEnum getColor() {
        return color;
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
