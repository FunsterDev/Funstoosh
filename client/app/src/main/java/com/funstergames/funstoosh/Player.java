package com.funstergames.funstoosh;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class Player {
    public String phoneNumber;
    public String name;
    public int score = 0;

    public Player() {
    }

    public Player(Context context, String phoneNumber) {
        this.phoneNumber = phoneNumber;

        // Getting contact name
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                        new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null
                );

        if (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
        }

        cursor.close();
    }

    @Override
    public String toString() {
        return (name != null) ? name : phoneNumber;
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
