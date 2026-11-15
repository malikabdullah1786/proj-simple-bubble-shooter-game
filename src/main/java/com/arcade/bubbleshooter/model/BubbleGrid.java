package com.arcade.bubbleshooter.model;

import java.awt.Point;
import java.util.*;

public class BubbleGrid {
    public static final int ROWS = 12;
    public static final int COLS = 10;
    public static final double BUBBLE_RADIUS = 20.0;
    public static final double BUBBLE_DIAMETER = BUBBLE_RADIUS * 2;
    public static final double ROW_HEIGHT = BUBBLE_DIAMETER * 0.866; // sqrt(3)/2 for close hexagonal packing
    public static final double CANVAS_WIDTH = 800;
    public static final double CANVAS_HEIGHT = 500; // top playable area

    private Bubble[][] grid;

    public BubbleGrid() {
        grid = new Bubble[ROWS][COLS];
        initInitialGrid();
    }

    public void initInitialGrid() {
        for (int r = 0; r < ROWS; r++) {
            int rowCols = getColsForRow(r);
            for (int c = 0; c < COLS; c++) {
                if (c < rowCols && r < 5) {
                    // Populate top 5 rows with random colors
                    grid[r][c] = new Bubble(ColorEnum.randomColor(), r, c);
                } else {
                    grid[r][c] = new Bubble(null, r, c);
                    grid[r][c].setEmpty(true);
                }
            }
        }
    }

    public int getColsForRow(int r) {
        // Hexagonal staggered grid: even rows have COLS, odd rows have COLS - 1
        return (r % 2 == 0) ? COLS : COLS - 1;
    }

    public Bubble[][] getGrid() {
        return grid;
    }

    public Point getBubbleCenter(int row, int col) {
        double rowIndent = (row % 2 == 1) ? BUBBLE_RADIUS : 0.0;
        // Center horizontally with padding
        double leftPadding = (CANVAS_WIDTH - (COLS * BUBBLE_DIAMETER)) / 2.0;
        double x = leftPadding + rowIndent + col * BUBBLE_DIAMETER + BUBBLE_RADIUS;
        double y = BUBBLE_RADIUS + row * ROW_HEIGHT;
        return new Point((int) Math.round(x), (int) Math.round(y));
    }

    public int[] findClosestEmptySlot(double px, double py) {
        int bestRow = -1;
        int bestCol = -1;
        double minDist = Double.MAX_VALUE;

        for (int r = 0; r < ROWS; r++) {
            int rowCols = getColsForRow(r);
            for (int c = 0; c < rowCols; c++) {
                Bubble b = grid[r][c];
                if (b.isEmpty()) {
                    Point center = getBubbleCenter(r, c);
                    double dist = Math.hypot(center.x - px, center.y - py);
                    if (dist < minDist) {
                        minDist = dist;
                        bestRow = r;
                        bestCol = c;
                    }
                }
            }
        }

        if (minDist <= BUBBLE_DIAMETER * 1.2 && bestRow != -1) {
            return new int[]{bestRow, bestCol};
        }
        return null;
    }

    public List<Point> snapProjectile(Projectile proj) {
        int[] slot = findClosestEmptySlot(proj.getX(), proj.getY());
        List<Point> popped = new ArrayList<>();

        if (slot != null) {
            int r = slot[0];
            int c = slot[1];
            grid[r][c].setColor(proj.getColor());
            grid[r][c].setEmpty(false);

            // Process matches and floating bubbles
            popped = processMatchesAndFloating(r, c);
        }
        return popped;
    }

    public List<Point> processMatchesAndFloating(int startRow, int startCol) {
        List<Point> removedPoints = new ArrayList<>();
        ColorEnum targetColor = grid[startRow][startCol].getColor();
        if (targetColor == null) return removedPoints;

        // 1. Find connected cluster of same color (BFS)
        Set<Point> cluster = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();
        Point startPt = new Point(startRow, startCol);
        queue.add(startPt);
        cluster.add(startPt);

        while (!queue.isEmpty()) {
            Point p = queue.poll();
            for (Point neighbor : getNeighbors(p.x, p.y)) {
                if (!cluster.contains(neighbor)) {
                    Bubble nb = grid[neighbor.x][neighbor.y];
                    if (!nb.isEmpty() && nb.getColor() == targetColor) {
                        cluster.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // If cluster size >= 3, remove them
        if (cluster.size() >= 3) {
            for (Point p : cluster) {
                grid[p.x][p.y].setEmpty(true);
                removedPoints.add(p);
            }

            // 2. Find floating bubbles not connected to the ceiling (Row 0)
            Set<Point> floating = findFloatingBubbles();
            for (Point f : floating) {
                grid[f.x][f.y].setEmpty(true);
                removedPoints.add(f);
            }
        }

        return removedPoints;
    }

    private Set<Point> findFloatingBubbles() {
        Set<Point> connectedToCeiling = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();

        // Add all non-empty bubbles in row 0
        int row0Cols = getColsForRow(0);
        for (int c = 0; c < row0Cols; c++) {
            if (!grid[0][c].isEmpty()) {
                Point p = new Point(0, c);
                connectedToCeiling.add(p);
                queue.add(p);
            }
        }

        // BFS from ceiling
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            for (Point neighbor : getNeighbors(p.x, p.y)) {
                if (!connectedToCeiling.contains(neighbor)) {
                    Bubble nb = grid[neighbor.x][neighbor.y];
                    if (!nb.isEmpty()) {
                        connectedToCeiling.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // Floating bubbles are all non-empty bubbles NOT in connectedToCeiling
        Set<Point> floating = new HashSet<>();
        for (int r = 0; r < ROWS; r++) {
            int rowCols = getColsForRow(r);
            for (int c = 0; c < rowCols; c++) {
                if (!grid[r][c].isEmpty()) {
                    Point p = new Point(r, c);
                    if (!connectedToCeiling.contains(p)) {
                        floating.add(p);
                    }
                }
            }
        }
        return floating;
    }

    public List<Point> getNeighbors(int r, int c) {
        List<Point> neighbors = new ArrayList<>();
        int rowCols = getColsForRow(r);

        // Hexagonal neighbor offsets
        // Even rows vs odd rows offset directions
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

        int[][] dirs = (r % 2 == 0) ? directionsEven : directionsOdd;

        for (int[] d : dirs) {
            int nr = r + d[0];
            int nc = c + d[1];
            if (nr >= 0 && nr < ROWS) {
                int nCols = getColsForRow(nr);
                if (nc >= 0 && nc < nCols) {
                    neighbors.add(new Point(nr, nc));
                }
            }
        }

        return neighbors;
    }

    public boolean checkGameOver() {
        // Check if any bubble reached bottom rows (e.g., row 10 or 11)
        for (int r = ROWS - 2; r < ROWS; r++) {
            int rowCols = getColsForRow(r);
            for (int c = 0; c < rowCols; c++) {
                if (!grid[r][c].isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void shiftGridDown() {
        // Shift all rows down by 1, insert new row at top
        for (int r = ROWS - 1; r > 0; r--) {
            int rowCols = getColsForRow(r);
            int prevRowCols = getColsForRow(r - 1);
            for (int c = 0; c < rowCols; c++) {
                if (c < prevRowCols) {
                    Bubble prev = grid[r - 1][c];
                    grid[r][c].setColor(prev.getColor());
                    grid[r][c].setEmpty(prev.isEmpty());
                } else {
                    grid[r][c].setEmpty(true);
                }
            }
        }
        // Top row gets new random bubbles
        int row0Cols = getColsForRow(0);
        for (int c = 0; c < row0Cols; c++) {
            grid[0][c] = new Bubble(ColorEnum.randomColor(), 0, c);
        }
    }
}
