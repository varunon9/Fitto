package me.varunon9.fitto;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by varun on 20/8/17.
 */

public class CanvasBoardView extends View {

    private Paint userStonePaint;
    private Paint computerStonePaint;
    private Paint pickedStonePaint;
    private Paint boardPaint;
    private Path boardPath;
    private Path tripletPath;
    private Paint displayMessagePaint;
    private Paint tripletPaint;
    private Paint scoreMessagePaint;
    private Canvas canvas;
    private Junction junctionsArray[];
    private FitTriplet fitTripletsArray[];
    private String playerUser;
    private String playerComputer;
    private String computerStoneColor;
    private String userStoneColor;
    private int maximumInitialStones;
    private int userStonesLeft;
    private int computerStonesLeft;
    private int canvasMargin;
    private int stoneRadius;
    private boolean userTurn;
    private String displayMessage;
    private int computerHealth;
    private int userHealth;
    private boolean gameWon;
    private String gameStatus;
    private int pickedStoneJunctionNo;
    private int latestUserStoneJunctionNo;
    private int latestComputerStoneJunctionNo;
    private List<Triplet> activeTripletsList;
    private Context context;

    private static final String TAG = "CanvasBoardView";

    // settings preferences
    private SharedPreferences settingsPreferences;

    // innerMost square has side 2 * unitLength;
    // initialised while building junctionsArray
    private int unitLength;

    private GameUtility gameUtility;

    private Vibrator vibrator;
    private MediaPlayer mediaPlayer;

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CanvasBoardView, 0, 0);
        playerUser = typedArray.getString(R.styleable.CanvasBoardView_playerUser);
        playerComputer = typedArray.getString(R.styleable.CanvasBoardView_playerComputer);
        computerStoneColor = typedArray.getString(R.styleable.CanvasBoardView_computerStoneColor);
        userStoneColor = typedArray.getString(R.styleable.CanvasBoardView_userStoneColor);
        maximumInitialStones =
                typedArray.getInt(R.styleable.CanvasBoardView_maximumInitialStones, 8);
        stoneRadius = typedArray.getInt(R.styleable.CanvasBoardView_stoneRadius, 20);
        canvasMargin = typedArray.getInt(R.styleable.CanvasBoardView_margin, 20);
        userStonePaint = new Paint();
        userStonePaint.setAntiAlias(true);
        userStonePaint.setDither(true);
        userStonePaint.setColor(Color.parseColor(userStoneColor));
        userStonePaint.setStrokeWidth(stoneRadius);

        computerStonePaint = new Paint();
        computerStonePaint.setAntiAlias(true);
        computerStonePaint.setDither(true);
        computerStonePaint.setColor(Color.parseColor(computerStoneColor));
        computerStonePaint.setStrokeWidth(stoneRadius);

        pickedStonePaint = new Paint();
        pickedStonePaint.setAntiAlias(true);
        pickedStonePaint.setDither(true);
        pickedStonePaint.setColor(Color.parseColor("#8C8C8C"));
        pickedStonePaint.setStrokeWidth(stoneRadius);

        boardPaint = new Paint();
        boardPaint.setAntiAlias(true);
        boardPaint.setDither(true);
        boardPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeJoin(Paint.Join.ROUND);
        boardPaint.setStrokeWidth(4);

        tripletPath = new Path();

        displayMessagePaint = new Paint();
        displayMessagePaint.setAntiAlias(true);
        displayMessagePaint.setTextAlign(Paint.Align.CENTER);
        displayMessagePaint.setTextSize(2 * canvasMargin);
        displayMessagePaint.setColor(Color.parseColor(computerStoneColor));

        scoreMessagePaint = new Paint();
        scoreMessagePaint.setAntiAlias(true);
        scoreMessagePaint.setTextAlign(Paint.Align.CENTER);
        scoreMessagePaint.setTextSize((float) 1.5 * canvasMargin);
        scoreMessagePaint.setColor(Color.parseColor(userStoneColor));

        tripletPaint = new Paint();
        tripletPaint.setAntiAlias(true);
        tripletPaint.setStyle(Paint.Style.STROKE);
        tripletPaint.setColor(Color.parseColor(computerStoneColor));
        tripletPaint.setStrokeWidth(5);

        typedArray.recycle();

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // 24 junctions starting from 1 to 24
        junctionsArray = new Junction[25];

        gameUtility = new GameUtility();
        this.context = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(context, R.raw.served);
    }

    public CanvasBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // disable hardware acceleration
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        init(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                userTouchesBoard(x, y);
                break;
            }
        }
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG, "onSizeChanged called");

        // build junctionsArray
        buildJunctionsArray(junctionsArray, w, h, canvasMargin);
        boardPath = gameUtility.getBoardPath(junctionsArray);
        restartGame();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "on draw called");
        this.canvas = canvas;
        drawBoard();
        placeStones(junctionsArray, canvas);
        paintTriplets();
        drawMessage();
        if (gameWon) {
            return;
        }
        if (!userTurn) {
            playComputer();
        }
    }

    public void initialiseButtons(Button restartButton, Button undoButton) {
        restartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });

        undoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                undoMove();
            }
        });
    }


    private void buildJunctionsArray(Junction junctionsArray[], int width,
                                     int height, int margin) {

        // leaving 10 + 10 dp margin
        width -= 2 * margin;

        /**
         * Structure of BoardView-
         * Board has 3 square namely outerSquare, middleSquare and innerSquare
         * innerSquare has side = 2 * unitLength
         * MiddleSquare has side = 4 * unitLength;
         * OuterSquare has side = 6 * unitLength;
         */
        unitLength = width / 12;
        int outerSquareSide = 12 * unitLength;
        unitLength = outerSquareSide / 6;

        /**
         * x and y are first coordinates of outerSquare
         * x and y will not be 0, 0 because we have left some margin as well as
         * outerSquareSide is multiple of 12 so x might get decreased further
         */

        int y = margin;
        int x = margin + (width - outerSquareSide) / 2;

        // initialising junctions 1 to 8
        gameUtility.initializeJunctions(junctionsArray, 1, x, y, 3 * unitLength);

        // initialising junctions 9 to 16
        gameUtility.initializeJunctions(junctionsArray, 9,
                (x + unitLength), (y + unitLength), 2 * unitLength);

        // initialising junctions 17 to 24
        gameUtility.initializeJunctions(junctionsArray, 17, (x + 2 * unitLength),
                (y + 2 * unitLength), 1 * unitLength);

    }

    private void placeStones(Junction junctionsArray[], Canvas canvas) {
        for (int i = 1; i < junctionsArray.length; i++) {
            Junction junction = junctionsArray[i];
            String occupiedBy = junction.getOccupiedBy();
            if (occupiedBy == null) {
                continue;
            }
            int x = junctionsArray[i].getX();
            int y = junctionsArray[i].getY();

            if (occupiedBy.equals(playerUser)) {
                drawUserStone(canvas, x, y);
            } else if (occupiedBy.equals(playerComputer)) {
                drawComputerStone(canvas, x, y);
            }

            // if picked stone, draw it common
            if (junction.isPicked()) {
                drawStone(canvas, x, y, pickedStonePaint);
            }
        }
    }

    private void drawComputerStone(Canvas canvas, int x, int y) {
        drawStone(canvas, x, y, computerStonePaint);
    }

    private void drawUserStone(Canvas canvas, int x, int y) {
        drawStone(canvas, x, y, userStonePaint);
    }

    private void drawStone(Canvas canvas, int x, int y, Paint paint) {
        canvas.drawCircle(x, y, stoneRadius, paint);
    }

    private void userTouchesBoard(float x, float y) {

        // game is over
        if (gameWon) {
            return;
        }

        // we don't care if it's computer's turn and user is touching board
        if (!userTurn) {
            Log.d(TAG, "Not user turn");
            return;
        }

        int junctionNo = gameUtility.getUserTouchedJunction(x, y, junctionsArray, unitLength);
        Log.d(TAG, "User Touched board at junction No: " + junctionNo);

        // if no junction found, i.e. user touched somewhere else
        if (junctionNo < 0) {
            return;
        }

        Junction junction = junctionsArray[junctionNo];
        String occupiedBy = junction.getOccupiedBy();

        // to avoid unnecessary calling of invalidate()
        boolean gameStatusChanged = false;

        if (gameStatus.equals(GameStatus.PLACE_STONE)) {
            if (occupiedBy == null || occupiedBy.equals("")) {

                // check if junctionNo is adjacent to pickedStoneNo
                if (gameUtility.isAdjacent(junctionNo, pickedStoneJunctionNo)) {

                    // valid position, user can place stone
                    junctionsArray[pickedStoneJunctionNo].setPicked(false);
                    junctionsArray[pickedStoneJunctionNo].setOccupiedBy("");
                    userDrawsOrPlaceStone(junctionNo);

                    // if picked stone was part of triplet, disable it
                    disableTripletFit(pickedStoneJunctionNo);
                    displayMessage = DisplayMessage.PICK_STONE;
                    gameStatus = GameStatus.PICK_STONE;
                    userTurn = false;
                    if (isTripletFit(junctionNo, playerUser)) {
                        userEatsStone();
                    }
                    gameStatusChanged = true;
                }

            }
        } else if (gameStatus.equals(GameStatus.PICK_STONE)) {
            if (occupiedBy != null && occupiedBy.equals(playerUser)) {

                // check if adjacent position is vacant
                if (gameUtility.isAdjacentVacant(junctionNo, junctionsArray)) {

                    // valid position, user can pick stone
                    junction.setPicked(true);
                    pickedStoneJunctionNo = junctionNo;
                    displayMessage = DisplayMessage.PLACE_STONE;
                    gameStatus = GameStatus.PLACE_STONE;
                    userTurn = true;
                    gameStatusChanged = true;
                }
            }
        } else if (gameStatus.equals(GameStatus.DRAW_STONE)) {
            if (occupiedBy == null || occupiedBy.equals("")) {

                // valid position, user can draw stone
                userDrawsOrPlaceStone(junctionNo);
                if (userStonesLeft > 0) {
                    displayMessage = getUserStoneBalanceMessage();
                    gameStatus = GameStatus.DRAW_STONE;
                } else {
                    displayMessage = DisplayMessage.PICK_STONE;
                    gameStatus = GameStatus.PICK_STONE;
                }
                userTurn = false;
                if (isTripletFit(junctionNo, playerUser)) {
                    userEatsStone();
                }
                gameStatusChanged = true;
            }
        } else if (gameStatus.equals(GameStatus.EAT_STONE)) {
            if (occupiedBy != null && occupiedBy.equals(playerComputer)) {

                // check if this stone is not part of triplet
                if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo)) {

                    // valid position, user can eat stone
                    junction.setOccupiedBy("");
                    computerHealth--;
                    if (userStonesLeft > 0) {
                        displayMessage = getUserStoneBalanceMessage();
                        gameStatus = GameStatus.DRAW_STONE;
                    } else {
                        displayMessage = DisplayMessage.PICK_STONE;
                        gameStatus = GameStatus.PICK_STONE;
                    }
                    userTurn = false;
                    gameStatusChanged = true;
                }
            }
        }

        // updating game
        if (gameStatusChanged) {
            playSoundAndVibration(playerUser);
            invalidate();
        }
    }

    private void drawMessage() {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        int outerSquareSideLength = 6 * unitLength;
        int x = width / 2;

        // y1 is for displayMessage
        int y1 = outerSquareSideLength +
                (height - canvasMargin - outerSquareSideLength) / 2;

        // y2 is for score Message
        int y2 = y1 +
                (height - canvasMargin - outerSquareSideLength) / 4;
        y1 = y1 - (int) (displayMessagePaint.ascent() + displayMessagePaint.descent()) / 2;
        y2 = y2 - (int) (scoreMessagePaint.ascent() + scoreMessagePaint.descent()) / 2;

        canvas.drawText(displayMessage, x, y1, displayMessagePaint);
        canvas.drawText(getScoreMessage(), x, y2, scoreMessagePaint);
    }

    private void drawBoard() {
        canvas.drawPath(boardPath, boardPaint);
    }

    private void paintTriplets() {
        for (int i = 0; i < activeTripletsList.size(); i++) {
            Triplet triplet = activeTripletsList.get(i);
            gameUtility.paintTriplet(triplet, canvas, tripletPaint,
                    junctionsArray, tripletPath);
        }
    }

    private String getUserStoneBalanceMessage() {
        return DisplayMessage.DRAW_STONE + userStonesLeft;
    }

    private String getScoreMessage() {
        String message = "You: " + userHealth + "/" + userStonesLeft
                + ", Computer: " + computerHealth + "/" + computerStonesLeft;
        return message;
    }

    private void playComputer() {

        // check winner
        String winner =
                gameUtility.getWinner(playerUser, playerComputer, userHealth, computerHealth);
        if (winner == null) {
        } else {
            gameWon = true;
            setWinnerMessage(winner);
            invalidate();
        }

        // game is over
        if (gameWon) {
            return;
        }

        List<Integer> junctionsListToFitTripletComputer =
                gameUtility.getAllJunctionNumbersToFitTriplet(junctionsArray,
                        fitTripletsArray, playerComputer);
        List<Integer> junctionsListToFitTripletUser =
                gameUtility.getAllJunctionNumbersToFitTriplet(junctionsArray,
                        fitTripletsArray, playerUser);

        /**
         * junctionsListFormingDualTriplet and junctionsListToFitTriplet combined together
         * make dualTripletFit possible.
         */
        List<Integer> junctionsListToFitDualTripletUser =
                gameUtility.getAllJunctionNumbersToFitFutureDualTriplet(junctionsArray,
                        fitTripletsArray, playerUser, latestUserStoneJunctionNo);
        List<Integer> junctionsListToFitDualTripletComputer =
                gameUtility.getAllJunctionNumbersToFitFutureDualTriplet(junctionsArray,
                        fitTripletsArray, playerComputer, latestComputerStoneJunctionNo);
        List <Integer> junctionsListFormingDualTripletUser =
                gameUtility.getJunctionsListFormingDualTriplet(junctionsArray,
                        fitTripletsArray, playerUser, latestUserStoneJunctionNo);
        List <Integer> junctionsListFormingDualTripletComputer =
                gameUtility.getJunctionsListFormingDualTriplet(junctionsArray,
                        fitTripletsArray, playerComputer, latestComputerStoneJunctionNo);

        List<Triplet> twoOccupiedAndOneVacantTripletsListUser =
                gameUtility.getTwoOccupiedAndOneVacantTripletsList(junctionsArray,
                        fitTripletsArray, playerUser);

        if (computerStonesLeft > 0) {

            // Fit a triplet, if such junction Exist
            if (!junctionsListToFitTripletComputer.isEmpty()) {
                Log.d(TAG, "computer fits a triplet");
                int junctionNo = junctionsListToFitTripletComputer.get(0);
                computerDrawsOrPlaceStone(junctionNo);

                // make this triplet active
                isTripletFit(junctionNo, playerComputer);

                // eat best stone of user
                computerEatsStone(twoOccupiedAndOneVacantTripletsListUser,
                        junctionsListFormingDualTripletUser, junctionNo);
            } else {

                // block user from making/forming/fitting a triplet
                if (!junctionsListToFitTripletUser.isEmpty()) {
                    Log.d(TAG, "computer blocks user from making a triplet");

                    // todo block best junction and not first
                    int junctionNo = junctionsListToFitTripletUser.get(0);
                    computerDrawsOrPlaceStone(junctionNo);
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
                        int junctionNo = junctionsListToFitDualTripletUser.get(0);
                        computerDrawsOrPlaceStone(junctionNo);
                    } else if(!junctionsListToFitDualTripletComputer.isEmpty()) {
                        Log.d(TAG, "computer draw stone to make a future dual triplet");

                        // place a stone at junction where a dual triplet can be fit
                        // todo place stone at best junction
                        int junctionNo = junctionsListToFitDualTripletComputer.get(0);
                        computerDrawsOrPlaceStone(junctionNo);
                    } else {
                        Log.d(TAG, "computer places stone at opposite corner in same square "
                                + "as that of user");

                        // draw a stone in same square as that of user and at opposite corner
                        // todo
                    }
                }
            }
        } else {

            // pick a stone and place at adjacent position
        }

        winner = gameUtility.getWinner(playerUser, playerComputer, userHealth, computerHealth);
        if (winner == null) {
            userTurn = true;
        } else {
            gameWon = true;
            setWinnerMessage(winner);
        }

        // updating game
        invalidate();
    }

    private void computerDrawsOrPlaceStone(int junctionNo) {
        junctionsArray[junctionNo].setOccupiedBy(playerComputer);
        latestComputerStoneJunctionNo = junctionNo;
        if (computerStonesLeft > 0) {
            computerStonesLeft--;
        }
    }

    private void userDrawsOrPlaceStone(int junctionNo) {
        junctionsArray[junctionNo].setOccupiedBy(playerUser);
        latestUserStoneJunctionNo = junctionNo;
        if (userStonesLeft > 0) {
            userStonesLeft--;
        }
    }

    /**
     * This method checks if newly made move fit any triplet
     * this method is also responsible for setting all junctions (affected by newly made move)
     * to update value isPartOfTriplet
     * @param junctionNo
     * @param player
     * @return
     */
    private boolean isTripletFit(int junctionNo, String player) {
        Triplet tripletsArray[] = fitTripletsArray[junctionNo].getTripletsArray();
        for (int i = 0; i < tripletsArray.length; i++) {
            Triplet triplet = tripletsArray[i];
            int junctionNo1 = triplet.getJunctionNo1();
            int junctionNo2 = triplet.getJunctionNo2();
            int junctionNo3 = triplet.getJunctionNo3();
            if (junctionsArray[junctionNo1].getOccupiedBy() != null &&
                    junctionsArray[junctionNo1].getOccupiedBy().equals(player)) {
                if (junctionsArray[junctionNo2].getOccupiedBy() != null &&
                        junctionsArray[junctionNo2].getOccupiedBy().equals(player)) {
                    if (junctionsArray[junctionNo3].getOccupiedBy() != null &&
                            junctionsArray[junctionNo3].getOccupiedBy().equals(player)) {
                        triplet.setActive(true);
                        triplet.setOwnedBy(player);

                        // will add triplet to activeList if it is not already added
                        gameUtility.addTripletToActiveTripletsList(triplet, activeTripletsList);
                        Log.d(TAG, "Triplet formed at junctionNo " + junctionNo + " by " + player);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void disableTripletFit(int junctionNo) {
        Triplet tripletsArray[] = fitTripletsArray[junctionNo].getTripletsArray();
        for (int i = 0; i < tripletsArray.length; i++) {
            Triplet triplet = tripletsArray[i];
            triplet.setActive(false);
            triplet.setOwnedBy("");

            // will remove it from activeList if it is present
            gameUtility.removeTripletFromActiveTripletsList(triplet, activeTripletsList);
        }
    }

    private void computerEatsStone(List<Triplet> twoOccupiedAndOneVacantTripletsListUser,
                                   List <Integer> junctionsListFormingDualTripletUser,
                                   int junctionNoWhereTripletFormed) {
        if (gameUtility.canEatPlayerStone(junctionsArray,
                playerUser, activeTripletsList)) {
            Log.d(TAG, "computer eats a stone");
            boolean ateStone = false;

            // eat a stone from two stones triplet
            if (!twoOccupiedAndOneVacantTripletsListUser.isEmpty()) {
                for (int i = 0; i < twoOccupiedAndOneVacantTripletsListUser.size(); i++) {
                    Triplet triplet = twoOccupiedAndOneVacantTripletsListUser.get(i);
                    int junctionNo1 = triplet.getJunctionNo1();
                    int junctionNo2 = triplet.getJunctionNo2();
                    int junctionNo3 = triplet.getJunctionNo3();
                    if (gameUtility.isVacant(junctionNo1, junctionsArray)) {
                        if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo2)) {
                            computerEatsStone(junctionNo2);
                            ateStone = true;
                            break;
                        } else if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo3)) {
                            computerEatsStone(junctionNo3);
                            ateStone = true;
                            break;
                        }
                    } else if (gameUtility.isVacant(junctionNo2, junctionsArray)) {
                        if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo1)) {
                            computerEatsStone(junctionNo1);
                            ateStone = true;
                            break;
                        } else if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo3)) {
                            computerEatsStone(junctionNo3);
                            ateStone = true;
                            break;
                        }
                    } else if (gameUtility.isVacant(junctionNo3, junctionsArray)) {
                        if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo1)) {
                            computerEatsStone(junctionNo1);
                            ateStone = true;
                            break;
                        } else if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo2)) {
                            computerEatsStone(junctionNo2);
                            ateStone = true;
                            break;
                        }
                    }
                }
            } else if (!junctionsListFormingDualTripletUser.isEmpty()) {

                // eat a stone which will be part of dual triplet in future
                for (int i = 0; i < junctionsListFormingDualTripletUser.size(); i++) {
                    int junctionNo = junctionsListFormingDualTripletUser.get(i);
                    if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo)) {
                        computerEatsStone(junctionNo);
                        ateStone = true;
                        break;
                    }
                }
            } else {

                // eat latestUserStoneJunctionNo
                if (!gameUtility.isPartOfTriplet(activeTripletsList, latestUserStoneJunctionNo)) {
                    computerEatsStone(latestUserStoneJunctionNo);
                    ateStone = true;
                } else {

                    // eat a stone adjacent to junctions where computer formed a triplet
                    // consider adjacent junctions of triplet formed
                }
            }
            if (!ateStone) {

                // eat 1st eligible user stone starting from junctionNo 1
            }
        } else {
            computerHealth++;
        }
    }

    private void computerEatsStone(int junctionNo) {

        // we are sure that a stone can be eaten from given junctionNo
        junctionsArray[junctionNo].setOccupiedBy("");
        userHealth--;
        playSoundAndVibration(playerComputer);
    }

    private void userEatsStone() {

        // if user can eat stone, it's fine. else userHealth will increase
        if (gameUtility.canEatPlayerStone(junctionsArray,
                playerComputer, activeTripletsList)) {
            displayMessage = DisplayMessage.EAT_STONE;
            gameStatus = GameStatus.EAT_STONE;
            userTurn = true;
        } else {
            userHealth++;
        }
    }

    private void setWinnerMessage(String winner) {
        if (winner.equals(playerComputer)) {
            displayMessage = DisplayMessage.COMPUTER_WON;
        } else {
            displayMessage = DisplayMessage.USER_WON;
        }
    }

    private void restartGame() {
        Log.d(TAG, "restart called");

        // reinitialising all variables
        computerHealth = maximumInitialStones;
        userHealth = maximumInitialStones;
        gameWon = false;
        userTurn = false;
        userStonesLeft = maximumInitialStones;
        computerStonesLeft = maximumInitialStones;
        displayMessage = getUserStoneBalanceMessage();
        for (int i = 1; i < junctionsArray.length; i++) {
            junctionsArray[i].setOccupiedBy("");
        }
        fitTripletsArray = gameUtility.getFitTripletsArray();
        gameStatus = GameStatus.DRAW_STONE;
        activeTripletsList = new ArrayList();

        // checking if first move is by computer
        if (settingsPreferences.getBoolean("fitto_computer_plays_first", true)) {
            playComputer();
        } else {
            userTurn = true;
        }
        invalidate();
    }

    private void playSoundAndVibration(String player) {
        if (player.equals(playerUser)) {
            if (gameStatus.equals(GameStatus.EAT_STONE)) {
                playSound();
                playVibration(500);
            } else if (gameStatus.equals(GameStatus.DRAW_STONE)) {
                playSound();
            } else if (gameStatus.equals(GameStatus.PLACE_STONE)) {
                playVibration(100);
            }
        } else {
            playSound();
            playVibration(500);
        }
    }

    private void playVibration(long duration) {
        if (settingsPreferences.getBoolean("fitto_vibration_enabled", true)) {
            try {
                vibrator.vibrate(duration);
            } catch(Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void playSound() {
        if (settingsPreferences.getBoolean("fitto_sound_enabled", true)) {
            try {
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void undoMove() {
        //Log.d(TAG, "UndoMove");
    }

}
