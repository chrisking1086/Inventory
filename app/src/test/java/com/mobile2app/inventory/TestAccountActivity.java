package com.mobile2app.inventory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.Robolectric;
import org.mockito.MockitoAnnotations;
import org.mockito.Mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)

public class TestAccountActivity {

    @Mock
    private AccountActivity accountActivity;
    @Mock
    private RegistrationActivity registrationActivity;
    @Mock
    private LoginActivity loginActivity;
    @Mock
    private EditItemActivity editItemActivity;
    @Mock
    private AddItemActivity addItemActivity;
    @Mock
    private InventoryActivity inventoryActivity;

    @Mock
    private DatabaseHelper mockDbHelper;

    @Mock
    private SharedPreferences mockSharedPreferences;

    @Mock
    private SharedPreferences.Editor mockEditor;

    private static final String PREFS_NAME = "myprefs";
    private static final String LOGGED_IN_USER = "loggedInUsername";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        Context context = ApplicationProvider.getApplicationContext();
        // Mock SharedPreferences
        when(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)).thenReturn(mockSharedPreferences);
        when(mockSharedPreferences.edit()).thenReturn(mockEditor);

        Intent intent = new Intent(context, AccountActivity.class);
        accountActivity = Robolectric.buildActivity(AccountActivity.class, intent).create().resume().get();
        registrationActivity = Robolectric.buildActivity(RegistrationActivity.class).create().resume().get();
        loginActivity = Robolectric.buildActivity(LoginActivity.class).create().resume().get();

        editItemActivity = Robolectric.buildActivity(EditItemActivity.class).create().resume().get();
        addItemActivity = Robolectric.buildActivity(AddItemActivity.class).create().resume().get();
        inventoryActivity = Robolectric.buildActivity(InventoryActivity.class).create().resume().get();

        accountActivity.db = mockDbHelper;
    }

    @Test
    public void testRegistrationActivity_validInput_success() {
        EditText usernameEditText = registrationActivity.findViewById(R.id.newUsernameEditText);
        EditText passwordEditText = registrationActivity.findViewById(R.id.newPasswordEditText);
        EditText passwordVerifyEditText = registrationActivity.findViewById(R.id.newPasswordVerifyEditText);
        EditText emailEditText = registrationActivity.findViewById(R.id.newEmailEditText);
        EditText phoneEditText = registrationActivity.findViewById(R.id.newPhoneEditText);

        usernameEditText.setText("testuser");
        passwordEditText.setText("password123");
        passwordVerifyEditText.setText("password123");
        emailEditText.setText("john.doe@example.com");
        phoneEditText.setText("1234567890");

        Button registerButton = registrationActivity.findViewById(R.id.newRegisterButton);
        registerButton.performClick();

        String expectedToastMessage = "Account created successfully";
        String actualToastMessage = ShadowToast.getTextOfLatestToast();
        assertEquals(expectedToastMessage, actualToastMessage);
    }

    @Test
    public void testLoginActivity_validInput_success() {

        EditText usernameEditText = loginActivity.findViewById(R.id.usernameEditText);
        EditText passwordEditText = loginActivity.findViewById(R.id.passwordEditText);

        usernameEditText.setText("testuser");
        passwordEditText.setText("password123");

        Button loginButton = loginActivity.findViewById(R.id.loginButton);
        loginButton.performClick();

        String expectedToastMessage = "Login successful";
        String actualToastMessage = ShadowToast.getTextOfLatestToast();
        assertEquals(expectedToastMessage, actualToastMessage);
    }

    @Test
    public void testEditAccountButton_validInput_success() {
        EditText editPasswordEditText = accountActivity.findViewById(R.id.editPasswordEditText);
        EditText editPasswordVerifyEditText = accountActivity.findViewById(R.id.editPasswordVerifyEditText);
        EditText editEmailEditText = accountActivity.findViewById(R.id.editEmailEditText);
        EditText editPhoneEditText = accountActivity.findViewById(R.id.editPhoneEditText);

        when(mockSharedPreferences.getString(LOGGED_IN_USER, "")).thenReturn("testuser");

        editPasswordEditText.setText("password123");
        editPasswordVerifyEditText.setText("password123");
        editEmailEditText.setText("jane.doe@example.com");
        editPhoneEditText.setText("1234567890");

        Button saveButton = accountActivity.findViewById(R.id.editAccountButton);
        saveButton.performClick();

        String expectedToastMessage = "Account updated successfully";
        String actualToastMessage = ShadowToast.getTextOfLatestToast();
        assertEquals(expectedToastMessage, actualToastMessage);

    }
}