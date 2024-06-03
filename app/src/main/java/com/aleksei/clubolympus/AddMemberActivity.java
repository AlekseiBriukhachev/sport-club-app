package com.aleksei.clubolympus;

import static com.aleksei.clubolympus.db.ClubOlympusContract.MemberEntry;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;


public class AddMemberActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EDIT_MEMBER_LOADER = 1111;
    private Uri currentMemberUri;
    private EditText firstNameInput, lastNameInput, sportInput;
    private Spinner genderSpinner;
    private int gender = 0;
    private ArrayAdapter<CharSequence> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_member);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        currentMemberUri = intent.getData();
        if (currentMemberUri == null) {
            setTitle("Add a Member");
            invalidateOptionsMenu();
        } else {
            setTitle("Edit a Member");
            getSupportLoaderManager().initLoader(EDIT_MEMBER_LOADER, null, this);
        }

        firstNameInput = findViewById(R.id.first_name_field);
        lastNameInput = findViewById(R.id.last_name_field);
        sportInput = findViewById(R.id.sport_field);
        genderSpinner = findViewById(R.id.gender_spinner);
        genderSpinner.setOnItemSelectedListener(this);

        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(spinnerAdapter);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentMemberUri == null) {
            MenuItem menuItem = menu.findItem(R.id.delete_member);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedGender = parent.getItemAtPosition(position).toString();
        if (!TextUtils.isEmpty(selectedGender)) {
            gender = switch (selectedGender) {
                case "Male" -> MemberEntry.GENDER_MALE;
                case "Female" -> MemberEntry.GENDER_FEMALE;
                default -> MemberEntry.GENDER_UNKNOWN;
            };
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        gender = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_member_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.save_member) {
            saveMember();
            return true;
        } else if (itemId == R.id.delete_member) {
            showDeleteMemberDialog();
            return true;
        } else if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteMemberDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Delete this member?")
                .setPositiveButton("Delete", (dialog, which) -> deleteMember())
                .setNegativeButton("Cancel", (dialog, which) -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.show();
    }

    private void deleteMember() {
        if (currentMemberUri != null) {
            try {
                int rowsDeleted = getContentResolver().delete(currentMemberUri, null, null);
                if (rowsDeleted == 0) {
                    Toast.makeText(this, "Error with deleting member", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Member deleted", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Member deleting error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void saveMember() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String sport = sportInput.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(MemberEntry.COLUMN_FIRST_NAME, firstName);
        values.put(MemberEntry.COLUMN_LAST_NAME, lastName);
        values.put(MemberEntry.COLUMN_SPORT, sport);
        values.put(MemberEntry.COLUMN_GENDER, gender);

        if (currentMemberUri == null) {
            ContentResolver contentResolver = getContentResolver();
            Uri uri = null;
            try {
                uri = contentResolver.insert(MemberEntry.CONTENT_URI, values);
                Toast.makeText(this, "Member saved", Toast.LENGTH_SHORT).show();
            } catch (IllegalArgumentException e) {
                Toast.makeText(this, "Error with saving member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            try {
                int rowsChanged = getContentResolver().update(currentMemberUri, values, null, null);
                if (rowsChanged > 0) {
                    Toast.makeText(this, "Member updated", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error with updating member: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = {
                MemberEntry._ID,
                MemberEntry.COLUMN_FIRST_NAME,
                MemberEntry.COLUMN_LAST_NAME,
                MemberEntry.COLUMN_GENDER,
                MemberEntry.COLUMN_SPORT};

        return new CursorLoader(this, currentMemberUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        if (data.moveToFirst()) {
            int currentFirstNameIndex = data.getColumnIndex(MemberEntry.COLUMN_FIRST_NAME);
            int currentLastNameIndex = data.getColumnIndex(MemberEntry.COLUMN_LAST_NAME);
            int currentGenderIndex = data.getColumnIndex(MemberEntry.COLUMN_GENDER);
            int currentSportIndex = data.getColumnIndex(MemberEntry.COLUMN_SPORT);

            String currentFirstName = data.getString(currentFirstNameIndex);
            String currentLastName = data.getString(currentLastNameIndex);
            int currentGender = data.getInt(currentGenderIndex);
            String currentSport = data.getString(currentSportIndex);

            firstNameInput.setText(currentFirstName);
            lastNameInput.setText(currentLastName);
            genderSpinner.setSelection(currentGender);
            sportInput.setText(currentSport);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}