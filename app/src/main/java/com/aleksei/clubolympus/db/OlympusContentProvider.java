package com.aleksei.clubolympus.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aleksei.clubolympus.db.ClubOlympusContract.MemberEntry;

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
        return switch (match) {
            case MEMBERS ->
                    db.query(MemberEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            case MEMBER_ID -> {
                selection = MemberEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(uri)};
                yield db.query(MemberEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
            }
            default -> throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        };
    }

    
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = databaseHandler.getWritableDatabase();
        int match = uriMatcher.match(uri);
        if (match == MEMBERS) {
            long id = db.insert(MemberEntry.TABLE_NAME, null, values);
            if (id == -1) {
                Log.e("insertMethod", "Insertion failed for " + uri);
                throw new IllegalArgumentException("Insertion failed for " + uri);
            }
            return Uri.withAppendedPath(uri, String.valueOf(id));
        }
        throw new IllegalArgumentException("Insertion is not supported for " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
