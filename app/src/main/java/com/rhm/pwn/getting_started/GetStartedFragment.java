package com.rhm.pwn.getting_started;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rhm.pwn.R;

public class GetStartedFragment extends DialogFragment implements View.OnClickListener {

    private final static String ARG_LAYOUT_ID = "layoutId";
    public static final String TAG = "GetStartedFragment";
    private int index=0;
    private int max;
    private String[] messages;
    private String[] headers;
    private int[] images;
    private TextView headerView;
    private TextView messageView;
    private ImageView imageView;
    private Button button;

    public GetStartedFragment() {
    }

    public static GetStartedFragment newInstance() {
        GetStartedFragment getStartedFragment = new GetStartedFragment();
        return getStartedFragment;
    }

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        messages = getResources().getStringArray(R.array.get_started_message_array);
        headers = getResources().getStringArray(R.array.get_started_header_array);
        images = getResources().getIntArray(R.array.get_started_image_array);
        max = messages.length;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_getting_started, container);
        headerView = view.findViewById(R.id.gs_header);
        messageView = view.findViewById(R.id.gs_message);
        imageView = view.findViewById(R.id.gs_image);
        button = view.findViewById(R.id.gs_button);
        button.setOnClickListener(this);
        setupGettingStarted();
        return view;
    }

    private void setupGettingStarted() {
        headerView.setText(headers[index]);
        messageView.setText(messages[index]);
        imageView.setImageResource(images[index]);
        if (hasMore()) {
            button.setText("Next");
        } else {
            button.setText("Done");
        }
    }

    public boolean hasMore() {
        return (index < max -1);
    }

    @Override
    public void onClick(View v) {
        index++;
        //handle next state
        if (hasMore()) {
            setupGettingStarted();
        } else {
            dismiss();
        }
    }
}


