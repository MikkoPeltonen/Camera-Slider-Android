package fi.peltoset.mikko.cameraslider;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class KeyframeAdapter extends RecyclerView.Adapter<KeyframeAdapter.ViewHolder> {
    private ArrayList<KeyframePOJO> dataset;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView keyframeDuration;
        TextView keyframeStartsAt;
        TextView slideLength;
        TextView panAngle;
        TextView tiltAngle;
        TextView zoomAmount;
        TextView focusAngle;

        public ViewHolder(View itemView) {
            super(itemView);

            keyframeDuration = (TextView) itemView.findViewById(R.id.keyframeDuration);
            keyframeStartsAt = (TextView) itemView.findViewById(R.id.keyframeStartsAt);
            slideLength = (TextView) itemView.findViewById(R.id.slideLength);
            panAngle = (TextView) itemView.findViewById(R.id.panAngle);
            tiltAngle = (TextView) itemView.findViewById(R.id.tiltAngle);
            zoomAmount = (TextView) itemView.findViewById(R.id.zoomAmount);
            focusAngle = (TextView) itemView.findViewById(R.id.focusAngle);
        }
    }

    public KeyframeAdapter(ArrayList<KeyframePOJO> dataset) {
        this.dataset = dataset;
    }

    /**
     * Create new view by inflating a new layout.
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.keyframe_listview_item, parent, false);


        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    /**
     * Replace contents of a view.
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        KeyframePOJO keyframe = dataset.get(position);

        holder.keyframeDuration.setText(keyframe.getFormattedDuration());
        holder.slideLength.setText(keyframe.getFormattedSlideLength());
        holder.panAngle.setText(keyframe.getFormattedPanAngle());
        holder.tiltAngle.setText(keyframe.getFormattedSlideLength());
        holder.zoomAmount.setText(keyframe.getFormattedZoom());
        holder.focusAngle.setText(keyframe.getFormattedFocus());
    }

    @Override
    public int getItemCount() {
        return dataset.size();
    }
}