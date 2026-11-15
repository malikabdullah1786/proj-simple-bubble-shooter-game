package com.arcade.bubbleshooter.model;

import java.util.prefs.Preferences;

/**
 * Tracks score, high score, missed shots counter, active level, and game-over states.
 */
public class GameState {
    private int score;
    private int highScore;
    private int missedShots;
    private static final int MAX_MISSED_SHOTS = 5;
    private boolean gameOver;
    private boolean gameWon;
    private int level;
    private Bubble currentBubble;
    private Bubble nextBubble;
    private int activeColors;

    private static final String PREF_HIGH_SCORE = "BUBBLE_SHOOTER_HIGH_SCORE";

    public GameState() {
        Preferences prefs = Preferences.userNodeForPackage(GameState.class);
        this.highScore = prefs.getInt(PREF_HIGH_SCORE, 0);
        resetGame();
    }

    public void resetGame() {
        this.score = 0;
        this.missedShots = 0;
        this.gameOver = false;
        this.gameWon = false;
        this.level = 1;
        this.activeColors = 4; // Start with 4 colors
        this.currentBubble = null;
        this.nextBubble = null;
    }

    public void addScore(int points) {
        this.score += points;
        if (this.score > this.highScore) {
            this.highScore = this.score;
            saveHighScore();
        }
    }

    private void saveHighScore() {
        try {
            Preferences prefs = Preferences.userNodeForPackage(GameState.class);
            prefs.putInt(PREF_HIGH_SCORE, this.highScore);
        } catch (Exception e) {
            // Ignore if preferences are restricted
        }
    }

    public void incrementMissedShots() {
        this.missedShots++;
        if (this.missedShots >= MAX_MISSED_SHOTS) {
            this.missedShots = 0; // Reset counter after dropping row
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
        if (this.score > this.highScore) {
            this.highScore = this.score;
            saveHighScore();
        }
    }

    public int getHighScore() {
        return highScore;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
        saveHighScore();
    }

    public int getMissedShots() {
        return missedShots;
    }

    public void setMissedShots(int missedShots) {
        this.missedShots = missedShots;
    }

    public int getMaxMissedShots() {
        return MAX_MISSED_SHOTS;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean isGameWon() {
        return gameWon;
    }

    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        // Increase color variety with levels up to max 8
        this.activeColors = Math.min(4 + (level - 1), ColorEnum.values().length);
    }

    public void advanceLevel() {
        this.level++;
        this.activeColors = Math.min(4 + (level - 1), ColorEnum.values().length);
    }

    public Bubble getCurrentBubble() {
        return currentBubble;
    }

    public void setCurrentBubble(Bubble currentBubble) {
        this.currentBubble = currentBubble;
    }

    public Bubble getNextBubble() {
        return nextBubble;
    }

    public void setNextBubble(Bubble nextBubble) {
        this.nextBubble = nextBubble;
    }

    public int getActiveColors() {
        return activeColors;
    }

    public void setActiveColors(int activeColors) {
        this.activeColors = activeColors;
    }
}
