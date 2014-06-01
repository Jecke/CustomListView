package com.treeapps.treenotes;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {
	public static final int NOTIFICATION_ID = 1;
	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;
	static final String TAG_GCM = "GCM";

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			/*
			 * Filter messages based on message type. Since it is likely that
			 * GCM will be extended in the future with new message types, just
			 * ignore any message types you're not interested in, or that you
			 * don't recognize.
			 */
			if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
				sendNotification("Send error: " + extras.toString());
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
				sendNotification("Deleted messages on server: " + extras.toString());
				// If it's a regular GCM message, do some work.
			} else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				// Do work here
				String strNoteUuid = extras.getString("note_uuid");
				String strNotificationType = extras.getString("notification_type");
				String strMessage = extras.getString("message");
				// Send message to Activity
				enumNotificationType objNotificationType = enumNotificationType.getValue(strNotificationType);
				if (objNotificationType == enumNotificationType.NOTIFY_AND_SYNC) {
					Intent localIntent =  new Intent(ActivityNoteStartup.BROADCAST_ACTION)
				            // Puts the status into the Intent
				            .putExtra(ActivityNoteStartup.BROADCAST_DATA_NOTE_UUID, strNoteUuid);
				    // Broadcasts the Intent to receivers in this app.
				    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
				} else if (objNotificationType == enumNotificationType.NOTIFY_ONLY) {
					// Post notification of received message.
					sendNotification("Received: " + strMessage);
				}
				Log.i(TAG_GCM, "Received: " + strMessage);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String msg) {
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ActivityExplorerStartup.class), 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher_app_icon).setContentTitle("TreeNotes")
				.setStyle(new NotificationCompat.BigTextStyle().bigText(msg)).setContentText(msg);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	public enum enumNotificationType {
		NONE(0), NOTIFY_ONLY(1), NOTIFY_AND_SYNC(2);

		final int numTab;

		private enumNotificationType(int num) {
			this.numTab = num;
		}

		public int getValue() {
			return this.numTab;
		}
		
		public static enumNotificationType getValue(String strName) {
			for (int i = 0; i < enumNotificationType.values().length; i++) {
				if (enumNotificationType.values()[i].name().equals(strName)) {
					return enumNotificationType.values()[i];
				}
			}
			return enumNotificationType.values()[0];
		}

	};
}
