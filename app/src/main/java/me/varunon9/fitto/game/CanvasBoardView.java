package me.varunon9.fitto.game;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.List;

import me.varunon9.fitto.oldversion.FitTriplet;
import me.varunon9.fitto.R;
import me.varunon9.fitto.oldversion.Triplet;

/**
 * Game Board View, will be used for both mode- online as well as vs computer
 */
public abstract class CanvasBoardView extends View {

    private Context context;
    private Canvas canvas;
    private String TAG = "CanvasBoardView";

    protected Player player1;
    protected Player player2;
    protected Player winner;

    private Vibrator vibrator;

    private Paint boardPaint;

    private Paint tripletPaint;

    private int canvasMargin = 20;

    // innerMost square has side 2 * unitLength;
    // initialised while building junctionsArray
    private int unitLength;

    protected Junction junctionsArray[];

    private Path boardPath; // will be initialized in `onSizeChanged`

    protected FitTriplet fitTripletsArray[];

    protected List<Triplet> activeTripletsList;

    protected String instructionMessage;

    public CanvasBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.context = context;

        boardPaint = new Paint();
        boardPaint.setAntiAlias(true);
        boardPaint.setDither(true);
        boardPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeJoin(Paint.Join.ROUND);
        boardPaint.setStrokeWidth(4);

        tripletPaint = new Paint();
        tripletPaint.setAntiAlias(true);
        tripletPaint.setStyle(Paint.Style.STROKE);
        tripletPaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        tripletPaint.setStrokeWidth(5);

        // 24 junctions starting from 1 to 24
        junctionsArray = new Junction[25];

        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    protected void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    protected void setPlayer2(Player player2) {
        this.player2 = player2;
    }

    protected void setBoardPaint(Paint boardPaint) {
        if (boardPaint != null) {
            this.boardPaint = boardPaint;
        }
    }

    public void setTripletPaint(Paint tripletPaint) {
        if (tripletPaint != null) {
            this.tripletPaint = tripletPaint;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) { // will be called only one time
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged called");

        // build junctionsArray
        buildJunctionsArray(junctionsArray, w, h, canvasMargin);
        boardPath = getBoardPath(junctionsArray);
        fitTripletsArray = getFitTripletsArray();
        restartGame();
    }

    @Override
    protected void onDraw(Canvas canvas) { // will be called after each invalidate
        super.onDraw(canvas);
        Log.d(TAG, "on draw called");
        this.canvas = canvas;
        drawBoard();
        placeStones();
        paintTriplets();
        drawMessage();
    }

    protected abstract void restartGame();

    private Path getBoardPath(Junction junctionsArray[]) {
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

    private void buildJunctionsArray(Junction junctionsArray[], int width,
                                     int height, int margin) {

        // leaving 10 + 10 dp margin
        width -= 2 * margin;

        /*
          Structure of BoardView-
          Board has 3 square namely outerSquare, middleSquare and innerSquare
          innerSquare has side = 2 * unitLength
          MiddleSquare has side = 4 * unitLength;
          OuterSquare has side = 6 * unitLength;
         */
        unitLength = width / 12;
        int outerSquareSide = 12 * unitLength;
        unitLength = outerSquareSide / 6;

        /*
          x and y are first coordinates of outerSquare
          x and y will not be 0, 0 because we have left some margin as well as
          outerSquareSide is multiple of 12 so x might get decreased further
         */

        int y = margin;
        int x = margin + (width - outerSquareSide) / 2;

        // initialising junctions 1 to 8
        initializeJunctions(junctionsArray, 1, x, y, 3 * unitLength);

        // initialising junctions 9 to 16
        initializeJunctions(junctionsArray, 9,
                (x + unitLength), (y + unitLength), 2 * unitLength);

        // initialising junctions 17 to 24
        initializeJunctions(junctionsArray, 17, (x + 2 * unitLength),
                (y + 2 * unitLength), 1 * unitLength);

    }

    private void initializeJunctions(Junction junctionsArray[], int startIndex,
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

    private void drawBoard() {
        canvas.drawPath(boardPath, boardPaint);
    }

    private void placeStones() {
        for (int i = 1; i < junctionsArray.length; i++) {
            Junction junction = junctionsArray[i];
            Player occupiedBy = junction.getOccupiedBy();
            if (occupiedBy == null) {
                continue;
            }
            int x = junctionsArray[i].getX();
            int y = junctionsArray[i].getY();

            drawStone(occupiedBy, x, y);
        }
    }

    protected void drawStone(Player player, int x, int y) {
        canvas.drawCircle(x, y, 20, player.getStonePaint());
    }

    private void paintTriplets() {
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            paintTriplet(triplet);
        }
    }

    private void paintTriplet(Triplet triplet) {
        int junctionNo1 = triplet.getJunctionNo1();
        int junctionNo2 = triplet.getJunctionNo2();
        int junctionNo3 = triplet.getJunctionNo3();

        Path tripletPath = new Path();
        tripletPath.moveTo(junctionsArray[junctionNo1].getX(), junctionsArray[junctionNo1].getY());
        tripletPath.lineTo(junctionsArray[junctionNo2].getX(), junctionsArray[junctionNo2].getY());
        tripletPath.lineTo(junctionsArray[junctionNo3].getX(), junctionsArray[junctionNo3].getY());

        canvas.drawPath(tripletPath, tripletPaint);
    }

    private void drawMessage() {
        Paint displayMessagePaint = new Paint();
        displayMessagePaint.setAntiAlias(true);
        displayMessagePaint.setTextAlign(Paint.Align.CENTER);
        displayMessagePaint.setTextSize(2 * canvasMargin);
        displayMessagePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));

        Paint scoreMessagePaint = new Paint();
        scoreMessagePaint.setAntiAlias(true);
        scoreMessagePaint.setTextAlign(Paint.Align.CENTER);
        scoreMessagePaint.setTextSize((float) 1.5 * canvasMargin);
        scoreMessagePaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));

        Paint instructionMessagePaint = new Paint();
        instructionMessagePaint.setAntiAlias(true);
        instructionMessagePaint.setTextAlign(Paint.Align.CENTER);
        instructionMessagePaint.setTextSize((float) 1.5 * canvasMargin);
        instructionMessagePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));

        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int outerSquareSideLength = 6 * unitLength;
        int x = width / 2;

        // y1 is for displayMessage
        int y1 = outerSquareSideLength + (height - canvasMargin - outerSquareSideLength) / 2;

        // y2 is for score Message
        int y2 = y1 + (height - canvasMargin - outerSquareSideLength) / 4;

        // y3 is for instruction message
        int y3 = y2 + (height - canvasMargin - outerSquareSideLength) / 4;

        y1 = y1 - (int) (displayMessagePaint.ascent() + displayMessagePaint.descent()) / 2;
        y2 = y2 - (int) (scoreMessagePaint.ascent() + scoreMessagePaint.descent()) / 2;
        y3 = y3 - (int) (instructionMessagePaint.ascent() + instructionMessagePaint.descent()) / 2;

        String displayMessage;
        String playerName;
        if (player1.isTurn()) {
            playerName = player1.getName();
        } else {
            playerName = player2.getName();
        }
        if (playerName.equalsIgnoreCase("you")) {
            displayMessage = "Your Turn";
        } else {
            displayMessage = playerName + "'s Turn";
        }

        if (winner != null) {
            displayMessage = winner.getName() + " won!!";
        }
        canvas.drawText(displayMessage, x, y1, displayMessagePaint);

        String scoreMessage = "stones left("
                + player1.getName()
                + "): "
                + player1.getStonesLeft()
                + ", stones left("
                + player2.getName()
                + "): "
                + player2.getStonesLeft();
        canvas.drawText(displayMessage, x, y1, displayMessagePaint);
        canvas.drawText(scoreMessage, x, y2, scoreMessagePaint);

        if (instructionMessage != null && !instructionMessage.isEmpty()) {
            canvas.drawText(instructionMessage, x, y3, instructionMessagePaint);
        }
    }

    /**
     * @return an array of array, each corner junction/position will hold three triplets
     * while middle junction/position will hold 2 triplets of which they are part of
     */
    private FitTriplet[] getFitTripletsArray() {

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

    private Triplet buildTriplet(int junctionNo1, int junctionNo2, int junctionNo3) {
        Triplet triplet = new Triplet();
        triplet.setJunctionNo1(junctionNo1);
        triplet.setJunctionNo2(junctionNo2);
        triplet.setJunctionNo3(junctionNo3);
        triplet.setActive(false);
        return triplet;
    }

    protected int getTouchedJunction(float x, float y) {

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

    /**
     * This method checks if newly made move fit any triplet
     * @param junctionNo
     * @param player
     * @return
     */
    protected boolean isTripletFit(int junctionNo, Player player) {
        Triplet tripletsArray[] = fitTripletsArray[junctionNo].getTripletsArray();
        for (int i = 0; i < tripletsArray.length; i++) {
            Triplet triplet = tripletsArray[i];
            int junctionNo1 = triplet.getJunctionNo1();
            int junctionNo2 = triplet.getJunctionNo2();
            int junctionNo3 = triplet.getJunctionNo3();
            if (junctionsArray[junctionNo1].getOccupiedBy() != null &&
                    junctionsArray[junctionNo1].getOccupiedBy() == player) {
                if (junctionsArray[junctionNo2].getOccupiedBy() != null &&
                        junctionsArray[junctionNo2].getOccupiedBy() == player) {
                    if (junctionsArray[junctionNo3].getOccupiedBy() != null &&
                            junctionsArray[junctionNo3].getOccupiedBy() == player) {
                        triplet.setActive(true);

                        // will add triplet to activeList if it is not already added
                        addTripletToActiveTripletsList(triplet);
                        Log.d(TAG, "Triplet formed at junctionNo " + junctionNo + " by " + player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void addTripletToActiveTripletsList(Triplet tripletToBeAdded) {
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            if (triplet.equals(tripletToBeAdded)) {
                return;
            }
        }
        activeTripletsList.add(tripletToBeAdded);
    }

    protected boolean canEatPlayerStone(Player player) {
        for (int i = 1; i < junctionsArray.length; i++) {
            if (junctionsArray[i].getOccupiedBy() != null
                    && junctionsArray[i].getOccupiedBy() == player) {
                if (!isPartOfTriplet(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isPartOfTriplet(int junctionNo) {
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

    protected void playVibration(long duration) {
        try {
            vibrator.vibrate(duration);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void disableTripletFit(int junctionNo) {
        Triplet tripletsArray[] = fitTripletsArray[junctionNo].getTripletsArray();
        for (int i = 0; i < tripletsArray.length; i++) {
            Triplet triplet = tripletsArray[i];
            triplet.setActive(false);

            // will remove it from activeList if it is present
            removeTripletFromActiveTripletsList(triplet);
        }
    }

    private void removeTripletFromActiveTripletsList(Triplet tripletToBeRemoved) {
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            if (triplet.equals(tripletToBeRemoved)) {
                activeTripletsList.remove(i);
            }
        }
    }

    protected boolean isAdjacent(int junctionNo1, int junctionNo2) {

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
}
