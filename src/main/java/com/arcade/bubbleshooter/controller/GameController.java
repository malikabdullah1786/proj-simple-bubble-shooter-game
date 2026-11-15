package com.arcade.bubbleshooter.controller;

import com.arcade.bubbleshooter.model.*;

import java.util.List;
import java.util.Set;

public class GameController {
    private final GameState gameState;
    private boolean gameOverTriggered = false;

    public GameController(GameState gameState) {
        this.gameState = gameState;
    }

    public GameState getGameState() {
        return gameState;
    }

    /**
     * Updates physics, checks wall and bubble collisions.
     * @param deltaTime time elapsed in seconds
     */
    public void update(double deltaTime) {
        if (gameState.isGameOver()) {
            return;
        }

        Projectile projectile = gameState.getProjectile();
        if (projectile != null && projectile.isInFlight()) {
            // Update projectile position
            projectile.update(deltaTime);

            // 1. Check Ceiling Collision (y <= radius)
            double radius = gameState.getGrid().getBubbleRadius();
            if (projectile.getY() <= radius) {
                snapProjectileToGrid();
                return;
            }

            // 2. Check Left/Right Wall Collisions (x <= radius or x >= canvasWidth - radius)
            // Assuming standard canvas width e.g. 800 or based on grid columns * diameter
            double canvasWidth = gameState.getGrid().getCols() * gameState.getGrid().getBubbleDiameter();
            if (projectile.getX() <= radius) {
                projectile.setX(radius);
                projectile.reflectX();
            } else if (projectile.getX() >= canvasWidth - radius) {
                projectile.setX(canvasWidth - radius);
                projectile.reflectX();
            }

            // 3. Check Bubble-to-Bubble Collision with existing grid bubbles
            BubbleGrid grid = gameState.getGrid();
            double collisionDistSq = Math.pow(grid.getBubbleDiameter(), 2); // Center-to-center distance squared threshold

            for (int r = 0; r < grid.getRows(); r++) {
                int rCols = grid.getColsForRow(r);
                for (int c = 0; c < rCols; c++) {
                    Bubble neighbor = grid.getBubble(r, c);
                    if (neighbor != null) {
                        double[] center = grid.getBubbleCenter(r, c);
                        double distSq = Math.pow(projectile.getX() - center[0], 2) + Math.pow(projectile.getY() - center[1], 2);
                        if (distSq <= collisionDistSq) {
                            snapProjectileToGrid();
                            return;
                        }
                    }
                }
            }

            // If projectile goes out of bounds at the bottom
            if (projectile.getY() > 600) {
                gameState.setProjectile(null);
                gameState.incrementMissedShots();
                checkMissedShotsPenalty();
                prepareNextShot();
            }
        }
    }

    /**
     * Snaps the active projectile to the grid, performs match-3 BFS, handles floating bubbles and score calculation.
     */
    public void snapProjectileToGrid() {
        Projectile proj = gameState.getProjectile();
        if (proj == null) return;

        BubbleGrid grid = gameState.getGrid();
        int[] snapCoords = grid.snapToGrid(proj.getX(), proj.getY());
        int targetR = snapCoords[0];
        int targetC = snapCoords[1];

        // Place new bubble in grid
        Bubble newBubble = new Bubble(proj.getColor(), targetR, targetC);
        grid.setBubble(targetR, targetC, newBubble);

        // Clear active projectile
        gameState.setProjectile(null);

        // Perform Match-3 BFS
        List<int[]> matchedBubbles = grid.findMatches(targetR, targetC);

        if (matchedBubbles.size() >= 3) {
            // Remove matched bubbles
            for (int[] coords : matchedBubbles) {
                grid.setBubble(coords[0], coords[1], null);
            }

            // Add score: e.g. 10 points per matched bubble * multiplier
            int matchScore = matchedBubbles.size() * 10;
            gameState.addScore(matchScore);

            // Find and remove floating bubbles
            Set<int[]> floatingBubbles = grid.findFloatingBubbles();
            if (!floatingBubbles.isEmpty()) {
                for (int[] coords : floatingBubbles) {
                    grid.setBubble(coords[0], coords[1], null);
                }
                int floatScore = floatingBubbles.size() * 20; // Bonus points for dropping floaters
                gameState.addScore(floatScore);
            }
        } else {
            // No match of 3+, count as a missed shot towards penalty
            gameState.incrementMissedShots();
            checkMissedShotsPenalty();
        }

        // Check Win/Loss conditions
        if (grid.isEmpty()) {
            gameState.setGameOver(true);
            gameState.setGameWon(true);
        } else if (grid.isGameOver()) {
            gameState.setGameOver(true);
            gameState.setGameWon(false);
        }

        prepareNextShot();
    }

    private void checkMissedShotsPenalty() {
        if (gameState.getMissedShots() >= 5) {
            gameState.resetMissedShots();
            gameState.getGrid().shiftDownAddCeiling();
            if (gameState.getGrid().isGameOver()) {
                gameState.setGameOver(true);
                gameState.setGameWon(false);
            }
        }
    }

    private void prepareNextShot() {
        if (!gameState.isGameOver()) {
            gameState.cycleNextBubble();
        }
    }

    public void fireProjectile(double targetX, double targetY) {
        Projectile proj = gameState.getProjectile();
        if (proj != null && !proj.isInFlight()) {
            double startX = 400; // Canvas center bottom
            double startY = 560;
            proj.setX(startX);
            proj.setY(startY);

            double dx = targetX - startX;
            double dy = targetY - startY;
            double angle = Math.atan2(dy, dx);

            // Clamp angle to upper hemisphere (prevent shooting downwards)
            if (angle > -0.1) {
                angle = -0.1;
            } else if (angle < -Math.PI + 0.1) {
                angle = -Math.PI + 0.1;
            }

            double speed = 800.0; // pixels per second
            proj.setVelocity(Math.cos(angle) * speed, Math.sin(angle) * speed);
            proj.setInFlight(true);
        }
    }

    public void resetGame() {
        gameState.reset();
        gameOverTriggered = false;
    }
}
