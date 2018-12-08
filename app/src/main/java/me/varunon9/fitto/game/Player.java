package me.varunon9.fitto.game;

import android.graphics.Paint;

public class Player {

    private String id;
    private String name;
    private Paint stonePaint;
    private boolean turn;
    private int stonesLeft;
    private int health;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Paint getStonePaint() {
        return stonePaint;
    }

    public void setStonePaint(Paint stonePaint) {
        this.stonePaint = stonePaint;
    }

    public boolean isTurn() {
        return turn;
    }

    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    public int getStonesLeft() {
        return stonesLeft;
    }

    public void setStonesLeft(int stonesLeft) {
        this.stonesLeft = stonesLeft;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    @Override
    public String toString() {
        String player = "Name: "
                + getName()
                + ", Turn: "
                + isTurn()
                + ", StonesLeft: "
                + getStonesLeft();
        return player;
    }
}
