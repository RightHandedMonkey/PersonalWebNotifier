package com.rhm.pwn.home;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import io.reactivex.Completable;
import io.reactivex.schedulers.Schedulers;
import com.rhm.pwn.R;
import com.rhm.pwn.model.PWNDatabase;
import com.rhm.pwn.model.URLCheck;
import com.rhm.pwn.model.URLCheckChangeNotifier;
import com.rhm.pwn.model.URLCheckInterval;

/**
 * Created by sambo on 8/31/2017.
 */

public class URLCheckDialog extends DialogFragment {

    URLCheck urlc = new URLCheck();


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle b = getArguments();

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        // Pass null as the parent view because its going in the dialog layout
        View v = inflater.inflate(R.layout.fragment_pwnedit, null);
        TextInputEditText urlEdit = (TextInputEditText) v.findViewById(R.id.urlEditText);
        TextInputEditText cssEdit = (TextInputEditText) v.findViewById(R.id.cssEditText);
        Spinner intervalSpinner = (Spinner) v.findViewById(R.id.checkIntervalSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_spinner_item, URLCheckInterval.getIntervalStrings());
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(spinnerArrayAdapter);
        intervalSpinner.setSelection(URLCheckInterval.DEFAULT_VAL);

        CheckBox enableCheckbox = (CheckBox) v.findViewById(R.id.notificationCheckBox);

        if (b!= null && b.get(URLCheck.class.getName()) != null) {
            urlc = (URLCheck) b.get(URLCheck.class.getName());
            urlEdit.setText(urlc.getUrl());
            cssEdit.setText(urlc.getCssSelectorToInspect());
            enableCheckbox.setChecked(urlc.isEnableNotifications());
            intervalSpinner.setSelection(URLCheckInterval.getIndexFromInterval(urlc.getCheckInterval()));
        }
        builder.setView(v);

        builder.setMessage("Edit URL Check")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Completable.fromAction( () -> {
                            Long interval = URLCheckInterval.getIntervalFromDescription(intervalSpinner.getSelectedItem().toString());
                            boolean reloadService = urlc.getCheckInterval() != interval;
                            reloadService = reloadService || (enableCheckbox.isChecked() != urlc.isEnableNotifications());
                            String url =urlEdit.getText().toString();
                            urlc.setUrl(url);
                            String css = cssEdit.getText().toString();
                            urlc.setCssSelectorToInspect(css);
                            urlc.setCheckInterval(interval);
                            urlc.setEnableNotifications(enableCheckbox.isChecked());
                            //reset last runtime for the edited object because it may have a different configuration
                            urlc.setLastElapsedRealtime(0);
                            PWNDatabase.getInstance(getActivity().getApplicationContext()).urlCheckDao().insertAll(urlc);
                            URLCheckChangeNotifier.getNotifier().update(reloadService);
                        })      .subscribeOn(Schedulers.io())
                                .subscribe();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
