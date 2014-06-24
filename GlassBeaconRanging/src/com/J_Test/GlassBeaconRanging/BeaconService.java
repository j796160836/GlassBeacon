package com.J_Test.GlassBeaconRanging;

import java.util.Collection;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;
import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

// http://developer.radiusnetworks.com/ibeacon/android/configure.html

public class BeaconService extends Service implements IBeaconConsumer {

	private static final String TAG = "BeaconService";

	private IBeaconManager iBeaconManager = IBeaconManager
			.getInstanceForApplication(this);

	private LiveCard mLiveCard;
	private RemoteViews mLiveCardView;

	@Override
	public void onCreate() {
		super.onCreate();
		iBeaconManager.bind(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mLiveCard == null) {

			// Get an instance of a live card
			mLiveCard = new LiveCard(this, TAG);

			// Inflate a layout into a remote view
			mLiveCardView = new RemoteViews(getPackageName(),
					R.layout.message_card);

			mLiveCard.setViews(mLiveCardView);

			// Set up the live card's action with a pending intent
			// to show a menu when tapped
			Intent menuIntent = new Intent(this, MenuActivity.class);
			menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent,
					0));

			// Publish the live card
			mLiveCard.publish(PublishMode.REVEAL);

		}
		if (iBeaconManager.isBound(this))
			iBeaconManager.setBackgroundMode(this, false);

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

		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onIBeaconServiceConnect() {

		Log.v(TAG, "==onIBeaconServiceConnect");
		iBeaconManager.setRangeNotifier(new RangeNotifier() {

			@Override
			public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons,
					Region region) {

				if (iBeacons.size() > 0) {
					IBeacon beacon = iBeacons.iterator().next();
					StringBuilder sb = new StringBuilder();
					sb.append("uuid=" + beacon.getProximityUuid() + "\n");
					sb.append("Major=" + beacon.getMajor() + "\n");
					sb.append("Minor=" + beacon.getMinor() + "\n");
					sb.append("rssi=" + beacon.getRssi() + "\n");
					sb.append("Accuracy=" + beacon.getAccuracy() + "\n");
					sb.append("Proximity=" + beacon.getProximity() + "\n");
					sb.append("TxPower=" + beacon.getTxPower() + "\n");
					final String msg = sb.toString();
					mLiveCardView.setTextViewText(R.id.message_txt, msg);

					mLiveCard.setViews(mLiveCardView);
				}
			}
		});

		try {
			Log.d("HLog", "=============");
			Log.d("HLog", "startRangingBeaconsInRegion");
			Log.d("HLog", "=============");
			iBeaconManager.startRangingBeaconsInRegion(new Region(
					"E2C56DB5-DFFB-48D2-B060-D0F5A71096E0", null, null, null));
		} catch (RemoteException e) {
		}
	}
}