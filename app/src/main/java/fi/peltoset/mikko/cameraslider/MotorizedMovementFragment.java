package fi.peltoset.mikko.cameraslider;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MotorizedMovementFragment extends Fragment {

    private FloatingActionButton startMovement;

    public MotorizedMovementFragment() {}

    public static MotorizedMovementFragment newInstance() {
        MotorizedMovementFragment fragment = new MotorizedMovementFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_motorized_movement, container, false);

        startMovement = (FloatingActionButton) view.findViewById(R.id.startMovement);

        startMovement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }
}
