package com.rhm.pwn.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

import com.rhm.pwn.R;

/**
 * Created by sambo on 4/25/2016.
 */
public class URLNewItemDialog extends DialogFragment{

    String editText;
    String title;
    int position;
    public static final String POSITION = "position";
    public static final String TEXT = "text";
    public static final String TITLE = "title";

    public static URLNewItemDialog newInstance(String editText, int position, String title) {
        Log.d(URLNewItemDialog.class.getName(), "Starting newInstance(...)");
        URLNewItemDialog f = new URLNewItemDialog();
        Bundle args = new Bundle();
        args.putString(TEXT, editText);
        args.putInt(POSITION, position);
        args.putString(TITLE, title);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(this.getClass().getName(), "Starting onCreate(...)");
        super.onCreate(savedInstanceState);
        editText = getArguments().getString(TEXT);
        position = getArguments().getInt(POSITION);
        title = getArguments().getString(TITLE);

    }

    public void onPositiveButtonClicked(DialogInterface dialog, int id) {
        // delegate the action
        Log.d(this.getClass().getName(), "positive selected - onClick(...), " + id);
        Intent intent = new Intent();

        EditText edit = (EditText) URLNewItemDialog.this.getDialog().findViewById(R.id.urlNameEdit);
        intent.putExtra(POSITION, position);
        intent.putExtra(TEXT, edit.getText().toString());
        Fragment f = getTargetFragment();
        if (getTargetFragment() != null) {
            //if sent as fragment
            getTargetFragment().onActivityResult(getTargetRequestCode(), 1, intent);
        } else {
            //if sent as activity
            onActivityResult(getTargetRequestCode(), 1, intent);
        }
        //close the dialog so it doesn't reappear if fragment or activity changes state
        URLNewItemDialog.this.getDialog().dismiss();
    }

    public void onCancelButtonClicked(DialogInterface dialog, int id) {
        Log.d(this.getClass().getName(), "negative selected - onClick(...)");
        URLNewItemDialog.this.getDialog().cancel();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(this.getClass().getName(), "Starting onCreateDialog(...)");
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View view = inflater.inflate(R.layout.url_new_item_dialog, null);
        EditText edit = (EditText) view.findViewById(R.id.urlNameEdit);
        edit.setText(editText);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        onPositiveButtonClicked(dialog, id);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                onCancelButtonClicked(dialog, id);
            }
        });

        edit.requestFocus();
        if (title != null)
            builder.setTitle(title);
        Dialog dialog = builder.create();
        //show the keyboard automatically
        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        return dialog;
    }
}
