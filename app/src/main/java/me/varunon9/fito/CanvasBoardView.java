package me.varunon9.fito;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by varun on 20/8/17.
 */

public class CanvasBoardView extends View {

    private Paint paint;
    private Bitmap bitmap;
    private Canvas canvas;
    private Path path;
    Context context;
    private Junction junctionsArray[];
    private final String playerUser = "user";
    private final String playerComputer = "computer";

    private void init() {
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(3);

        // 24 junctions starting from 1 to 24
        junctionsArray = new Junction[25];
    }

    public CanvasBoardView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public CanvasBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public CanvasBoardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        /*bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);*/

        // build junctionsArray
        int margin = 10;
        buildJunctionsArray(junctionsArray, w, h, margin);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoard(canvas, junctionsArray);
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

            path.moveTo(junctionsArray[start].getX(), junctionsArray[start].getY());

            int x, y;
            for (int j = 1; j < 8; j++) {
                x = junctionsArray[start + j].getX();
                y = junctionsArray[start + j].getY();
                path.lineTo(x, y);
            }

            // complete square by reaching to starting point
            path.lineTo(junctionsArray[start].getX(), junctionsArray[start].getY());
        }

        // drawing lines intersecting all three squares (on corners)
        for (int i = 1; i <= 7; i += 2) {
            path.moveTo(junctionsArray[i].getX(), junctionsArray[i].getY());
            path.lineTo(junctionsArray[i + 16].getX(), junctionsArray[i + 16].getY());
        }

        // drawing lines intersecting all three squares (on mid)
        for (int i = 2; i <= 8; i += 2) {
            path.moveTo(junctionsArray[i].getX(), junctionsArray[i].getY());
            path.lineTo(junctionsArray[i + 16].getX(), junctionsArray[i + 16].getY());
        }

        canvas.drawPath(path, paint);

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
        int unitLength = width / 12;
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

    private void placeStones(Junction junctionsArray[]) {
        //todo
    }

}
