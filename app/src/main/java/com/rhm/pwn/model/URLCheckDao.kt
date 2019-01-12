package com.rhm.pwn.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.reactivex.Observable

/**
 * Created by sambo on 8/28/2017.
 */
@Dao
interface URLCheckDao {
    @Query("SELECT * FROM urlcheck ORDER BY id ASC")
    fun all(): List<URLCheck>

    @Query("SELECT * FROM urlcheck ORDER BY id ASC")
    fun allObservable(): Observable<List<URLCheck>>

    @Query("SELECT * FROM urlcheck WHERE enableNotifications > 0 and updateShown < 1 ORDER BY id ASC")
    fun allEnabledAndNotUpdateShown(): List<URLCheck>

    @Query("SELECT * FROM urlcheck WHERE enableNotifications > 0 ORDER BY id ASC")
    fun allEnabled(): List<URLCheck>

    @Query("SELECT * FROM pwnlog ORDER BY id ASC LIMIT 500")
    fun logs(): List<PWNLog>

    @Query("SELECT * FROM pwnlog ORDER BY id DESC LIMIT 500")
    fun logsDescending(): List<PWNLog>

    @Query("SELECT * FROM pwntask ORDER BY id DESC")
    fun tasks(): List<PWNTask>

    @Query("SELECT * FROM urlcheck WHERE id = :id")
    operator fun get(id: Int): URLCheck

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg urlChecks: URLCheck): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCheck(urlCheck: URLCheck): Long?

    @Insert
    fun insertTask(pwntask: PWNTask): Long?

    @Insert
    fun insertLog(log: PWNLog): Long?

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
    fun reduceTasks()

    @Query("SELECT * FROM pwntask WHERE id = :id")
    fun getTask(id: Int): PWNTask

    @Update
    fun updateTask(pwnTask: PWNTask)

    @Delete
    fun delete(urlCheck: URLCheck)

    @Update
    fun update(urlCheck: List<URLCheck>)

    @Update
    fun update(urlCheck: URLCheck)

    @Query("DELETE FROM urlcheck")
    fun wipeTable()

}
