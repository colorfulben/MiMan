package com.flintsoft.miman.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Xin on 2015/12/20.
 */
public class FavoritesDAO extends SQLiteOpenHelper {
    private static final String TAG = FavoritesDAO.class.getSimpleName();

    public FavoritesDAO(Context context) {
        super(context, Favorites.DB_NAME, null, Favorites.DB_VERSION);
    }

    // Called only once first time we create the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = String
                .format("create table %s (%s int primary key)",
                        Favorites.TABLE,
                        Favorites.Column.ID);
        Log.d(TAG, "onCreate with SQL: " + sql);
        db.execSQL(sql);
    }

    // Gets called whenever existing version != new version, i.e. schema changed
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Typically you do ALTER TABLE ...
        db.execSQL("drop table if exists " + Favorites.TABLE);
        onCreate(db);
    }
}
