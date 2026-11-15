package com.arcade.bubbleshooter.view;

import com.arcade.bubbleshooter.controller.GameController;
import com.arcade.bubbleshooter.controller.InputController;
import com.arcade.bubbleshooter.model.Bubble;
import com.arcade.bubbleshooter.model.BubbleGrid;
import com.arcade.bubbleshooter.model.GameState;
import com.arcade.bubbleshooter.model.Projectile;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.List;

/**
 * GameCanvas: Active render loop and mouse input listener for shooting bubbles.
 */
public class GameCanvas extends JPanel {

    private final GameState gameState;
    private final GameController gameController;

    public GameCanvas(GameState gameState, GameController gameController) {
        this.gameState = gameState;
        this.gameController = gameController;

        setBackground(new Color(20, 20, 30));
        setDoubleBuffered(true);

        InputController inputController = new InputController(gameState, gameController, this);
        addMouseListener(inputController);
        addMouseMotionListener(inputController);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 1. Draw Background Grid pattern or gradient
        drawBackground(g2d);

        // 2. Draw Bubble Grid
        drawBubbleGrid(g2d);

        // 3. Draw Aim Line if dragging/aiming
        drawAimLine(g2d);

        // 4. Draw Active Projectile
        drawProjectile(g2d);

        // 5. Draw Shooter Cannon at bottom center
        drawCannon(g2d);

        // 6. Draw Game Over / Victory Overlay if active
        if (gameState.isGameOver() || gameState.isVictory()) {
            drawGameOverOverlay(g2d);
        }
    }

    private void drawBackground(Graphics2D g2d) {
        int width = getWidth();
        int height = getHeight();

        // Subtle dark gradient background
        GradientPaint bgGradient = new GradientPaint(0, 0, new Color(25, 25, 40), 0, height, new Color(10, 10, 18));
        g2d.setPaint(bgGradient);
        g2d.fillRect(0, 0, width, height);

        // Subtle decorative grid dots
        g2d.setColor(new Color(40, 40, 60, 80));
        for (int x = 40; x < width; x += 80) {
            for (int y = 40; y < height; y += 80) {
                g2d.fillOval(x, y, 2, 2);
            }
        }
    }

    private void drawBubbleGrid(Graphics2D g2d) {
        BubbleGrid grid = gameState.getGrid();
        Bubble[][] matrix = grid.getMatrix();
        int rows = grid.getRows();
        int cols = grid.getCols();
        double radius = grid.getBubbleRadius();
        double diameter = grid.getBubbleDiameter();

        for (int r = 0; r < rows; r++) {
            double rowOffset = (r % 2 != 0) ? radius : 0.0;
            for (int c = 0; c < cols; c++) {
                Bubble bubble = matrix[r][c];
                if (bubble != null && bubble.getColor() != null) {
                    double x = c * diameter + radius + rowOffset + BubbleGrid.GRID_ORIGIN_X;
                    double y = r * (diameter * BubbleGrid.ROW_HEIGHT_RATIO) + radius + BubbleGrid.GRID_ORIGIN_Y;

                    drawBubble(g2d, x, y, radius, bubble.getColor().getAwtColor());
                }
            }
        }
    }

    private void drawBubble(Graphics2D g2d, double cx, double cy, double radius, Color color) {
        int x = (int) (cx - radius);
        int y = (int) (cy - radius);
        int d = (int) (radius * 2);

        // Outer shadow / depth
        g2d.setColor(new Color(0, 0, 0, 80));
        g2d.fillOval(x + 2, y + 3, d, d);

        // Main bubble gradient
        GradientPaint gradient = new GradientPaint(
                (float)(cx - radius * 0.5), (float)(cy - radius * 0.5),
                color.brighter().brighter(),
                (float)(cx + radius), (float)(cy + radius),
                color.darker().darker()
        );
        g2d.setPaint(gradient);
        g2d.fillOval(x, y, d, d);

        // Glossy highlight reflection
        g2d.setColor(new Color(255, 255, 255, 200));
        g2d.fillOval((int)(cx - radius * 0.5), (int)(cy - radius * 0.6), (int)(radius * 0.7), (int)(radius * 0.4));

        // Border outline
        g2d.setColor(color.darker());
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawOval(x, y, d, d);
    }

    private void drawAimLine(Graphics2D g2d) {
        if (gameState.isProjectileInFlight()) {
            return;
        }

        Point mousePos = gameState.getMousePosition();
        if (mousePos == null) {
            return;
        }

        double startX = BubbleGrid.CANVAS_WIDTH / 2.0;
        double startY = BubbleGrid.CANVAS_HEIGHT - 45.0;

        // Do not draw if mouse is below the shooter
        if (mousePos.y >= startY) {
            return;
        }

        double dx = mousePos.x - startX;
        double dy = mousePos.y - startY;
        double angle = Math.atan2(dy, dx);

        // Limit angle to upper hemisphere [-pi to 0]
        if (angle > -0.1) angle = -0.1;
        if (angle < -Math.PI + 0.1) angle = -Math.PI + 0.1;

        // Dotted trajectory raycast with wall bouncing preview
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1.0f, new float[]{6f, 6f}, 0f));

        double currX = startX;
        double currY = startY;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);

        double maxDist = 600.0;
        double step = 15.0;
        double traveled = 0;

        while (traveled < maxDist) {
            double nextX = currX + cos * step;
            double nextY = currY + sin * step;

            // Check left/right wall bounce
            if (nextX < BubbleGrid.BUBBLE_RADIUS) {
                nextX = BubbleGrid.BUBBLE_RADIUS;
                g2d.draw(new Line2D.Double(currX, currY, nextX, nextY));
                cos = -cos; // reflect angle horizontally
                currX = nextX;
                currY = nextY;
            } else if (nextX > BubbleGrid.CANVAS_WIDTH - BubbleGrid.BUBBLE_RADIUS) {
                nextX = BubbleGrid.CANVAS_WIDTH - BubbleGrid.BUBBLE_RADIUS;
                g2d.draw(new Line2D.Double(currX, currY, nextX, nextY));
                cos = -cos;
                currX = nextX;
                currY = nextY;
            } else {
                g2d.draw(new Line2D.Double(currX, currY, nextX, nextY));
                currX = nextX;
                currY = nextY;
            }

            if (nextY <= BubbleGrid.GRID_ORIGIN_Y) {
                break;
            }

            traveled += step;
        }
    }

    private void drawProjectile(Graphics2D g2d) {
        Projectile proj = gameState.getProjectile();
        if (proj != null && proj.isActive() && proj.getCurrentColor() != null) {
            drawBubble(g2d, proj.getX(), proj.getY(), BubbleGrid.BUBBLE_RADIUS, proj.getCurrentColor().getAwtColor());
        }
    }

    private void drawCannon(Graphics2D g2d) {
        double centerX = BubbleGrid.CANVAS_WIDTH / 2.0;
        double centerY = BubbleGrid.CANVAS_HEIGHT - 45.0;

        // Draw aiming cannon barrel if projectile is not in flight
        Point mousePos = gameState.getMousePosition();
        double angle = -Math.PI / 2.0; // default straight up

        if (mousePos != null) {
            double dx = mousePos.x - centerX;
            double dy = mousePos.y - centerY;
            angle = Math.atan2(dy, dx);
            if (angle > -0.1) angle = -0.1;
            if (angle < -Math.PI + 0.1) angle = -Math.PI + 0.1;
        }

        // Cannon barrel
        g2d.setColor(new Color(80, 90, 110));
        g2d.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        double barrelLength = 35.0;
        g2d.draw(new Line2D.Double(centerX, centerY, centerX + Math.cos(angle) * barrelLength, centerY + Math.sin(angle) * barrelLength));

        // Base swivel mount
        g2d.setColor(new Color(50, 50, 70));
        g2d.fillOval((int)centerX - 24, (int)centerY - 24, 48, 48);
        g2d.setColor(new Color(120, 130, 160));
        g2d.setStroke(new BasicStroke(3.0f));
        g2d.drawOval((int)centerX - 24, (int)centerY - 24, 48, 48);

        // Current loaded bubble in cannon (if projectile not active)
        if (!gameState.isProjectileInFlight() && gameState.getCurrentBubble() != null && gameState.getCurrentBubble().getColor() != null) {
            drawBubble(g2d, centerX, centerY, BubbleGrid.BUBBLE_RADIUS, gameState.getCurrentBubble().getColor().getAwtColor());
        }
    }

    private void drawGameOverOverlay(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setFont(new Font("SansSerif", Font.BOLD, 42));
        FontMetrics fm = g2d.getFontMetrics();

        String msg = gameState.isVictory() ? "VICTORY!" : "GAME OVER";
        Color msgColor = gameState.isVictory() ? new Color(0, 255, 128) : new Color(255, 60, 60);

        g2d.setColor(msgColor);
        g2d.drawString(msg, (getWidth() - fm.stringWidth(msg)) / 2, getHeight() / 2 - 30);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));
        String subMsg = "Final Score: " + gameState.getScore() + "  |  Click to Play Again";
        g2d.setColor(Color.WHITE);
        FontMetrics fmSub = g2d.getFontMetrics();
        g2d.drawString(subMsg, (getWidth() - fmSub.stringWidth(subMsg)) / 2, getHeight() / 2 + 20);
    }
}
