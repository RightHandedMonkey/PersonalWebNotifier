package com.rhm.pwn.model;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by sambo on 8/28/2017.
 */
@Dao
public interface URLCheckDao {
    @Query("SELECT * FROM urlcheck ORDER BY id ASC")
    List<URLCheck> getAll();

    @Query("SELECT * FROM urlcheck WHERE enableNotifications > 0 and updateShown < 1 ORDER BY id ASC")
    List<URLCheck> getAllEnabledAndNotUpdateShown();

    @Query("SELECT * FROM urlcheck WHERE enableNotifications > 0 ORDER BY id ASC")
    List<URLCheck> getAllEnabled();

    @Query("SELECT * FROM urlcheck WHERE id = :id")
    URLCheck get(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insertAll(URLCheck... urlChecks);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Long insertCheck(URLCheck urlCheck);

    @Insert
    Long insertTask(PWNTask pwntask);

    @Insert
    Long insertLog(PWNLog log);

    @Query("SELECT * FROM pwnlog ORDER BY id ASC LIMIT 500")
    List<PWNLog> getLogs();

    @Query("SELECT * FROM pwnlog ORDER BY id DESC LIMIT 500")
    List<PWNLog> getLogsDescending();

    @Query("DELETE FROM pwntask\n" +
            "WHERE id NOT IN (\n" +
            "  SELECT id\n" +
            "  FROM (\n" +
            "    SELECT id\n" +
            "    FROM pwntask\n" +
            "    ORDER BY id DESC\n" +
            "    LIMIT 20 -- keep this many records\n" +
            "  ) foo\n" +
            ");")
    void reduceTasks();

    @Query("SELECT * FROM pwntask WHERE id = :id")
    PWNTask getTask(int id);

    @Query("SELECT * FROM pwntask ORDER BY id DESC")
    List<PWNTask> getTasks();

    @Update
    void updateTask(PWNTask pwnTask);

    @Delete
    void delete(URLCheck urlCheck);

    @Update
    void update(List<URLCheck> urlCheck);

    @Update
    void update(URLCheck urlCheck);

    @Query("DELETE FROM urlcheck")
    void wipeTable();

}
