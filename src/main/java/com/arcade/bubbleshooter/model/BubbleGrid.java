package com.arcade.bubbleshooter.model;

import java.util.*;

public class BubbleGrid {
    private final int rows = 12;
    private final int cols = 10;
    private final Bubble[][] grid;
    private final double bubbleDiameter;
    private final double bubbleRadius;

    public BubbleGrid(double bubbleDiameter) {
        this.bubbleDiameter = bubbleDiameter;
        this.bubbleRadius = bubbleDiameter / 2.0;
        this.grid = new Bubble[rows][cols];
        initializeInitialGrid();
    }

    private void initializeInitialGrid() {
        // Populate top 5 rows with random colors
        for (int r = 0; r < 5; r++) {
            int rowCols = (r % 2 == 0) ? cols : cols - 1;
            for (int c = 0; c < rowCols; c++) {
                grid[r][c] = new Bubble(ColorEnum.randomColor(), r, c);
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getColsForRow(int r) {
        if (r < 0 || r >= rows) return cols;
        return (r % 2 == 0) ? cols : cols - 1;
    }

    public Bubble getBubble(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= getColsForRow(r)) {
            return null;
        }
        return grid[r][c];
    }

    public void setBubble(int r, int c, Bubble bubble) {
        if (r >= 0 && r < rows && c >= 0 && c < getColsForRow(r)) {
            grid[r][c] = bubble;
            if (bubble != null) {
                bubble.setRow(r);
                bubble.setCol(c);
            }
        }
    }

    public double getBubbleDiameter() {
        return bubbleDiameter;
    }

    public double getBubbleRadius() {
        return bubbleRadius;
    }

    /**
     * Converts grid row and col into pixel (x, y) coordinates for the center of the bubble.
     */
    public double[] getBubbleCenter(int r, int c) {
        double xOffset = (r % 2 == 1) ? bubbleRadius : 0.0;
        double x = c * bubbleDiameter + bubbleRadius + xOffset;
        double y = r * (bubbleDiameter * 0.866) + bubbleRadius; // ~sqrt(3)/2 for close hexagonal packing
        return new double[]{x, y};
    }

    /**
     * Snap continuous (x, y) collision point to nearest valid empty grid slot.
     */
    public int[] snapToGrid(double px, double py) {
        double rowHeight = bubbleDiameter * 0.866;
        int targetRow = (int) Math.round((py - bubbleRadius) / rowHeight);
        targetRow = Math.max(0, Math.min(rows - 1, targetRow));

        int maxCols = getColsForRow(targetRow);
        double xOffset = (targetRow % 2 == 1) ? bubbleRadius : 0.0;
        int targetCol = (int) Math.round((px - bubbleRadius - xOffset) / bubbleDiameter);
        targetCol = Math.max(0, Math.min(maxCols - 1, targetCol));

        // If target slot is occupied, search neighbors for the closest empty slot
        if (getBubble(targetRow, targetCol) != null) {
            double minDist = Double.MAX_VALUE;
            int bestR = targetRow;
            int bestC = targetCol;

            for (int r = 0; r < rows; r++) {
                int rCols = getColsForRow(r);
                for (int c = 0; c < rCols; c++) {
                    if (grid[r][c] == null) {
                        double[] center = getBubbleCenter(r, c);
                        double dist = Math.hypot(center[0] - px, center[1] - py);
                        if (dist < minDist) {
                            minDist = dist;
                            bestR = r;
                            bestC = c;
                        }
                    }
                }
            }
            targetRow = bestR;
            targetCol = bestC;
        }

        return new int[]{targetRow, targetCol};
    }

    /**
     * Finds neighbors of a grid coordinate in a hexagonal grid.
     */
    public List<int[]> getNeighbors(int r, int c) {
        List<int[]> neighbors = new ArrayList<>();
        boolean evenRow = (r % 2 == 0);

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
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < getColsForRow(nr)) {
                neighbors.add(new int[]{nr, nc});
            }
        }

        return neighbors;
    }

    /**
     * Performs Breadth-First Search to find all connected matching color bubbles.
     */
    public Set<int[]> findCluster(int startR, int startC, ColorEnum color) {
        Set<int[]> cluster = new HashSet<>();
        if (getBubble(startR, startC) == null) return cluster;

        // Reset visited flags
        resetVisited();

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startR, startC});
        grid[startR][startC].setVisited(true);
        cluster.add(new int[]{startR, startC});

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            for (int[] n : getNeighbors(curr[0], curr[1])) {
                int nr = n[0];
                int nc = n[1];
                Bubble nb = grid[nr][nc];
                if (nb != null && !nb.isVisited() && nb.getColor() == color) {
                    nb.setVisited(true);
                    cluster.add(new int[]{nr, nc});
                    queue.add(new int[]{nr, nc});
                }
            }
        }

        return cluster;
    }

    /**
     * Finds floating bubbles (bubbles not connected to the ceiling row 0) after removals.
     */
    public Set<int[]> findFloatingBubbles() {
        resetVisited();
        Queue<int[]> queue = new LinkedList<>();

        // Add all bubbles connected to row 0
        for (int c = 0; c < getColsForRow(0); c++) {
            Bubble b = grid[0][c];
            if (b != null) {
                b.setVisited(true);
                queue.add(new int[]{0, c});
            }
        }

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            for (int[] n : getNeighbors(curr[0], curr[1])) {
                Bubble nb = grid[n[0]][n[1]];
                if (nb != null && !nb.isVisited()) {
                    nb.setVisited(true);
                    queue.add(new int[]{n[0], n[1]});
                }
            }
        }

        // Any bubble not visited is floating
        Set<int[]> floating = new HashSet<>();
        for (int r = 0; r < rows; r++) {
            int rCols = getColsForRow(r);
            for (int c = 0; c < rCols; c++) {
                Bubble b = grid[r][c];
                if (b != null && !b.isVisited()) {
                    floating.add(new int[]{r, c});
                }
            }
        }

        return floating;
    }

    private void resetVisited() {
        for (int r = 0; r < rows; r++) {
            int rCols = getColsForRow(r);
            for (int c = 0; c < rCols; c++) {
                if (grid[r][c] != null) {
                    grid[r][c].setVisited(false);
                }
            }
        }
    }

    public boolean isGameOver() {
        // Check if any bubble has reached or passed row 11 (bottom row)
        int bottomRow = rows - 1;
        for (int c = 0; c < getColsForRow(bottomRow); c++) {
            if (grid[bottomRow][c] != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        for (int r = 0; r < rows; r++) {
            int rCols = getColsForRow(r);
            for (int c = 0; c < rCols; c++) {
                if (grid[r][c] != null) return false;
            }
        }
        return true;
    }
}
