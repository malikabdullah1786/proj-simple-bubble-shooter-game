package com.arcade.bubbleshooter.model;

public class Projectile {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private ColorEnum color;
    private boolean active;
    private static final double SPEED = 12.0;

    public Projectile(double startX, double startY, double targetX, double targetY, ColorEnum color) {
        this.x = startX;
        this.y = startY;
        this.color = color;
        this.active = true;

        double dx = targetX - startX;
        double dy = targetY - startY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) dist = 1.0;

        this.vx = (dx / dist) * SPEED;
        this.vy = (dy / dist) * SPEED;
    }

    public void update() {
        if (!active) return;
        x += vx;
        y += vy;

        // Bounce off left and right walls
        double radius = BubbleGrid.BUBBLE_RADIUS;
        if (x - radius <= 0) {
            x = radius;
            vx = -vx;
        } else if (x + radius >= BubbleGrid.CANVAS_WIDTH) {
            x = BubbleGrid.CANVAS_WIDTH - radius;
            vx = -vx;
        }

        // Top wall collision
        if (y - radius <= 0) {
            y = radius;
            active = false;
        }
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVx() {
        return vx;
    }

    public void setVx(double vx) {
        this.vx = vx;
    }

    public double getVy() {
        return vy;
    }

    public void setVy(double vy) {
        this.vy = vy;
    }

    public ColorEnum getColor() {
        return color;
    }

    public void setColor(ColorEnum color) {
        this.color = color;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
