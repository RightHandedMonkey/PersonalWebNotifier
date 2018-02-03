package com.rhm.pwn.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by sambo on 8/28/2017.
 */

@Entity
public class URLCheck implements Serializable{
    @PrimaryKey(autoGenerate = true)
    int id;
    String url = "";
    String title = "";
    String lastUpdated = "";
    String lastChecked = "";
    long lastElapsedRealtime=0;
    String lastValue = "";
    String cssSelectorToInspect = "";
    long checkInterval = URLCheckInterval.INT_1_DAY.getInterval();
    boolean enableNotifications = true;
    boolean hasBeenUpdated = true;
    boolean updateShown = false;
    int lastRunCode = CODE_NOT_RUN;
    String lastRunMessage = "";
    @Ignore
    private boolean urlValid = false;

    public boolean isUrlValid() {
        return urlValid;
    }

    public void setUrlValid(boolean urlValid) {
        this.urlValid = urlValid;
    }

    @Ignore
    private String baseUrl = "";

    @Ignore
    public static final int CODE_NOT_RUN=-1;
    @Ignore
    public static final int CODE_RUN_SUCCESSFUL=0;
    @Ignore
    public static final int CODE_RUN_FAILURE=1;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastRunMessage() {
        return lastRunMessage;
    }

    public void setLastRunMessage(String lastRunMessage) {
        this.lastRunMessage = lastRunMessage;
    }


    public int getLastRunCode() {
        return lastRunCode;
    }

    public void setLastRunCode(int lastRunCode) {
        this.lastRunCode = lastRunCode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /*
        url1.name = "A.N.N.E.";
        url1.url = "https://www.kickstarter.com/projects/1445624543/anne/posts";
        selector = ":nth-child(1) > .project_post_summary > .normal > .green-dark"

        url2.name = "Paradise Lost - First Contact";
        url2.url = "https://www.kickstarter.com/projects/1183462809/paradise-lost-first-contact/posts";
        selector = ":nth-child(1) > .project_post_summary > .normal > .green-dark"

        url3.name = "World Clock Time";
        url3.url = "https://www.timeanddate.com/worldclock/";
        selector = "#p0"

        url3.name = "Foxnews";
        url3.url = "http://www.foxnews.com";
        selector = ".collection-spotlight > :nth-child(1) > .story-1 > .info > .info-header > .title > a"

     */
    private void setBaseUrl(String _url) {
        try {
            URL urlObj = new URL(_url);
            baseUrl = urlObj.getProtocol()+"://"+urlObj.getHost();
            setUrlValid(true);
        } catch (MalformedURLException e) {
            baseUrl = "{Malformed URL}";
            setUrlValid(false);
            Log.e("SAMB", "Couldn't convert url to URL Object"+ _url);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        URLCheck urlCheck = (URLCheck) o;

        if (checkInterval != urlCheck.checkInterval) return false;
        if (enableNotifications != urlCheck.enableNotifications) return false;
        if (!url.equals(urlCheck.url)) return false;
        if (!lastValue.equals(urlCheck.lastValue)) return false;
        return cssSelectorToInspect.equals(urlCheck.cssSelectorToInspect);
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + lastValue.hashCode();
        result = 31 * result + cssSelectorToInspect.hashCode();
        result = 31 * result + (int) (checkInterval ^ (checkInterval >>> 32));
        result = 31 * result + (enableNotifications ? 1 : 0);
        return result;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        setBaseUrl(url);
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(String lastChecked) {
        this.lastChecked = lastChecked;
    }

    public String getLastValue() {
        return lastValue;
    }

    public void setLastValue(String lastValue) {
        this.lastValue = lastValue;
    }

    public String getCssSelectorToInspect() {
        return cssSelectorToInspect;
    }

    public void setCssSelectorToInspect(String cssSelectorToInspect) {
        this.cssSelectorToInspect = cssSelectorToInspect;
    }

    public long getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(long checkInterval) {
        this.checkInterval = checkInterval;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }

    public void setEnableNotifications(boolean enableNotifications) {
        this.enableNotifications = enableNotifications;
    }

    public boolean isHasBeenUpdated() {
        return hasBeenUpdated;
    }

    public void setHasBeenUpdated(boolean hasBeenUpdated) {
        this.hasBeenUpdated = hasBeenUpdated;
    }

    public long getLastElapsedRealtime() {
        return lastElapsedRealtime;
    }

    public void setLastElapsedRealtime(long lastElapsedRealtime) {
        this.lastElapsedRealtime = lastElapsedRealtime;
    }

    public boolean isUpdateShown() {
        return updateShown;
    }

    public void setUpdateShown(boolean updateShown) {
        this.updateShown = updateShown;
    }

    public String getDisplayTitle() {
        if (TextUtils.isEmpty(title)) {
            return getBaseUrl();
        } else {
            return title;
        }
    }

    public String getDisplayBody() {
        if (!TextUtils.isEmpty(lastValue)) {
            return lastValue;
        } else {
            return url;
        }
    }

}
