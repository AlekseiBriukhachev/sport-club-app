package com.aleksei.clubolympus.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aleksei.clubolympus.db.ClubOlympusContract.MemberEntry;

import java.util.Objects;

public class OlympusContentProvider extends ContentProvider {
    private DatabaseHandler databaseHandler;
    private static final int MEMBERS = 111;
    private static final int MEMBER_ID = 222;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(ClubOlympusContract.AUTHORITY, ClubOlympusContract.PATH_MEMBERS, MEMBERS);
        uriMatcher.addURI(ClubOlympusContract.AUTHORITY, ClubOlympusContract.PATH_MEMBERS + "/#", MEMBER_ID);
    }

    @Override
    public boolean onCreate() {
        databaseHandler = new DatabaseHandler(getContext());
        return true;
    }


    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = databaseHandler.getReadableDatabase();
        int match = uriMatcher.match(uri);

        Cursor cursor;
        cursor =  switch (match) {
            case MEMBERS ->
                    db.query(MemberEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case MEMBER_ID -> {
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                yield db.query(MemberEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            }
            default -> throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        };
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);
        return cursor;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        int match = uriMatcher.match(uri);
        return switch (match) {
            case MEMBERS -> MemberEntry.CONTENT_LIST_TYPE;
            case MEMBER_ID -> MemberEntry.CONTENT_ITEM_TYPE;
            default -> throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        };
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        validateFields(values);

        SQLiteDatabase db = databaseHandler.getWritableDatabase();
        int match = uriMatcher.match(uri);
        if (match == MEMBERS) {
            long id = db.insert(MemberEntry.TABLE_NAME, null, values);
            if (id == -1) {
                Log.e("insertMethod", "Insertion failed for " + uri);
                throw new IllegalArgumentException("Insertion failed for " + uri);
            }
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
            return Uri.withAppendedPath(uri, String.valueOf(id));
        }
        throw new IllegalArgumentException("Insertion is not supported for " + uri);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {

       validateFields(values);

        SQLiteDatabase db = databaseHandler.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int rowsUpdated = switch (match) {
            case MEMBERS -> db.update(MemberEntry.TABLE_NAME, values, selection, selectionArgs);
            case MEMBER_ID -> {
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                yield db.update(MemberEntry.TABLE_NAME, values, selection, selectionArgs);
            }
            default -> throw new IllegalArgumentException("Cannot update unknown URI " + uri);
        };

        if (rowsUpdated != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = databaseHandler.getWritableDatabase();
        int match = uriMatcher.match(uri);
        int rowsDeleted = switch (match) {
            case MEMBERS ->
                db.delete(MemberEntry.TABLE_NAME, selection, selectionArgs);
            case MEMBER_ID -> {
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                yield db.delete(MemberEntry.TABLE_NAME, selection, selectionArgs);
            }
            default -> throw new IllegalArgumentException("Cannot delete unknown URI " + uri);
        };
        if (rowsDeleted != 0) {
            Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    private boolean isValidGender(Integer gender) {
        return gender == MemberEntry.GENDER_MALE || gender == MemberEntry.GENDER_FEMALE;
    }

    private void validateFields(ContentValues values) {
        String firstName = Objects.requireNonNull(values).getAsString(MemberEntry.COLUMN_FIRST_NAME);
        if (firstName.isEmpty()) {
            throw new IllegalArgumentException("Member requires a first name");
        }

        String lastName = values.getAsString(MemberEntry.COLUMN_LAST_NAME);
        if (lastName.isEmpty()) {
            throw new IllegalArgumentException("Member requires a last name");
        }

        Integer gender = values.getAsInteger(MemberEntry.COLUMN_GENDER);
        if (isValidGender(gender)) {
            throw new IllegalArgumentException("Choose the gender");
        }

        String sport = values.getAsString(MemberEntry.COLUMN_SPORT);
        if (sport.isEmpty()) {
            throw new IllegalArgumentException("Member requires a sport");
        }
    }
}
