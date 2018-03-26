package me.varunon9.fitto;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.MediaPlayer;
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
 * Created by varun on 25/3/18.
 */

public class PlayerCanvasBoardView extends View {

    private Paint player1StonePaint;
    private Paint player2StonePaint;
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
    private String player1;
    private String player2;
    private String player1StoneColor;
    private String player2StoneColor;
    private int maximumInitialStones;
    private int player1StonesLeft;
    private int player2StonesLeft;
    private int canvasMargin;
    private int stoneRadius;
    private boolean player1Turn;
    private boolean player2Turn;
    private String displayMessage;
    private int player2Health;
    private int player1Health;
    private boolean gameWon;
    private String gameStatus;
    private int pickedStoneJunctionNo;
    private int latestPlayer1StoneJunctionNo;
    private int latestPlayer2StoneJunctionNo;
    private List<Triplet> activeTripletsList;
    private Context context;

    private static final String TAG = "PlayerCanvasBoardView";

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
                R.styleable.PlayerCanvasBoardView, 0, 0);
        player1 = typedArray.getString(R.styleable.PlayerCanvasBoardView_player1);
        player2 = typedArray.getString(R.styleable.PlayerCanvasBoardView_player2);
        player2StoneColor = typedArray.getString(R.styleable.PlayerCanvasBoardView_player2StoneColor);
        player1StoneColor = typedArray.getString(R.styleable.PlayerCanvasBoardView_player1StoneColor);
        maximumInitialStones =
                typedArray.getInt(R.styleable.ComputerCanvasBoardView_maximumInitialStones, 8);
        stoneRadius = typedArray.getInt(R.styleable.ComputerCanvasBoardView_stoneRadius, 20);
        canvasMargin = typedArray.getInt(R.styleable.ComputerCanvasBoardView_margin, 20);
        player1StonePaint = new Paint();
        player1StonePaint.setAntiAlias(true);
        player1StonePaint.setDither(true);
        player1StonePaint.setColor(Color.parseColor(player1StoneColor));
        player1StonePaint.setStrokeWidth(stoneRadius);

        player2StonePaint = new Paint();
        player2StonePaint.setAntiAlias(true);
        player2StonePaint.setDither(true);
        player2StonePaint.setColor(Color.parseColor(player2StoneColor));
        player2StonePaint.setStrokeWidth(stoneRadius);

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
        displayMessagePaint.setColor(Color.parseColor(player2StoneColor));

        scoreMessagePaint = new Paint();
        scoreMessagePaint.setAntiAlias(true);
        scoreMessagePaint.setTextAlign(Paint.Align.CENTER);
        scoreMessagePaint.setTextSize((float) 1.5 * canvasMargin);
        scoreMessagePaint.setColor(Color.parseColor(player1StoneColor));

        tripletPaint = new Paint();
        tripletPaint.setAntiAlias(true);
        tripletPaint.setStyle(Paint.Style.STROKE);
        tripletPaint.setColor(Color.parseColor(player2StoneColor));
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

    public PlayerCanvasBoardView(Context context, AttributeSet attrs) {
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
                playerTouchesBoard(x, y);
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
        displayMessage = DisplayMessage.PLAYER1_DRAW_STONE;
        drawMessage();
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

            if (occupiedBy.equals(player1)) {
                drawPlayer1Stone(canvas, x, y);
            } else if (occupiedBy.equals(player2)) {
                drawPlayer2Stone(canvas, x, y);
            }

            // if picked stone, draw it common
            if (junction.isPicked()) {
                drawStone(canvas, x, y, pickedStonePaint);
            }
        }
    }

    private void drawPlayer2Stone(Canvas canvas, int x, int y) {
        drawStone(canvas, x, y, player2StonePaint);
    }

    private void drawPlayer1Stone(Canvas canvas, int x, int y) {
        drawStone(canvas, x, y, player1StonePaint);
    }

    private void drawStone(Canvas canvas, int x, int y, Paint paint) {
        canvas.drawCircle(x, y, stoneRadius, paint);
    }

    private void playerTouchesBoard(float x, float y) {

        // game is over
        if (gameWon) {
            return;
        }

        int junctionNo = gameUtility.getUserTouchedJunction(x, y, junctionsArray, unitLength);
        Log.d(TAG, "Player Touched board at junction No: " + junctionNo);

        // if no junction found, i.e. player touched somewhere else
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

                    // valid position, player can place stone
                    junctionsArray[pickedStoneJunctionNo].setPicked(false);
                    junctionsArray[pickedStoneJunctionNo].setOccupiedBy("");
                    if (player1Turn) {
                        player1DrawsOrPlaceStone(junctionNo);
                    } else {
                        player2DrawsOrPlaceStone(junctionNo);
                    }

                    // if picked stone was part of triplet, disable it
                    disableTripletFit(pickedStoneJunctionNo);

                    if (player1Turn) {
                        displayMessage = DisplayMessage.PLAYER1_PICK_STONE;
                    } else {
                        displayMessage = DisplayMessage.PLAYER2_PICK_STONE;
                    }
                    gameStatus = GameStatus.PICK_STONE;
                    player1Turn = !player1Turn;
                    player2Turn = !player2Turn;
                    if (isTripletFit(junctionNo, player1)) {
                        playerEatsStone();
                    }
                    gameStatusChanged = true;
                }

            } else if (junctionNo == pickedStoneJunctionNo) {

                // if it is picked stone then unpick it
                junction.setPicked(false);

                if (player1Turn) {
                    displayMessage = DisplayMessage.PLAYER1_PICK_STONE;
                } else {
                    displayMessage = DisplayMessage.PLAYER2_PICK_STONE;
                }

                gameStatus = GameStatus.PICK_STONE;
                player1Turn = !player1Turn;
                player2Turn = !player2Turn;
                gameStatusChanged = true;

            }
        } else if (gameStatus.equals(GameStatus.PICK_STONE)) {
            if (occupiedBy != null && occupiedBy.equals(player1)) {

                // check if adjacent position is vacant
                if (gameUtility.isAdjacentVacant(junctionNo, junctionsArray)) {

                    // valid position, player can pick stone
                    junction.setPicked(true);
                    pickedStoneJunctionNo = junctionNo;

                    if (player1Turn) {
                        displayMessage = DisplayMessage.PLAYER1_PLACE_STONE;
                    } else {
                        displayMessage = DisplayMessage.PLAYER2_PLACE_STONE;
                    }

                    gameStatus = GameStatus.PLACE_STONE;
                    player1Turn = !player1Turn;
                    player2Turn = !player2Turn;
                    gameStatusChanged = true;
                }
            }
        } else if (gameStatus.equals(GameStatus.DRAW_STONE)) {
            if (occupiedBy == null || occupiedBy.equals("")) {

                // valid position, player can draw stone
                if (player1Turn) {
                    player1DrawsOrPlaceStone(junctionNo);
                    if (player1StonesLeft > 0) {
                        displayMessage = getPlayerStoneBalanceMessage(player1);
                        gameStatus = GameStatus.DRAW_STONE;
                    } else {
                        displayMessage = DisplayMessage.PLAYER2_PICK_STONE;
                        gameStatus = GameStatus.PICK_STONE;
                    }
                } else {
                    player2DrawsOrPlaceStone(junctionNo);
                    if (player2StonesLeft > 0) {
                        displayMessage = getPlayerStoneBalanceMessage(player2);
                        gameStatus = GameStatus.DRAW_STONE;
                    } else {
                        displayMessage = DisplayMessage.PLAYER1_PICK_STONE;
                        gameStatus = GameStatus.PICK_STONE;
                    }
                }


                player1Turn = !player1Turn;
                player2Turn = !player2Turn;
                if (isTripletFit(junctionNo, player1)) {
                    playerEatsStone();
                }
                gameStatusChanged = true;
            }
        } else if (gameStatus.equals(GameStatus.EAT_STONE)) {
            if (player1Turn) {
                if (occupiedBy != null && occupiedBy.equals(player2)) {

                    // check if this stone is not part of triplet
                    if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo)) {

                        // valid position, player can eat stone
                        junction.setOccupiedBy("");
                        player2Health--;
                        if (player1StonesLeft > 0) {
                            displayMessage = getPlayerStoneBalanceMessage(player1);
                            gameStatus = GameStatus.DRAW_STONE;
                        } else {
                            displayMessage = DisplayMessage.PLAYER2_PICK_STONE;
                            gameStatus = GameStatus.PICK_STONE;
                        }
                        player1Turn = !player1Turn;
                        player2Turn = !player2Turn;
                        gameStatusChanged = true;
                    }
                }
            } else {
                if (occupiedBy != null && occupiedBy.equals(player1)) {

                    // check if this stone is not part of triplet
                    if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo)) {

                        // valid position, player can eat stone
                        junction.setOccupiedBy("");
                        player2Health--;
                        if (player1StonesLeft > 0) {
                            displayMessage = getPlayerStoneBalanceMessage(player1);
                            gameStatus = GameStatus.DRAW_STONE;
                        } else {
                            displayMessage = DisplayMessage.PLAYER1_PICK_STONE;
                            gameStatus = GameStatus.PICK_STONE;
                        }
                        player1Turn = !player1Turn;
                        player2Turn = !player2Turn;
                        gameStatusChanged = true;
                    }
                }
            }

        }

        // updating game
        if (gameStatusChanged) {
            if (player1Turn) {
                playSoundAndVibration(player1);
            } else {
                playSoundAndVibration(player2);
            }
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

    private String getPlayerStoneBalanceMessage(String player) {
        if (player.equals(player1)) {
            return DisplayMessage.DRAW_STONE + player1StonesLeft;
        } else {
            return DisplayMessage.DRAW_STONE + player2StonesLeft;
        }
    }

    private String getScoreMessage() {
        String message = "Player1: " + player1Health + "/" + player1StonesLeft
                + ", Player2: " + player2Health + "/" + player2StonesLeft;
        return message;
    }

    private void player2DrawsOrPlaceStone(int junctionNo) {
        junctionsArray[junctionNo].setOccupiedBy(player2);
        latestPlayer2StoneJunctionNo = junctionNo;
        if (player2StonesLeft > 0) {
            player2StonesLeft--;
        }
    }

    private void player1DrawsOrPlaceStone(int junctionNo) {
        junctionsArray[junctionNo].setOccupiedBy(player1);
        latestPlayer1StoneJunctionNo = junctionNo;
        if (player1StonesLeft > 0) {
            player1StonesLeft--;
        }
    }

    /**
     * This method checks if newly made move fit any triplet
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

    private void player2EatsStone(List<Triplet> twoOccupiedAndOneVacantTripletsListplayer,
                                   List<Integer> junctionsListFormingDualTripletplayer,
                                   List<Triplet> twoOccupiedByplayer2AndOneOccupiedByplayerTripletsList,
                                   List<Integer> tripletsAdjacentJunctionNumbersListplayer2,
                                   int junctionNoWhereTripletFormed) {
        if (gameUtility.canEatPlayerStone(junctionsArray, player1, activeTripletsList)) {
            boolean ateStone = false;

            // eat a stone from two stones triplet
            if (!twoOccupiedAndOneVacantTripletsListplayer.isEmpty()) {
                for (int i = 0; i < twoOccupiedAndOneVacantTripletsListplayer.size(); i++) {
                    Triplet triplet = twoOccupiedAndOneVacantTripletsListplayer.get(i);
                    int junctionNo1 = triplet.getJunctionNo1();
                    int junctionNo2 = triplet.getJunctionNo2();
                    int junctionNo3 = triplet.getJunctionNo3();
                    if (gameUtility.isVacant(junctionNo1, junctionsArray)) {
                        if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo2)) {
                            player2EatsStone(junctionNo2);
                            ateStone = true;
                            break;
                        } else if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo3)) {
                            player2EatsStone(junctionNo3);
                            ateStone = true;
                            break;
                        }
                    } else if (gameUtility.isVacant(junctionNo2, junctionsArray)) {
                        if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo1)) {
                            player2EatsStone(junctionNo1);
                            ateStone = true;
                            break;
                        } else if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo3)) {
                            player2EatsStone(junctionNo3);
                            ateStone = true;
                            break;
                        }
                    } else if (gameUtility.isVacant(junctionNo3, junctionsArray)) {
                        if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo1)) {
                            player2EatsStone(junctionNo1);
                            ateStone = true;
                            break;
                        } else if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo2)) {
                            player2EatsStone(junctionNo2);
                            ateStone = true;
                            break;
                        }
                    }
                }
            } else if(!twoOccupiedByplayer2AndOneOccupiedByplayerTripletsList.isEmpty()) {

                // eat a stone from triplets having two junctions occupied by player2 and one by player
                for (int i = 0; i < twoOccupiedByplayer2AndOneOccupiedByplayerTripletsList.size(); i++) {
                    Triplet triplet = twoOccupiedByplayer2AndOneOccupiedByplayerTripletsList.get(i);
                    int junctionNo1 = triplet.getJunctionNo1();
                    int junctionNo2 = triplet.getJunctionNo2();
                    int junctionNo3 = triplet.getJunctionNo3();

                    // stone junctionNo to be eaten by player2
                    int junctionNo = junctionNo1;
                    if (junctionsArray[junctionNo2].getOccupiedBy().equals(player1)) {
                        junctionNo = junctionNo2;
                    } else if (junctionsArray[junctionNo3].getOccupiedBy().equals(player1)) {
                        junctionNo = junctionNo3;
                    }
                    if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo)) {
                        player2EatsStone(junctionNo);
                        ateStone = true;
                        break;
                    }
                }

            } else  if (!junctionsListFormingDualTripletplayer.isEmpty()) {

                // eat a stone which will be part of dual triplet in future
                for (int i = 0; i < junctionsListFormingDualTripletplayer.size(); i++) {
                    int junctionNo = junctionsListFormingDualTripletplayer.get(i);
                    if (!gameUtility.isPartOfTriplet(activeTripletsList, junctionNo)) {
                        player2EatsStone(junctionNo);
                        ateStone = true;
                        break;
                    }
                }
            } else {

                // eat latestPlayer1StoneJunctionNo
                if (!gameUtility.isPartOfTriplet(activeTripletsList, latestPlayer1StoneJunctionNo)) {
                    player2EatsStone(latestPlayer1StoneJunctionNo);
                    ateStone = true;
                } else {

                    // eat a stone adjacent to junctions where player2 formed a triplet
                    if (!tripletsAdjacentJunctionNumbersListplayer2.isEmpty()) {
                        for (int i = 0;
                             i < tripletsAdjacentJunctionNumbersListplayer2.size(); i++) {
                            int adjacentJunctionNo =
                                    tripletsAdjacentJunctionNumbersListplayer2.get(i);

                            // check if it is not vacant and not occupied by player2
                            if (junctionsArray[adjacentJunctionNo].getOccupiedBy() != null
                                    && !junctionsArray[adjacentJunctionNo].getOccupiedBy()
                                    .equals(player2)) {
                                if (!gameUtility.isPartOfTriplet(activeTripletsList,
                                        adjacentJunctionNo)) {

                                    player2EatsStone(adjacentJunctionNo);
                                    ateStone = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            if (!ateStone) {

                // eat 1st eligible player stone starting from junctionNo 1
                for (int i = 1; i < junctionsArray.length; i++) {
                    Junction junction = junctionsArray[i];
                    if (junction.getOccupiedBy() != null
                            && junction.getOccupiedBy().equals(player1)) {
                        if (!gameUtility.isPartOfTriplet(activeTripletsList, i)) {
                            player2EatsStone(i);
                            break;
                        }
                    }
                }
            }
        } else {
            player2Health++;
        }
    }

    private void player2EatsStone(int junctionNo) {
        Log.d(TAG, "player2 eats a stone at junction No: " + junctionNo);

        // we are sure that a stone can be eaten from given junctionNo
        junctionsArray[junctionNo].setOccupiedBy("");
        player1Health--;
        playSoundAndVibration(player2);
    }

    private void playerEatsStone() {

        // if player can eat stone, it's fine. else player1Health will increase
        if (gameUtility.canEatPlayerStone(junctionsArray,
                player2, activeTripletsList)) {
            displayMessage = DisplayMessage.EAT_STONE;
            gameStatus = GameStatus.EAT_STONE;
            player1Turn = true;
        } else {
            player1Health++;
        }
    }

    private void setWinnerMessage(String winner) {
        if (winner.equals(player2)) {
            displayMessage = DisplayMessage.PLAYER2_WON;
        } else {
            displayMessage = DisplayMessage.PLAYER1_WON;
        }
    }

    private void restartGame() {
        Log.d(TAG, "restart called");

        // reinitialising all variables
        player2Health = maximumInitialStones;
        player1Health = maximumInitialStones;
        gameWon = false;
        player1Turn = false;
        player2Turn = false;
        player1StonesLeft = maximumInitialStones;
        player2StonesLeft = maximumInitialStones;
        for (int i = 1; i < junctionsArray.length; i++) {
            junctionsArray[i].setOccupiedBy("");
        }
        fitTripletsArray = gameUtility.getFitTripletsArray();
        gameStatus = GameStatus.DRAW_STONE;
        activeTripletsList = new ArrayList();

        // checking if first move is by player2
        if (settingsPreferences.getBoolean("fitto_player2_plays_first", true)) {
            player2Turn = true;
            displayMessage = getPlayerStoneBalanceMessage(player2);
        } else {
            player1Turn = true;
            displayMessage = getPlayerStoneBalanceMessage(player1);
        }
        invalidate();
    }

    private void playSoundAndVibration(String player) {
        if (player.equals(player1)) {
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
