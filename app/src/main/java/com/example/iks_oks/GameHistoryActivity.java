package com.example.iks_oks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.iks_oks.databinding.ActivityGameHistoryBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class GameHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityGameHistoryBinding binding;

    private FirebaseDatabase database;
    private DatabaseReference ref;
    private long length;
    private int arrLength;
    private String[][] array;

    private String id;
    private String xScore;
    private String oScore;
    private String isVisible;

    private AlertDialog.Builder dialogBuilder;

    private AlertDialog progressDialog;
    private boolean userDismiss;

    private AlertDialog optionsDialog;
    private TextView textViewPlayerXScore;
    private TextView textViewPlayerOScore;
    private Button buttonDelete;
    private Button buttonBack;
    private int optionItemId;

    private EditText editTextFilter;
    private Button buttonFilter;
    private int filter;
    private int maxItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGameHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog();
        loadFilter();
        readData();
        options();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.buttonFilter:
                saveFilter();
                readData();
                break;
            case R.id.buttonDelete:
                deleteItem();
                optionsDialog.dismiss();
                readData();
                break;
            case R.id.buttonBack:
                optionsDialog.dismiss();
                break;
        }
    }

    private void readData() {
        progressDialog.show();

        database = FirebaseDatabase.getInstance("https://iks-oks-8eecc-default-rtdb.europe-west1.firebasedatabase.app/");
        ref = database.getReference().child("GameSession");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                length = snapshot.getChildrenCount();
                arrLength = 0;
                maxItems = 0;

                for (int i = 0; i < length; i++) {
                    isVisible = snapshot.child(i + "").child("isVisible").getValue().toString();
                    if (isVisible.equals("true")) {
                        if (arrLength < filter) {
                            arrLength++;
                        }
                        maxItems++;
                    }
                }

                array = new String[arrLength][4];
                int arrId = 0;

                for (int i = (int)length - 1; i >= 0; i--) {
                    id = i + "";
                    xScore = snapshot.child(i + "").child("playerXScore").getValue().toString();
                    oScore = snapshot.child(i + "").child("playerOScore").getValue().toString();
                    isVisible = snapshot.child(i + "").child("isVisible").getValue().toString();

                    if (isVisible.equals("true") && arrId < arrLength) {
                        array[arrId][0] = id;
                        array[arrId][1] = xScore;
                        array[arrId][2] = oScore;
                        array[arrId][3] = isVisible;
                        arrId++;
                    }
                }
                insertIntoList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void insertIntoList() {
        ArrayList<GameSession> gameSessionArrayList = new ArrayList<>();

        for (int i = 0; i < arrLength; i++) {
            GameSession gameSession = new GameSession();

            gameSession.setPlayerXScore(Integer.parseInt(array[i][1]));
            gameSession.setPlayerOScore(Integer.parseInt(array[i][2]));

            gameSessionArrayList.add(gameSession);
        }

        ListAdapter listAdapter = new ListAdapter(this, gameSessionArrayList);
        binding.listViewGameHistory.setAdapter(listAdapter);

        userDismiss = false;
        progressDialog.dismiss();
    }

    private void progressDialog() {
        dialogBuilder = new AlertDialog.Builder(this);
        final View progress = getLayoutInflater().inflate(R.layout.progress_dialog, null);

        dialogBuilder.setView(progress);
        progressDialog = dialogBuilder.create();
        progressDialog.setCanceledOnTouchOutside(false);

        userDismiss = true;
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (userDismiss) {
                    finish();
                }
            }
        });
    }

    private void loadFilter() {
        editTextFilter = findViewById(R.id.editTextFilter);
        buttonFilter = findViewById(R.id.buttonFilter);
        buttonFilter.setOnClickListener(this);

        SharedPreferences sharedPreferences = getSharedPreferences("FilterSharedPrefPrefix", 0);
        filter = sharedPreferences.getInt("filter", 0);
        if (filter != 0) {
            editTextFilter.setHint("Filtriraj prikaz " + "(" + filter + ")");
        }
    }

    private void saveFilter() {
        filter = Integer.parseInt(editTextFilter.getText().toString());
        if (filter > maxItems) {
            filter = maxItems;
        }
        editTextFilter.setHint("Filtriraj prikaz " + "(" + filter + ")");
        editTextFilter.setText("");

        SharedPreferences sharedPreferences = getSharedPreferences("FilterSharedPrefPrefix", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("filter", filter);
        editor.apply();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextFilter.getWindowToken(), 0);
    }

    private void options() {
        binding.listViewGameHistory.setClickable(true);
        binding.listViewGameHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                optionItemId = (int)id;
                optionsDialog();
                optionsDialog.show();
            }
        });
    }

    private void optionsDialog() {
        final View options = getLayoutInflater().inflate(R.layout.game_history_options, null);

        textViewPlayerXScore = options.findViewById(R.id.textViewPlayerXScore);
        textViewPlayerOScore = options.findViewById(R.id.textViewPlayerOScore);

        textViewPlayerXScore.setText(array[optionItemId][1]);
        textViewPlayerOScore.setText(array[optionItemId][2]);

        buttonDelete = options.findViewById(R.id.buttonDelete);
        buttonBack = options.findViewById(R.id.buttonBack);

        buttonDelete.setOnClickListener(this);
        buttonBack.setOnClickListener(this);

        dialogBuilder.setView(options);
        optionsDialog = dialogBuilder.create();
    }

    private void deleteItem() {
        GameSession gameSession = new GameSession();

        gameSession.setPlayerXScore(Integer.parseInt(array[optionItemId][1]));
        gameSession.setPlayerOScore(Integer.parseInt(array[optionItemId][2]));
        gameSession.setVisible(false);

        ref.child(String.valueOf(array[optionItemId][0])).setValue(gameSession);

        if (maxItems == filter) {
            maxItems--;
            editTextFilter.setHint("Filtriraj prikaz " + "(" + maxItems + ")");
        }
    }
}