package com.rhm.pwn.home;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;
import com.rhm.pwn.model.URLCheckSelectedAction;

/**
 * A placeholder fragment containing a simple view.
 */
public class PWNHomeActivityFragment extends Fragment implements Observer {

    RecyclerView recyclerView;
    TextView emptyView;
    URLCheckAdapter mAdapter;
    List<URLCheck> list;

    public static String ACTION = "action";
    public static String ACTION_EDIT = "edit";

    URLCheckSelectedAction urlCheckAction = new URLCheckSelectedAction() {
        @Override
        public void onSelectedURLCheck(URLCheck urlc) {
            if (isAdded()) {
                Log.d("SAMB", this.getClass().getName() + ", onSelectedURLCheck() called");
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                Intent selectAction = new Intent(PWNHomeActivityFragment.this.getActivity(), PWNHomeActivity.class);
                PendingIntent pi = PendingIntent.getActivity(PWNHomeActivityFragment.this.getActivity(), 0, selectAction, 0);
                Bitmap selectIcon = BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_input_get);
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(PWNHomeActivityFragment.this.getActivity(), Uri.parse(urlc.getUrl()));
                Completable.fromAction(() -> {
                            urlc.setHasBeenUpdated(false);
                            PWNDatabase.getInstance(PWNHomeActivityFragment.this.getContext()).urlCheckDao().update(urlc);
                        }
                ).subscribeOn(Schedulers.io())
                        .subscribe(() ->
                                URLCheckChangeNotifier.getNotifier().update(true));
                Log.d("SAMB", this.getClass().getName() + ", onSelectedURLCheck() finished");
            }
        }

        @Override
        public boolean onEditURLCheck(@NonNull URLCheck urlc) {
            // Create an instance of the dialog fragment and show it
            DialogFragment dialog = new URLCheckDialog();
            Bundle b = new Bundle();
            b.putSerializable(URLCheck.class.getName(), urlc);
            dialog.setArguments(b);
            dialog.show(PWNHomeActivityFragment.this.getFragmentManager(), URLCheckDialog.class.getName());
            return true;
        }

    };

    public PWNHomeActivityFragment() {
        Log.d("SAMB", this.getClass().getName() + ", ::constructor() called");
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("SAMB", this.getClass().getName() + ", onCreateView() called");
        list = new ArrayList<>();
        mAdapter = new URLCheckAdapter(list, urlCheckAction);
        return inflater.inflate(R.layout.fragment_pwnhome, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("SAMB", this.getClass().getName() + ", onViewCreated() called");

        URLCheckChangeNotifier.getNotifier().addObserver(this);
        recyclerView = view.findViewById(R.id.urlc_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        emptyView = view.findViewById(R.id.empty_view);
        updateFromDb();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d("SAMB", this.getClass().getName() + ", onDestroyView() called");
        URLCheckChangeNotifier.getNotifier().deleteObserver(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("SAMB", this.getClass().getName() + ", onDestroy() called");
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.d("SAMB", "Update called from Observable");
        if (observable instanceof URLCheckChangeNotifier) {
            updateFromDb();
        } else {
            throw new RuntimeException("Update received from unexpected Observable type");
        }
    }

    private void updateFromDb() {
        Completable.fromAction(
                () -> list = PWNDatabase.getInstance(PWNHomeActivityFragment.this.getContext()).urlCheckDao().getAll()
        ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    mAdapter.setValues(list);
                    recyclerView.invalidate();
                    if (list.isEmpty()) {
                        recyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerView.setVisibility(View.VISIBLE);
                        emptyView.setVisibility(View.GONE);
                    }
                });
    }
}
