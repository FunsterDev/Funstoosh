package com.funstergames.funstoosh;

import android.content.Context;

public class Player extends Contact {
    public static final int SCORE_REQUIRED_FOR_PICTURE = 15;
    public static final int SCORE_REQUIRED_FOR_MAGIC_WAND = 10;

    public int score = 0;

    public enum State {
        PLAYING,
        WON,
        LOST,
    }

    public State state = State.PLAYING;

    public Player(Context context, String phoneNumber) {
        super(context, phoneNumber);
    }

    public void addedPicture() {
        score += 20;
    }

    public void usedPicture() {
        score -= SCORE_REQUIRED_FOR_PICTURE;
    }

    public void usedMagicWand() {
        score -= SCORE_REQUIRED_FOR_MAGIC_WAND;
    }

    public void win() {
        state = State.WON;
        score += 20;
    }

    public void lose() {
        state = State.LOST;
    }

    public void playerWon() {
    }

    public void playerLost() {
        score += 10;
    }
}
