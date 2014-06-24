package com.J_Test.GlassBeaconMonitoring;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.Region;

// http://developer.radiusnetworks.com/ibeacon/android/configure.html

public class BeaconService extends Service implements IBeaconConsumer {

	private static final String TAG = "BeaconService";

	private IBeaconManager iBeaconManager = IBeaconManager
			.getInstanceForApplication(this);

	private LiveCard mLiveCard;
	private LiveCard mMessageCard;
	private RemoteViews mLiveCardView;
	private RemoteViews mMessageCardView;

	private StringBuilder msgBuilder;

	@Override
	public void onCreate() {
		super.onCreate();
		msgBuilder = new StringBuilder();
		iBeaconManager.bind(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLiveCard == null) {

			// Get an instance of a live card
			mLiveCard = new LiveCard(this, TAG);

			// Inflate a layout into a remote view
			mLiveCardView = new RemoteViews(getPackageName(),
					R.layout.status_livecard);

			mLiveCard.setViews(mLiveCardView);

			// Set up the live card's action with a pending intent
			// to show a menu when tapped
			Intent menuIntent = new Intent(this, MenuActivity.class);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent,
					0));

			// Publish the live card
			mLiveCard.publish(PublishMode.SILENT);

		}
		if (iBeaconManager.isBound(this))
			iBeaconManager.setBackgroundMode(this, true);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		iBeaconManager.unBind(this);

		if (mLiveCard != null && mLiveCard.isPublished()) {
			// Stop the handler from queuing more Runnable jobs

			mLiveCard.unpublish();
			mLiveCard = null;
		}

		if (mMessageCard != null && mMessageCard.isPublished()) {
			// Stop the handler from queuing more Runnable jobs

			mMessageCard.unpublish();
			mMessageCard = null;
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onIBeaconServiceConnect() {

		Log.v(TAG, "==onIBeaconServiceConnect");
		iBeaconManager.setMonitorNotifier(new MonitorNotifier() {
			@Override
			public void didEnterRegion(Region region) {
				logToDisplay("I just saw an iBeacon for the first time!");
			}

			@Override
			public void didExitRegion(Region region) {
				logToDisplay("I no longer see an iBeacon");
			}

			@Override
			public void didDetermineStateForRegion(int state, Region region) {
				logToDisplay("I have just switched from seeing/not seeing iBeacons: "
						+ state);
			}

		});
		try {
			iBeaconManager.startMonitoringBeaconsInRegion(new Region(
					"E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", null, null, null));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private void logToDisplay(final String line) {
		Log.v(TAG, "Msg: " + line);

		showNotification2(line);
	}

	private void showNotification2(String msg) {
		RemoteViews views = new RemoteViews(getPackageName(),
				R.layout.message_card);
		views.setTextViewText(R.id.message_txt, msg);
		mMessageCard = new LiveCard(getApplication(), "beacon");
		mMessageCard.setViews(views);
		Intent intent = new Intent(getApplication(), BeaconService.class);
		mMessageCard.setAction(PendingIntent.getActivity(getApplication(), 0,
				intent, 0));
		mMessageCard.publish(LiveCard.PublishMode.REVEAL);
	}

	private void showNotification(String msg) {
		if (mMessageCard == null) {

			// Get an instance of a live card
			mMessageCard = new LiveCard(this, TAG);

			// Inflate a layout into a remote view
			mMessageCardView = new RemoteViews(getPackageName(),
					R.layout.message_card);
			// Set up the live card's action with a pending intent
			// to show a menu when tapped
			Intent menuIntent = new Intent(this, MenuActivity.class);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mMessageCard.setAction(PendingIntent.getActivity(this, 0,
					menuIntent, 0));

			// Publish the live card
			mMessageCard.publish(PublishMode.REVEAL);
		}

		msgBuilder.append(msg);

		mMessageCardView.setTextViewText(R.id.message_txt,
				msgBuilder.toString());

		// Always call setViews() to update the live card's RemoteViews.
		mMessageCard.setViews(mMessageCardView);

	}
}