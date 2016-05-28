package com.flintsoft.miman.dao;

import android.provider.BaseColumns;

/**
 * Created by Xin on 2015/11/22.
 */
public class LastReadPosition {
    public static final String DB_NAME = "miman.LastReadPosition.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE = "LastReadPosition";
    public static final String DEFAULT_SORT = Column.ID + " DESC";

    public class Column { //
        public static final String ID = BaseColumns._ID;
        public static final String ChapterIndex = "chapterIndex";
        public static final String PageIndex = "pageIndex";
    }
}
