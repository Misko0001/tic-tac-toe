package com.example.iks_oks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ListAdapter extends ArrayAdapter<GameSession> {

    public ListAdapter(Context context, ArrayList<GameSession> gameSessionArrayList) {
        super(context,R.layout.game_history_item, gameSessionArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        GameSession gameSession = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.game_history_item, parent, false);
        }

        TextView textViewXScore = convertView.findViewById(R.id.textViewXScore);
        TextView textViewOScore = convertView.findViewById(R.id.textViewOScore);

        textViewXScore.setText(Integer.toString(gameSession.getPlayerXScore()));
        textViewOScore.setText(Integer.toString(gameSession.getPlayerOScore()));

        return convertView;
    }
}