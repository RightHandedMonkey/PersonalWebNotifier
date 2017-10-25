package rhm.com.pwn.model;

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

    @Insert
    Long insertTask(PWNTask pwntask);

    @Query("SELECT * FROM pwntask WHERE id = :id")
    PWNTask getTask(int id);

    @Query("SELECT * FROM pwntask")
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
