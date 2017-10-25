package rhm.com.pwn;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import rhm.com.pwn.home.BootCompleteReceiver;
import rhm.com.pwn.home.URLCheckService;
import rhm.com.pwn.model.PWNDatabase;
import rhm.com.pwn.model.URLCheck;
import rhm.com.pwn.model.URLCheckChangeNotifier;
import rhm.com.pwn.model.URLCheckJobCompletedNotifier;
import rhm.com.pwn.model.URLCheckTask;
import rhm.com.pwn.network.PWNRetroFitConnector;

/**
 * Created by sambo on 8/28/2017.
 */

public class PWNApp extends Application implements Observer {

    public static final String CHANNEL_ID="PWNApp";


    @Override
    public void onCreate() {
        super.onCreate();
        registerNotifications();
        URLCheckChangeNotifier.getNotifier().addObserver(this);
        Log.d("SAMB", "Application Created.");
        PWNDatabase.getInstance(getApplicationContext());
        Log.d("SAMB", "Room Persistence Library Initialized.");

//        registerReceiver(new BootCompleteReceiver(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
//        registerReceiver(new YourConnectionChangedBroadcastReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        URL url;
//        final URLCheck urlc = new URLCheck();
//        urlc.setUrl("https://www.kickstarter.com/projects/1445624543/anne/posts");
//        urlc.setCssSelectorToInspect(":nth-child(1) > .project_post_summary > .normal > .green-dark");
//
//
//        Log.d("SAMB", "Getting baseurl: "+urlc.getBaseUrl());
//
//        Retrofit retrofit = PWNRetroFitConnector.getInstance(urlc.getBaseUrl());
//        PWNRetroFitConnector.GetPage getPageService = retrofit.create(PWNRetroFitConnector.GetPage.class);
//        getPageService.GetPageAsString(urlc.getUrl()).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(Call<String> call, Response<String> response) {
//
//                Document doc = Jsoup.parse(response.body().toString());
//                Element tags = doc.select(urlc.getCssSelectorToInspect()).first();
//                Log.d("SAMB", "Response was: " + tags.text());
//            }
//
//            @Override
//            public void onFailure(Call<String> call, Throwable t) {
//                Log.e("SAMB", "Error Occurred", t);
//
//            }
//        });

        startService();
    }

    @Override
    public void onTerminate() {
        Log.d("SAMB", this.getClass().getName()+" - Application Shutting down");
        super.onTerminate();
        URLCheckChangeNotifier.getNotifier().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof URLCheckChangeNotifier) {
            if (o instanceof  Boolean && (Boolean)((Boolean) o).booleanValue()) {
                startService();
            }
        } else {
            throw new RuntimeException("Update received from unexpected Observable type");
        }
    }

    public void startService() {
        Intent service = new Intent(getApplicationContext(), URLCheckService.class);
        service.putExtra(URLCheckService.class.getName(), true);
        getApplicationContext().startService(service);
    }

    public void registerNotifications() {
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // The user-visible name of the channel.
        CharSequence name = getString(R.string.app_name);
        // The user-visible description of the channel.
        String description = getString(R.string.app_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        // Configure the notification channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(true);
            // Sets the notification light color for notifications posted to this
            // channel, if the device supports this feature.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

}
