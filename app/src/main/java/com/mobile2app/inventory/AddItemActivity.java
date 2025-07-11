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

public class AddItemActivity extends AppCompatActivity {

    private EditText itemNameEditText;
    private EditText itemDescriptionEditText;
    private EditText itemQuantityEditText;
    private Button saveButton;
    private ImageButton closeButton;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);

        itemNameEditText = findViewById(R.id.addNewItemNameEditText);
        itemDescriptionEditText = findViewById(R.id.addNewItemDescriptionEditText);
        itemQuantityEditText = findViewById(R.id.addNewItemQuantityEditText);
        saveButton = findViewById(R.id.addItemButton);
        closeButton = findViewById(R.id.addItemCloseButton);

        db = new DatabaseHelper(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        saveButton.setOnClickListener(v -> {
            String name = itemNameEditText.getText().toString();
            String description = itemDescriptionEditText.getText().toString();
            String quantity = itemQuantityEditText.getText().toString();

            if (name.isEmpty() || description.isEmpty() || quantity.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                Log.d("AddItemActivity", "Please fill in all fields");
                return;
            }

            int quantityInt = Integer.parseInt(quantity);

            InventoryItem item = new InventoryItem(name, description, quantityInt);
            db.insertInventory(item);

            finish();

            Intent intent = new Intent(this, InventoryActivity.class);
            startActivity(intent);
        });

        closeButton.setOnClickListener(v -> {
            finish();
        });



    }
}