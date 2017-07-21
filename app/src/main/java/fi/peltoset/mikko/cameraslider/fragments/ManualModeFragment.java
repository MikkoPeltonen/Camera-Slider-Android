package fi.peltoset.mikko.cameraslider.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fi.peltoset.mikko.cameraslider.R;


public class ManualModeFragment extends Fragment {
    public ManualModeFragment() {}

    public static ManualModeFragment newInstance() {
        ManualModeFragment fragment = new ManualModeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manual_mode, container, false);

        return view;
    }
}
