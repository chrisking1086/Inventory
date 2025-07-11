package com.mobile2app.inventory;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private Button registerButton;
    private int failedAttempts = 0;
    private static final String PREFS_NAME = "myprefs";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String LOGGED_IN_USER = "loggedInUsername";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize views after setContentView()
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up login button click listener
        loginButton.setOnClickListener(v -> {
            // Get username and password from EditTexts
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // Create a DatabaseHelper instance
            DatabaseHelper db = new DatabaseHelper(this);

            // Check if the account exists
            if (!db.checkAccountExists(username)) {
                Toast.makeText(this, "Account does not exist", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify the account credentials
            try {
                if (db.verifyAccount(username, password)) {
                    // Reset failed attempts on successful login
                    failedAttempts = 0;

                    // Store login state and username in SharedPreferences
                    SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(IS_LOGGED_IN, true);
                    editor.putString(LOGGED_IN_USER, username);
                    editor.apply();
                    Log.d("LoginActivity", "Login successful for user: " + username);
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                    // Navigate to InventoryActivity
                    Intent intent = new Intent(this, InventoryActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Show error message for invalid credentials
                    Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();

                    // Increment failed attempts counter
                    failedAttempts++;
                    if (failedAttempts >= 3) {
                        // Disable login functionality after 3 failed attempts
                        Toast.makeText(this, "Too many failed attempts", Toast.LENGTH_SHORT).show();
                        usernameEditText.setEnabled(false);
                        passwordEditText.setEnabled(false);
                        loginButton.setEnabled(false);
                        db.deactivateAccount(username);
                        finish();
                    }
                }
            } catch (Exception e) {
                // Handle potential exceptions during database operations
                Log.e("LoginActivity", "Error during login", e);
                Toast.makeText(this, "An error occurred during login", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up register button click listener
        registerButton.setOnClickListener(v -> {
            // Navigate to RegistrationActivity
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        });
    }
}