package me.varunon9.fitto;

/**
 * Created by varun on 25/8/17.
 */

public class Triplet {

    public Triplet() {
    }

    private int junctionNo1;
    private int junctionNo2;
    private int junctionNo3;
    private String ownedBy;
    private boolean active;

    // credited = true means whoever owned this triplet got his credit by picking
    // other player's stone
    private boolean credited;

    public int getJunctionNo1() {
        return junctionNo1;
    }

    public void setJunctionNo1(int junctionNo1) {
        this.junctionNo1 = junctionNo1;
    }

    public int getJunctionNo2() {
        return junctionNo2;
    }

    public void setJunctionNo2(int junctionNo2) {
        this.junctionNo2 = junctionNo2;
    }

    public int getJunctionNo3() {
        return junctionNo3;
    }

    public void setJunctionNo3(int junctionNo3) {
        this.junctionNo3 = junctionNo3;
    }

    public String getOwnedBy() {
        return ownedBy;
    }

    public void setOwnedBy(String ownedBy) {
        this.ownedBy = ownedBy;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isCredited() {
        return credited;
    }

    public void setCredited(boolean credited) {
        this.credited = credited;
    }
}
