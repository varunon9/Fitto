package me.varunon9.fitto.oldversion;

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

    // this property helps in painting triplet
    private boolean active;

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

    public boolean equals(Triplet triplet) {
        int junctionNo1 = triplet.getJunctionNo1();
        int junctionNo2 = triplet.getJunctionNo2();
        int junctionNo3 = triplet.getJunctionNo3();
        int thisJunctionNo1 = this.getJunctionNo1();
        int thisJunctionNo2 = this.getJunctionNo2();
        int thisJunctionNo3 = this.getJunctionNo3();
        if ((junctionNo1 + junctionNo2 + junctionNo3)
                != (thisJunctionNo1 + thisJunctionNo2 + thisJunctionNo3)) {
            return false;
        }
        if (thisJunctionNo1 == junctionNo1) {
            return areTwoSetsOfNumbersEquals(junctionNo2, junctionNo3,
                    thisJunctionNo2, thisJunctionNo3);
        } else if (thisJunctionNo1 == junctionNo2) {
            return areTwoSetsOfNumbersEquals(junctionNo1, junctionNo3,
                    thisJunctionNo2, thisJunctionNo3);
        } else if (thisJunctionNo1 == junctionNo3) {
            return areTwoSetsOfNumbersEquals(junctionNo1, junctionNo2,
                    thisJunctionNo2, thisJunctionNo3);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        String string = "[" + this.getJunctionNo1()
                + ", " + this.getJunctionNo2()
                + ", " + this.getJunctionNo3()
                + "]";
        return string;
    }

    private boolean areTwoSetsOfNumbersEquals(int junctionNo1, int junctionNo2,
                                              int thisJunctionNo1, int thisJunctionNo2) {
        if (thisJunctionNo1 == junctionNo1) {
            if (thisJunctionNo2 == junctionNo2) {
                return true;
            } else {
                return false;
            }
        } else if (thisJunctionNo1 == junctionNo2) {
            if (thisJunctionNo2 == junctionNo1) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}
