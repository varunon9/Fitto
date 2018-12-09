package me.varunon9.fitto.computer;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.varunon9.fitto.oldversion.FitTriplet;
import me.varunon9.fitto.oldversion.Triplet;
import me.varunon9.fitto.game.Junction;
import me.varunon9.fitto.game.Player;

class ComputerLogic {

    private String TAG = "ComputerLogic";

    // at which position should computer place stone (guaranteed that computer has stones left)
    int getJunctionNoToPlaceStone(Junction junctionsArray[],
                                  FitTriplet fitTripletsArray[],
                                  Player playerComputer,
                                  Player playerUser,
                                  int latestUserStoneJunctionNo,
                                  int latestComputerStoneJunctionNo,
                                  List<Triplet> activeTripletsList) {
        int junctionNoToPlaceStone = -1;

        List<Integer> junctionsListToFitTripletComputer =
                getAllJunctionNumbersToFitTriplet(junctionsArray,
                        fitTripletsArray, playerComputer);
        List<Integer> junctionsListToFitTripletUser =
                getAllJunctionNumbersToFitTriplet(junctionsArray,
                        fitTripletsArray, playerUser);

        /**
         * junctionsListFormingDualTriplet and junctionsListToFitTriplet combined together
         * make dualTripletFit possible.
         */
        List<Integer> junctionsListToFitDualTripletUser =
                getAllJunctionNumbersToFitFutureDualTriplet(junctionsArray,
                        fitTripletsArray, playerUser, latestUserStoneJunctionNo);
        List<Integer> junctionsListToFitDualTripletComputer =
                getAllJunctionNumbersToFitFutureDualTriplet(junctionsArray,
                        fitTripletsArray, playerComputer, latestComputerStoneJunctionNo);
        List<Integer> junctionsListFormingDualTripletUser =
                getJunctionsListFormingDualTriplet(junctionsArray,
                        fitTripletsArray, playerUser, latestUserStoneJunctionNo);
        List<Integer> junctionsListFormingDualTripletComputer =
                getJunctionsListFormingDualTriplet(junctionsArray,
                        fitTripletsArray, playerComputer, latestComputerStoneJunctionNo);

        List<Triplet> twoOccupiedAndOneVacantTripletsListUser =
                getTwoOccupiedAndOneVacantTripletsList(junctionsArray,
                        fitTripletsArray, playerUser);
        List<Triplet> twoOccupiedAndOneVacantTripletsListComputer =
                getTwoOccupiedAndOneVacantTripletsList(junctionsArray,
                        fitTripletsArray, playerComputer);

        List<Triplet> twoOccupiedByComputerAndOneOccupiedByUserTripletsList =
                getTwoOccupiedByComputerAndOneOccupiedByUserTripletsList(junctionsArray,
                        fitTripletsArray, playerUser, playerComputer);

        List<Integer> tripletsAdjacentJunctionNumbersListComputer =
                getTripletsAdjacentJunctionNumbersList(activeTripletsList,
                        junctionsArray, playerComputer);

        // Fit a triplet, if such junction Exist
        if (!junctionsListToFitTripletComputer.isEmpty()) {
            Log.d(TAG, "computer fits a triplet");
            junctionNoToPlaceStone = junctionsListToFitTripletComputer.get(0);
        } else {

            // block user from making/forming/fitting a triplet
            if (!junctionsListToFitTripletUser.isEmpty()) {
                Log.d(TAG, "computer blocks user from making a triplet");

                // todo block best junction and not first
                junctionNoToPlaceStone = junctionsListToFitTripletUser.get(0);
            } else {

                /**
                 * block user from making a future dual triplet
                 * i.e. if user has stone at junction 1 and 5 (2, 3, 4 are vacant)
                 * then computer draws stone at 3
                 * similarly if user has stone at 1 and 10 (2, 3 , 18 are vacant)
                 * then computer draws stone at 2
                 */
                if (!junctionsListToFitDualTripletUser.isEmpty()) {
                    Log.d(TAG, "computer blocks user from making a future dual triplet");

                    // todo block best junction and not first
                    junctionNoToPlaceStone = junctionsListToFitDualTripletUser.get(0);
                } else if(!junctionsListToFitDualTripletComputer.isEmpty()) {
                    Log.d(TAG, "computer draw stone to make a future dual triplet");

                    // place a stone at junction where a dual triplet can be fit
                    // todo place stone at best junction
                    junctionNoToPlaceStone = junctionsListToFitDualTripletComputer.get(0);
                } else {
                    boolean placedStone = false;

                    // check if user placed stone at a corner
                    if (latestUserStoneJunctionNo % 2 == 1) {
                        int oppositeJunctionNo = latestUserStoneJunctionNo + 4;
                        if ((latestUserStoneJunctionNo > 4 && latestUserStoneJunctionNo < 8)
                                || (latestUserStoneJunctionNo > 12
                                && latestUserStoneJunctionNo < 16)
                                || (latestUserStoneJunctionNo > 20
                                && latestUserStoneJunctionNo < 24)) {

                            oppositeJunctionNo = latestUserStoneJunctionNo - 4;
                        }
                        if (isVacant(oppositeJunctionNo, junctionsArray)) {
                            Log.d(TAG, "computer places stone at opposite corner in " +
                                    "same square as that of user");
                            junctionNoToPlaceStone = oppositeJunctionNo;
                            placedStone = true;
                        }
                    }

                    if (!placedStone) {

                        // place stone starting from 1st and on odd position
                        for (int i = 1; i < junctionsArray.length; i += 2) {
                            if (isVacant(i, junctionsArray)) {
                                junctionNoToPlaceStone = i;
                                placedStone = true;
                                break;
                            }
                        }
                    }

                    if (!placedStone) {

                        // place stone starting from 1st and on even position (last option)
                        for (int i = 1; i < junctionsArray.length; i += 2) {
                            if (isVacant(i, junctionsArray)) {
                                junctionNoToPlaceStone = i;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return junctionNoToPlaceStone;
    }
    
    int getJunctionNoToEatUserStone(Junction junctionsArray[],
                                    FitTriplet fitTripletsArray[],
                                    Player playerComputer,
                                    Player playerUser,
                                    int latestUserStoneJunctionNo,
                                    int latestComputerStoneJunctionNo,
                                    List<Triplet> activeTripletsList) {
        int junctionNoToEatUserStone = -1;
        
        List<Triplet> twoOccupiedAndOneVacantTripletsListUser =
                getTwoOccupiedAndOneVacantTripletsList(junctionsArray,
                        fitTripletsArray, playerUser);
        List<Integer> junctionsListFormingDualTripletUser =
                getJunctionsListFormingDualTriplet(junctionsArray,
                        fitTripletsArray, playerUser, latestUserStoneJunctionNo);
        List<Triplet> twoOccupiedByComputerAndOneOccupiedByUserTripletsList =
                getTwoOccupiedByComputerAndOneOccupiedByUserTripletsList(junctionsArray,
                        fitTripletsArray, playerUser, playerComputer);
        List<Integer> tripletsAdjacentJunctionNumbersListComputer =
                getTripletsAdjacentJunctionNumbersList(activeTripletsList,
                        junctionsArray, playerComputer);

        // eat a stone from two stones triplet
        if (!twoOccupiedAndOneVacantTripletsListUser.isEmpty()) {
            for (int i = 0; i < twoOccupiedAndOneVacantTripletsListUser.size(); i++) {
                Triplet triplet = twoOccupiedAndOneVacantTripletsListUser.get(i);
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();
                if (isVacant(junctionNo1, junctionsArray)) {
                    if (!isPartOfTriplet(activeTripletsList, junctionNo2)) {
                        junctionNoToEatUserStone = junctionNo2;
                        break;
                    } else if (!isPartOfTriplet(activeTripletsList, junctionNo3)) {
                        junctionNoToEatUserStone = junctionNo3;
                        break;
                    }
                } else if (isVacant(junctionNo2, junctionsArray)) {
                    if (!isPartOfTriplet(activeTripletsList, junctionNo1)) {
                        junctionNoToEatUserStone = junctionNo1;
                        break;
                    } else if (!isPartOfTriplet(activeTripletsList, junctionNo3)) {
                        junctionNoToEatUserStone = junctionNo3;
                        break;
                    }
                } else if (isVacant(junctionNo3, junctionsArray)) {
                    if (!isPartOfTriplet(activeTripletsList, junctionNo1)) {
                        junctionNoToEatUserStone = junctionNo1;
                        break;
                    } else if (!isPartOfTriplet(activeTripletsList, junctionNo2)) {
                        junctionNoToEatUserStone = junctionNo2;
                        break;
                    }
                }
            }
        } else if(!twoOccupiedByComputerAndOneOccupiedByUserTripletsList.isEmpty()) {

            // eat a stone from triplets having two junctions occupied by computer and one by user
            for (int i = 0; i < twoOccupiedByComputerAndOneOccupiedByUserTripletsList.size(); i++) {
                Triplet triplet = twoOccupiedByComputerAndOneOccupiedByUserTripletsList.get(i);
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();

                // stone junctionNo to be eaten by computer
                int junctionNo = junctionNo1;
                if (junctionsArray[junctionNo2].getOccupiedBy() == playerUser) {
                    junctionNo = junctionNo2;
                } else if (junctionsArray[junctionNo3].getOccupiedBy() == playerUser) {
                    junctionNo = junctionNo3;
                }
                if (!isPartOfTriplet(activeTripletsList, junctionNo)) {
                    junctionNoToEatUserStone = junctionNo;
                    break;
                }
            }

        } else  if (!junctionsListFormingDualTripletUser.isEmpty()) {

            // eat a stone which will be part of dual triplet in future
            for (int i = 0; i < junctionsListFormingDualTripletUser.size(); i++) {
                int junctionNo = junctionsListFormingDualTripletUser.get(i);
                if (!isPartOfTriplet(activeTripletsList, junctionNo)) {
                    junctionNoToEatUserStone = junctionNo;
                    break;
                }
            }
        } else {

            // eat latestUserStoneJunctionNo
            if (!isPartOfTriplet(activeTripletsList, latestUserStoneJunctionNo)) {
                junctionNoToEatUserStone = latestUserStoneJunctionNo;
            } else {

                // eat a stone adjacent to junctions where computer formed a triplet
                if (!tripletsAdjacentJunctionNumbersListComputer.isEmpty()) {
                    for (int i = 0;
                         i < tripletsAdjacentJunctionNumbersListComputer.size(); i++) {
                        int adjacentJunctionNo =
                                tripletsAdjacentJunctionNumbersListComputer.get(i);

                        // check if it is not vacant and not occupied by computer
                        if (junctionsArray[adjacentJunctionNo].getOccupiedBy() != null
                                && junctionsArray[adjacentJunctionNo].getOccupiedBy() != playerComputer) {
                            if (!isPartOfTriplet(activeTripletsList,
                                    adjacentJunctionNo)) {

                                junctionNoToEatUserStone = adjacentJunctionNo;
                                break;
                            }
                        }
                    }
                }
            }
        }
        if (junctionNoToEatUserStone == -1) {

            // eat 1st eligible user stone starting from junctionNo 1
            for (int i = 1; i < junctionsArray.length; i++) {
                Junction junction = junctionsArray[i];
                if (junction.getOccupiedBy() != null
                        && junction.getOccupiedBy() == playerUser) {
                    if (!isPartOfTriplet(activeTripletsList, i)) {
                        junctionNoToEatUserStone = i;
                        break;
                    }
                }
            }
        }
        
        return junctionNoToEatUserStone;
    }

    int[] getPickAndPlaceJunctionArray(Junction junctionsArray[],
                                       FitTriplet fitTripletsArray[],
                                       Player playerComputer,
                                       Player playerUser,
                                       int latestUserStoneJunctionNo,
                                       int latestComputerStoneJunctionNo,
                                       List<Triplet> activeTripletsList) {
        int pickAndPlaceJunctionArray[] = new int[2];
        boolean placedStone = false;

        List<Triplet> twoOccupiedAndOneVacantTripletsListComputer =
                getTwoOccupiedAndOneVacantTripletsList(junctionsArray,
                        fitTripletsArray, playerComputer);
        List<Triplet> twoOccupiedAndOneVacantTripletsListUser =
                getTwoOccupiedAndOneVacantTripletsList(junctionsArray,
                        fitTripletsArray, playerUser);

        // make triplet if possible
        if (!twoOccupiedAndOneVacantTripletsListComputer.isEmpty()) {
            for (int i = 0;
                 i < twoOccupiedAndOneVacantTripletsListComputer.size() && !placedStone; i++) {
                Triplet triplet = twoOccupiedAndOneVacantTripletsListComputer.get(i);
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();

                // getting vacant junction number
                int vacantJunctionNo;
                if (isVacant(junctionNo1, junctionsArray)) {
                    vacantJunctionNo = junctionNo1;
                } else if (isVacant(junctionNo2, junctionsArray)) {
                    vacantJunctionNo = junctionNo2;
                } else {
                    vacantJunctionNo = junctionNo3;
                }

                // get list of all junctions adjacent to vacant
                List<Integer> adjacentJunctionNumbersList =
                        getJunctionAdjacentJunctionNumbersList(vacantJunctionNo);
                for (int j = 0; j < adjacentJunctionNumbersList.size(); j++) {
                    int adjacentJunction = adjacentJunctionNumbersList.get(j);

                    // if this junction is not vacant,
                    // not triplet's part and not by user occupied
                    if (!isVacant(adjacentJunction, junctionsArray)) {
                        if (adjacentJunction != junctionNo1
                                && adjacentJunction != junctionNo2
                                && adjacentJunction != junctionNo3) {
                            if (junctionsArray[adjacentJunction].getOccupiedBy() == playerComputer) {

                                // pick stone at this junctionNo
                                pickAndPlaceJunctionArray[0] = adjacentJunction;

                                // place it at vacant position
                                pickAndPlaceJunctionArray[1] = vacantJunctionNo;

                                placedStone = true;
                                Log.d(TAG, "computer formed triplet by pick and move at " +
                                        "junction No: " + vacantJunctionNo);
                                break;
                            }
                        }
                    }
                }
            }
        } else if (!twoOccupiedAndOneVacantTripletsListUser.isEmpty()) {

            // block user from making triplet if user has such opportunity in next move
            for (int i = 0;
                 i < twoOccupiedAndOneVacantTripletsListUser.size() && !placedStone; i++) {
                Triplet triplet = twoOccupiedAndOneVacantTripletsListUser.get(i);
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();

                // getting vacant junction number
                int vacantJunctionNo;
                if (isVacant(junctionNo1, junctionsArray)) {
                    vacantJunctionNo = junctionNo1;
                } else if (isVacant(junctionNo2, junctionsArray)) {
                    vacantJunctionNo = junctionNo2;
                } else {
                    vacantJunctionNo = junctionNo3;
                }

                // get list of all junctions adjacent to vacant
                List<Integer> adjacentJunctionNumbersList =
                        getJunctionAdjacentJunctionNumbersList(vacantJunctionNo);
                for (int j = 0; j < adjacentJunctionNumbersList.size(); j++) {
                    int adjacentJunction = adjacentJunctionNumbersList.get(j);

                    // if this junction is not vacant and not by user occupied
                    // todo move if it is computer's triplet part??
                    if (!isVacant(adjacentJunction, junctionsArray)) {

                        if (junctionsArray[adjacentJunction].getOccupiedBy() == playerComputer) {

                            // pick stone at this junctionNo
                            pickAndPlaceJunctionArray[0] = adjacentJunction;

                            // place it at vacant position
                            pickAndPlaceJunctionArray[1] = vacantJunctionNo;

                            placedStone = true;
                            Log.d(TAG, "computer blocked user's triplet by pick and move at " +
                                    "junction No: " + vacantJunctionNo);
                            break;
                        }
                    }
                }
            }
        } else if (!activeTripletsList.isEmpty()) {

            // break a triplet if user is not going to block it in next move and
            // user is not going to form a triplet in next move

            // proceed if user is going to form a triplet in next move
            for (int i = 0; i < activeTripletsList.size() && !placedStone; i++) {
                Triplet triplet = activeTripletsList.get(i);
                int junctionNo1 = triplet.getJunctionNo1();
                int junctionNo2 = triplet.getJunctionNo2();
                int junctionNo3 = triplet.getJunctionNo3();

                // check if this triplet is occupied by computer else skip
                if (junctionsArray[junctionNo1].getOccupiedBy() == playerUser) {
                    continue;
                }

                int junctionNumbersArray[] = {junctionNo1, junctionNo2, junctionNo3};

                for (int j = 0; j < junctionNumbersArray.length; j++) {
                    int junctionNo = junctionNumbersArray[j];

                    // get list of all junctions adjacent to junctionNo
                    List<Integer> adjacentJunctionNumbersList =
                            getJunctionAdjacentJunctionNumbersList(junctionNo);

                    // -1 means no vacant junction found
                    int vacantJunctionNo = -1;
                    boolean userCanBlockTriplet = false;
                    for (int k = 0; k < adjacentJunctionNumbersList.size(); k++) {
                        int adjacentJunctionNo = adjacentJunctionNumbersList.get(k);

                        // if this junction is vacant
                        if (isVacant(adjacentJunctionNo, junctionsArray)) {
                            vacantJunctionNo = adjacentJunctionNo;
                        } else {

                            // if any adjacent junction is occupied by user
                            // then he can block triplet
                            if (junctionsArray[adjacentJunctionNo].getOccupiedBy() == playerUser) {
                                userCanBlockTriplet = true;
                            }
                        }
                    }

                    if (!userCanBlockTriplet && vacantJunctionNo != -1) {

                        // pick stone at this junctionNo
                        pickAndPlaceJunctionArray[0] = junctionNo;

                        // place it at vacant position
                        pickAndPlaceJunctionArray[1] = vacantJunctionNo;

                        Log.d(TAG, "computer blocked user's triplet by pick and move at " +
                                "junction No: " + vacantJunctionNo);
                        placedStone = true;
                        break;
                    }
                }
            }
        }

        // move a random stone
        if (!placedStone) {
            for (int i = 1; i < junctionsArray.length && !placedStone; i++) {

                // not vacant and occupied by computer
                if (!isVacant(i, junctionsArray)) {
                    if (junctionsArray[i].getOccupiedBy() == playerComputer) {

                        // find list of junctions adjacent to this junctionNo
                        List<Integer> adjacentJunctionNumbersList =
                                getJunctionAdjacentJunctionNumbersList(i);
                        for (int j = 0; j < adjacentJunctionNumbersList.size(); j++) {
                            int adjacentJunctionNo = adjacentJunctionNumbersList.get(j);

                            // if this junction is vacant
                            if (isVacant(adjacentJunctionNo, junctionsArray)) {

                                // pick stone at this junctionNo
                                pickAndPlaceJunctionArray[0] = i;

                                // place it at vacant position
                                pickAndPlaceJunctionArray[1] = adjacentJunctionNo;

                                placedStone = true;
                                Log.d(TAG, "computer randomly placed a stone at " +
                                        "junction No: " + adjacentJunctionNo);
                                break;

                            }
                        }
                    }
                }
            }
        }
        
        return pickAndPlaceJunctionArray;
    }

    /**
     * @param junctionsArray
     * @param fitTripletsArray
     * @param player
     * @return list of all junctionNos where by drawing a stone, a triplet will be fit
     */
    private List<Integer> getAllJunctionNumbersToFitTriplet(Junction junctionsArray[],
                                                           FitTriplet fitTripletsArray[],
                                                           Player player) {
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

    private boolean isVacant(int junctionNo, Junction junctionsArray[]) {
        if (junctionsArray[junctionNo].getOccupiedBy() == null) {
            return true;
        }
        return false;
    }

    private boolean isBothJunctionsOccupiedByPlayer(Junction junctionsArray[], Player player,
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

    private boolean isOccupiedBy(int junctionNo, Junction junctionsArray[], Player player) {
        if (junctionsArray[junctionNo].getOccupiedBy() != null
                && junctionsArray[junctionNo].getOccupiedBy() == player) {
            return true;
        }
        return false;
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
    private List<Integer> getAllJunctionNumbersToFitFutureDualTriplet(
            Junction junctionsArray[],
            FitTriplet fitTripletsArray[], Player player, int latestPlayerStoneJunctionNo) {

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
                            || !isVacant(junctionNo3, junctionsArray)) {
                        continue;
                    }
                } else if (junctionNo2 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo1, junctionsArray)
                            || !isVacant(junctionNo3, junctionsArray)) {
                        continue;
                    }
                } else if (junctionNo3 == latestPlayerStoneJunctionNo) {
                    if (!isVacant(junctionNo1, junctionsArray)
                            || !isVacant(junctionNo2, junctionsArray)) {
                        continue;
                    }
                }
                if (junctionNo1 != latestPlayerStoneJunctionNo) {
                    if (areTripletsOneOccupiedAndTwoVacant(junctionNo1, latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray, player)) {
                        addIntegerToList(junctionNumbersList, junctionNo1);
                    }
                }
                if (junctionNo2 != latestPlayerStoneJunctionNo) {
                    if (areTripletsOneOccupiedAndTwoVacant(junctionNo2, latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray, player)) {
                        addIntegerToList(junctionNumbersList, junctionNo2);
                    }
                }
                if (junctionNo3 != latestPlayerStoneJunctionNo) {
                    if (areTripletsOneOccupiedAndTwoVacant(junctionNo3, latestPlayerStoneJunctionNo,
                            junctionsArray, fitTripletsArray, player)) {
                        addIntegerToList(junctionNumbersList, junctionNo3);
                    }
                }
            }
        }
        return junctionNumbersList;
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
                                                       Player player) {
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

    private void addIntegerToList(List<Integer> integersList, int num) {
        if (!integersList.contains(num)) {
            integersList.add(num);
        }
    }

    /**
     * @param junctionsArray
     * @param fitTripletsArray
     * @param player
     * @return list of all junction numbers which is occupied by given player and which will be
     * part of dual triplet in future
     */
    private List<Integer> getJunctionsListFormingDualTriplet(Junction junctionsArray[],
                                                            FitTriplet fitTripletsArray[],
                                                            Player player,
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
            Player player, List<Integer> junctionsListFormingDualTriplet) {

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

    public List<Triplet> getTwoOccupiedAndOneVacantTripletsList(Junction junctionsArray[],
                                                                FitTriplet fitTripletsArray[],
                                                                Player player) {
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

    private List<Triplet> getTwoOccupiedByComputerAndOneOccupiedByUserTripletsList(
            Junction junctionsArray[],
            FitTriplet fitTripletsArray[], Player playerUser, Player playerComputer) {
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
                    if (junctionsArray[junctionNo1].getOccupiedBy() == playerUser) {
                        if (junctionsArray[junctionNo2].getOccupiedBy() == playerComputer) {
                            if (junctionsArray[junctionNo3].getOccupiedBy() == playerComputer) {
                                addTripletToList(
                                        twoOccupiedByComputerAndOneOccupiedByUserTripletsList,
                                        triplet);
                            }
                        }
                    } else if (junctionsArray[junctionNo2].getOccupiedBy() == playerUser) {
                        if (junctionsArray[junctionNo1].getOccupiedBy() == playerComputer) {
                            if (junctionsArray[junctionNo3].getOccupiedBy() == playerComputer) {
                                addTripletToList(
                                        twoOccupiedByComputerAndOneOccupiedByUserTripletsList,
                                        triplet);
                            }
                        }
                    } else if (junctionsArray[junctionNo3].getOccupiedBy() == playerUser) {
                        if (junctionsArray[junctionNo2].getOccupiedBy() == playerComputer) {
                            if (junctionsArray[junctionNo1].getOccupiedBy() == playerComputer) {
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
    private List<Integer> getTripletsAdjacentJunctionNumbersList(List<Triplet> activeTripletsList,
                                                                Junction junctionsArray[],
                                                                Player player) {
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
                if (junctionsArray[junctionNo].getOccupiedBy() == player) {
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

    private boolean isAdjacent(int junctionNo1, int junctionNo2) {

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

    private boolean isPartOfTriplet(List<Triplet> activeTripletsList, int junctionNo) {
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

    private List<Integer> getJunctionAdjacentJunctionNumbersList(int junctionNo) {
        List<Integer> junctionAdjacentJunctionNumbersList = new ArrayList<>();
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
                addIntegerToList(junctionAdjacentJunctionNumbersList, adjacentJunctionNo);
            }

            adjacentJunctionNo = junctionNo - distance;
            if (isAdjacent(junctionNo, adjacentJunctionNo)) {
                addIntegerToList(junctionAdjacentJunctionNumbersList, adjacentJunctionNo);
            }
        }
        return junctionAdjacentJunctionNumbersList;
    }
}
