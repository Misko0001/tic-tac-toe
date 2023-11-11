package com.example.iks_oks;

public class GameSession {

    private int playerXScore;
    private int playerOScore;
    private boolean isVisible = true;

    public GameSession() { }

    public int getPlayerXScore() { return playerXScore; }
    public int getPlayerOScore() { return playerOScore; }
    public boolean getIsVisible() { return isVisible; }

    public void setPlayerXScore(int playerXScore) { this.playerXScore = playerXScore; }
    public void setPlayerOScore(int playerOScore) { this.playerOScore = playerOScore; }
    public void setVisible(boolean visible) { isVisible = visible; }
}