package com.example.iks_oks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textViewPlayerX;
    private TextView textViewPlayerO;
    private TextView textViewPlayerXScore;
    private TextView textViewPlayerOScore;
    private ImageButton[] fields = new ImageButton[9];

    private int playerXScore;
    private int playerOScore;
    private int turn;
    private int round;
    int[] table = {0, 0, 0, 0, 0, 0, 0, 0, 0};  // empty = 0, X = 1, O = 2
    private boolean gameSaved;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private GameSession gameSession;
    private long sessionId;

    private AlertDialog.Builder dialogBuilder;

    private AlertDialog dialogOutcome;
    private TextView textViewGameOutcome;
    private Button buttonPlayAgain;
    private Button buttonEndGameSession;

    private AlertDialog dialogExit;
    private Button buttonBackToMenu;
    private Button buttonStay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        initMainComponents();
        initDatabaseComponents();
        initOutcomeDialog();
        initExitDialog();
    }

    private void initMainComponents() {
        textViewPlayerX = findViewById(R.id.textViewPlayerX);
        textViewPlayerO = findViewById(R.id.textViewPlayerO);
        textViewPlayerXScore = findViewById(R.id.textViewPlayerXScore);
        textViewPlayerOScore = findViewById(R.id.textViewPlayerOScore);

        for (int i = 0; i < fields.length; i++) {
            String id = "imageButtonField" + i;
            int rId = getResources().getIdentifier(id, "id", getPackageName());
            fields[i] = findViewById(rId);
            fields[i].setOnClickListener(this);
        }
    }

    private void initDatabaseComponents() {
        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("GameSession");
        gameSession = new GameSession();
        sessionId = 0;

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    sessionId = snapshot.getChildrenCount();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void initOutcomeDialog() {
        dialogBuilder = new AlertDialog.Builder(this);

        final View outcomeDialog = getLayoutInflater().inflate(R.layout.game_outcome_dialog, null);

        textViewGameOutcome = outcomeDialog.findViewById(R.id.textViewGameOutcome);
        buttonPlayAgain = outcomeDialog.findViewById(R.id.buttonPlayAgain);
        buttonEndGameSession = outcomeDialog.findViewById(R.id.buttonEndGameSession);

        buttonPlayAgain.setOnClickListener(this);
        buttonEndGameSession.setOnClickListener(this);

        dialogBuilder.setView(outcomeDialog);
        dialogOutcome = dialogBuilder.create();
        dialogOutcome.setCancelable(false);
        dialogOutcome.setCanceledOnTouchOutside(false);
    }

    private void initExitDialog() {
        final View exitDialog = getLayoutInflater().inflate(R.layout.game_exit_dialog, null);

        buttonBackToMenu = exitDialog.findViewById(R.id.buttonBackToMenu);
        buttonStay = exitDialog.findViewById(R.id.buttonStay);

        buttonBackToMenu.setOnClickListener(this);
        buttonStay.setOnClickListener(this);

        dialogBuilder.setView(exitDialog);
        dialogExit = dialogBuilder.create();
    }

    @Override
    public void onBackPressed() {
        dialogExit.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadSharedPref();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveSharedPref(gameSaved);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonStay:
                dialogExit.dismiss();
                break;
            case R.id.buttonPlayAgain:
                restartRound();
                dialogOutcome.dismiss();
                break;
            case R.id.buttonEndGameSession:
                insertData();
                dialogOutcome.dismiss();
                finish();
                break;
            case R.id.buttonBackToMenu:
                insertData();
                dialogExit.dismiss();
                finish();
                break;
            default:
                playGame(v);
        }
    }

    private void playGame(View v) {
        String id = v.getResources().getResourceEntryName(v.getId());
        int position = Integer.parseInt(id.substring(id.length() - 1));

        if (table[position] != 0) {
            return;
        }

        if (turn % 2 == 0) {
            fields[position].setImageResource(R.drawable.x);
            table[position] = 1;
            if (isWinner(1)) {
                incrementScore(1);
                textViewGameOutcome.setText("Igrač X je pobedio!");
                dialogOutcome.show();
                return;
            } else {
                playerTurn();
            }
        } else {
            fields[position].setImageResource(R.drawable.o);
            table[position] = 2;
            if (isWinner(2)) {
                incrementScore(2);
                textViewGameOutcome.setText("Igrač O je pobedio!");
                dialogOutcome.show();
                return;
            } else {
                playerTurn();
            }
        }
        turn++;

        if (turn == 9 + (round % 2)) {
            textViewGameOutcome.setText("Nerešeno je!");
            dialogOutcome.show();
        }
    }

    private boolean isWinner(int player) {
        return
            (table[0] == player && table[1] == player && table[2] == player) ||
            (table[3] == player && table[4] == player && table[5] == player) ||
            (table[6] == player && table[7] == player && table[8] == player) ||
            (table[0] == player && table[3] == player && table[6] == player) ||
            (table[1] == player && table[4] == player && table[7] == player) ||
            (table[2] == player && table[5] == player && table[8] == player) ||
            (table[0] == player && table[4] == player && table[8] == player) ||
            (table[2] == player && table[4] == player && table[6] == player);
    }

    private void incrementScore(int player) {
        if (player == 1) {
            playerXScore++;
            textViewPlayerXScore.setText(Integer.toString(playerXScore));
        } else if (player == 2) {
            playerOScore++;
            textViewPlayerOScore.setText(Integer.toString(playerOScore));
        }
    }

    private void restartRound() {
        round++;
        if (round % 2 == 0) {
            turn = 0;
            highlightPlayer(1);
        } else {
            turn = 1;
            highlightPlayer(2);
        }
        for (int i = 0; i < fields.length; i++) {
            fields[i].setImageResource(R.drawable.empty);
            table[i] = 0;
        }
    }

    private void playerTurn() {
        if (turn % 2 == 1) {
            highlightPlayer(1);
        } else {
            highlightPlayer(2);
        }
    }

    private void highlightPlayer(int player) {
        if (player == 1) {
            textViewPlayerX.setTypeface(null, Typeface.BOLD);
            textViewPlayerX.setTextColor(ContextCompat.getColor(this, R.color.blue));
            textViewPlayerXScore.setTypeface(null, Typeface.BOLD);
            textViewPlayerXScore.setTextColor(ContextCompat.getColor(this, R.color.blue));

            textViewPlayerO.setTypeface(null, Typeface.NORMAL);
            textViewPlayerO.setTextColor(ContextCompat.getColor(this, R.color.grey));
            textViewPlayerOScore.setTypeface(null, Typeface.NORMAL);
            textViewPlayerOScore.setTextColor(ContextCompat.getColor(this, R.color.grey));
        } else if (player == 2) {
            textViewPlayerO.setTypeface(null, Typeface.BOLD);
            textViewPlayerO.setTextColor(ContextCompat.getColor(this, R.color.blue));
            textViewPlayerOScore.setTypeface(null, Typeface.BOLD);
            textViewPlayerOScore.setTextColor(ContextCompat.getColor(this, R.color.blue));

            textViewPlayerX.setTypeface(null, Typeface.NORMAL);
            textViewPlayerX.setTextColor(ContextCompat.getColor(this, R.color.grey));
            textViewPlayerXScore.setTypeface(null, Typeface.NORMAL);
            textViewPlayerXScore.setTextColor(ContextCompat.getColor(this, R.color.grey));
        }
    }

    private void insertData() {
        if (playerXScore == 0 && playerOScore == 0) {
            turn = 0;
            round = 0;
            Arrays.fill(table, 0);
        } else {
            gameSession.setPlayerXScore(playerXScore);
            gameSession.setPlayerOScore(playerOScore);
            gameSession.setVisible(true);

            ref.child(String.valueOf(sessionId)).setValue(gameSession);
            gameSaved = true;
        }
    }

    private void loadSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences("TableSharedPrefPrefix", 0);

        playerXScore = sharedPreferences.getInt("playerXScore", 0);
        playerOScore = sharedPreferences.getInt("playerOScore", 0);
        turn = sharedPreferences.getInt("turn", 0);
        round = sharedPreferences.getInt("round", 0);

        textViewPlayerXScore.setText(Integer.toString(playerXScore));
        textViewPlayerOScore.setText(Integer.toString(playerOScore));

        if (turn % 2 == 0) {
            highlightPlayer(1);
        } else {
            highlightPlayer(2);
        }

        for (int i = 0; i < table.length; i++) {
            table[i] = sharedPreferences.getInt("id" +  i, 0);
            if (table[i] == 1) {
                fields[i].setImageResource(R.drawable.x);
            } else if (table[i] == 2) {
                fields[i].setImageResource(R.drawable.o);
            }
        }

        gameSaved = false;
    }

    private void saveSharedPref(boolean restart) {
        SharedPreferences sharedPreferences = getSharedPreferences("TableSharedPrefPrefix", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (restart) {
            editor.remove("playerXScore");
            editor.remove("playerOScore");
            editor.remove("turn");
            editor.remove("round");
            for (int i = 0; i < table.length; i++) {
                editor.remove("id" + i);
                fields[i].setImageResource(R.drawable.empty);
            }
            textViewPlayerXScore.setText("0");
            textViewPlayerOScore.setText("0");
        } else {
            editor.putInt("playerXScore", playerXScore);
            editor.putInt("playerOScore", playerOScore);
            editor.putInt("turn", turn);
            editor.putInt("round", round);
            for (int i = 0; i < table.length; i++) {
                editor.putInt("id" + i, table[i]);
            }
        }
        editor.apply();
    }
}
