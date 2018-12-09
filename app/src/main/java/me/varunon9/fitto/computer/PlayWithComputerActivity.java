package me.varunon9.fitto.computer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import me.varunon9.fitto.R;

public class PlayWithComputerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_with_computer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button restartButton = findViewById(R.id.restartButton);
        PlayWithComputerCanvasBoardView playWithComputerCanvasBoardView =
                findViewById(R.id.playWithComputerCanvasBoardView);
        playWithComputerCanvasBoardView.initialiseButtons(restartButton);
    }

}
