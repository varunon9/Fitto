package me.varunon9.fitto;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment {


    public PlayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_play, container, false);
        Button restartButton = (Button) rootView.findViewById(R.id.restartButton);
        Button undoButton = (Button) rootView.findViewById(R.id.undoButton);
        CanvasBoardView canvasBoardView =
                (CanvasBoardView) rootView.findViewById(R.id.canvasBoardView);
        canvasBoardView.initialiseButtons(restartButton, undoButton);
        return rootView;
    }

}
