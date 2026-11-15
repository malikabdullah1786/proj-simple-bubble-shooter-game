package com.arcade.bubbleshooter.model;

public class Projectile {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private ColorEnum color;
    private boolean active;

    public Projectile(double startX, double startY, ColorEnum color) {
        this.x = startX;
        this.y = startY;
        this.color = color;
        this.vx = 0;
        this.vy = 0;
        this.active = false;
    }

    public void launch(double targetX, double targetY, double speed) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) dist = 1;
        this.vx = (dx / dist) * speed;
        this.vy = (dy / dist) * speed;
        this.active = true;
    }

    public void update(double dt) {
        if (!active) return;
        x += vx * dt;
        y += vy * dt;
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
