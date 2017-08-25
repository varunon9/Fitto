package me.varunon9.fitto;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

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
    private Paint textPaint;
    private Paint tripletPaint;
    private Canvas mCanvas;
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

    private static final String TAG = "CanvasBoardView";

    // settings preferences
    private SharedPreferences settingsPreferences;

    // innerMost square has side 2 * unitLength;
    // initialised while building junctionsArray
    private int unitLength;

    private GameUtility gameUtility;

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
        boardPaint.setStrokeWidth(3);

        tripletPath = new Path();

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(2 * canvasMargin);
        textPaint.setColor(Color.parseColor(computerStoneColor));

        tripletPaint = new Paint();
        tripletPaint.setAntiAlias(true);
        tripletPaint.setStyle(Paint.Style.STROKE);
        tripletPaint.setColor(Color.parseColor(computerStoneColor));
        tripletPaint.setStrokeWidth(4);

        typedArray.recycle();

        settingsPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // 24 junctions starting from 1 to 24
        junctionsArray = new Junction[25];

        gameUtility = new GameUtility();
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
        return  true;
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
        mCanvas = canvas;
        drawBoard();
        placeStones(junctionsArray, canvas);
        paintTriplets();
        drawMessage();
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

            // if picked stone, draw it common
            if (junction.isPicked()) {
                drawStone(canvas, x, y, pickedStonePaint);
            }
            if (occupiedBy.equals(playerUser)) {
                drawUserStone(canvas, x, y);
            } else if (occupiedBy.equals(playerComputer)) {
                drawComputerStone(canvas, x, y);
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
        Log.d(TAG, "User Touched board");

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
                    junction.setOccupiedBy(playerUser);
                    displayMessage = DisplayMessage.PICK_STONE;
                    gameStatus = GameStatus.PICK_STONE;
                    userTurn = false;
                    gameStatusChanged = true;
                }
                if (isTripletFit(junctionNo, playerUser)) {
                    displayMessage = DisplayMessage.EAT_STONE;
                    gameStatus = GameStatus.EAT_STONE;
                    userTurn = true;
                }
            }
        } else if (gameStatus.equals(GameStatus.PICK_STONE)) {
            if (occupiedBy != null && occupiedBy.equals(playerUser)) {
                // valid position, user can pick stone
                junction.setPicked(true);
                pickedStoneJunctionNo = junctionNo;
                displayMessage = DisplayMessage.PLACE_STONE;
                gameStatus = GameStatus.PLACE_STONE;
                userTurn = true;
                gameStatusChanged = true;
            }
        } else if (gameStatus.equals(GameStatus.DRAW_STONE)) {
            if (occupiedBy == null || occupiedBy.equals("")) {
                // valid position, user can draw stone
                junction.setOccupiedBy(playerUser);
                userStonesLeft --;
                if (userStonesLeft > 0) {
                    displayMessage = getUserStoneBalanceMessage();
                    gameStatus = GameStatus.DRAW_STONE;
                } else {
                    displayMessage = DisplayMessage.PICK_STONE;
                    gameStatus = GameStatus.PICK_STONE;
                }
                userTurn = false;
                if (isTripletFit(junctionNo, playerUser)) {
                    displayMessage = DisplayMessage.EAT_STONE;
                    gameStatus = GameStatus.EAT_STONE;
                    userTurn = true;
                }
                gameStatusChanged = true;
            }
        } else if (gameStatus.equals(GameStatus.EAT_STONE)) {
            if (occupiedBy != null && occupiedBy.equals(playerComputer)) {
                // check if this stone is not part of triplet
                if (gameUtility.isNotPartOfTriplet(fitTripletsArray, junctionNo)) {
                    // valid position, user can eat stone
                    junction.setOccupiedBy("");
                    computerHealth --;
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
            invalidate();
        }
    }

    private void drawMessage() {
        int width = mCanvas.getWidth();
        int height = mCanvas.getHeight();
        int outerSquareSideLength = 6 * unitLength;
        int x = width / 2;
        int y = outerSquareSideLength +
                (height - canvasMargin - outerSquareSideLength) / 2;
        y = y - (int) (textPaint.ascent() + textPaint.descent()) / 2;
        mCanvas.drawText(displayMessage, x, y, textPaint);
    }

    private void drawBoard() {
        mCanvas.drawPath(boardPath, boardPaint);
    }

    private void paintTriplets() {
        for (int i = 1; i < fitTripletsArray.length; i++) {
            FitTriplet fitTriplet = fitTripletsArray[i];
            Triplet tripletsArray[] = fitTriplet.getTripletsArray();
            for (int j = 0; j < tripletsArray.length; j++) {
                Triplet triplet = tripletsArray[j];
                if (triplet.isActive()) {
                    Log.d(TAG, "painting triplet for junction " + i);
                    gameUtility.paintTriplet(triplet, mCanvas, tripletPaint,
                            junctionsArray, tripletPath);
                }
            }
        }
    }

    private String getUserStoneBalanceMessage() {
        return DisplayMessage.DRAW_STONE + userStonesLeft;
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

        if (computerStonesLeft > 0) {
            // place a stone at best place
            computerStonesLeft --;
        } else {
            // pick a stone and place at adjacent position
        }

        winner =  gameUtility.getWinner(playerUser, playerComputer, userHealth, computerHealth);
        if (winner == null) {
            userTurn = true;
        } else {
            gameWon = true;
            setWinnerMessage(winner);
        }
        // updating game
        invalidate();
    }

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
                        Log.d(TAG, "Triplet formed at junctionNo " + junctionNo + " by "
                                + player);
                        return true;
                    }
                }
            }
        }
        return false;
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

        // checking if first move is by computer
        if (settingsPreferences.getBoolean("fitto_computer_plays_first", true)) {
            playComputer();
        } else {
            userTurn = true;
        }
        invalidate();
    }

    private void undoMove() {
        //Log.d(TAG, "UndoMove");
    }

}
