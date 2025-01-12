package com.example.face_recognition_attendance_app.Activities.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.face_recognition_attendance_app.Activities.Interfaces.DatabaseCallback;
import com.example.face_recognition_attendance_app.Activities.Models.AttendanceDBModel;

public class SqliteHelper extends SQLiteOpenHelper {

    private static final String DBNAME = "AttendanceDB";
    private static final int DBVERSION = 1;

    public static final String TABLENAME = "AttendanceTable";
    public static final String  COLUMN_ID = "id";
    public static final String  COLUMN_NAME = "name";
    public static final String  COLUMN_CHECKINTIME = "checkInTime";
    public static final String  COLUMN_CHECKOUTTIME = "checkOutTime";
    public static final String  COLUMN_CHECKINDATE = "CheckInDate";
    public static final String  COLUMN_ISCHECKIN = "isCheckIn";


    public SqliteHelper(@Nullable Context context) {
        super(context, DBNAME,null,DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableQuery = "CREATE TABLE " + TABLENAME + " (" +
                COLUMN_ID + " TEXT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_CHECKINTIME + " TEXT, " +
                COLUMN_CHECKOUTTIME + " TEXT, " +
                COLUMN_CHECKINDATE + " TEXT, " +
                COLUMN_ISCHECKIN + " INTEGER)";
        sqLiteDatabase.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
        // Recreate the table
        onCreate(sqLiteDatabase);
    }
    public boolean checkAttendanceExists(String uid, String currentDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLENAME + " WHERE " +
                COLUMN_ID + " = ? AND CheckInDate = ?", new String[]{uid, currentDate});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
    public boolean checkIfCheckOut(String uid, String currentDate) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query to check if checkout time is not null and not empty
        String query = "SELECT * FROM " + TABLENAME + " WHERE "
                + COLUMN_ID + " = ? AND "
                + COLUMN_CHECKINDATE + " = ? AND "
                + COLUMN_CHECKOUTTIME + " IS NOT NULL AND "
                + COLUMN_CHECKOUTTIME + " != ''";
        Cursor cursor = db.rawQuery(query, new String[]{uid, currentDate});

        boolean hasCheckedOut = cursor.getCount() > 0;
        cursor.close();
        return hasCheckedOut;
    }
    public void updateAttendance(AttendanceDBModel model, String currentDate, DatabaseCallback callback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (model.getIsCheckIn() == 1) {
            values.put(COLUMN_CHECKINTIME, model.getCheckInTime());
        } else {
            values.put(COLUMN_CHECKOUTTIME, model.getCheckOutTime());
        }

        int rowsAffected = db.update(
                TABLENAME,
                values,
                COLUMN_ID + " = ? AND CheckInDate = ?",
                new String[]{model.getId(), currentDate}
        );

        if (rowsAffected > 0) {
            callback.onSuccess("Attendance updated successfully");
        } else {
            callback.onFailure("Failed to update attendance");
        }
    }
    public void markAttendance(AttendanceDBModel model, DatabaseCallback callback) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, model.getId());
        values.put(COLUMN_NAME, model.getName());
        values.put(COLUMN_CHECKINTIME, model.getCheckInTime());
        values.put(COLUMN_ISCHECKIN, model.getIsCheckIn());
        values.put(COLUMN_CHECKINDATE, model.getCheckInDate());
        values.put(COLUMN_CHECKOUTTIME, ""); // Initialize with empty string

        long result = db.insert(TABLENAME, null, values);

        if (result == -1) {
            callback.onFailure("Failed to check in");
        } else {
            callback.onSuccess("Check-in successful");
        }

    }
    public void updateCheckoutTime(String userId, String checkoutTime, String checkInDate, DatabaseCallback callback) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if there's an open attendance record for the given user
        String query = "SELECT * FROM " + TABLENAME + " WHERE "
                + COLUMN_ID + " = ? AND "
                + COLUMN_CHECKINDATE + " = ? AND ("
                + COLUMN_CHECKOUTTIME + " IS NULL OR "
                + COLUMN_CHECKOUTTIME + " = '')";
        Cursor cursor = db.rawQuery(query, new String[]{userId, checkInDate});

        if (cursor != null && cursor.moveToFirst()) {
            // Open record found; update the checkout time
            ContentValues values = new ContentValues();
            values.put(COLUMN_CHECKOUTTIME, checkoutTime);

            int rowsAffected = db.update(
                    TABLENAME,
                    values,
                    COLUMN_ID + " = ? AND "
                            + COLUMN_CHECKINDATE + " = ? AND ("
                            + COLUMN_CHECKOUTTIME + " IS NULL OR "
                            + COLUMN_CHECKOUTTIME + " = '')",
                    new String[]{userId, checkInDate}
            );

            if (rowsAffected > 0) {
                callback.onSuccess("Check-out time updated successfully");
            } else {
                callback.onFailure("Failed to update check-out time");
            }
        } else {
            // No open record found to update
            callback.onFailure("No open attendance record found to update");
        }

        if (cursor != null) {
            cursor.close();
        }
    }


//    public long insertAttendance(AttendanceDBModel attendanceDBModel, DatabaseCallback databaseCallback) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_ID, attendanceDBModel.getId());
//        values.put(COLUMN_NAME, attendanceDBModel.getName());
//        values.put(COLUMN_CHECKINTIME, attendanceDBModel.getIsCheckIn());
//        values.put(COLUMN_CHECKOUTTIME, attendanceDBModel.getCheckOutTime());
//        values.put(COLUMN_ISCHECKIN, attendanceDBModel.getIsCheckIn());
//        values.put(COLUMN_CHECKINDATE, attendanceDBModel.getCheckInDate());
//        long result = db.insert(TABLENAME, null, values);
//        if (result == -1){
//            databaseCallback.onFailure("db error");
//        }else {
//            databaseCallback.onSuccess("success");
//        }
//        return db.insert(TABLENAME, null, values);
//    }

    // Retrieve all data from the database
    public Cursor getAllAttendance() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLENAME, null);
    }

}
