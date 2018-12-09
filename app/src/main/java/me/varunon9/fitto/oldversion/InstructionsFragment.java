package me.varunon9.fitto.oldversion;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import me.varunon9.fitto.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class InstructionsFragment extends Fragment {


    public InstructionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_instructions, container, false);
        WebView instructionsWebView = (WebView) rootView.findViewById(R.id.instructions_webView);
        instructionsWebView.loadUrl("file:///android_asset/instructions.html");
        return rootView;
    }

}
