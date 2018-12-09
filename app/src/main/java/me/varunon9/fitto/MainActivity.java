package me.varunon9.fitto;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void playOnline(View view) {

    }

    public void playWithComputer(View view) {

    }

    public void switchToOldVersion(View view) {
        Intent intent = new Intent(MainActivity.this, OldVersionMainActivity.class);
        startActivity(intent);
    }

}
