package com.rhm.pwn.model;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;

/**
 * Created by sambo on 8/28/2017.
 */

@Database(entities = {URLCheck.class, PWNTask.class}, version = 1)
public abstract class PWNDatabase extends RoomDatabase {
    public abstract URLCheckDao urlCheckDao();

    private static PWNDatabase ourInstance;

    public static PWNDatabase getInstance(Context appContext) {
        if (ourInstance == null) {
            ourInstance = Room.databaseBuilder(appContext,
                    PWNDatabase.class, "PWNDb")
                    //.addMigrations(MIGRATION_1_2)
                    .build();
        }
        return ourInstance;
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            Log.d("SAMB", "Running database migration");
        }
    };
}
