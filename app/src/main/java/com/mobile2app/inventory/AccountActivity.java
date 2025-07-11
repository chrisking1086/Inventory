package com.mobile2app.inventory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class AccountActivity extends AppCompatActivity {

    private TextView currentUsernameTextView;
    private EditText editPasswordEditText;
    private EditText editPasswordVerifyEditText;
    private EditText editEmailEditText;
    private EditText editPhoneEditText;
    private Button saveButton;
    private ImageButton closeButton;
    protected DatabaseHelper db;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "myprefs";
    private static final String LOGGED_IN_USER = "loggedInUsername";
    private static final String IS_LOGGED_IN = "isLoggedIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account);

        currentUsernameTextView = findViewById(R.id.currentUsernameTextView);
        editPasswordEditText = findViewById(R.id.editPasswordEditText);
        editPasswordVerifyEditText = findViewById(R.id.editPasswordVerifyEditText);
        editEmailEditText = findViewById(R.id.editEmailEditText);
        editPhoneEditText = findViewById(R.id.editPhoneEditText);
        saveButton = findViewById(R.id.editAccountButton);
        closeButton = findViewById(R.id.editAccountCloseButton);

        db = new DatabaseHelper(this);

        // Load user data from SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String loggedInUsername = sharedPreferences.getString(LOGGED_IN_USER, "");
        currentUsernameTextView.setText(loggedInUsername);

        saveButton.setOnClickListener(v -> {
            String newPassword = editPasswordEditText.getText().toString().trim();
            String newPasswordVerify = editPasswordVerifyEditText.getText().toString().trim();
            String newEmail = editEmailEditText.getText().toString().trim();
            String newPhone = editPhoneEditText.getText().toString().trim();

            // Validate input
            if (newPassword.isEmpty() || newPasswordVerify.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(newPasswordVerify)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                db.updateAccount(loggedInUsername, newPassword, newEmail, newPhone, 1);
                Toast.makeText(this, "Account updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } catch (Exception e) {
                Log.e("AccountActivity", "Error updating account", e);
                Toast.makeText(this, "Error updating account", Toast.LENGTH_SHORT).show();
            }
        });

        closeButton.setOnClickListener(v -> {
            finish();
        });
    }
}