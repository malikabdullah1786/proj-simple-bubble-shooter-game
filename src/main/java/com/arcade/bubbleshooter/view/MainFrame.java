package com.arcade.bubbleshooter.view;

import com.arcade.bubbleshooter.controller.GameController;
import com.arcade.bubbleshooter.model.GameState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * MainFrame: Window container (800x600px fixed) hosting GameCanvas and ControlPanel,
 * and orchestrating the 60 FPS Game Loop timer.
 */
public class MainFrame extends JFrame {

    private final GameState gameState;
    private final GameController gameController;
    private final GameCanvas gameCanvas;
    private final ControlPanel controlPanel;
    private Timer gameLoopTimer;

    public MainFrame() {
        setTitle("Bubble Shooter Arcade - Java Swing Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Initialize Model & Controller
        this.gameState = new GameState();
        this.gameController = new GameController(gameState);

        // Initialize UI components
        this.controlPanel = new ControlPanel(gameState);
        this.gameCanvas = new GameCanvas(gameState, gameController);

        add(controlPanel, BorderLayout.NORTH);
        add(gameCanvas, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);

        // Start 60 FPS game tick loop (~16.6 ms per frame)
        initGameLoop();
    }

    private void initGameLoop() {
        int fps = 60;
        int delay = 1000 / fps;

        gameLoopTimer = new Timer(delay, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 1. Update physics / game state
                gameController.update();

                // 2. Repaint HUD control panel and canvas
                controlPanel.repaint();
                gameCanvas.repaint();
            }
        });
        gameLoopTimer.start();
    }

    public static void main(String[] args) {
        // Ensure EDT execution
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
