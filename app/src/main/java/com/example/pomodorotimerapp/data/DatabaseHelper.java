package com.example.pomodorotimerapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "pomodoro.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_STATS = "stats";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_FOCUS_COUNT = "focus_count";
    private static final String COLUMN_BREAK_COUNT = "break_count";
    private static final String COLUMN_LONG_BREAK_COUNT = "long_break_count";
    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_STATS_TABLE = "CREATE TABLE " + TABLE_STATS + "("
                + COLUMN_DATE + " TEXT PRIMARY KEY,"
                + COLUMN_FOCUS_COUNT + " INTEGER,"
                + COLUMN_BREAK_COUNT + " INTEGER,"
                + COLUMN_LONG_BREAK_COUNT + " INTEGER" + ")";
        db.execSQL(CREATE_STATS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
        onCreate(db);
    }

    public void addOrUpdateStats(String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String date = dateFormat.format(new Date());

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATE, date);

        Cursor cursor = db.query(TABLE_STATS, null, COLUMN_DATE + " = ?", new String[]{date}, null, null, null);

        if (cursor.moveToFirst()) {
            int focusCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOCUS_COUNT));
            int breakCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BREAK_COUNT));
            int longBreakCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LONG_BREAK_COUNT));

            switch (type) {
                case "focus":
                    values.put(COLUMN_FOCUS_COUNT, focusCount + 1);
                    values.put(COLUMN_BREAK_COUNT, breakCount);
                    values.put(COLUMN_LONG_BREAK_COUNT, longBreakCount);
                    break;
                case "break":
                    values.put(COLUMN_FOCUS_COUNT, focusCount);
                    values.put(COLUMN_BREAK_COUNT, breakCount + 1);
                    values.put(COLUMN_LONG_BREAK_COUNT, longBreakCount);
                    break;
                case "longBreak":
                    values.put(COLUMN_FOCUS_COUNT, focusCount);
                    values.put(COLUMN_BREAK_COUNT, breakCount);
                    values.put(COLUMN_LONG_BREAK_COUNT, longBreakCount + 1);
                    break;
            }
            db.update(TABLE_STATS, values, COLUMN_DATE + " = ?", new String[]{date});
        } else {
            values.put(COLUMN_FOCUS_COUNT, type.equals("focus") ? 1 : 0);
            values.put(COLUMN_BREAK_COUNT, type.equals("break") ? 1 : 0);
            values.put(COLUMN_LONG_BREAK_COUNT, type.equals("longBreak") ? 1 : 0);
            db.insert(TABLE_STATS, null, values);
        }
        cursor.close();
        db.close();
    }

    public List<DailyStats> getWeeklyStats() {
        List<DailyStats> statsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String sevenDaysAgo = dateFormat.format(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));

        String selectQuery = "SELECT * FROM " + TABLE_STATS + " WHERE " + COLUMN_DATE + " >= ? ORDER BY " + COLUMN_DATE + " ASC";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{sevenDaysAgo});

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
                int focusCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FOCUS_COUNT));
                int breakCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BREAK_COUNT));
                int longBreakCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LONG_BREAK_COUNT));
                statsList.add(new DailyStats(date, focusCount, breakCount, longBreakCount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return statsList;
    }


    public void resetStats() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STATS, null, null);
        db.close();
    }

    public static class DailyStats {
        public String date;
        public int focusCount;
        public int breakCount;
        public int longBreakCount;

        public DailyStats(String date, int focusCount, int breakCount, int longBreakCount) {
            this.date = date;
            this.focusCount = focusCount;
            this.breakCount = breakCount;
            this.longBreakCount = longBreakCount;
        }
    }
}