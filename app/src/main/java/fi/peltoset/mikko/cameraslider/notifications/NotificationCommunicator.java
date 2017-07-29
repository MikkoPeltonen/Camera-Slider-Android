package fi.peltoset.mikko.cameraslider.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import fi.peltoset.mikko.cameraslider.R;
import fi.peltoset.mikko.cameraslider.activities.CameraSliderMainActivity;
import fi.peltoset.mikko.cameraslider.activities.RecordingRunningActivity;
import fi.peltoset.mikko.cameraslider.interfaces.NotificationCommunicatorListener;

public class NotificationCommunicator {

  public static final String INTENT_PAUSE_PLAY_BUTTON_PRESSED = "INTENT_PAUSE_PLAY_BUTTON_PRESSED";
  public static final String INTENT_STOP_BUTTON_PRESSED = "INTENT_STOP_BUTTON_PRESSED";
  public static final String INTENT_RECONNECT = "INTENT_RECONNECT";

  private static final int NOTIFICATION_ID = 1;

  private Context context;
  private Service service;
  private NotificationCommunicatorListener listener;
  private NotificationManager notificationManager;

  private int totalFrames;
  private int capturedFrames;
  private int secondsElapsed;
  private int secondsRemaining;
  private int totalKeyframes;
  private int currentKeyframe;
  private int completePercentage;

  public NotificationCommunicator(Service service, NotificationCommunicatorListener listener) {
    this.service = service;
    this.context = (Context) service;
    this.listener = listener;

    notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
  }

  /**
   * Display timelapse notification
   */
  public void displayTimelapseNotification() {
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
        .setPriority(-1)
        .setAutoCancel(false)
        .setOngoing(true);

//    bigContentView.setOnClickPendingIntent(R.id.pauseButton, pause);
//    bigContentView.setOnClickPendingIntent(R.id.stopButton, stop);

    ((Service) context).startForeground(NOTIFICATION_ID, notificationBuilder.build());
  }

  /**
   * Display status bar info notification
   *
   * @param headerText
   * @param contentText
   */
  public void displayInfoNotification(String headerText, String contentText) {
    NotificationCompat.Builder notificationBuilder = constructInfoNotification(headerText, contentText);

    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

    stackBuilder.addParentStack(CameraSliderMainActivity.class);
    stackBuilder.addNextIntent(new Intent(context, CameraSliderMainActivity.class));

    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

    notificationBuilder.setContentIntent(resultPendingIntent);

    ((Service) context).startForeground(NOTIFICATION_ID, notificationBuilder.build());
  }

  public void displayTapToConnectNotification() {
    NotificationCompat.Builder notificationBuilder = constructInfoNotification("Camera Slider", "Disconnected, tap to reconnect");

    PendingIntent resultPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(INTENT_RECONNECT), 0);

    notificationBuilder.setContentIntent(resultPendingIntent);

    ((Service) context).startForeground(NOTIFICATION_ID, notificationBuilder.build());
  }

  private NotificationCompat.Builder constructInfoNotification(String headerText, String contentText) {
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context);

    RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_info);

    contentView.setImageViewResource(R.id.notificationIcon, R.mipmap.ic_launcher_camera);
    contentView.setTextViewText(R.id.infoHeader, headerText);
    contentView.setTextViewText(R.id.infoText, contentText);

    notificationBuilder
        .setSmallIcon(R.drawable.ic_linked_camera_white_48dp)
        .setOngoing(true)
        .setAutoCancel(false)
        .setContent(contentView)
        .setPriority(-1);

    return notificationBuilder;
  }

  /**
   * Remove all notifications
   */
  public void cancel() {
    notificationManager.cancel(NOTIFICATION_ID);
  }
}
