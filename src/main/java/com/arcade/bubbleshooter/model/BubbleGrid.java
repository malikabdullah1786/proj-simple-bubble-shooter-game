package com.arcade.bubbleshooter.model;

import java.util.*;

public class BubbleGrid {
    public static final int ROWS = 12;
    public static final int COLS = 10;
    public static final double BUBBLE_DIAMETER = 40.0;
    public static final double BUBBLE_RADIUS = 20.0;
    public static final double ROW_HEIGHT = BUBBLE_DIAMETER * 0.866; // sqrt(3)/2

    private Bubble[][] grid;
    private double originX = 50.0;
    private double originY = 50.0;

    public BubbleGrid(double originX, double originY) {
        this.originX = originX;
        this.originY = originY;
        this.grid = new Bubble[ROWS][COLS];
        initGrid();
    }

    public void initGrid() {
        for (int r = 0; r < ROWS; r++) {
            int colsInRow = (r % 2 == 0) ? COLS : COLS - 1;
            for (int c = 0; c < COLS; c++) {
                if (c < colsInRow) {
                    // Initial rows 0 to 4 filled with random bubbles
                    ColorEnum color = (r < 5) ? ColorEnum.randomColor() : null;
                    double x = getBubbleX(r, c);
                    double y = getBubbleY(r);
                    grid[r][c] = new Bubble(r, c, color, x, y);
                } else {
                    grid[r][c] = new Bubble(r, c, null, 0, 0);
                    grid[r][c].setOccupied(false);
                }
            }
        }
    }

    public double getBubbleX(int row, int col) {
        double offset = (row % 2 != 0) ? BUBBLE_RADIUS : 0.0;
        return originX + col * BUBBLE_DIAMETER + offset + BUBBLE_RADIUS;
    }

    public double getBubbleY(int row) {
        return originY + row * ROW_HEIGHT + BUBBLE_RADIUS;
    }

    public Bubble getBubble(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return null;
        int colsInRow = (row % 2 == 0) ? COLS : COLS - 1;
        if (col >= colsInRow) return null;
        return grid[row][col];
    }

    public List<int[]> getNeighbors(int row, int col) {
        List<int[]> neighbors = new ArrayList<>();
        boolean evenRow = (row % 2 == 0);
        
        int[][] directionsEven = {
            {-1, -1}, {-1, 0},
            {0, -1},  {0, 1},
            {1, -1},  {1, 0}
        };

        int[][] directionsOdd = {
            {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, 0},  {1, 1}
        };

        int[][] dirs = evenRow ? directionsEven : directionsOdd;

        for (int[] d : dirs) {
            int nr = row + d[0];
            int nc = col + d[1];
            if (nr >= 0 && nr < ROWS) {
                int colsInRow = (nr % 2 == 0) ? COLS : COLS - 1;
                if (nc >= 0 && nc < colsInRow) {
                    Bubble b = grid[nr][nc];
                    if (b != null) {
                        neighbors.add(new int[]{nr, nc});
                    }
                }
            }
        }
        return neighbors;
    }

    public int[] snapToGrid(double px, double py) {
        // Find closest valid empty grid position
        double minDist = Double.MAX_VALUE;
        int bestR = -1;
        int bestC = -1;

        for (int r = 0; r < ROWS; r++) {
            int colsInRow = (r % 2 == 0) ? COLS : COLS - 1;
            for (int c = 0; c < colsInRow; c++) {
                Bubble b = grid[r][c];
                if (!b.isOccupied()) {
                    double bx = getBubbleX(r, c);
                    double by = getBubbleY(r);
                    double dist = Math.hypot(px - bx, py - by);
                    if (dist < minDist) {
                        minDist = dist;
                        bestR = r;
                        bestC = c;
                    }
                }
            }
        }

        if (bestR != -1 && minDist <= BUBBLE_DIAMETER * 1.2) {
            return new int[]{bestR, bestC};
        }
        return null;
    }

    public List<int[]> findCluster(int startRow, int startCol, ColorEnum targetColor) {
        List<int[]> cluster = new ArrayList<>();
        if (targetColor == null) return cluster;

        boolean[][] visited = new boolean[ROWS][COLS];
        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            cluster.add(curr);

            for (int[] nCoord : getNeighbors(curr[0], curr[1])) {
                int nr = nCoord[0];
                int nc = nCoord[1];
                if (!visited[nr][nc]) {
                    visited[nr][nc] = true;
                    Bubble nb = grid[nr][nc];
                    if (nb != null && nb.isOccupied() && nb.getColor() == targetColor) {
                        queue.add(new int[]{nr, nc});
                    }
                }
            }
        }
        return cluster;
    }

    public List<int[]> removeFloatingBubbles() {
        boolean[][] connected = new boolean[ROWS][COLS];
        Queue<int[]> queue = new LinkedList<>();

        // Mark all bubbles connected to the top row (row 0)
        for (int c = 0; c < COLS; c++) {
            Bubble b = grid[0][c];
            if (b != null && b.isOccupied()) {
                connected[0][c] = true;
                queue.add(new int[]{0, c});
            }
        }

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            for (int[] nCoord : getNeighbors(curr[0], curr[1])) {
                int nr = nCoord[0];
                int nc = nCoord[1];
                Bubble nb = grid[nr][nc];
                if (nb != null && nb.isOccupied() && !connected[nr][nc]) {
                    connected[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        List<int[]> floating = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            int colsInRow = (r % 2 == 0) ? COLS : COLS - 1;
            for (int c = 0; c < colsInRow; c++) {
                Bubble b = grid[r][c];
                if (b != null && b.isOccupied() && !connected[r][c]) {
                    floating.add(new int[]{r, c});
                    b.setOccupied(false);
                }
            }
        }
        return floating;
    }

    public boolean isGameOver() {
        // Check if any bubble reaches row ROWS-2 or bottom
        for (int c = 0; c < COLS; c++) {
            Bubble b = grid[ROWS - 2][c];
            if (b != null && b.isOccupied()) {
                return true;
            }
        }
        return false;
    }

    public boolean isLevelCleared() {
        for (int r = 0; r < ROWS; r++) {
            int colsInRow = (r % 2 == 0) ? COLS : COLS - 1;
            for (int c = 0; c < colsInRow; c++) {
                Bubble b = grid[r][c];
                if (b != null && b.isOccupied()) {
                    return false;
                }
            }
        }
        return true;
    }

    public void addTopRow() {
        // Shift all rows down by 1
        for (int r = ROWS - 1; r > 0; r--) {
            int colsInCurrent = (r % 2 == 0) ? COLS : COLS - 1;
            int colsInPrev = ((r - 1) % 2 == 0) ? COLS : COLS - 1;
            for (int c = 0; c < colsInCurrent; c++) {
                if (c < colsInPrev) {
                    Bubble prev = grid[r - 1][c];
                    ColorEnum col = (prev != null) ? prev.getColor() : null;
                    grid[r][c].setColor(col);
                    grid[r][c].setOccupied(prev != null && prev.isOccupied());
                } else {
                    grid[r][c].setOccupied(false);
                }
            }
        }
        // Fill new top row with random bubbles
        for (int c = 0; c < COLS; c++) {
            grid[0][c].setColor(ColorEnum.randomColor());
            grid[0][c].setOccupied(true);
        }
    }

    public Bubble[][] getGridArray() {
        return grid;
    }
}
