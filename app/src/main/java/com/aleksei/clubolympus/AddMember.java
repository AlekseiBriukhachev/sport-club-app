package com.aleksei.clubolympus;

import static com.aleksei.clubolympus.db.ClubOlympusContract.MemberEntry;

import android.content.ContentResolver;
import android.content.ContentValues;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class AddMember extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

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

        firstNameInput = findViewById(R.id.first_name_field);
        lastNameInput = findViewById(R.id.last_name_field);
        sportInput = findViewById(R.id.sport_field);
        genderSpinner = findViewById(R.id.gender_spinner);

        spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(spinnerAdapter);

        insertMember();

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedGender = parent.getItemAtPosition(position).toString();
        if (TextUtils.isEmpty(selectedGender)) {
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
            return true;
        } else if (itemId == R.id.delete_member) {
            return true;
        } else if (itemId == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void insertMember() {
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String sport = sportInput.getText().toString().trim();

        ContentValues values = new ContentValues();
        values.put(MemberEntry.COLUMN_FIRST_NAME, firstName);
        values.put(MemberEntry.COLUMN_LAST_NAME, lastName);
        values.put(MemberEntry.COLUMN_SPORT, sport);
        values.put(MemberEntry.COLUMN_GENDER, gender);

        ContentResolver contentResolver = getContentResolver();
        Uri uri = null;
        try {
            uri = contentResolver.insert(MemberEntry.CONTENT_URI, values);
            Toast.makeText(this, "Member saved", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Error with saving member", Toast.LENGTH_SHORT).show();
        }

    }
}