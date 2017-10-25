package rhm.com.pwn.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rhm.com.pwn.R;
import rhm.com.pwn.model.PWNDatabase;
import rhm.com.pwn.model.URLCheck;
import rhm.com.pwn.model.URLCheckChangeNotifier;
import rhm.com.pwn.model.URLCheckSelectedAction;
import rhm.com.pwn.view_url.WebViewActivity;

/**
 * A placeholder fragment containing a simple view.
 */
public class PWNHomeActivityFragment extends Fragment implements Observer {

    RecyclerView recyclerView;
    URLCheckAdapter mAdapter;
    List<URLCheck> list;

    URLCheckSelectedAction urlCheckAction = new URLCheckSelectedAction() {
        @Override
        public void onSelectedURLCheck(URLCheck urlc) {
            Intent i = new Intent(PWNHomeActivityFragment.this.getContext(), WebViewActivity.class);
            i.putExtra(URLCheck.class.getName(), urlc.getId());
            startActivity(i);
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
        recyclerView = (RecyclerView) view.findViewById(R.id.urlc_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
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
        Completable.fromAction(() -> {
                    list = PWNDatabase.getInstance(PWNHomeActivityFragment.this.getContext())
                            .urlCheckDao().getAll();

                }
        ).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                    mAdapter.setValues(list);
                    recyclerView.invalidate();
                });
    }
}
