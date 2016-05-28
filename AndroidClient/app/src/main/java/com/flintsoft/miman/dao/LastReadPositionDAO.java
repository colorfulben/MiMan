package com.flintsoft.miman.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Xin on 2015/11/22.
 */
public class LastReadPositionDAO extends SQLiteOpenHelper {
    private static final String TAG = LastReadPositionDAO.class.getSimpleName();

    public LastReadPositionDAO(Context context) {
        super(context, LastReadPosition.DB_NAME, null, LastReadPosition.DB_VERSION);
    }

    // Called only once first time we create the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String
                .format("create table %s (%s int primary key, %s int, %s int, %s int)",
                        LastReadPosition.TABLE,
                        LastReadPosition.Column.ID,
                        LastReadPosition.Column.ChapterIndex,
                        LastReadPosition.Column.PageIndex);
        Log.d(TAG, "onCreate with SQL: " + sql);
        db.execSQL(sql);
    }

    // Gets called whenever existing version != new version, i.e. schema changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Typically you do ALTER TABLE ...
        db.execSQL("drop table if exists " + LastReadPosition.TABLE);
        onCreate(db);
    }
}
