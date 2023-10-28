package com.example.urbancycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import androidx.test.platform.app.InstrumentationRegistry;

public class DatabaseTest {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;

    @Before
    public void setUp() throws Exception {
        // Get the context of the test environment
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Instantiate the DatabaseHelper
        dbHelper = new DatabaseHelper(context);

        // Open the database for writing
        db = dbHelper.getWritableDatabase();
    }

    @After
    public void tearDown() throws Exception {
        if (db != null) {
            db.close();
        }
        db = null;
        dbHelper = null;
    }


    @Test
    public void insertAndReadUserTest() {
        // Test data
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String password = "password123";

        // Insert user
        long userId = dbHelper.addUser(firstName, lastName, email, password);

        // Fetch user by email
        Cursor cursor = dbHelper.getUserByEmail(email);

        // Check if we have a result
        assertTrue(cursor.getCount() > 0);
        cursor.moveToFirst();

        // Validate the data
        assertEquals(firstName, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FIRST_NAME)));
        assertEquals(lastName, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LAST_NAME)));
        Assert.assertEquals(email, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_EMAIL)));

        cursor.close();
    }

    // Similarly, add more test methods for other CRUD operations and tables...
}

