package com.mobile2app.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditItemActivity extends AppCompatActivity {

    private EditText editItemNameEditText;
    private EditText editItemDescriptionEditText;
    private EditText editItemQuantityEditText;
    private Button saveButton;
    private ImageButton closeButton;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_item);

        editItemNameEditText = findViewById(R.id.editItemNameEditText);
        editItemDescriptionEditText = findViewById(R.id.editItemDescriptionEditText);
        editItemQuantityEditText = findViewById(R.id.editQuantityEditText);
        saveButton = findViewById(R.id.editItemButton);
        closeButton = findViewById(R.id.editItemCloseButton);

        db = new DatabaseHelper(this);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String description = intent.getStringExtra("description");
        int quantity = intent.getIntExtra("quantity", 0);

        editItemNameEditText.setText(name);
        editItemDescriptionEditText.setText(description);
        editItemQuantityEditText.setText(String.valueOf(quantity));

        saveButton.setOnClickListener(v -> {
            String newName = editItemNameEditText.getText().toString().trim();
            String newDescription = editItemDescriptionEditText.getText().toString().trim();
            String newQuantityString = editItemQuantityEditText.getText().toString().trim();

            // Validate input
            if (newName.isEmpty() || newDescription.isEmpty() || newQuantityString.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            int newQuantity;

            try {
                newQuantity = Integer.parseInt(newQuantityString);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                return;
            }

            InventoryItem item = new InventoryItem(newName, newDescription, newQuantity);

            try {
                db.updateInventory(item);
                finish();
            } catch (Exception e) {
                Log.e("EditItemActivity", "Error updating inventory", e);
                Toast.makeText(this, "Error updating inventory", Toast.LENGTH_SHORT).show();
            }
        });

        closeButton.setOnClickListener(v -> {
            finish();
        });

    }

}