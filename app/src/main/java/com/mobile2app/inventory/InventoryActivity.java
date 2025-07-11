package com.mobile2app.inventory;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class InventoryActivity extends AppCompatActivity {
    private List<InventoryItem> inventoryList;
    private InventoryAdapter inventoryAdapter;
    private TextView inventoryListTextView;
    private DatabaseHelper db;
    private Button addItemButton;
    private Button editAccountButton;
    private RecyclerView inventoryRecyclerView;
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventory);


        addItemButton = findViewById(R.id.addItemButton);
        editAccountButton = findViewById(R.id.editAccountButton);
        inventoryRecyclerView = findViewById(R.id.inventoryRecyclerView);
        inventoryListTextView = findViewById(R.id.inventoryListTextView);

        db = new DatabaseHelper(this);
        inventoryList = new ArrayList<>();
        inventoryList.addAll(db.getInventoryList());

        inventoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        inventoryAdapter = new InventoryAdapter(inventoryList, db);
        inventoryRecyclerView.setAdapter(inventoryAdapter);

        // Add divider item decoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(inventoryRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        Drawable divider = ContextCompat.getDrawable(this, R.drawable.divider);
        if (divider != null) {
            dividerItemDecoration.setDrawable(divider);
        }
        inventoryRecyclerView.addItemDecoration(dividerItemDecoration);

        ViewCompat.setOnApplyWindowInsetsListener(inventoryListTextView, (v, insets) -> {
            DisplayCutoutCompat cutoutInsets = insets.getDisplayCutout();
            if (cutoutInsets != null) {
                int topInset = cutoutInsets.getSafeInsetTop();
                v.setPadding(v.getPaddingLeft(), topInset, v.getPaddingRight(), v.getPaddingBottom());
            }
            return insets;
        });

        inventoryAdapter.setOnItemClickListener(new InventoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                InventoryItem item = inventoryList.get(position);
                Intent intent = new Intent(InventoryActivity.this, EditItemActivity.class);
                intent.putExtra("name", item.getItemName());
                intent.putExtra("description", item.getItemDescription());
                intent.putExtra("quantity", item.getItemQuantity());
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(int position) {
                InventoryItem item = inventoryList.get(position);
                inventoryAdapter.deleteItem(item);
            }

            @Override
            public void onDeleteClick(InventoryItem item) {
                inventoryAdapter.deleteItem(item);
            }


        });

        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        editAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(InventoryActivity.this, AccountActivity.class);
            startActivity(intent);
        });

        // Request SMS permission
        requestSmsPermission();
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now send SMS
                Log.d("InventoryActivity", "SMS permission granted");
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied
                Log.e("InventoryActivity", "SMS permission denied");
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateRecyclerView() {
        inventoryList.clear();
        inventoryList.addAll(db.getInventoryList());
        inventoryAdapter.setInventoryList(inventoryList);
    }

    protected void onResume() {
        super.onResume();
        updateRecyclerView();
    }
}