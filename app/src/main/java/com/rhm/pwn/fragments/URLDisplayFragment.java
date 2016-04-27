package com.rhm.pwn.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.rhm.pwn.R;

/**
 * Created by sambo on 4/25/2016.
 */
public class URLDisplayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public static String SELECTED_URL_ITEM = "sel_url_item";
    public static String SELECTED_URL="sel_url";
    //public static URLItem SELECTED_URL_OBJECT;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String passedUrl;

//    private OnFragmentInteractionListener mListener;

    public URLDisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param url Parameter 1.
     * @return A new instance of fragment URLDisplayFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static URLDisplayFragment newInstance(String url) {
        URLDisplayFragment fragment = new URLDisplayFragment();
        Bundle args = new Bundle();
        args.putString(SELECTED_URL, url);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            passedUrl = getArguments().getString(SELECTED_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_urldisplay, container, false);
        String url = passedUrl;
        WebView wv = (WebView) view.findViewById(R.id.webView);
        setupWebView(wv);
        wv.loadUrl(url);
        return view;
    }

    public void setupWebView(WebView wv) {
        final Activity activity = this.getActivity();
        wv.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                activity.setProgress(progress*1000);
            }
        }) ;

        wv.setWebViewClient(new WebViewClient() {
            public void OnReceivedError(WebView view, int errorCode, String desc, String url) {
                Toast.makeText(activity, "Oops! "+desc, Toast.LENGTH_SHORT).show();
            }
        });

    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    public void updateSelectedItem(String url) {
        //Load the data
        passedUrl = url;
        WebView wv = (WebView) getActivity().findViewById(R.id.webView);
        setupWebView(wv);
        wv.loadUrl(url);
    }
}
