package me.varunon9.fitto;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by varun on 25/8/17.
 * This class contains utility functions related to Fitto game which are called frequently
 */

public class GameUtility {

    public Path getBoardPath(Junction junctionsArray[]) {
        Path boardPath = new Path();
        /**
         * Visual representation of BoardView
         * Numbers represent junction where stones will be put
         *    1---------------------2------------------------3
         *    |                                               |
         *    |      9---------------10---------------11      |
         *    |      |                                |       |
         *    |      |     17---------18--------19    |       |
         *    |      |     |                    |     |       |
         *    |      |     |                    |     |       |
         *    8     16     24                   20    12     4
         *    |                                               |
         *    |                                               |
         *    |                                               |
         *    |                                               |
         *    |                                               |
         *    |                                               |
         *    |                                               |
         *    7---------------------6-----------------------5
         *
         *    1 will have coordinate x, y
         *    2 will have coordinate x + 3, y
         *    3 will have coordinate x + 6, y
         *    9 will have coordinate x + 1, y + 1
         *    10 will have coordinate x + 3, y + 1
         *    11 will have coordinate x + 5, y likewise
         */

        // drawing 3 squares
        for (int i = 1; i <= 3; i++) {
            int start;

            if (i == 1) {
                start = 1;
            } else if (i == 2) {
                start = 9;
            } else {
                start = 17;
            }

            boardPath.moveTo(junctionsArray[start].getX(), junctionsArray[start].getY());

            int x, y;
            for (int j = 1; j < 8; j++) {
                x = junctionsArray[start + j].getX();
                y = junctionsArray[start + j].getY();
                boardPath.lineTo(x, y);
            }

            // complete square by reaching to starting point
            boardPath.lineTo(junctionsArray[start].getX(), junctionsArray[start].getY());
        }

        // drawing lines intersecting all three squares (on corners)
        for (int i = 1; i <= 7; i += 2) {
            boardPath.moveTo(junctionsArray[i].getX(), junctionsArray[i].getY());
            boardPath.lineTo(junctionsArray[i + 16].getX(), junctionsArray[i + 16].getY());
        }

        // drawing lines intersecting all three squares (on mid)
        for (int i = 2; i <= 8; i += 2) {
            boardPath.moveTo(junctionsArray[i].getX(), junctionsArray[i].getY());
            boardPath.lineTo(junctionsArray[i + 16].getX(), junctionsArray[i + 16].getY());
        }

        return boardPath;
    }

    public int getUserTouchedJunction(float x, float y, Junction junctionsArray[],
                                      int unitLength) {

        // finding corresponding junction from junctionsArray
        int junctionNo = -1;
        float proximityThreshold = unitLength / 2;
        int xCord = 0;
        int yCord = 0;
        for (int i = 1; i < junctionsArray.length; i++) {
            xCord = junctionsArray[i].getX();
            yCord = junctionsArray[i].getY();
            if ((Math.abs(xCord - x) < proximityThreshold) &&
                    (Math.abs(yCord - y) < proximityThreshold)) {
                junctionNo = i;
                break;
            }
        }
        return junctionNo;
    }

    public void initializeJunctions(Junction junctionsArray[], int startIndex,
                                    int startX, int startY, int distance) {
        int i, corner;
        for (i = startIndex, corner = 1; corner <= 8; i++, corner++) {
            junctionsArray[i] = new Junction();
            junctionsArray[i].setPicked(false);
            switch (corner) {
                case 1: {
                    junctionsArray[i].setX(startX);
                    junctionsArray[i].setY(startY);
                    break;
                }
                case 2: {
                    junctionsArray[i].setX(startX + distance);
                    junctionsArray[i].setY(startY);
                    break;
                }
                case 3: {
                    junctionsArray[i].setX(startX + 2 * distance);
                    junctionsArray[i].setY(startY);
                    break;
                }
                case 4: {
                    junctionsArray[i].setX(startX + 2 * distance);
                    junctionsArray[i].setY(startY + distance);
                    break;
                }
                case 5: {
                    junctionsArray[i].setX(startX + 2 * distance);
                    junctionsArray[i].setY(startY + 2 * distance);
                    break;
                }
                case 6: {
                    junctionsArray[i].setX(startX + distance);
                    junctionsArray[i].setY(startY + 2 * distance);
                    break;
                }
                case 7: {
                    junctionsArray[i].setX(startX);
                    junctionsArray[i].setY(startY + 2 * distance);
                    break;
                }
                case 8: {
                    junctionsArray[i].setX(startX);
                    junctionsArray[i].setY(startY + distance);
                    break;
                }
            }
        }

    }

    public String getWinner(String playerUser, String playerComputer,
                            int userHealth, int computerHealth) {
        String winner = null;

        // less than 3 means players can't make a fit triplet
        if (userHealth < 3) {
            winner = playerComputer;
        } else if (computerHealth < 3) {
            winner = playerUser;
        }

        return winner;
    }

    /**
     * @return an array of array, each corner junction/position will hold three triplets
     * while middle junction/position will hold 2 triplets of which they are part of
     */
    public FitTriplet[] getFitTripletsArray() {

        // there are total 24 FitTriplets
        FitTriplet fitTripletsArray[] = new FitTriplet[25];
        for (int i = 1; i <= 24; i++) {
            fitTripletsArray[i] = new FitTriplet();
            fitTripletsArray[i].setJunctionNo(i);
            int size = 2;
            if (i % 2 == 1) {
                // odd will have size 3 while even will have size 2
                size = 3;
            }
            fitTripletsArray[i].setSize(size);
            fitTripletsArray[i].setTripletsArray(getTripletsArray(i));
        }

        return fitTripletsArray;
    }

    private Triplet buildTriplet(int junctionNo1, int junctionNo2, int junctionNo3) {
        Triplet triplet = new Triplet();
        triplet.setJunctionNo1(junctionNo1);
        triplet.setJunctionNo2(junctionNo2);
        triplet.setJunctionNo3(junctionNo3);
        triplet.setActive(false);
        return triplet;
    }

    private Triplet[] getTripletsArray(int junctionNo) {
        Triplet tripletsArray[];
        if (junctionNo % 2 == 1) {
            // corner junction
            tripletsArray = new Triplet[3];
            int i = 0;

            // 1 triplet connecting all 3 squares
            // assuming junctionNo <= 7
            int junctionNo1 = junctionNo;
            int junctionNo2 = junctionNo1 + 8;
            int junctionNo3 = junctionNo2 + 8;

            if (junctionNo >= 9 && junctionNo <= 15) {
                junctionNo1 = junctionNo - 8;
                junctionNo2 = junctionNo;
                junctionNo3 = junctionNo + 8;
            } else if (junctionNo >= 17 && junctionNo <= 23) {
                junctionNo3 = junctionNo;
                junctionNo2 = junctionNo3 - 8;
                junctionNo1 = junctionNo2 - 8;
            }
            tripletsArray[i++] = buildTriplet(junctionNo1, junctionNo2, junctionNo3);

            // 2 triplets connecting only single square (increasing and decreasing)
            // increasing
            junctionNo1 = junctionNo;
            junctionNo2 = junctionNo1 + 1;
            if (junctionNo != 7 && junctionNo != 15 && junctionNo != 23) {
                junctionNo3 = junctionNo2 + 1;
            } else {
                junctionNo3 = junctionNo2 - 7;
            }
            tripletsArray[i++] = buildTriplet(junctionNo1, junctionNo2, junctionNo3);

            // decreasing
            junctionNo1 = junctionNo;
            if (junctionNo != 1 && junctionNo != 9 && junctionNo != 17) {
                junctionNo2 = junctionNo1 - 1;
            } else {
                junctionNo2 = junctionNo1 + 7;
            }
            junctionNo3 = junctionNo2 - 1;
            tripletsArray[i++] = buildTriplet(junctionNo1, junctionNo2, junctionNo3);
            return tripletsArray;
        } else {
            // middle junction
            tripletsArray = new Triplet[2];
            int i = 0;

            // one triplet connecting all three sqares
            // assuming junctionNo <= 8
            int junctionNo1 = junctionNo;
            int junctionNo2 = junctionNo1 + 8;
            int junctionNo3 = junctionNo2 + 8;

            if (junctionNo >= 10 && junctionNo <= 16) {
                junctionNo2 = junctionNo;
                junctionNo1 = junctionNo2 - 8;
                junctionNo3 = junctionNo2 + 8;
            } else if (junctionNo >= 18) {
                junctionNo3 = junctionNo;
                junctionNo2 = junctionNo3 - 8;
                junctionNo1 = junctionNo2 - 8;
            }
            tripletsArray[i++] = buildTriplet(junctionNo1, junctionNo2, junctionNo3);

            // one triplet connecting single square
            junctionNo2 = junctionNo;
            junctionNo3 = junctionNo2 - 1;
            if (junctionNo != 8 && junctionNo != 16 && junctionNo != 24) {
                junctionNo1 = junctionNo2 + 1;
            } else {
                junctionNo1 = junctionNo2 - 7;
            }
            tripletsArray[i++] = buildTriplet(junctionNo1, junctionNo2, junctionNo3);
            return tripletsArray;
        }
    }

    public void paintTriplet(Triplet triplet, Canvas canvas, Paint tripletPaint,
                             Junction junctionsArray[], Path tripletPath) {
        int junctionNo1 = triplet.getJunctionNo1();
        int junctionNo2 = triplet.getJunctionNo2();
        int junctionNo3 = triplet.getJunctionNo3();

        tripletPath.reset();
        tripletPath.moveTo(junctionsArray[junctionNo1].getX(), junctionsArray[junctionNo1].getY());
        tripletPath.lineTo(junctionsArray[junctionNo2].getX(), junctionsArray[junctionNo2].getY());
        tripletPath.lineTo(junctionsArray[junctionNo3].getX(), junctionsArray[junctionNo3].getY());

        canvas.drawPath(tripletPath, tripletPaint);
    }

    public boolean isAdjacent(int junctionNo1, int junctionNo2) {
        
        // validations
        if (junctionNo1 <= 0 || junctionNo2 <= 0) {
            return false;
        }
        if (junctionNo1 >= 25 || junctionNo2 >= 25) {
            return false;
        }
        int difference = Math.abs(junctionNo1 - junctionNo2);
        if (difference == 8) {
            return true;
        }
        if (difference == 7) {
            if ((junctionNo1 == 1 && junctionNo2 == 8)
                    || (junctionNo1 == 8 && junctionNo2 == 1)) {
                return true;
            }
            if ((junctionNo1 == 9 && junctionNo2 == 16)
                    || (junctionNo1 == 16 && junctionNo2 == 9)) {
                return true;
            }
            if ((junctionNo1 == 17 && junctionNo2 == 24)
                    || (junctionNo1 == 24 && junctionNo2 == 17)) {
                return true;
            }
        }
        if (difference == 1) {
            if ((junctionNo1 == 8 && junctionNo2 == 9)
                    || (junctionNo1 == 9 && junctionNo2 == 8)) {
                return false;
            }
            if ((junctionNo1 == 16 && junctionNo2 == 17)
                    || (junctionNo1 == 17 && junctionNo2 == 16)) {
                return false;
            }
            return true;
        }
        return false;
    }

    public boolean isPartOfTriplet(List<Triplet> activeTripletsList, int junctionNo) {
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            int junctionNo1 = triplet.getJunctionNo1();
            int junctionNo2 = triplet.getJunctionNo2();
            int junctionNo3 = triplet.getJunctionNo3();
            if (junctionNo == junctionNo1
                    || junctionNo == junctionNo2
                    || junctionNo == junctionNo3) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdjacentVacant(int junctionNo, Junction junctionsArray[]) {

        // checking vacant position in other square
        int adjacentJunctionNo = junctionNo + 8;
        if (adjacentJunctionNo <= 24) {
            if (isVacant(adjacentJunctionNo, junctionsArray)) {
                return true;
            }
        }
        adjacentJunctionNo = junctionNo - 8;
        if (adjacentJunctionNo >= 1) {
            if (isVacant(adjacentJunctionNo, junctionsArray)) {
                return true;
            }
        }

        // checking vacant position in same square by increasing position
        if (junctionNo != 8 && junctionNo != 16 && junctionNo != 24) {
            adjacentJunctionNo = junctionNo + 1;
        } else {
            adjacentJunctionNo = junctionNo - 7;
        }
        if (isVacant(adjacentJunctionNo, junctionsArray)) {
            return true;
        }

        // checking vacant position in same square by decreasing position
        if (junctionNo != 1 && junctionNo != 9 && junctionNo != 17) {
            adjacentJunctionNo = junctionNo - 1;
        } else {
            adjacentJunctionNo = junctionNo + 7;
        }
        if (isVacant(adjacentJunctionNo, junctionsArray)) {
            return true;
        }
        return false;
    }

    public boolean isVacant(int junctionNo, Junction junctionsArray[]) {
        if (junctionsArray[junctionNo].getOccupiedBy() == null
                || junctionsArray[junctionNo].getOccupiedBy().equals("")) {
            return true;
        }
        return false;
    }

    private boolean isOccupiedBy(int junctionNo, Junction junctionsArray[], String player) {
        if (junctionsArray[junctionNo].getOccupiedBy() != null
                && junctionsArray[junctionNo].getOccupiedBy().equals(player)) {
            return true;
        }
        return false;
    }

    /**
     * @param junctionsArray
     * @param fitTripletsArray
     * @param player
     * @return list of all junctionNos where by drawing a stone, a triplet will be fit
     */
    public List<Integer> getAllJunctionNumbersToFitTriplet(Junction junctionsArray[],
                                                           FitTriplet fitTripletsArray[],
                                                           String player) {
        List<Integer> junctionNumbersList = new ArrayList<>();
        for (int i = 1; i < fitTripletsArray.length; i++) {
            Triplet tripletsArray[] = fitTripletsArray[i].getTripletsArray();
            for (int j = 0; j < tripletsArray.length; j++) {
                Triplet triplet = tripletsArray[j];

                // triplet must not be active
                if (!triplet.isActive()) {
                    int junctionNo1 = triplet.getJunctionNo1();
                    int junctionNo2 = triplet.getJunctionNo2();
                    int junctionNo3 = triplet.getJunctionNo3();

                    // any two junctions should be occupied by player and 3rd must be vacant
                    if (isVacant(junctionNo1, junctionsArray)) {
                        if (isBothJunctionsOccupiedByPlayer(junctionsArray, player,
                                junctionNo2, junctionNo3)) {
                            junctionNumbersList.add(junctionNo1);
                        }
                    } else if (isVacant(junctionNo2, junctionsArray)) {
                        if (isBothJunctionsOccupiedByPlayer(junctionsArray, player,
                                junctionNo1, junctionNo3)) {
                            junctionNumbersList.add(junctionNo2);
                        }
                    } else if (isVacant(junctionNo3, junctionsArray)) {
                        if (isBothJunctionsOccupiedByPlayer(junctionsArray, player,
                                junctionNo1, junctionNo2)) {
                            junctionNumbersList.add(junctionNo3);
                        }
                    }
                }
            }
        }
        return junctionNumbersList;
    }

    /**
     * A dual triplet is arrangement of stones in such a way that player can form 2 triplets
     * Thus even if opponent blocks one triplet, player will form another
     *
     * @param junctionsArray
     * @param fitTripletsArray
     * @param player
     * @return list of all junctions, where by drawing a stone a dual triplet will be formed
     */
    public List<Integer> getAllJunctionNumbersToFitFutureDualTriplet(
            Junction junctionsArray[],
            FitTriplet fitTripletsArray[], String player, int latestPlayerStoneJunctionNo) {
        List<Integer> junctionNumbersList = new ArrayList<>();
        if (latestPlayerStoneJunctionNo > 0) {
            Triplet tripletsArray[] =
                    fitTripletsArray[latestPlayerStoneJunctionNo].getTripletsArray();
            for (int j = 0; j < tripletsArray.length; j++) {
                Triplet triplet = tripletsArray[j];

                /**
                 * one junction will be equal to latestPlayerStoneJunctionNo and other two
                 * must be vacant
                 * if it matches this criteria then-
                 * get other two junctions of triplet, except the current one (i.e. i)
                 * and check if triplets at those two junctions (except current triplet)
                 * are one occupied and two vacant
                 */
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();

                if (junctionNo1 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo2, junctionsArray)
                            && !isVacant(junctionNo3, junctionsArray)) {
                        continue;
                    }
                } else if (junctionNo2 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo1, junctionsArray)
                            && !isVacant(junctionNo3, junctionsArray)) {
                        continue;
                    }
                } else if (junctionNo3 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo1, junctionsArray)
                            && !isVacant(junctionNo2, junctionsArray)) {
                        continue;
                    }
                }
                if (junctionNo1 != latestPlayerStoneJunctionNo) {
                    if (areTripletsOneOccupiedAndTwoVacant(junctionNo1, latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray, player)) {
                        junctionNumbersList.add(junctionNo1);
                    }
                }
                if (junctionNo2 != latestPlayerStoneJunctionNo) {
                    if (areTripletsOneOccupiedAndTwoVacant(junctionNo2, latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray, player)) {
                        junctionNumbersList.add(junctionNo2);
                    }
                }
                if (junctionNo3 != latestPlayerStoneJunctionNo) {
                    if (areTripletsOneOccupiedAndTwoVacant(junctionNo3, latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray, player)) {
                        junctionNumbersList.add(junctionNo3);
                    }
                }
            }
        }
        return junctionNumbersList;
    }

    private boolean isBothJunctionsOccupiedByPlayer(Junction junctionsArray[], String player,
                                                    int junctionNo1, int junctionNo2) {
        if (!isVacant(junctionNo1, junctionsArray)) {
            if (!isVacant(junctionNo2, junctionsArray)) {
                if (isOccupiedBy(junctionNo1, junctionsArray, player)) {
                    if (isOccupiedBy(junctionNo2, junctionsArray, player)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method tells whether all triplets at junctionNo (excluding the triplet of which
     * excludedJunctionNo is part of) are one occupied ant two vacant
     * junctionNo has been already checked for vacant in caller method
     *
     * @param junctionNo
     * @param excludedJunctionNo
     * @param junctionsArray
     * @param fitTripletsArray
     * @return
     */
    private boolean areTripletsOneOccupiedAndTwoVacant(int junctionNo,
                                                       int excludedJunctionNo,
                                                       Junction junctionsArray[],
                                                       FitTriplet fitTripletsArray[],
                                                       String player) {
        Triplet tripletsArray[] = fitTripletsArray[junctionNo].getTripletsArray();
        for (int i = 0; i < tripletsArray.length; i++) {
            Triplet triplet = tripletsArray[i];
            int junctionNo1 = triplet.getJunctionNo1();
            int junctionNo2 = triplet.getJunctionNo2();
            int junctionNo3 = triplet.getJunctionNo3();
            if (junctionNo1 != excludedJunctionNo
                    && junctionNo2 != excludedJunctionNo
                    && junctionNo3 != excludedJunctionNo) {

                // junctionNo is vacant, we know it. so other two junctions
                // should be occupied and vacant
                if (junctionNo1 == junctionNo) {
                    if (isVacant(junctionNo2, junctionsArray)) {
                        if (isOccupiedBy(junctionNo3, junctionsArray, player)) {
                            return true;
                        }
                    } else if (isVacant(junctionNo3, junctionsArray)) {
                        if (isOccupiedBy(junctionNo2, junctionsArray, player)) {
                            return true;
                        }
                    }
                }
                if (junctionNo2 == junctionNo) {
                    if (isVacant(junctionNo1, junctionsArray)) {
                        if (isOccupiedBy(junctionNo3, junctionsArray, player)) {
                            return true;
                        }
                    } else if (isVacant(junctionNo3, junctionsArray)) {
                        if (isOccupiedBy(junctionNo1, junctionsArray, player)) {
                            return true;
                        }
                    }
                }
                if (junctionNo3 == junctionNo) {
                    if (isVacant(junctionNo1, junctionsArray)) {
                        if (isOccupiedBy(junctionNo2, junctionsArray, player)) {
                            return true;
                        }
                    } else if (isVacant(junctionNo2, junctionsArray)) {
                        if (isOccupiedBy(junctionNo1, junctionsArray, player)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public void removeTripletFromActiveTripletsList(Triplet tripletToBeRemoved,
                                                    List<Triplet> activeTripletsList) {
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            if (triplet.equals(tripletToBeRemoved)) {
                activeTripletsList.remove(i);
            }
        }
    }

    public void addTripletToActiveTripletsList(Triplet tripletToBeAdded,
                                               List<Triplet> activeTripletsList) {
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            if (triplet.equals(tripletToBeAdded)) {
                return;
            }
        }
        activeTripletsList.add(tripletToBeAdded);
    }

    public boolean canEatPlayerStone(Junction junctionsArray[],
                                     String player,
                                     List<Triplet> activeTripletsList) {
        for (int i = 1; i < junctionsArray.length; i++) {
            if (junctionsArray[i].getOccupiedBy() != null
                    && junctionsArray[i].getOccupiedBy().equals(player)) {
                if (!isPartOfTriplet(activeTripletsList, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Triplet> getTwoOccupiedAndOneVacantTripletsList(Junction junctionsArray[],
                                                                FitTriplet fitTripletsArray[],
                                                                String player) {
        List<Triplet> twoOccupiedAndOneVacantTripletsList = new ArrayList<>();
        for (int i = 1; i < fitTripletsArray.length; i++) {
            Triplet tripletsArray[] = fitTripletsArray[i].getTripletsArray();
            for (int j = 0; j < tripletsArray.length; j++) {
                Triplet triplet = tripletsArray[j];

                // triplet must not be active
                if (!triplet.isActive()) {
                    int junctionNo1 = triplet.getJunctionNo1();
                    int junctionNo2 = triplet.getJunctionNo2();
                    int junctionNo3 = triplet.getJunctionNo3();

                    // any two junctions should be occupied by player and 3rd must be vacant
                    if (isVacant(junctionNo1, junctionsArray)) {
                        if (isBothJunctionsOccupiedByPlayer(junctionsArray, player,
                                junctionNo2, junctionNo3)) {
                            addTripletToList(twoOccupiedAndOneVacantTripletsList, triplet);
                        }
                    } else if (isVacant(junctionNo2, junctionsArray)) {
                        if (isBothJunctionsOccupiedByPlayer(junctionsArray, player,
                                junctionNo1, junctionNo3)) {
                            addTripletToList(twoOccupiedAndOneVacantTripletsList, triplet);
                        }
                    } else if (isVacant(junctionNo3, junctionsArray)) {
                        if (isBothJunctionsOccupiedByPlayer(junctionsArray, player,
                                junctionNo1, junctionNo2)) {
                            addTripletToList(twoOccupiedAndOneVacantTripletsList, triplet);
                        }
                    }
                }
            }
        }
        return twoOccupiedAndOneVacantTripletsList;
    }

    private void addTripletToList(List<Triplet> tripletsList, Triplet triplet) {
        boolean found = false;
        for (int i = 0; i < tripletsList.size(); i++) {
            if (tripletsList.get(i).equals(triplet)) {
                found = true;
                break;
            }
        }
        if (!found) {
            tripletsList.add(triplet);
        }
    }

    /**
     * @param junctionsArray
     * @param fitTripletsArray
     * @param player
     * @return list of all junction numbers which is occupied by given player and which will be
     * part of dual triplet in future
     */
    public List<Integer> getJunctionsListFormingDualTriplet(Junction junctionsArray[],
                                                            FitTriplet fitTripletsArray[],
                                                            String player,
                                                            int latestPlayerStoneJunctionNo) {
        List<Integer> junctionsListFormingDualTriplet = new ArrayList<>();
        if (latestPlayerStoneJunctionNo > 0) {
            Triplet tripletsArray[] =
                    fitTripletsArray[latestPlayerStoneJunctionNo].getTripletsArray();
            for (int j = 0; j < tripletsArray.length; j++) {
                Triplet triplet = tripletsArray[j];

                /**
                 * one junction will be equal to latestPlayerStoneJunctionNo and other two
                 * must be vacant
                 * if it matches this criteria then-
                 * get other two junctions of triplet, except the current one (i.e. i)
                 * and check if triplets at those two junctions (except current triplet)
                 * are one occupied and two vacant and add occupied one to list
                 */
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();

                if (junctionNo1 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo2, junctionsArray)
                            && !isVacant(junctionNo3, junctionsArray)) {
                        continue;
                    }
                } else if (junctionNo2 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo1, junctionsArray)
                            && !isVacant(junctionNo3, junctionsArray)) {
                        continue;
                    }
                } else if (junctionNo3 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo1, junctionsArray)
                            && !isVacant(junctionNo2, junctionsArray)) {
                        continue;
                    }
                }
                if (junctionNo1 != latestPlayerStoneJunctionNo) {
                    addJunctionNumberOfOneOccupiedAndTwoVacantTriplet(junctionNo1,
                            latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray,
                            player, junctionsListFormingDualTriplet);
                }
                if (junctionNo2 != latestPlayerStoneJunctionNo) {
                    addJunctionNumberOfOneOccupiedAndTwoVacantTriplet(junctionNo2,
                            latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray,
                            player, junctionsListFormingDualTriplet);
                }
                if (junctionNo3 != latestPlayerStoneJunctionNo) {
                    addJunctionNumberOfOneOccupiedAndTwoVacantTriplet(junctionNo3,
                            latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray,
                            player, junctionsListFormingDualTriplet);
                }
            }
        }
        return junctionsListFormingDualTriplet;
    }

    private void addJunctionNumberOfOneOccupiedAndTwoVacantTriplet(
            int junctionNo,
            int excludedJunctionNo,
            Junction junctionsArray[],
            FitTriplet fitTripletsArray[],
            String player, List<Integer> junctionsListFormingDualTriplet) {

        Triplet tripletsArray[] = fitTripletsArray[junctionNo].getTripletsArray();
        for (int i = 0; i < tripletsArray.length; i++) {
            Triplet triplet = tripletsArray[i];
            int junctionNo1 = triplet.getJunctionNo1();
            int junctionNo2 = triplet.getJunctionNo2();
            int junctionNo3 = triplet.getJunctionNo3();
            if (junctionNo1 != excludedJunctionNo
                    && junctionNo2 != excludedJunctionNo
                    && junctionNo3 != excludedJunctionNo) {

                // junctionNo is vacant, we know it. so other two junctions
                // should be occupied and vacant
                if (junctionNo1 == junctionNo) {
                    if (isVacant(junctionNo2, junctionsArray)) {
                        if (isOccupiedBy(junctionNo3, junctionsArray, player)) {
                            junctionsListFormingDualTriplet.add(junctionNo3);
                        }
                    } else if (isVacant(junctionNo3, junctionsArray)) {
                        if (isOccupiedBy(junctionNo2, junctionsArray, player)) {
                            junctionsListFormingDualTriplet.add(junctionNo2);
                        }
                    }
                }
                if (junctionNo2 == junctionNo) {
                    if (isVacant(junctionNo1, junctionsArray)) {
                        if (isOccupiedBy(junctionNo3, junctionsArray, player)) {
                            junctionsListFormingDualTriplet.add(junctionNo3);
                        }
                    } else if (isVacant(junctionNo3, junctionsArray)) {
                        if (isOccupiedBy(junctionNo1, junctionsArray, player)) {
                            junctionsListFormingDualTriplet.add(junctionNo1);
                        }
                    }
                }
                if (junctionNo3 == junctionNo) {
                    if (isVacant(junctionNo1, junctionsArray)) {
                        if (isOccupiedBy(junctionNo2, junctionsArray, player)) {
                            junctionsListFormingDualTriplet.add(junctionNo2);
                        }
                    } else if (isVacant(junctionNo2, junctionsArray)) {
                        if (isOccupiedBy(junctionNo1, junctionsArray, player)) {
                            junctionsListFormingDualTriplet.add(junctionNo1);
                        }
                    }
                }
            }
        }
    }

    public List<Triplet> getTwoOccupiedByComputerAndOneOccupiedByUserTripletsList(
            Junction junctionsArray[],
            FitTriplet fitTripletsArray[], String playerUser, String playerComputer) {
        List<Triplet> twoOccupiedByComputerAndOneOccupiedByUserTripletsList =
                new ArrayList<>();
        for (int i = 1; i < fitTripletsArray.length; i++) {
            Triplet tripletsArray[] = fitTripletsArray[i].getTripletsArray();
            for (int j = 0; j < tripletsArray.length; j++) {
                Triplet triplet = tripletsArray[j];
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();
                if (!isVacant(junctionNo1, junctionsArray)
                        && !isVacant(junctionNo2, junctionsArray)
                        && !isVacant(junctionNo3, junctionsArray)) {
                    if (junctionsArray[junctionNo1].getOccupiedBy().equals(playerUser)) {
                        if (junctionsArray[junctionNo2].getOccupiedBy().equals(playerComputer)) {
                            if (junctionsArray[junctionNo3].getOccupiedBy().equals(playerComputer)) {
                                addTripletToList(
                                        twoOccupiedByComputerAndOneOccupiedByUserTripletsList,
                                        triplet);
                            }
                        }
                    } else if (junctionsArray[junctionNo2].getOccupiedBy().equals(playerUser)) {
                        if (junctionsArray[junctionNo1].getOccupiedBy().equals(playerComputer)) {
                            if (junctionsArray[junctionNo3].getOccupiedBy().equals(playerComputer)) {
                                addTripletToList(
                                        twoOccupiedByComputerAndOneOccupiedByUserTripletsList,
                                        triplet);
                            }
                        }
                    } else if (junctionsArray[junctionNo3].getOccupiedBy().equals(playerUser)) {
                        if (junctionsArray[junctionNo2].getOccupiedBy().equals(playerComputer)) {
                            if (junctionsArray[junctionNo1].getOccupiedBy().equals(playerComputer)) {
                                addTripletToList(
                                        twoOccupiedByComputerAndOneOccupiedByUserTripletsList,
                                        triplet);
                            }
                        }
                    }
                }
            }
        }
        return twoOccupiedByComputerAndOneOccupiedByUserTripletsList;
    }

    /**
     * This method will return list of all junctionNo adjacent to triplet occupied by player
     * Returned list will contains vacant/non-vacant junctions including that of triplet
     * @param activeTripletsList
     * @param junctionsArray
     * @param player
     * @return
     */
    public List<Integer> getTripletsAdjacentJunctionNumbersList(List<Triplet> activeTripletsList,
                                                                Junction junctionsArray[],
                                                                String player) {
        List<Integer> tripletsAdjacentJunctionNumbersList = new ArrayList<>();
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            int junctionNo1 = triplet.getJunctionNo1();
            int junctionNo2 = triplet.getJunctionNo2();
            int junctionNo3 = triplet.getJunctionNo3();

            int junctionNumbersArray[] = {junctionNo1, junctionNo2, junctionNo3};

            for (int j = 0; j < junctionNumbersArray.length; j++) {
                int junctionNo = junctionNumbersArray[j];

                // check if given player held this triplet
                // (all 3 junctions will be occupied by same player)
                if (junctionsArray[junctionNo].getOccupiedBy().equals(player)) {
                    int adjacentJunctionNo;

                    // adjacentJunctionNo is +1, -1, +8, -8, +7, -7 with necessary validations

                    for (int k = 0; k < 3; k++) {
                        int distance = 1;

                        switch (k) {
                            case 1:
                                distance = 8;
                                break;
                            case 2:
                                distance = 7;
                                break;
                            default: ;
                        }

                        adjacentJunctionNo = junctionNo + distance;
                        if (isAdjacent(junctionNo, adjacentJunctionNo)) {
                            addIntegerToList(tripletsAdjacentJunctionNumbersList, adjacentJunctionNo);
                        }

                        adjacentJunctionNo = junctionNo - distance;
                        if (isAdjacent(junctionNo, adjacentJunctionNo)) {
                            addIntegerToList(tripletsAdjacentJunctionNumbersList, adjacentJunctionNo);
                        }
                    }
                }
            }
        }
        return tripletsAdjacentJunctionNumbersList;
    }
    
    private void addIntegerToList(List<Integer> integersList, int num) {
        if (!integersList.contains(num)) {
            integersList.add(num);
        }
    }
}
