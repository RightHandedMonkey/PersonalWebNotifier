package com.rhm.pwn.model;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sambo on 8/29/2017.
 */
@RunWith(AndroidJUnit4.class)
public class URLCheckDaoTest {

    private PWNDatabase db;
    List<URLCheck> urls;
    private static String LONG_MSG_SEED="Refunds: " +
            "Are refunds available? " +
            "For Monthly and Quarterly Subscriptions " +
            "When cancelling a monthly or quarterly subscription, all future charges associated with your subscription will be cancelled. You may notify us of your intent to cancel at any time, however your cancellation will become effective at the end of your current billing period. You will not receive a refund; your subscription access and/or delivery and accompanying subscriber benefits will continue for the remainder of the current billing period. " +
            "For Semi-Annual and Annual Subscriptions " +
            "If you cancel a 6-month or annual subscription, your subscription and other subscriber benefits will end immediately upon your cancellation and you will receive a refund prorated to the effective date of your cancellation. If you cancel within the final 30 days of your subscription, the cancellation will not take effect until the end of your current billing period. You will not receive a refund; however your subscription access and/or delivery and accompanying subscriber benefits will continue for the remainder of the current billing period.";
    private static String LONG= "http://cnn.com\n" +
            "Refunds: Are refunds available? For Monthly and Quarterly Subscriptions When cancelling a monthly or quarterly subscription, all future charges associated with your subscription will be cancelled. You may notify us of your intent to cancel at any time, however your cancellation will become effective at the end of your current billing period. You will not receive a refund; your subscription access and/or delivery and accompanying subscriber benefits will continue for the remainder of the current billing period. For Semi-Annual and Annual Subscriptions If you cancel a 6-month or annual subscription, your subscription and other subscriber benefits will end immediately upon your cancellation and you will receive a refund prorated to the effective date of your c…\n" +
            "\n" +
            "\n" +
            "http://cnn.com\n" +
            "Refunds: Are refunds available? For Monthly and Quarterly Subscriptions When cancelling a monthly or quarterly subscription, all future charges associated with your subscription will be cancelled. You may notify us of your intent to cancel at any time, however your cancellation will become effective at the end of your current billing period. You will not receive a refund; your subscription access and/or delivery and accompanying subscriber benefits will continue for the remainder of the current billing period. For Semi-Annual and Annual Subscriptions If you cancel a 6-month or annual subscription, your subscription and other subscriber benefits will end immediately upon your cancellation and you will receive a refund prorated to the effective date of your c…";
    private static String SHORT="Refunds: Are refunds available? For Monthly and Quarterly Subscriptions When cancelling a monthly or quarterly subscription, all future charges associated with your subscription will be cancelled. You may notify us of your intent to cancel at any time, however your cancellation will become effective at the end of your current billing period. You will not receive a refund; your subscription access and/or delivery and accompanying subscriber benefits will continue for the remainder of the current billing period. For Semi-Annual and Annual Subscriptions If you cancel a 6-month or annual subscription, your subscription and other subscriber benefits will end immediately upon your cancellation and you will receive a refund prorated to the effective date of your c…";

    @Before
    public void setUp() throws Exception {
        urls = new ArrayList<>();
        URLCheck urla = URLCheckGenerator.getSample(0);
        urla.setLastValue(LONG_MSG_SEED);
        URLCheck urlb = URLCheckGenerator.getSample(1);
        urlb.setLastValue(LONG_MSG_SEED);

        urls.add(urla);
        urls.add(urlb);

        Context context = InstrumentationRegistry.getTargetContext();
        if (db == null) {
            db = PWNDatabase.getInstance(context);
        }
        db.urlCheckDao().wipeTable();
    }


    @After
    public void tearDown() throws Exception {
        db.urlCheckDao().wipeTable();
    }

    @Test
    public void getAll() throws Exception {
        //test getting 3 items
        URLCheck urlc1 = URLCheckGenerator.getSample(1);
        URLCheck urlc2 = URLCheckGenerator.getSample(2);
        URLCheck urlc3 = URLCheckGenerator.getSample(3);
        db.urlCheckDao().insertAll(urlc1, urlc2, urlc3);
        List<URLCheck> list = db.urlCheckDao().getAll();
        Assert.assertEquals(3, list.size());
    }

    @Test
    public void getURLCheck() throws Exception {
        URLCheck urlc1 = URLCheckGenerator.getSample(1);
        URLCheck urlc2 = URLCheckGenerator.getSample(2);
        List<Long> list = db.urlCheckDao().insertAll(urlc1, urlc2);
        URLCheck urlc = db.urlCheckDao().get(list.get(0).intValue());
        Assert.assertEquals(urlc1, urlc);
        Assert.assertFalse(urlc2.equals(urlc));
    }

    @Test
    public void insertAll() throws Exception {
        URLCheck urlc1 = URLCheckGenerator.getSample(1);
        URLCheck urlc2 = URLCheckGenerator.getSample(2);
        URLCheck urlc3 = URLCheckGenerator.getSample(3);
        List<Long> list = db.urlCheckDao().insertAll(urlc1, urlc2, urlc3);
        URLCheck urlcRead1 = db.urlCheckDao().get(list.get(0).intValue());
        URLCheck urlcRead2 = db.urlCheckDao().get(list.get(1).intValue());
        URLCheck urlcRead3 = db.urlCheckDao().get(list.get(2).intValue());
        Assert.assertEquals(urlc1, urlcRead1);
        Assert.assertFalse(urlc1.equals(urlcRead2));
        Assert.assertFalse(urlc1.equals(urlcRead3));

        Assert.assertFalse(urlc2.equals(urlcRead1));
        Assert.assertEquals(urlc2, urlcRead2);
        Assert.assertFalse(urlc2.equals(urlcRead3));

        Assert.assertFalse(urlc3.equals(urlcRead1));
        Assert.assertFalse(urlc3.equals(urlcRead2));
        Assert.assertEquals(urlc3, urlcRead3);
    }

    @Test
    public void delete() throws Exception {
        URLCheck urlc1 = URLCheckGenerator.getSample(1);
        URLCheck urlc2 = URLCheckGenerator.getSample(2);
        URLCheck urlc3 = URLCheckGenerator.getSample(3);
        List<Long> list = db.urlCheckDao().insertAll(urlc1, urlc2, urlc3);
        URLCheck urlcRead2 = db.urlCheckDao().get(list.get(1).intValue());

        db.urlCheckDao().delete(urlcRead2);
        List<URLCheck> listUrlc = db.urlCheckDao().getAll();
        Assert.assertEquals(2, listUrlc.size());
        Assert.assertFalse(urlc2.equals(listUrlc.get(0)));
        Assert.assertFalse(urlc2.equals(listUrlc.get(1)));

    }

    @Test
    public void updateUsers() throws Exception {
        URLCheck urlc1 = URLCheckGenerator.getSample(1);
        URLCheck urlc2 = URLCheckGenerator.getSample(2);
        URLCheck urlc3 = URLCheckGenerator.getSample(3);
        List<Long> list = db.urlCheckDao().insertAll(urlc1, urlc2, urlc3);
        URLCheck urlcRead2 = db.urlCheckDao().get(list.get(1).intValue());
        urlcRead2.setUrl("New url");
        db.urlCheckDao().update(urlcRead2);
        List<URLCheck> listUrlc = db.urlCheckDao().getAll();
        Assert.assertEquals(3, listUrlc.size());
        Assert.assertFalse(urlc2.equals(listUrlc.get(1)));
        Assert.assertEquals(urlcRead2, listUrlc.get(1));
    }

    @Test
    public void checkGeneratorItem1() throws Exception {
        URLCheck urlc1 = URLCheckGenerator.getSample(1);
        List<Long> list = db.urlCheckDao().insertAll(urlc1);
        URLCheck urlcRead = db.urlCheckDao().get(list.get(0).intValue());
        List<URLCheck> listUrlc = db.urlCheckDao().getAll();
        Assert.assertEquals(1, listUrlc.size());
        Assert.assertEquals(false, urlcRead.isEnableNotifications());
        Assert.assertEquals(false, urlcRead.isHasBeenUpdated());
    }

    @Test
    public void getShortUpdateText() {
        String str= URLCheckTask.buildShortNotificationMessage(urls);
        Assert.assertEquals(SHORT, str);
    }

    @Test
    public void getLongUpdateText() {
        String shortStr= URLCheckTask.buildShortNotificationMessage(urls);
        String longStr= URLCheckTask.buildLongNotificationMessage(urls);
        Assert.assertTrue("The long message should be bigger than the short one, but was not", longStr.length() > shortStr.length());
        Assert.assertEquals("The long string doesn't match the expected value", LONG, longStr);
    }

}