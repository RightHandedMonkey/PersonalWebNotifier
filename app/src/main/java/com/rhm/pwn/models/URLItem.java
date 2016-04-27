package com.rhm.pwn.models;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.Date;
import java.util.List;

/**
 * Created by sambo on 4/25/2016.
 */
@Table(name = "URLItems", id = BaseColumns._ID)
public class URLItem extends Model {
    @Column(name = "Name")
    public String name;
    @Column(name = "URL")
    public String url;
    @Column(name = "Period")
    public int period=URLPeriods.DAY_1;

    @Column(name = "jQuerySelector")
    public String jquerySelector="";
    @Column(name = "LastValue")
    public String lastValue="";
    @Column(name = "MarkedRead")
    public boolean markedRead = false;

    @Column(name = "LastCheckDatetime")
    public Date lastCheckDatetime;
    @Column(name = "LastUpdateDatetime")
    public Date lastUpdateDatetime;

    @Column(name = "Position")
    public int position=0;

    /* Future use */
    @Column(name = "LoginPageURL")
    public String loginPageURL="";
    @Column(name = "LoginPostField1")
    public String loginPostField1="";
    @Column(name = "LoginPostValue1")
    public String loginPostValue1="";
    @Column(name = "LoginPostField2")
    public String loginPostField2="";
    @Column(name = "LoginPostValue2")
    public String loginPostValue2="";

    @Column(name = "UUID")
    public String uuid="";
    @Column(name = "Uploaded")
    public boolean uploaded=false;
    @Column(name = "LocalID")
    public int localId=0;
    @Column(name = "ServerID")
    public int serverId=0;

    public List<URLItem> items() {
        return getMany(URLItem.class, "URLItem");
    }

    /*
    Loaders: can be moved to a separate class.  Just put here for now
     */
    public static List<URLItem> getAll() {
        return new Select()
                .from(URLItem.class)
                .orderBy("Position ASC")
                .execute();
    }

    public static URLItem getItem(long id) {
        return new Select()
                .from(URLItem.class)
                .where(BaseColumns._ID + "=?", id)
                .executeSingle();
    }

    public static int getMaxPosition() {
        URLItem item = new Select().from(URLItem.class).orderBy("Position DESC").executeSingle();
        int pos = 0;
        if (item != null) {
            pos = item.position;
        }
        return pos;
    }
}
