package com.funstergames.funstoosh;

import android.content.Context;

public class Player extends Contact {
    public int score = 0;

    public Player(Context context, String phoneNumber) {
        super(context, phoneNumber);
    }

    public Player(String phoneNumber, String name) {
        super(phoneNumber, name);
    }

    public void addedPicture() {
        score += 0;
    }

    public void usedPicture() {
        score -= 0;
    }

    public void usedMagicWand() {
        score -= 0;
    }

    public void win() {
        score += 0;
    }

    public void lose() {
        score -= 0;
    }
}
