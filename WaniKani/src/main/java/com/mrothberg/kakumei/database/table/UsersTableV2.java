package com.mrothberg.kakumei.database.table;

import android.provider.BaseColumns;

public class UsersTableV2 implements BaseColumns {

    public static final String TABLE_NAME = "usersV2";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_LEVEL = "level";
    public static final String COLUMN_NAME_STARTED_AT = "started_at";
    public static final String COLUMN_NAME_NULLABLE = "nullable";

    public static final String[] COLUMNS = {
            COLUMN_NAME_ID,
            COLUMN_NAME_USERNAME,
            COLUMN_NAME_LEVEL,
            COLUMN_NAME_STARTED_AT,
            COLUMN_NAME_NULLABLE
    };

}
