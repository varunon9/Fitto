package me.varunon9.fitto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import me.varunon9.fitto.computer.PlayWithComputerActivity;
import me.varunon9.fitto.oldversion.OldVersionMainActivity;

public class MainActivity extends AppCompatActivity {

    private Singleton singleton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        singleton = Singleton.getInstance();
    }

    public void playOnline(View view) {
        String playerName = singleton.getPlayerName();
        if (playerName == null || playerName.isEmpty()) {
            showInputNameDialog();
        } else {
            checkAndMatchPlayer(playerName);
        }
    }

    public void playWithComputer(View view) {
        showComputerPlaysFirstDialog();
    }

    public void switchToOldVersion(View view) {
        Intent intent = new Intent(MainActivity.this, OldVersionMainActivity.class);
        startActivity(intent);
    }

    private void showInputNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your name");

        View inflatedView = LayoutInflater.from(this)
                .inflate(R.layout.input_name_dialog, null);
        builder.setView(inflatedView);

        builder
                .setPositiveButton("Play", (dialog, which) -> {
                    EditText playerNameEditText = inflatedView.findViewById(R.id.playerNameEditText);
                    String playerName = playerNameEditText.getText().toString();
                    if (playerName.isEmpty()) {
                        playerName = AppConstants.DEFAULT_PLAYER_NAME;
                    }
                    singleton.setPlayerName(playerName);
                    checkAndMatchPlayer(playerName);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                });
        builder.show();
    }

    private void showComputerPlaysFirstDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please choose");

        View inflatedView = LayoutInflater.from(this)
                .inflate(R.layout.computer_plays_first_dialog, null);
        builder.setView(inflatedView);

        builder
                .setPositiveButton("Play", (dialog, which) -> {
                    Intent intent = new Intent(MainActivity.this, PlayWithComputerActivity.class);
                    CheckBox computerPlayFirstCheckBox =
                            inflatedView.findViewById(R.id.computerPlaysFirstCheckbox);
                    boolean computerPlaysFirst = computerPlayFirstCheckBox.isChecked();
                    intent.putExtra(AppConstants.COMPUTER_PLAYS_FIRST, computerPlaysFirst);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                });
        builder.show();
    }

    private void checkAndMatchPlayer(String playerName) {
        // todo
    }

}
