package com.example.urbancycle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "UrbanCycleDB";
    private static final int DATABASE_VERSION = 1;

    // for Users table
    public static final String TABLE_USERS = "Users";
    public static final String COLUMN_USER_ID = "UserID";
    public static final String COLUMN_FIRST_NAME = "FirstName";
    public static final String COLUMN_LAST_NAME = "LastName";
    public static final String COLUMN_EMAIL = "Email";
    public static final String COLUMN_PASSWORD = "Password";
    public static final String COLUMN_CARBON_SAVINGS = "CarbonSavings";
    public static final String COLUMN_POINTS = "Points";

    // for Routes table
    public static final String TABLE_ROUTES = "Routes";
    public static final String COLUMN_ROUTE_ID = "RouteID";
    public static final String COLUMN_START_LOCATION = "StartLocation";
    public static final String COLUMN_END_LOCATION = "EndLocation";

    // for TransportModes table
    public static final String TABLE_TRANSPORT_MODES = "TransportModes";
    public static final String COLUMN_MODE_ID = "ModeID";
    public static final String COLUMN_MODE_NAME = "ModeName";

    // for RouteModes table
    public static final String TABLE_ROUTE_MODES = "RouteModes";

    // for Rewards table
    public static final String TABLE_REWARDS = "Rewards";
    public static final String COLUMN_REWARD_ID = "RewardID";
    public static final String COLUMN_REWARD_NAME = "Name";
    public static final String COLUMN_REWARD_DESCRIPTION = "Description";
    public static final String COLUMN_POINTS_REQUIRED = "PointsRequired";
    public static final String COLUMN_REWARD_NUMBERS = "RewardNumbers";

    // for UserRewards table
    public static final String TABLE_USER_REWARDS = "UserRewards";
    public static final String COLUMN_DATE_EARNED = "DateEarned";

    // for Feedback table
    public static final String TABLE_FEEDBACK = "Feedback";
    public static final String COLUMN_FEEDBACK_ID = "FeedbackID";
    public static final String COLUMN_COMMENT = "Comment";
    public static final String COLUMN_DATE = "Date";

    // for UserRoutes table
    public static final String TABLE_USER_ROUTES = "UserRoutes";
    public static final String COLUMN_USER_ROUTE_ID = "UserRouteID";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Users table
        String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FIRST_NAME + " TEXT,"
                + COLUMN_LAST_NAME + " TEXT,"
                + COLUMN_EMAIL + " TEXT UNIQUE,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_CARBON_SAVINGS + " REAL,"
                + COLUMN_POINTS + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE_USERS);

        // Routes table
        String CREATE_TABLE_ROUTES = "CREATE TABLE " + TABLE_ROUTES + "("
                + COLUMN_ROUTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_START_LOCATION + " TEXT,"
                + COLUMN_END_LOCATION + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_ROUTES);

        // TransportModes table
        String CREATE_TABLE_TRANSPORTMODES = "CREATE TABLE " + TABLE_TRANSPORT_MODES + "("
                + COLUMN_MODE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_MODE_NAME + " TEXT" + ")";
        db.execSQL(CREATE_TABLE_TRANSPORTMODES);

        // RouteModes table
        String CREATE_TABLE_ROUTEMODES = "CREATE TABLE " + TABLE_ROUTE_MODES + "("
                + COLUMN_ROUTE_ID + " INTEGER,"
                + COLUMN_MODE_ID + " INTEGER,"
                + "FOREIGN KEY (" + COLUMN_ROUTE_ID + ") REFERENCES " + TABLE_ROUTES + "(" + COLUMN_ROUTE_ID + "),"
                + "FOREIGN KEY (" + COLUMN_MODE_ID + ") REFERENCES " + TABLE_TRANSPORT_MODES + "(" + COLUMN_MODE_ID + ")" + ")";
        db.execSQL(CREATE_TABLE_ROUTEMODES);

        // Rewards table
        String CREATE_TABLE_REWARDS = "CREATE TABLE " + TABLE_REWARDS + "("
                + COLUMN_REWARD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_REWARD_NAME + " TEXT,"
                + COLUMN_REWARD_DESCRIPTION + " TEXT,"
                + COLUMN_POINTS_REQUIRED + " INTEGER,"
                + COLUMN_REWARD_NUMBERS + " INTEGER" + ")";
        db.execSQL(CREATE_TABLE_REWARDS);

        // UserRewards table
        String CREATE_TABLE_USERREWARDS = "CREATE TABLE " + TABLE_USER_REWARDS + "("
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_REWARD_ID + " INTEGER,"
                + COLUMN_DATE_EARNED + " DATETIME,"
                + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY (" + COLUMN_REWARD_ID + ") REFERENCES " + TABLE_REWARDS + "(" + COLUMN_REWARD_ID + ")" + ")";
        db.execSQL(CREATE_TABLE_USERREWARDS);

        // Feedback table
        String CREATE_TABLE_FEEDBACK = "CREATE TABLE " + TABLE_FEEDBACK + "("
                + COLUMN_FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_COMMENT + " TEXT,"
                + COLUMN_DATE + " DATETIME,"
                + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")" + ")";
        db.execSQL(CREATE_TABLE_FEEDBACK);

        // UserRoutes table
        String CREATE_TABLE_USERROUTES = "CREATE TABLE " + TABLE_USER_ROUTES + "("
                + COLUMN_USER_ROUTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USER_ID + " INTEGER,"
                + COLUMN_ROUTE_ID + " INTEGER,"
                + "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + "),"
                + "FOREIGN KEY (" + COLUMN_ROUTE_ID + ") REFERENCES " + TABLE_ROUTES + "(" + COLUMN_ROUTE_ID + ")" + ")";
        db.execSQL(CREATE_TABLE_USERROUTES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade if required
    }

    /**
     * Implementing CRUD operations for each table
     */

    // User Table

    // Add a new User
    public long addUser(String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_CARBON_SAVINGS, 0);
        values.put(COLUMN_POINTS, 0);

        long id = db.insert(TABLE_USERS, null, values);
        db.close();
        return id;
    }

    // Retrieve a user by its unique Email
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, COLUMN_EMAIL + "=?",
                new String[] { email }, null, null, null);
    }

    // Update a user's points and carbon savings
    public int updateUserPointsAndCarbonSavings(int userId, int newPoints, float newCarbonSavings) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_POINTS, newPoints);
        values.put(COLUMN_CARBON_SAVINGS, newCarbonSavings);

        return db.update(TABLE_USERS, values, COLUMN_USER_ID + "=?",
                new String[] { String.valueOf(userId) });
    }

    // Delete a user based on their ID
    public int deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, COLUMN_USER_ID + "=?", new String[] { String.valueOf(userId) });
    }

    // Routes Table

    // Add a new Route
    public long addRoute(String startLocation, String endLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_START_LOCATION, startLocation);
        values.put(COLUMN_END_LOCATION, endLocation);

        long id = db.insert(TABLE_ROUTES, null, values);
        db.close();
        return id;
    }

    // Retrieve a Route by its ID
    public Cursor getRouteById(int routeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_ROUTES, null, COLUMN_ROUTE_ID + "=?",
                new String[] { String.valueOf(routeId) }, null, null, null);
    }

    // Update a Route
    public int updateRoute(int routeId, String startLocation, String endLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_START_LOCATION, startLocation);
        values.put(COLUMN_END_LOCATION, endLocation);

        return db.update(TABLE_ROUTES, values, COLUMN_ROUTE_ID + "=?",
                new String[] { String.valueOf(routeId) });
    }

    // Delete a Route by its ID
    public void deleteRoute(int routeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROUTES, COLUMN_ROUTE_ID + "=?", new String[] { String.valueOf(routeId) });
        db.close();
    }

    // TransportMode Table

    // Add a new transport mode
    public long addTransportMode(String modeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MODE_NAME, modeName);

        long id = db.insert(TABLE_TRANSPORT_MODES, null, values);
        db.close();
        return id;
    }

    // Retrieve a TransportMode by its ID
    public Cursor getTransportModeById(int modeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRANSPORT_MODES, null, COLUMN_MODE_ID + "=?",
                new String[] { String.valueOf(modeId) }, null, null, null);
    }

    // Update a TransportMode
    public int updateTransportMode(int modeId, String modeName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_MODE_NAME, modeName);

        return db.update(TABLE_TRANSPORT_MODES, values, COLUMN_MODE_ID + "=?",
                new String[] { String.valueOf(modeId) });
    }

    // Delete a TransportMode by its ID
    public void deleteTransportMode(int modeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRANSPORT_MODES, COLUMN_MODE_ID + "=?", new String[] { String.valueOf(modeId) });
        db.close();
    }

    // Rewards Table

    // Add a new reward
    public long addRewards(String rewardName, String description, int pointsRequired, int rewardNumbers) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REWARD_NAME, rewardName);
        values.put(COLUMN_REWARD_DESCRIPTION, description);
        values.put(COLUMN_POINTS_REQUIRED, pointsRequired);
        values.put(COLUMN_REWARD_NUMBERS, rewardNumbers);

        long id = db.insert(TABLE_REWARDS, null, values);
        db.close();
        return id;
    }

    // Retrieve a reward by its ID
    public Cursor getRewardById(int rewardId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_REWARDS, null, COLUMN_REWARD_ID + "=?",
                new String[] { String.valueOf(rewardId) }, null, null, null);
    }

    // Update a reward
    public int updateRewards(int rewardID, String rewardName, String rewardDescription, int pointsRequired,
                             int numbersLeft) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REWARD_NAME, rewardName);
        values.put(COLUMN_REWARD_DESCRIPTION, rewardDescription);
        values.put(COLUMN_REWARD_NUMBERS, numbersLeft);
        values.put(COLUMN_POINTS_REQUIRED, pointsRequired);

        return db.update(TABLE_REWARDS, values, COLUMN_REWARD_ID + "=?",
                new String[] { String.valueOf(rewardID) });
    }

    // Delete a reward
    public void deleteReward(int rewardId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_REWARDS, COLUMN_REWARD_ID + "=?",
                new String[] { String.valueOf(rewardId) });
        db.close();
    }

    // Route Modes Table

    // Add a new association between a Route and a TransportMode
    public long addRouteMode(int routeId, int modeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROUTE_ID, routeId);
        values.put(COLUMN_MODE_ID, modeId);

        long id = db.insert(TABLE_ROUTE_MODES, null, values);
        db.close();
        return id;
    }

    // Retrieve all TransportModes associated with a Route
    public Cursor getModesForRoute(int routeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_MODE_ID + " FROM " + TABLE_ROUTE_MODES +
                " WHERE " + COLUMN_ROUTE_ID + "=?";
        return db.rawQuery(query, new String[] { String.valueOf(routeId) });
    }

    // Check if a particular association exists
    public boolean associationExists(int routeId, int modeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_ROUTE_MODES, null,
                COLUMN_ROUTE_ID + "=? AND " + COLUMN_MODE_ID + "=?",
                new String[] { String.valueOf(routeId), String.valueOf(modeId) },
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // Delete an association between a Route and a TransportMode
    public void deleteRouteMode(int routeId, int modeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ROUTE_MODES,
                COLUMN_ROUTE_ID + "=? AND " + COLUMN_MODE_ID + "=?",
                new String[] { String.valueOf(routeId), String.valueOf(modeId) });
        db.close();
    }

    // User Rewards Table

    // Add a new UserReward
    public long addUserReward(int userId, int rewardId, String dateEarned) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_REWARD_ID, rewardId);
        values.put(COLUMN_DATE_EARNED, dateEarned);

        long id = db.insert(TABLE_USER_REWARDS, null, values);
        db.close();
        return id;
    }

    // Retrieve all Rewards for a User
    public Cursor getRewardsForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_REWARDS, null, COLUMN_USER_ID + "=?",
                new String[] { String.valueOf(userId) }, null, null, null);
        return cursor;
    }

    // Delete a UserReward
    public void deleteUserReward(int userId, int rewardId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_REWARDS,
                COLUMN_USER_ID + "=? AND " + COLUMN_REWARD_ID + "=?",
                new String[] { String.valueOf(userId), String.valueOf(rewardId) });
        db.close();
    }

    // Feedback Table

    // Add a new Feedback
    public long addFeedback(int userId, String comment, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_COMMENT, comment);
        values.put(COLUMN_DATE, date);

        long id = db.insert(TABLE_FEEDBACK, null, values);
        db.close();
        return id;
    }

    // Retrieve all Feedbacks for a User
    public Cursor getFeedbacksForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FEEDBACK, null, COLUMN_USER_ID + "=?",
                new String[] { String.valueOf(userId) }, null, null, null);
        return cursor;
    }

    // Update Feedback
    public int updateFeedback(int feedbackId, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMMENT, comment);

        return db.update(TABLE_FEEDBACK, values, COLUMN_FEEDBACK_ID + "=?",
                new String[] { String.valueOf(feedbackId) });
    }

    // Delete a Feedback
    public void deleteFeedback(int feedbackId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FEEDBACK, COLUMN_FEEDBACK_ID + "=?",
                new String[] { String.valueOf(feedbackId) });
        db.close();
    }

    // User Route Table

    // Add a new UserRoute
    public long addUserRoute(int userId, int routeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_ROUTE_ID, routeId);

        long id = db.insert(TABLE_USER_ROUTES, null, values);
        db.close();
        return id;
    }

    // Retrieve all Routes for a User
    public Cursor getRoutesForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_ROUTES, null, COLUMN_USER_ID + "=?",
                new String[] { String.valueOf(userId) }, null, null, null);
        return cursor;
    }

    // Check if a particular UserRoute association exists
    public boolean userRouteAssociationExists(int userId, int routeId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_ROUTES, null,
                COLUMN_USER_ID + "=? AND " + COLUMN_ROUTE_ID + "=?",
                new String[] { String.valueOf(userId), String.valueOf(routeId) },
                null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // Delete a specific UserRoute association
    public void deleteUserRoute(int userRouteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_ROUTES, COLUMN_USER_ROUTE_ID + "=?",
                new String[] { String.valueOf(userRouteId) });
        db.close();
    }

    // Delete a specific association by UserID and RouteID
    public void deleteUserRouteByUserIdAndRouteId(int userId, int routeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USER_ROUTES,
                COLUMN_USER_ID + "=? AND " + COLUMN_ROUTE_ID + "=?",
                new String[] { String.valueOf(userId), String.valueOf(routeId) });
        db.close();
    }

}
