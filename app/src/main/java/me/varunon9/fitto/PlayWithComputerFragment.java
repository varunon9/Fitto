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
public class PlayWithComputerFragment extends Fragment {


    public PlayWithComputerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_play_with_computer, container, false);
        Button restartButton = (Button) rootView.findViewById(R.id.restartButton);
        Button undoButton = (Button) rootView.findViewById(R.id.undoButton);
        ComputerCanvasBoardView computerCanvasBoardView =
                (ComputerCanvasBoardView) rootView.findViewById(R.id.computerCanvasBoardView);
        computerCanvasBoardView.initialiseButtons(restartButton, undoButton);
        return rootView;
    }

}
