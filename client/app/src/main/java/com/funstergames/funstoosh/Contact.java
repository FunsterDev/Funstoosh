package com.funstergames.funstoosh;

import android.*;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v4.content.ContextCompat;

public class Contact implements Parcelable {
    public String phoneNumber;
    public String name;

    public Contact(Context context, String phoneNumber) {
        this(phoneNumber, getNameByPhoneNumber(context, phoneNumber));
    }

    public Contact(String phoneNumber, String name) {
        this.phoneNumber = phoneNumber;
        this.name = name;
    }

    @Override
    public String toString() {
        return (name != null) ? name : phoneNumber;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!Contact.class.isAssignableFrom(obj.getClass())) return false;
        Contact other = (Contact)obj;
        return phoneNumber.equals(other.phoneNumber);
    }

    @Override
    public int hashCode() {
        return phoneNumber.hashCode();
    }

    public static String getNameByPhoneNumber(Context context, String phoneNumber) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Cursor cursor = context.getContentResolver()
                .query(
                        Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber)),
                        new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null
                );

        try {
            if (cursor.moveToNext()) {
                return cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(phoneNumber);
        dest.writeString(name);
    }

    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel source) {
            return new Contact(source);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    private Contact(Parcel source) {
        phoneNumber = source.readString();
        name = source.readString();
    }
}
