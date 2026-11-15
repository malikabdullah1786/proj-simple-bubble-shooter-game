package com.arcade.bubbleshooter.controller;

import com.arcade.bubbleshooter.model.GameState;
import com.arcade.bubbleshooter.view.GameCanvas;
import com.arcade.bubbleshooter.view.ControlPanel;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameController {
    private final GameState gameState;
    private final GameCanvas gameCanvas;
    private final ControlPanel controlPanel;
    private final Timer gameLoop;

    public GameController(GameState gameState, GameCanvas gameCanvas, ControlPanel controlPanel) {
        this.gameState = gameState;
        this.gameCanvas = gameCanvas;
        this.controlPanel = controlPanel;

        this.gameLoop = new Timer(16, e -> update());
    }

    public void startGame() {
        gameLoop.start();
    }

    private void update() {
        // Physics and logic updates
        gameState.update();
        
        // Trigger repaint
        gameCanvas.repaint();
        controlPanel.updateHUD(gameState.getScore(), gameState.getHighScore());
    }
}
