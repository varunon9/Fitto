package me.varunon9.fitto.computer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import me.varunon9.fitto.R;
import me.varunon9.fitto.oldversion.Triplet;
import me.varunon9.fitto.game.CanvasBoardView;
import me.varunon9.fitto.game.GameStatus;
import me.varunon9.fitto.game.Junction;
import me.varunon9.fitto.game.Player;

public class PlayWithComputerCanvasBoardView extends CanvasBoardView {

    private int initialStones = 8;
    private String TAG = "PlayWithComputerCanvasBoardView";
    private int gameStatus = GameStatus.DRAW_STONE;

    private int latestUserStoneJunctionNo;
    private int latestComputerStoneJunctionNo;
    private int pickedStoneJunctionNo = -1;
    
    private ComputerLogic computerLogic;

    private Player playerUser;
    private Player playerComputer;

    private int draggedStoneXCordinate;
    private int draggedStoneYCordinate;

    public PlayWithComputerCanvasBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        playerUser = new Player();
        playerUser.setName("You"); // todo read from Singleton

        Paint playerUserStonePaint = new Paint();
        playerUserStonePaint.setAntiAlias(true);
        playerUserStonePaint.setDither(true);
        playerUserStonePaint.setColor(ContextCompat.getColor(context, R.color.colorPrimary));
        playerUserStonePaint.setStrokeWidth(20);

        playerUser.setStonePaint(playerUserStonePaint);

        playerUser.setStonesLeft(initialStones);
        playerUser.setHealth(initialStones);
        playerUser.setTurn(false); // computer will play first todo

        playerComputer = new Player();
        playerComputer.setName("Computer"); // todo read from Singleton

        Paint playerComputerStonePaint = new Paint();
        playerComputerStonePaint.setAntiAlias(true);
        playerComputerStonePaint.setDither(true);
        playerComputerStonePaint.setColor(ContextCompat.getColor(context, R.color.colorAccent));
        playerComputerStonePaint.setStrokeWidth(20);

        playerComputer.setStonePaint(playerComputerStonePaint);

        playerComputer.setStonesLeft(initialStones);
        playerComputer.setHealth(initialStones);
        playerComputer.setTurn(true); // computer will play first todo

        this.setPlayer1(playerUser);
        this.setPlayer2(playerComputer);
        
        computerLogic = new ComputerLogic();
    }

    @Override
    protected void restartGame() {
        Log.d(TAG, "restart called");

        winner = null;
        playerUser.setTurn(false); // todo decide first turn logic
        playerComputer.setTurn(true);
        playerUser.setStonesLeft(initialStones);
        playerUser.setHealth(initialStones);
        playerComputer.setStonesLeft(initialStones);
        playerComputer.setHealth(initialStones);
        for (int i = 1; i < junctionsArray.length; i++) {
            junctionsArray[i].setOccupiedBy(null);
        }
        pickedStoneJunctionNo = -1;
        instructionMessage = null;
        gameStatus = GameStatus.DRAW_STONE;
        activeTripletsList = new ArrayList<Triplet>();
        
        if (playerComputer.isTurn()) {
            computerPlays();
        }
        
        invalidate();
    }

    private void drawDraggedStone() {
        if (pickedStoneJunctionNo > 0 && (gameStatus == GameStatus.PLACE_STONE)) {
            drawStone(playerUser, draggedStoneXCordinate, draggedStoneYCordinate);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawDraggedStone();
        instructionMessage = null;

        if (playerComputer.isTurn()) {
            computerPlays();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                playerPicksStone(x, y);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                playerDragsStone(x, y);
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

    private void playerPicksStone(float x, float y) {
        if (winner != null) {
            return;
        }

        // stone at this junction can be dragged when gameStatus is 'PLACE_STONE'
        int touchedJunctionNo = getTouchedJunction(x, y);
        if (touchedJunctionNo > 0) {
            if (gameStatus == GameStatus.PLACE_STONE
                    && junctionsArray[touchedJunctionNo].getOccupiedBy() == playerUser) {
                pickedStoneJunctionNo = touchedJunctionNo;
            }
        }
    }

    private void playerDragsStone(float x, float y) {
        if (winner != null) {
            return;
        }
        if (pickedStoneJunctionNo > 0 && (gameStatus == GameStatus.PLACE_STONE)) {
            junctionsArray[pickedStoneJunctionNo].setOccupiedBy(null);
            draggedStoneXCordinate = (int) x;
            draggedStoneYCordinate = (int) y;
            invalidate();
        }
    }

    // here player is user
    private void playerTouchesBoard(float x, float y) {
        if (winner != null) {
            // someone has won the game
            return;
        }
        if (!playerUser.isTurn()) {
            return;
        }

        int touchedJunctionNo = getTouchedJunction(x, y);

        Log.d(TAG, playerUser.toString());
        Log.d(TAG, "Touched junction: " + touchedJunctionNo);

        // if no junction found, i.e. player touched somewhere else
        if (touchedJunctionNo < 0) {
            if (pickedStoneJunctionNo > 0 && (gameStatus == GameStatus.PLACE_STONE)) {
                junctionsArray[pickedStoneJunctionNo].setOccupiedBy(playerUser);
                pickedStoneJunctionNo = -1; // marking it invalid
                invalidate();
            }
            return;
        }

        playerPlays(touchedJunctionNo);
    }
    
    private void computerPlays() {
        if (winner != null) {
            return;
        }
        if (gameStatus == GameStatus.EAT_STONE) {
            // eat user's stone
            int junctionNoToEatUserStone = computerLogic.getJunctionNoToEatUserStone(
                    junctionsArray, fitTripletsArray, playerComputer, playerUser,
                    latestUserStoneJunctionNo, latestComputerStoneJunctionNo, activeTripletsList
            );

            junctionsArray[junctionNoToEatUserStone].setOccupiedBy(null);
            playVibration(100);
            setPlayerTurnAndUpdateGameStatus(playerUser);
            playerUser.setHealth(playerUser.getHealth() - 1);
            checkWinner();
            // todo: animate using red color
        } else if (gameStatus == GameStatus.DRAW_STONE) {
            // place a stone at junctionNo
            int junctionNoToPlaceStone = computerLogic.getJunctionNoToPlaceStone(
                    junctionsArray, fitTripletsArray, playerComputer, playerUser,
                    latestUserStoneJunctionNo, latestComputerStoneJunctionNo, activeTripletsList
            );
            computerDrawsOrPlaceStone(junctionNoToPlaceStone);
            setPlayerTurnAndUpdateGameStatus(playerUser); // change turn to user

            // check if with new move, computer fits a triplet
            if (isTripletFit(junctionNoToPlaceStone, playerComputer)) {
                computerEatsStone();
            }
        } else if (gameStatus == GameStatus.PLACE_STONE) {
            // move a stone to adjacent junctionNo
            int pickAndPlaceJunctionArray[] = computerLogic.getPickAndPlaceJunctionArray(
                    junctionsArray, fitTripletsArray, playerComputer, playerUser,
                    latestUserStoneJunctionNo, latestComputerStoneJunctionNo, activeTripletsList
            );
            int pickedJunctionNo = pickAndPlaceJunctionArray[0];
            int placedJunctionNo = pickAndPlaceJunctionArray[1];

            // pick a stone
            junctionsArray[pickedJunctionNo].setOccupiedBy(null);

            // if picked junction is part of triplet then disable it
            if (isPartOfTriplet(pickedJunctionNo)) {
                disableTripletFit(pickedJunctionNo);
            }

            // place the stone to new position
            computerDrawsOrPlaceStone(placedJunctionNo);
            setPlayerTurnAndUpdateGameStatus(playerUser); // change turn to user

            // check if with new move, computer fits a triplet
            if (isTripletFit(placedJunctionNo, playerComputer)) {
                computerEatsStone();
            }

            // todo animate
        }

        // updating game
        invalidate();
    }

    private void playerPlays(int touchedJunctionNo) {
        Junction touchedJunction = junctionsArray[touchedJunctionNo];
        Player occupiedBy = touchedJunction.getOccupiedBy();

        // to avoid unnecessary calling of invalidate()
        boolean gameStatusChanged = false;

        if (gameStatus == GameStatus.EAT_STONE) {
            Log.d(TAG, "GameStatus.EAT_STONE");
            // eat computer's stone
            if (occupiedBy == playerComputer) {
                // check if this stone is not part of triplet
                if (!isPartOfTriplet(touchedJunctionNo)) {
                    junctionsArray[touchedJunctionNo].setOccupiedBy(null);
                    setPlayerTurnAndUpdateGameStatus(playerComputer);
                    // todo: animate
                    playerComputer.setHealth(playerComputer.getHealth() - 1);
                    checkWinner();
                    gameStatusChanged = true;
                }
            }
        } else if (gameStatus == GameStatus.DRAW_STONE) {
            Log.d(TAG, "GameStatus.DRAW_STONE");

            if (occupiedBy == null) {
                userDrawsOrPlaceStone(touchedJunctionNo);
                setPlayerTurnAndUpdateGameStatus(playerComputer); // change turn to computer

                // check if with new move, user fits a triplet
                if (isTripletFit(touchedJunctionNo, playerUser)) {
                    userEatsStone();
                }
                gameStatusChanged = true;
            }
        } else if (gameStatus == GameStatus.PLACE_STONE) {
            Log.d(TAG, "GameStatus.PLACE_STONE");

            // move a stone to adjacent junctionNo
            if (occupiedBy == null
                    && (touchedJunctionNo != pickedStoneJunctionNo)
                    && (isAdjacent(touchedJunctionNo, pickedStoneJunctionNo))) {
                userDrawsOrPlaceStone(touchedJunctionNo);

                // if picked stone was part of triplet, disable it
                disableTripletFit(pickedStoneJunctionNo);

                setPlayerTurnAndUpdateGameStatus(playerComputer); // change turn to computer

                // check if with new move, user fits a triplet
                if (isTripletFit(touchedJunctionNo, playerUser)) {
                    userEatsStone();
                }
                pickedStoneJunctionNo = -1; // making it invalid
                gameStatusChanged = true;
            } else {
                junctionsArray[pickedStoneJunctionNo].setOccupiedBy(playerUser);
                pickedStoneJunctionNo = -1; // marking it invalid
                gameStatusChanged = true;
            }
        }

        // updating game
        if (gameStatusChanged) {
            //playSoundAndVibration(playerUser); // todo
            invalidate();
        }
    }

    private void computerDrawsOrPlaceStone(int junctionNo) {
        junctionsArray[junctionNo].setOccupiedBy(playerComputer);
        latestComputerStoneJunctionNo = junctionNo;
        int stonesLeft = playerComputer.getStonesLeft();
        if ( stonesLeft > 0) {
            stonesLeft--;
            playerComputer.setStonesLeft(stonesLeft);
        }
    }

    private void userDrawsOrPlaceStone(int junctionNo) {
        junctionsArray[junctionNo].setOccupiedBy(playerUser);
        latestUserStoneJunctionNo = junctionNo;
        int stonesLeft = playerUser.getStonesLeft();
        if ( stonesLeft > 0) {
            stonesLeft--;
            playerUser.setStonesLeft(stonesLeft);
        }
    }

    /**
     * @param player next turn will will be assigned to this player
     */
    private void setPlayerTurnAndUpdateGameStatus(Player player) {
        if (player == playerUser) {
            playerUser.setTurn(true);
            playerComputer.setTurn(false);
        } else {
            playerUser.setTurn(false);
            playerComputer.setTurn(true);
        }

        if (player.getStonesLeft() > 0) {
            gameStatus = GameStatus.DRAW_STONE;
        } else {
            gameStatus = GameStatus.PLACE_STONE;
        }
    }

    private void computerEatsStone() {
        if (canEatPlayerStone(playerUser)) {  // can computer eat user's stone
            gameStatus = GameStatus.EAT_STONE;
            playerComputer.setTurn(true);
            playerUser.setTurn(false);
        }
    }

    private void userEatsStone() {
        if (canEatPlayerStone(playerComputer)) {  // can user eat computer's stone
            gameStatus = GameStatus.EAT_STONE;
            playerUser.setTurn(true);
            playerComputer.setTurn(false);

            instructionMessage = "Eat computer's stone";
        }
    }

    private void checkWinner() {
        if (playerUser.getHealth() < 3) {
            winner = playerComputer;
        } else if (playerComputer.getHealth() < 3) {
            winner = playerUser;
        }
    }

    public void initialiseButtons(Button restartButton) {
        restartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                restartGame();
            }
        });
    }
}
