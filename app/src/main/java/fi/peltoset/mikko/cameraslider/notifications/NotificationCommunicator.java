package fi.peltoset.mikko.cameraslider.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.activities.RecordingRunningActivity;
import fi.peltoset.mikko.cameraslider.interfaces.NotificationCommunicatorListener;

public class NotificationCommunicator {

  private static final String INTENT_PAUSE_PLAY_BUTTON_PRESSED = "INTENT_PAUSE_PLAY_BUTTON_PRESSED";
  private static final String INTENT_STOP_BUTTON_PRESSED = "INTENT_STOP_BUTTON_PRESSED";

  private static final int NOTIFICATION_TIMELAPSE = 1;

  private Context context;
  private NotificationCommunicatorListener listener;
  private NotificationManager notificationManager;

  private int totalFrames;
  private int capturedFrames;
  private int secondsElapsed;
  private int secondsRemaining;
  private int totalKeyframes;
  private int currentKeyframe;
  private int completePercentage;

  public NotificationCommunicator(Context context, NotificationCommunicatorListener listener) {
    this.context = context;
    this.listener = listener;

    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public void displaySampleNotification() {
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

    // Using TaskStackBuilder, we can add the main activity in the back stack so that it is opened
    // after the activity opened from the notification is closed.
    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

    stackBuilder.addParentStack(RecordingRunningActivity.class);
    stackBuilder.addNextIntent(new Intent(context, RecordingRunningActivity.class));

    PendingIntent notificationIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    notificationBuilder.setContentIntent(notificationIntent);

//    PendingIntent pause = PendingIntent.getActivity(context, 123, new Intent(), 0);
//    PendingIntent stop = PendingIntent.getActivity(context, 456, new Intent(), 0);

    RemoteViews bigContentView = new RemoteViews(context.getPackageName(), R.layout.notification_timelapse_running_expanded);

    bigContentView.setImageViewResource(R.id.notificationIcon, R.mipmap.ic_launcher_camera);
    bigContentView.setImageViewResource(R.id.framesCapturedIcon, R.drawable.ic_linked_camera_white_24dp);
    bigContentView.setImageViewResource(R.id.timeElapsedIcon, R.drawable.ic_access_time_white_36dp);
    bigContentView.setImageViewResource(R.id.keyframeIcon, R.drawable.ic_video_label_white_24dp);
    bigContentView.setImageViewResource(R.id.pauseIcon, R.drawable.ic_pause_black_48dp);
    bigContentView.setImageViewResource(R.id.stopIcon, R.drawable.ic_stop_black_48dp);

    RemoteViews smallContentView = new RemoteViews(context.getPackageName(), R.layout.notification_timelapse_running);

    smallContentView.setImageViewResource(R.id.notificationIcon, R.mipmap.ic_launcher_camera);

    notificationBuilder
        .setContent(smallContentView)
        .setCustomBigContentView(bigContentView)
        .setSmallIcon(R.drawable.ic_linked_camera_white_48dp)
        .setAutoCancel(false)
        .setOngoing(true);

//    bigContentView.setOnClickPendingIntent(R.id.pauseButton, pause);
//    bigContentView.setOnClickPendingIntent(R.id.stopButton, stop);

    notificationManager.notify(NOTIFICATION_TIMELAPSE, notificationBuilder.build());
  }

  public void cancel() {
    notificationManager.cancel(NOTIFICATION_TIMELAPSE);
  }
}
