package com.mobile2app.inventory;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.SmsManager;
import android.util.Log;

import org.mindrot.jbcrypt.BCrypt;
import android.content.ContentValues;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;
    private static final String PREFS_NAME = "myprefs";
    private static final String LOGGED_IN_USER = "loggedInUsername";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String SMS_PERMISSION = "android.permission.SEND_SMS";
    private static final int SMS_PERMISSION_REQUEST_CODE = 100;
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }


    private static class AccountTable{
        private static final String TableName = "accounts";
        private static final String ColumnId = "_id";
        private static final String ColumnName = "name";
        private static final String ColumnPassword = "password";
        private static final String ColumnEmail = "email";
        private static final String ColumnPhone = "phone";
        private static final String ColumnEnabled = "enabled";
    }

    private static class InventoryTable{
        private static final String TableName = "inventory";
        private static final String ColumnId = "_id";
        private static final String ColumnName = "name";
        private static final String ColumnDescription = "description";
        private static final String ColumnQuantity = "quantity";
    }

    // SQL statements for creating tables
    private static final String SQL_CREATE_INVENTORY_TABLE =
            "CREATE TABLE " +
                    InventoryTable.TableName + " (" +
                    InventoryTable.ColumnId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    InventoryTable.ColumnName + " TEXT, " +
                    InventoryTable.ColumnDescription + " TEXT, " +
                    InventoryTable.ColumnQuantity + " INTEGER)";

    private static final String SQL_CREATE_ACCOUNT_TABLE =
            "CREATE TABLE " +
                    AccountTable.TableName + " (" +
                    AccountTable.ColumnId + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    AccountTable.ColumnName + " TEXT, " +
                    AccountTable.ColumnPassword + " TEXT, " +
                    AccountTable.ColumnEmail + " TEXT, " +
                    AccountTable.ColumnPhone + " TEXT, " +
                    AccountTable.ColumnEnabled + " INTEGER)";

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create the tables
        try{
            db.execSQL(SQL_CREATE_ACCOUNT_TABLE);
        }catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating account table", e);
        }

        try{
            db.execSQL(SQL_CREATE_INVENTORY_TABLE);
        }catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating inventory table", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop the tables if they exist
        String dropAccountTable = "DROP TABLE IF EXISTS " + AccountTable.TableName;
        try{
            db.execSQL(dropAccountTable);
        }catch (Exception e) {
            Log.e("DatabaseHelper", "Error dropping account table", e);
        }
        String dropInventoryTable = "DROP TABLE IF EXISTS " + InventoryTable.TableName;
        try{
            db.execSQL(dropInventoryTable);
        }catch (Exception e) {
            Log.e("DatabaseHelper", "Error dropping inventory table", e);
        }
        onCreate(db);
    }

    public void insertAccount(String name, String password, String email, String phone, int enabled) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Validate input
        if (name.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Log.e("DatabaseHelper", "All fields are required");
            return;
        }

        // Check if the account already exists
        if (checkAccountExists(name)) {
            Log.e("DatabaseHelper", "Account already exists");
            return;
        }

        // Insert the account into the database
        ContentValues values = new ContentValues();

        values.put(AccountTable.ColumnName, name);
        values.put(AccountTable.ColumnPassword, password);
        values.put(AccountTable.ColumnEmail, email);
        values.put(AccountTable.ColumnPhone, phone);
        values.put(AccountTable.ColumnEnabled, enabled);

        try {
            db.insert(AccountTable.TableName, null, values);
        }catch (Exception e) {
            Log.e("DatabaseHelper", "Error creating account", e);
        }
    }

    public void updateAccount(String name, String password, String email, String phone, int enabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();


        if (!password.isEmpty()) {
            String hashedPassword = hashPassword(password);
            values.put(AccountTable.ColumnPassword, hashedPassword);
        }

        values.put(AccountTable.ColumnEmail, email);
        values.put(AccountTable.ColumnPhone, phone);
        values.put(AccountTable.ColumnEnabled, enabled);

        try{
            db.update(AccountTable.TableName, values, AccountTable.ColumnName + " = ?", new String[]{name});
            Log.d("DatabaseHelper", name + "Account updated successfully");
        }catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating account", e);
        }

    }

    // For disabling and enabling accounts
    public void deactivateAccount(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AccountTable.ColumnEnabled, 0);

        db.update(AccountTable.TableName, values, AccountTable.ColumnName + " = ?", new String[]{name});
    }

    // For future implementation of account control
    public boolean activateAccount(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AccountTable.ColumnEnabled, 1);

        db.update(AccountTable.TableName, values, AccountTable.ColumnName + " = ?", new String[]{name});
        return true;
    }

    // Check if the account already exists
    public boolean checkAccountExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String accountName = "SELECT * FROM "
                + AccountTable.TableName + " WHERE "
                + AccountTable.ColumnName + " = ?";

        Cursor cursor = db.rawQuery(accountName, new String[]{name});

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Verify the account to enable login
    public boolean verifyAccount(String name, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        if (name.isEmpty() || password.isEmpty()) {
            Log.e("DatabaseHelper", "All fields are required");
            return false;
        }

        if (!checkAccountEnabled(name)){
            Log.e("DatabaseHelper", "Account is disabled");
            return false;
        }

        String getHashedPasswordFromDatabase = "SELECT "
                + AccountTable.ColumnPassword + " FROM "
                + AccountTable.TableName + " WHERE "
                + AccountTable.ColumnName + " = ?";

        Cursor cursor = db.rawQuery(getHashedPasswordFromDatabase, new String[]{name});
        String hashedPasswordFromDatabase;
        if (cursor.moveToFirst()) {
            hashedPasswordFromDatabase = cursor.getString(cursor.getColumnIndexOrThrow(AccountTable.ColumnPassword));
        } else {
            cursor.close();
            return false;
        }
        cursor.close();

        return checkAccountPassword(password, hashedPasswordFromDatabase);
    }

    // Check if the entered password matches the hashed password stored in the database
    public boolean checkAccountPassword(String plainTextPassword, String hashedPasswordFromDatabase) {
        // Check if the entered password matches the hashed password stored in the database
        return BCrypt.checkpw(plainTextPassword, hashedPasswordFromDatabase);
    }

    // Check if the account is enabled for account control
    public boolean checkAccountEnabled(String name) {
        SQLiteDatabase db = this.getReadableDatabase();

        String accountEnabled = "SELECT "
                + AccountTable.ColumnEnabled + " FROM "
                + AccountTable.TableName + " WHERE "
                + AccountTable.ColumnName + " = ?";

        Cursor cursor = db.rawQuery(accountEnabled, new String[]{name});

        if (cursor.moveToFirst()) {
            boolean enabled = cursor.getInt(cursor.getColumnIndexOrThrow(AccountTable.ColumnEnabled)) == 1;
            cursor.close();
            return enabled;
        } else {
            cursor.close();
            return false;
        }
    }

    // Hash the password
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // Insert the inventory item into the database
    public void insertInventory(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare the content values
        ContentValues values = new ContentValues();
        values.put(InventoryTable.ColumnName, item.getItemName());
        values.put(InventoryTable.ColumnDescription, item.getItemDescription());
        values.put(InventoryTable.ColumnQuantity, item.getItemQuantity());

        // Check if the inventory item already exists
        if (checkInventoryExists(item.getItemName())) {
            Log.e("DatabaseHelper", "Inventory item already exists");
            return;
        }

        // Insert the inventory item into the database
        try{
            db.insert(InventoryTable.TableName, null, values);
        }catch (Exception e) {
            Log.e("DatabaseHelper", "Error inserting inventory item", e);
        }
    }

    // Check if the inventory item already exists
    private boolean checkInventoryExists(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String inventoryName = "SELECT * FROM "
                + InventoryTable.TableName + " WHERE "
                + InventoryTable.ColumnName + " = ?";

        // Check if the inventory item already exists
        Cursor cursor = db.rawQuery(inventoryName, new String[]{name});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Update the inventory item
    public void updateInventory(InventoryItem item) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Prepare the content values
        ContentValues values = new ContentValues();
        values.put(InventoryTable.ColumnName, item.getItemName());
        values.put(InventoryTable.ColumnDescription, item.getItemDescription());
        values.put(InventoryTable.ColumnQuantity, item.getItemQuantity());

        // Update the inventory item in the database
        String selection = InventoryTable.ColumnName + " LIKE ?";
        String[] selectionArgs = {item.getItemName()};

        try {
            db.update(InventoryTable.TableName, values, selection, selectionArgs);
            Log.d("DatabaseHelper", "Inventory item updated successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating inventory item", e);
        }

        if (item.getItemQuantity() == 0) {
            // Send SMS to the user
            try{
                sendSms(item.getItemName());
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error sending SMS", e);
            }
        }
    }

    // Delete the inventory item
    public void deleteInventory(String itemName) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = InventoryTable.ColumnName + " LIKE ?";
        String[] selectionArgs = {itemName};
        try {
            db.delete(InventoryTable.TableName, selection, selectionArgs);
            Log.d("DatabaseHelper", "Inventory item deleted successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error deleting inventory item", e);
        }
    }

    // Get the inventory list from the database
    public List<InventoryItem> getInventoryList() {
        List<InventoryItem> inventoryList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Retrieve the inventory list from the database
        String[] projection = {
                InventoryTable.ColumnId,
                InventoryTable.ColumnName,
                InventoryTable.ColumnDescription,
                InventoryTable.ColumnQuantity
        };

        Cursor cursor = db.query(
                InventoryTable.TableName,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        // Populate the inventory list
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.ColumnName));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(InventoryTable.ColumnDescription));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(InventoryTable.ColumnQuantity));
                InventoryItem item = new InventoryItem(name, description, quantity);
                inventoryList.add(item);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return inventoryList;
    }

    // Send SMS to the user
    private void sendSms(String itemName) {

        // Check if SMS permission is granted
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.e("DatabaseHelper", "SMS permission not granted");
            return;
        }

        // Get the phone number
        String phoneNumber = getPhoneNumber();

        // Check if the phone number exists
        if (phoneNumber == null) {
            Log.e("DatabaseHelper", "Phone number not found");
            return;
        }

        String message = "The quantity of " + itemName + " has reached 0.";

        // Send the SMS
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d("DatabaseHelper", "SMS sent successfully");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "SMS failed to send", e);
        }
    }

    // Get the phone number
    private String getPhoneNumber() {
        Log.d("DatabaseHelper", "getPhoneNumber() called");

        SQLiteDatabase db = this.getReadableDatabase();

        // Retrieve username from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String username = prefs.getString(LOGGED_IN_USER, null);
        Log.d("DatabaseHelper", "Username from SharedPreferences: " + username);
        if (username == null) {
            Log.e("DatabaseHelper", "Username not found in SharedPreferences");
            return null;
        }

        // Retrieve the phone number from the database
        String phoneNumber = "SELECT "
                + AccountTable.ColumnPhone + " FROM "
                + AccountTable.TableName + " WHERE "
                + AccountTable.ColumnName + " = ?";

        Cursor cursor = db.rawQuery(phoneNumber, new String[]{username});

        if (cursor.moveToFirst()) {
            String PhoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(AccountTable.ColumnPhone));
            Log.d("DatabaseHelper", "Phone number from database: " + PhoneNumber);
            cursor.close();
            return PhoneNumber;
        }
        Log.e("DatabaseHelper", "Phone number not found in database for username: " + username);

        cursor.close();
        return null;
    }


}
