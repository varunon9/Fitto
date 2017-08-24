package me.varunon9.fitto;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by varun on 20/8/17.
 */

public class CanvasBoardView extends View {

    private Paint userStonePaint;
    private Paint computerStonePaint;
    private Paint boardPaint;
    private Path boardPath;
    private Paint textPaint;
    private Context context;
    private Canvas mCanvas;
    private Junction junctionsArray[];
    private String playerUser;
    private String playerComputer;
    private String computerStoneColor;
    private String userStoneColor;
    private int maximumInitialStones;
    private int userStonesLeft;
    private int computerStonesLeft;
    private int canvasMargin;
    private int stoneRadius;
    private boolean userTurn = false;
    private String displayMessage;
    private final String pickStoneMessage = "Pick a stone";
    private final String placeStoneMessage = "Place the picked stone";
    private int computerScore = 0;
    private int userScore = 0;
    private boolean gameWon = false;

    // innerMost square has side 2 * unitLength;
    // initialised while building junctionsArray
    private int unitLength;

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
        this.context = context;
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

        boardPaint = new Paint();
        boardPaint.setAntiAlias(true);
        boardPaint.setDither(true);
        boardPaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        boardPaint.setStyle(Paint.Style.STROKE);
        boardPaint.setStrokeJoin(Paint.Join.ROUND);
        boardPaint.setStrokeWidth(3);


        boardPath = new Path();

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(2 * canvasMargin);
        textPaint.setColor(Color.parseColor(computerStoneColor));

        userStonesLeft = maximumInitialStones;
        computerStonesLeft = maximumInitialStones;
        displayMessage = getUserStoneBalanceMessage();

        typedArray.recycle();

        // 24 junctions starting from 1 to 24
        junctionsArray = new Junction[25];
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

        // build junctionsArray
        buildJunctionsArray(junctionsArray, w, h, canvasMargin);System.out.println("on size called");

        // first move by computer
        // todo check settings
        playComputer();

    }

    @Override
    protected void onDraw(Canvas canvas) {System.out.println("on Draw called");
        super.onDraw(canvas);
        mCanvas = canvas;
        drawBoard(canvas, junctionsArray);
        placeStones(junctionsArray, canvas);
        drawMessage();
    }

    /**
     * There are 24 junctions in BoardView occupiedBy either user or computer
     */
    private class Junction {
        private int x;
        private int y;
        private String occupiedBy;

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

    }

    private void drawBoard(Canvas canvas, Junction junctionsArray[]) {

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

        canvas.drawPath(boardPath, boardPaint);

    }

    private void initializeJunctions(Junction junctionsArray[], int startIndex,
                                    int startX, int startY, int distance) {
        int i, corner;
        for (i = startIndex, corner = 1; corner <= 8; i++, corner++) {
            junctionsArray[i] = new Junction();
            switch (corner) {
                case 1: {
                    junctionsArray[i].x = startX;
                    junctionsArray[i].y = startY;
                    break;
                }
                case 2: {
                    junctionsArray[i].x = startX + distance;
                    junctionsArray[i].y = startY;
                    break;
                }
                case 3: {
                    junctionsArray[i].x = startX + 2 * distance;
                    junctionsArray[i].y = startY;
                    break;
                }
                case 4: {
                    junctionsArray[i].x = startX + 2 * distance;
                    junctionsArray[i].y = startY + distance;
                    break;
                }
                case 5: {
                    junctionsArray[i].x = startX + 2 * distance;;
                    junctionsArray[i].y = startY + 2 * distance;
                    break;
                }
                case 6: {
                    junctionsArray[i].x = startX + distance;
                    junctionsArray[i].y = startY + 2 * distance;
                    break;
                }
                case 7: {
                    junctionsArray[i].x = startX;
                    junctionsArray[i].y = startY + 2 * distance;
                    break;
                }
                case 8: {
                    junctionsArray[i].x = startX;
                    junctionsArray[i].y = startY + distance;
                    break;
                }
            }
        }

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
        initializeJunctions(junctionsArray, 1, x, y, 3 * unitLength);

        // initialising junctions 9 to 16
        initializeJunctions(junctionsArray, 9, (x + unitLength), (y + unitLength), 2 * unitLength);

        // initialising junctions 17 to 24
        initializeJunctions(junctionsArray, 17, (x + 2 * unitLength),
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
        }
    }

    private void drawComputerStone(Canvas canvas, int x, int y) {
        drawStone(canvas, x, y, computerStonePaint);
    }

    private void drawUserStone(Canvas canvas, int x, int y) {
        drawStone(canvas, x, y, userStonePaint);
    }

    private void drawStone(Canvas canvas, int x, int y, Paint paint) {
        canvas.drawCircle(x, y, stoneRadius, paint);System.out.println(canvas);
    }

    private void userTouchesBoard(float x, float y) {

        // game is over
        if (gameWon) {
            return;
        }

        // we don't care if it's computer's turn and user is touching board
        if (!userTurn) {
            return;
        } else {
            userTurn = false;
        }

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

        // if no junction found, i.e. user touched somewhere else
        if (junctionNo < 0) {
            return;
        }

        Junction junction = junctionsArray[junctionNo];
        String occupiedBy = junction.getOccupiedBy();
        if (occupiedBy == null) {
            // valid position, user can draw stone
            junction.setOccupiedBy(playerUser);
            userStonesLeft --;
            displayMessage = getUserStoneBalanceMessage();
        }

        // updating game
        invalidate();

        playComputer();
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

    private String getUserStoneBalanceMessage() {
        return "Your Turn, Stones Left: " + userStonesLeft;
    }

    private String getComputerStoneBalanceMessage() {
        return "Computer's Turn, Stones Left: " + computerStonesLeft;
    }

    private void playComputer() {

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
        userTurn = true;

        // updating game
        //invalidate();
    }

}
