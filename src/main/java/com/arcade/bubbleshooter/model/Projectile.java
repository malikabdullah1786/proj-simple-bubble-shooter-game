package com.arcade.bubbleshooter.model;

public class Projectile {
    private double x;
    private double y;
    private double vx;
    private double vy;
    private final Bubble bubble;
    private boolean active;

    public Projectile(double x, double y, double vx, double vy, Bubble bubble) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.bubble = bubble;
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

    public Bubble getBubble() {
        return bubble;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
