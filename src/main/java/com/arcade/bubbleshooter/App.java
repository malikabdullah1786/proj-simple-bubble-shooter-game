package com.arcade.bubbleshooter;

import com.arcade.bubbleshooter.view.MainFrame;

import javax.swing.*;

/**
 * App entry point for Bubble Shooter Arcade.
 */
public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
