package com.rhm.pwn.home;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import com.jakewharton.rxbinding2.view.RxView;
import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;
import com.rhm.pwn.model.URLCheckInterval;
import com.rhm.pwn.view_url.WebViewActivity;

import org.jetbrains.annotations.NotNull;

/**
 * Created by sambo on 8/31/2017.
 */

public class URLCheckDialog extends DialogFragment {

    URLCheck urlc = new URLCheck();


    @Override
    @NotNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Pass null as the parent view because its going in the dialog layout
        @SuppressLint("InflateParams")
        View v = inflater.inflate(R.layout.fragment_pwnedit, null);
        TextInputEditText urlEdit = v.findViewById(R.id.urlEditText);
        TextInputEditText cssEdit = v.findViewById(R.id.cssEditText);
        Spinner intervalSpinner = v.findViewById(R.id.checkIntervalSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, URLCheckInterval.getIntervalStrings());
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(spinnerArrayAdapter);
        intervalSpinner.setSelection(URLCheckInterval.DEFAULT_VAL);
        CheckBox enableCheckbox = v.findViewById(R.id.notificationCheckBox);
        Button selectCSS = v.findViewById(R.id.selectCSSButton);

        if (b != null && b.get(URLCheck.class.getName()) != null) {
            urlc = (URLCheck) b.get(URLCheck.class.getName());
            urlEdit.setText(urlc.getUrl());
            cssEdit.setText(urlc.getCssSelectorToInspect());
            enableCheckbox.setChecked(urlc.isEnableNotifications());
            intervalSpinner.setSelection(URLCheckInterval.getIndexFromInterval(urlc.getCheckInterval()));
        }

        RxView.clicks(selectCSS).subscribe(aVoid -> {
            Completable.fromAction(() -> onSaveClickedHelper(urlc, intervalSpinner, enableCheckbox, urlEdit, cssEdit)).observeOn(AndroidSchedulers.mainThread())
                    .observeOn(Schedulers.io())
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        int id = (int) onSaveClickedDbHelper(urlc, true);
                        if (urlc.getId() > 0) {
                            id = urlc.getId();
                        }
                        Log.d("SAMB", this.getClass().getName() + ", onSelectCSSCheck() called for id#"+id);
                        Intent i = new Intent(this.getContext(), WebViewActivity.class);
                        i.putExtra(URLCheck.class.getName(), id);
                        startActivityForResult(i, WebViewActivity.HIGHLIGHT_ROW_FROM_CSS_SELECTOR);
                        Log.d("SAMB", this.getClass().getName() + ", onSelectedURLCheck() finished");
                        dismiss();
                    });
        });

        builder.setView(v);

        builder.setMessage("Edit URL Check")
                .setNeutralButton("Delete", (dialog, which) -> Completable.fromAction(() -> {
                    PWNDatabase.getInstance(getActivity().getApplicationContext()).urlCheckDao().delete(urlc);
                    URLCheckChangeNotifier.getNotifier().update(true);
                }).subscribeOn(Schedulers.io())
                        .subscribe())
                .setPositiveButton("Save", (dialog, id) -> Completable.fromAction(() -> {
                    boolean reload = onSaveClickedHelper(urlc, intervalSpinner, enableCheckbox, urlEdit, cssEdit);
                    onSaveClickedDbHelper(urlc, reload);
                }).subscribeOn(Schedulers.io())
                        .subscribe())
                .setNegativeButton("Cancel", (dialog, id) -> {
                    // User cancelled the dialog
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public boolean onSaveClickedHelper(URLCheck urlc, Spinner intervalSpinner, CheckBox enableCheckbox, TextInputEditText urlEdit, TextInputEditText cssEdit) {
        Long interval = URLCheckInterval.getIntervalFromDescription(intervalSpinner.getSelectedItem().toString());
        boolean reloadService = urlc.getCheckInterval() != interval;
        reloadService = reloadService || (enableCheckbox.isChecked() != urlc.isEnableNotifications());
        String url = urlEdit.getText().toString();
        urlc.setUrl(url);
        String css = cssEdit.getText().toString();
        urlc.setCssSelectorToInspect(css);
        urlc.setCheckInterval(interval);
        urlc.setEnableNotifications(enableCheckbox.isChecked());
        //reset last runtime for the edited object because it may have a different configuration
        urlc.setLastElapsedRealtime(0);
        return reloadService;
    }

    public long onSaveClickedDbHelper(URLCheck urlc, boolean reload) {
        long id = PWNDatabase.getInstance(getActivity().getApplicationContext()).urlCheckDao().insertCheck(urlc);
        URLCheckChangeNotifier.getNotifier().update(reload);
        return id;
    }
}
