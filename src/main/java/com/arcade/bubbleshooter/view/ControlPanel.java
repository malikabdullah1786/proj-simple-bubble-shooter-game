package com.arcade.bubbleshooter.view;

import com.arcade.bubbleshooter.model.Bubble;
import com.arcade.bubbleshooter.model.GameState;

import javax.swing.*;
import java.awt.*;

/**
 * ControlPanel: HUD rendering score, high score, next-bubble preview, and missed shot counter.
 */
public class ControlPanel extends JPanel {

    private final GameState gameState;

    public ControlPanel(GameState gameState) {
        this.gameState = gameState;
        setPreferredSize(new Dimension(800, 70));
        setBackground(new Color(25, 25, 35));
        setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(70, 70, 90)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Background gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(30, 30, 45), 0, height, new Color(15, 15, 25));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        // Fonts
        Font labelFont = new Font("SansSerif", Font.BOLD, 12);
        Font valueFont = new Font("SansSerif", Font.BOLD, 20);

        // 1. SCORE
        g2d.setFont(labelFont);
        g2d.setColor(new Color(160, 160, 180));
        g2d.drawString("SCORE", 30, 22);

        g2d.setFont(valueFont);
        g2d.setColor(new Color(255, 215, 0)); // Gold
        g2d.drawString(String.valueOf(gameState.getScore()), 30, 50);

        // 2. HIGH SCORE
        g2d.setFont(labelFont);
        g2d.setColor(new Color(160, 160, 180));
        g2d.drawString("HIGH SCORE", 160, 22);

        g2d.setFont(valueFont);
        g2d.setColor(new Color(0, 230, 150)); // Emerald
        g2d.drawString(String.valueOf(gameState.getHighScore()), 160, 50);

        // 3. MISSED SHOTS (Foul / Drop counter)
        g2d.setFont(labelFont);
        g2d.setColor(new Color(160, 160, 180));
        g2d.drawString("MISSED SHOTS", 310, 22);

        StringBuilder missesStr = new StringBuilder();
        int maxMisses = gameState.getMaxMissesBeforeDrop();
        int currentMisses = gameState.getMissedShots();
        for (int i = 0; i < maxMisses; i++) {
            missesStr.append(i < currentMisses ? "X " : "O ");
        }
        g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2d.setColor(currentMisses >= maxMisses - 1 ? new Color(255, 75, 75) : new Color(220, 100, 100));
        g2d.drawString(missesStr.toString().trim() + " (" + currentMisses + "/" + maxMisses + ")", 310, 48);

        // 4. LEVEL / WAVE
        g2d.setFont(labelFont);
        g2d.setColor(new Color(160, 160, 180));
        g2d.drawString("LEVEL", 510, 22);

        g2d.setFont(valueFont);
        g2d.setColor(new Color(100, 180, 255));
        g2d.drawString(String.valueOf(gameState.getLevel()), 510, 50);

        // 5. NEXT BUBBLE PREVIEW
        g2d.setFont(labelFont);
        g2d.setColor(new Color(160, 160, 180));
        g2d.drawString("NEXT BUBBLE", 640, 22);

        // Draw preview circle container
        int previewX = 670;
        int previewY = 40;
        int radius = 18;

        g2d.setColor(new Color(50, 50, 70));
        g2d.fillOval(previewX - radius - 4, previewY - radius - 4, (radius + 4) * 2, (radius + 4) * 2);
        g2d.setColor(new Color(90, 90, 120));
        g2d.drawOval(previewX - radius - 4, previewY - radius - 4, (radius + 4) * 2, (radius + 4) * 2);

        Bubble nextBubble = gameState.getNextBubble();
        if (nextBubble != null && nextBubble.getColor() != null) {
            Color bubbleColor = nextBubble.getColor().getAwtColor();
            
            // Outer glow / body
            GradientPaint bubbleGradient = new GradientPaint(
                    previewX - radius / 2, previewY - radius / 2,
                    bubbleColor.brighter().brighter(),
                    previewX + radius, previewY + radius,
                    bubbleColor.darker()
            );
            g2d.setPaint(bubbleGradient);
            g2d.fillOval(previewX - radius, previewY - radius, radius * 2, radius * 2);

            // Highlight glare
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fillOval(previewX - radius / 2, previewY - radius / 2 - 4, radius / 2, radius / 4);

            // Border
            g2d.setColor(bubbleColor.darker().darker());
            g2d.drawOval(previewX - radius, previewY - radius, radius * 2, radius * 2);
        }
    }
}
