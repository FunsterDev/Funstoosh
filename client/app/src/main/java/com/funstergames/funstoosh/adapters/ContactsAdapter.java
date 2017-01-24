package com.funstergames.funstoosh.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.funstergames.funstoosh.Constants;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by oded on 1/24/17.
 */

public class ContactsAdapter extends BaseAdapter {
    private Context _context;
    // Name, Phone Number
    private ArrayList<Map.Entry<String, String>> _available = new ArrayList<>();
    private HashSet<String> _selected = new HashSet<>();

    public ContactsAdapter(Context context) {
        _context = context;
        ContentResolver contentResolver = _context.getContentResolver();
        checkNextBatch(contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null));
    }

    public HashSet<String> getSelected() {
        return _selected;
    }

    @Override
    public int getCount() {
        return _available.size();
    }

    @Override
    public Map.Entry<String, String> getItem(int position) {
        return _available.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final CheckBox checkbox;
        if (convertView == null) {
            checkbox = new CheckBox(parent.getContext());

            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        _selected.add((String)checkbox.getTag());
                    } else {
                        _selected.remove(checkbox.getTag());
                    }
                }
            });
        } else {
            checkbox = (CheckBox)convertView;
        }

        Map.Entry<String, String> item = getItem(position);
        checkbox.setText(item.getKey());
        checkbox.setTag(item.getValue());
        checkbox.setChecked(_selected.contains(item.getValue()));

        return checkbox;
    }

    private void checkNextBatch(final Cursor cursor) {
        ContentResolver contentResolver = _context.getContentResolver();

        final HashMap<String, String> numberToName = new HashMap<>();

        while (cursor.moveToNext() && numberToName.size() < 50) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                Cursor phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[] { id }, null);
                while (phoneCursor.moveToNext()) {
                    String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    numberToName.put(phoneNumber, name);
                }
                phoneCursor.close();
            }
        }

        if (cursor.isAfterLast()) cursor.close();
        if (numberToName.isEmpty()) return;

        Ion.with(_context)
                .load(Constants.ROOT_URL + "/users")
                .addQuery("phone_numbers", StringUtils.join(numberToName.keySet(), ','))
                .asJsonArray()
                .withResponse()
                .setCallback(new FutureCallback<Response<JsonArray>>() {
                    @Override
                    public void onCompleted(Exception e, Response<JsonArray> result) {
                        if (e != null || result.getHeaders().code() != 200) return;

                        for (JsonElement phoneNumber : result.getResult()) {
                            _available.add(new AbstractMap.SimpleEntry<>(
                                    numberToName.get(phoneNumber.getAsString()),
                                    phoneNumber.getAsString()
                            ));
                        }
                        notifyDataSetChanged();

                        if (!cursor.isClosed()) checkNextBatch(cursor);
                    }
                });
    }
}
