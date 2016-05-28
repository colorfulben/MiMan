package com.flintsoft.miman.dao;

import android.provider.BaseColumns;

/**
 * Created by Xin on 2015/12/20.
 */
public class Favorites {
    public static final String DB_NAME = "miman.Favorites.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "Favorites";
    public static final String DEFAULT_SORT = Column.ID + " DESC";

    public class Column { //
        public static final String ID = BaseColumns._ID;
    }
}
