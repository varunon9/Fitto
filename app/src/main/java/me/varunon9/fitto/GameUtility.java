package me.varunon9.fitto;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

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

    public int getUserTouchedJunction(float x, float y, Junction junctionsArray[], int unitLength) {

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
        triplet.setCredited(false);
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
            if ((junctionNo1 == 8 && junctionNo2 != 9)
                    || (junctionNo1 == 9 && junctionNo2 != 8)) {
                return true;
            }
            if ((junctionNo1 == 16 && junctionNo2 != 17)
                    || (junctionNo1 == 17 && junctionNo2 != 16)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNotPartOfTriplet(FitTriplet fitTripletsArray[], int junctionNo) {
        Triplet tripletsArray[] = fitTripletsArray[junctionNo].getTripletsArray();
        for (int i = 0; i < tripletsArray.length; i++) {
            if (tripletsArray[i].isActive()) {
                return false;
            }
        }
        return true;
    }
}
