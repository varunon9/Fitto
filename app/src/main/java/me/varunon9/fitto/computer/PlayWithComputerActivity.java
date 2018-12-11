package me.varunon9.fitto.computer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import me.varunon9.fitto.AppConstants;
import me.varunon9.fitto.R;

public class PlayWithComputerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_with_computer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        boolean computerPlayFirst =
                getIntent().getBooleanExtra(AppConstants.COMPUTER_PLAYS_FIRST, true);
        Button restartButton = findViewById(R.id.restartButton);
        PlayWithComputerCanvasBoardView playWithComputerCanvasBoardView =
                findViewById(R.id.playWithComputerCanvasBoardView);
        playWithComputerCanvasBoardView.initialiseButtons(restartButton);
        playWithComputerCanvasBoardView.setWhoPlaysFirst(computerPlayFirst);
    }
}
