package com.mobile2app.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private EditText nameEditText;
    private EditText passwordEditText;
    private EditText verifyPasswordEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private Button registerButton;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        nameEditText = findViewById(R.id.newUsernameEditText);
        passwordEditText = findViewById(R.id.newPasswordEditText);
        verifyPasswordEditText = findViewById(R.id.newPasswordVerifyEditText);
        emailEditText = findViewById(R.id.newEmailEditText);
        phoneEditText = findViewById(R.id.newPhoneEditText);
        registerButton = findViewById(R.id.newRegisterButton);
        db = new DatabaseHelper(this);

        registerButton.setOnClickListener(v -> {
            String accountName = getText(nameEditText);
            String accountPassword = getText(passwordEditText);
            String accountVerifyPassword = getText(verifyPasswordEditText);
            String accountEmail = getText(emailEditText);
            String accountPhone = getText(phoneEditText);
            int accountEnabled = 1;

            // Validate input
            if (!validateInput(accountName, accountPassword, accountVerifyPassword, accountEmail, accountPhone)) {
                return;
            }

            // Insert the account into the database
            try {
                if (db.checkAccountExists(accountName)) {
                    Toast.makeText(this, "Account already exists", Toast.LENGTH_SHORT).show();
                    return;
                }
                String hashedPassword = db.hashPassword(accountPassword);
                db.insertAccount(accountName, hashedPassword, accountEmail, accountPhone, accountEnabled);
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                Log.d("RegistrationActivity", "Account created successfully");

                // Navigate to LoginActivity
                finish();
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                // Handle potential exceptions during database operations
                Log.e("RegistrationActivity", "Error during registration", e);
                Toast.makeText(this, "An error occurred during registration", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getText(EditText editText) {
        return editText.getText().toString().trim();
    }

    private boolean validateInput(String name, String password, String verifyPassword, String email, String phone) {
        if (name.isEmpty() || password.isEmpty() || verifyPassword.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(verifyPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Pattern.matches("[0-9]+", phone)) {
            Toast.makeText(this, "Invalid phone number format", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}