package com.example.iks_oks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonPlay;
    private Button buttonGameHistory;
    private Button buttonLeaveGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    private void initComponents() {
        buttonPlay = findViewById(R.id.buttonPlay);
        buttonGameHistory = findViewById(R.id.buttonGameHistory);
        buttonLeaveGame = findViewById(R.id.buttonLeaveGame);

        buttonPlay.setOnClickListener(this);
        buttonGameHistory.setOnClickListener(this);
        buttonLeaveGame.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonPlay:
                startActivity(new Intent(this, GameActivity.class));
                break;
            case R.id.buttonGameHistory:
                startActivity(new Intent(this, GameHistoryActivity.class));
                break;
            case R.id.buttonLeaveGame:
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(homeIntent);
        }
    }
}