package com.arcade.bubbleshooter.controller;

import com.arcade.bubbleshooter.model.*;
import com.arcade.bubbleshooter.view.ControlPanel;
import com.arcade.bubbleshooter.view.GameCanvas;
import com.arcade.bubbleshooter.view.MainFrame;

import javax.swing.*;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Main game controller handling the 60 FPS update loop, firing mechanics,
 * grid collisions, cluster popping, floating bubble drop, and UI synchronization.
 */
public class GameController implements ActionListener {
    private final GameState gameState;
    private final BubbleGrid bubbleGrid;
    private Projectile activeProjectile;
    private final GameCanvas canvas;
    private final ControlPanel controlPanel;
    private final MainFrame mainFrame;
    private final Timer gameLoopTimer;
    private InputController inputController;
    private boolean inFlight;

    private static final double BUBBLE_DIAMETER = 40.0;
    private static final double PROJECTILE_SPEED = 16.0;
    private static final int FPS = 60;

    public GameController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.gameState = new GameState();
        this.bubbleGrid = new BubbleGrid(BUBBLE_DIAMETER);
        this.canvas = new GameCanvas(bubbleGrid);
        this.controlPanel = new ControlPanel();

        mainFrame.setGameCanvas(canvas);
        mainFrame.setControlPanel(controlPanel);

        this.inputController = new InputController(this, canvas);
        canvas.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                inputController.handleMouseClick(e.getX(), e.getY());
            }
        });

        controlPanel.getRestartButton().addActionListener(e -> startNewGame());

        this.gameLoopTimer = new Timer(1000 / FPS, this);
        startNewGame();
    }

    public void startNewGame() {
        gameLoopTimer.stop();
        gameState.resetGame();
        bubbleGrid.clear();
        bubbleGrid.initInitialGrid(gameState.getActiveColors());

        // Setup current and next bubbles
        spawnNewBubble();
        spawnNewBubble();

        inFlight = false;
        activeProjectile = null;
        canvas.setProjectile(null);
        canvas.setCurrentBubble(gameState.getCurrentBubble());
        canvas.setNextBubble(gameState.getNextBubble());
        canvas.setAimAngle(-Math.PI / 2.0);

        updateHUD();
        canvas.repaint();
        gameLoopTimer.start();
    }

    private void spawnNewBubble() {
        if (gameState.getNextBubble() == null) {
            gameState.setCurrentBubble(new Bubble(ColorEnum.randomColor(gameState.getActiveColors())));
            gameState.setNextBubble(new Bubble(ColorEnum.randomColor(gameState.getActiveColors())));
        } else {
            gameState.setCurrentBubble(gameState.getNextBubble());
            gameState.setNextBubble(new Bubble(ColorEnum.randomColor(gameState.getActiveColors())));
        }
        canvas.setCurrentBubble(gameState.getCurrentBubble());
        canvas.setNextBubble(gameState.getNextBubble());
    }

    public void fireProjectile() {
        if (inFlight || gameState.isGameOver() || gameState.isGameWon()) return;

        double startX = canvas.getWidth() / 2.0;
        double startY = canvas.getHeight() - 40.0;

        double angle = inputController.getAimAngle();
        double targetX = startX + Math.cos(angle) * 1000.0;
        double targetY = startY + Math.sin(angle) * 1000.0;

        Bubble bubbleToShoot = gameState.getCurrentBubble();
        activeProjectile = new Projectile(startX, startY, targetX, targetY, PROJECTILE_SPEED, bubbleToShoot);
        canvas.setProjectile(activeProjectile);

        inFlight = true;

        // Spawn next bubble immediately in canon queue
        gameState.setCurrentBubble(gameState.getNextBubble());
        gameState.setNextBubble(new Bubble(ColorEnum.randomColor(gameState.getActiveColors())));
        canvas.setCurrentBubble(gameState.getCurrentBubble());
        canvas.setNextBubble(gameState.getNextBubble());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState.isGameOver() || gameState.isGameWon()) {
            return;
        }

        if (inFlight && activeProjectile != null) {
            activeProjectile.update(canvas.getWidth(), BUBBLE_DIAMETER);
            canvas.repaint();

            // Check collision with top wall or existing bubbles
            boolean collided = false;
            double radius = BUBBLE_DIAMETER / 2.0;

            if (activeProjectile.getY() - radius <= 0) {
                collided = true;
            } else {
                // Check distance to any existing bubble in grid
                for (int r = 0; r < bubbleGrid.getRows(); r++) {
                    int colsInRow = (r % 2 == 0) ? bubbleGrid.getCols() : bubbleGrid.getCols() - 1;
                    for (int c = 0; c < colsInRow; c++) {
                        Bubble b = bubbleGrid.getBubble(r, c);
                        if (b != null) {
                            java.awt.geom.Point2D.Double center = bubbleGrid.getBubbleCenter(r, c);
                            double dx = center.x - activeProjectile.getX();
                            double dy = center.y - activeProjectile.getY();
                            double dist = Math.sqrt(dx * dx + dy * dy);
                            if (dist <= BUBBLE_DIAMETER * 0.9) {
                                collided = true;
                                break;
                            }
                        }
                    }
                    if (collided) break;
                }
            }

            if (collided) {
                handleProjectileCollision();
            }
        }
    }

    private void handleProjectileCollision() {
        Point snap = bubbleGrid.snapProjectile(activeProjectile.getX(), activeProjectile.getY());
        bubbleGrid.setBubble(snap.x, snap.y, activeProjectile.getBubble());

        // Check for color matches (3 or more)
        List<Point> cluster = bubbleGrid.findConnectedCluster(snap.x, snap.y, activeProjectile.getBubble().getColorEnum());
        if (cluster.size() >= 3) {
            // Pop matched bubbles
            for (Point p : cluster) {
                bubbleGrid.setBubble(p.x, p.y, null);
            }
            int points = cluster.size() * 10;
            gameState.addScore(points);

            // Remove floating bubbles
            List<Point> floating = bubbleGrid.removeFloatingBubbles();
            if (!floating.isEmpty()) {
                gameState.addScore(floating.size() * 20);
            }

            // Check win condition (grid empty)
            if (bubbleGrid.isEmpty()) {
                gameState.advanceLevel();
                bubbleGrid.initInitialGrid(gameState.getActiveColors());
                gameState.addScore(500); // Level clear bonus
            }
        } else {
            // Missed matching cluster
            gameState.incrementMissedShots();
            if (gameState.getMissedShots() >= gameState.getMaxMissedShots()) {
                bubbleGrid.addRowToTop(gameState.getActiveColors());
                gameState.setMissedShots(0);
            }
        }

        // Check Game Over
        if (bubbleGrid.isGameOver()) {
            gameState.setGameOver(true);
            gameLoopTimer.stop();
            canvas.repaint();
            showGameOverDialog();
        }

        // Reset projectile
        activeProjectile = null;
        canvas.setProjectile(null);
        inFlight = false;

        updateHUD();
        canvas.repaint();
    }

    private void updateHUD() {
        controlPanel.updateStats(gameState.getScore(), gameState.getHighScore(), gameState.getLevel(), gameState.getMissedShots(), gameState.getMaxMissedShots());
    }

    private void showGameOverDialog() {
        int choice = JOptionPane.showConfirmDialog(
                mainFrame,
                "Game Over! Your Score: " + gameState.getScore() + "\nWould you like to play again?",
                "Game Over",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            startNewGame();
        }
    }

    public GameState getGameState() {
        return gameState;
    }

    public BubbleGrid getBubbleGrid() {
        return bubbleGrid;
    }
}
