package me.varunon9.fitto.oldversion;

/**
 * Created by varun on 25/8/17.
 */

public class FitTriplet {

    public FitTriplet() {
    }

    private int junctionNo;
    private int size;
    private Triplet tripletsArray[];

    public int getJunctionNo() {
        return junctionNo;
    }

    public void setJunctionNo(int junctionNo) {
        this.junctionNo = junctionNo;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Triplet[] getTripletsArray() {
        return tripletsArray;
    }

    public void setTripletsArray(Triplet[] tripletsArray) {
        this.tripletsArray = tripletsArray;
    }
}
