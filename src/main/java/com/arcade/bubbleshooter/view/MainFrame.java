package com.arcade.bubbleshooter.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame(GameCanvas gameCanvas, ControlPanel controlPanel) {
        setTitle("Bubble Shooter Arcade");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        add(gameCanvas, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);
    }
}
