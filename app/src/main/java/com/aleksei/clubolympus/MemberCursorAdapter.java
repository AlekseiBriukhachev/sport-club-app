package com.aleksei.clubolympus;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.aleksei.clubolympus.db.ClubOlympusContract.MemberEntry;

public class MemberCursorAdapter extends CursorAdapter {
    public MemberCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.member_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView idTextView = view.findViewById(R.id.idMemberTextView);
        TextView firstNameTextView = view.findViewById(R.id.firstNameMemberTextView);
        TextView lastNameTextView = view.findViewById(R.id.lastNameMemberTextView);
        TextView genderTextView = view.findViewById(R.id.genderMemberTextView);
        TextView sportTextView = view.findViewById(R.id.sportMemberTextView);

        int currentID = cursor.getInt(cursor.getColumnIndexOrThrow(MemberEntry._ID));
        String currentFirstName = cursor.getString(cursor.getColumnIndexOrThrow(MemberEntry.COLUMN_FIRST_NAME));
        String currentLastName = cursor.getString(cursor.getColumnIndexOrThrow(MemberEntry.COLUMN_LAST_NAME));
        int currentGender = cursor.getInt(cursor.getColumnIndexOrThrow(MemberEntry.COLUMN_GENDER));
        String currentSport = cursor.getString(cursor.getColumnIndexOrThrow(MemberEntry.COLUMN_SPORT));

        idTextView.setText(String.valueOf(currentID));
        firstNameTextView.setText(currentFirstName);
        lastNameTextView.setText(currentLastName);
        genderTextView.setText(String.valueOf(currentGender));
        sportTextView.setText(currentSport);

    }
}
