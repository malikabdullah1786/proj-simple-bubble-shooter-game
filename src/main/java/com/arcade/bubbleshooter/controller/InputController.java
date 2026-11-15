package com.arcade.bubbleshooter.controller;

import com.arcade.bubbleshooter.model.Bubble;
import com.arcade.bubbleshooter.model.BubbleGrid;
import com.arcade.bubbleshooter.model.ColorEnum;
import com.arcade.bubbleshooter.model.GameState;
import com.arcade.bubbleshooter.model.Projectile;
import com.arcade.bubbleshooter.view.GameCanvas;

import javax.swing.*;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

/**
 * Coordinates user input, canon aiming angle, projectile physics updates,
 * collision handling, scoring, and level progression.
 */
public class InputController implements MouseMotionListener {
    private final GameController gameController;
    private final GameCanvas canvas;
    private double aimAngle; // Radians from horizontal

    public InputController(GameController gameController, GameCanvas canvas) {
        this.gameController = gameController;
        this.canvas = canvas;
        this.aimAngle = -Math.PI / 2.0; // Pointing straight up
        canvas.addMouseMotionListener(this);
    }

    public double getAimAngle() {
        return aimAngle;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateAim(e.getX(), e.getY());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateAim(e.getX(), e.getY());
    }

    private void updateAim(int mouseX, int mouseY) {
        int canonX = canvas.getWidth() / 2;
        int canonY = canvas.getHeight() - 40;

        double dx = mouseX - canonX;
        double dy = mouseY - canonY;

        // Calculate angle, restricting to upper hemisphere (pointing upwards)
        double angle = Math.atan2(dy, dx);
        if (angle > -0.1) angle = -0.1;
        if (angle < -Math.PI + 0.1) angle = -Math.PI + 0.1;

        this.aimAngle = angle;
        canvas.setAimAngle(aimAngle);
    }

    public void handleMouseClick(int mouseX, int mouseY) {
        if (gameController.getGameState().isGameOver() || gameController.getGameState().isGameWon()) {
            return;
        }
        updateAim(mouseX, mouseY);
        gameController.fireProjectile();
    }
}
