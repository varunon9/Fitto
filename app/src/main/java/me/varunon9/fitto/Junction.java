package me.varunon9.fitto;

/**
 * Created by varun on 25/8/17.
 * There are 24 junctions in BoardView occupiedBy either user or computer
 */
public class Junction {

    private int x;
    private int y;
    private String occupiedBy;
    private boolean picked;

    public Junction() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getOccupiedBy() {
        return occupiedBy;
    }

    public void setOccupiedBy(String occupiedBy) {
        this.occupiedBy = occupiedBy;
    }

    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }

}
