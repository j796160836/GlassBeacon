package com.J_Test.GlassBeaconRanging;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Activity showing the options menu.
 */
public class MenuActivity extends Activity {

	private final Handler mHandler = new Handler();

	private boolean mAttachedToWindow;
	private boolean mOptionsMenuOpen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		mAttachedToWindow = true;
		openOptionsMenu();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAttachedToWindow = false;
	}

	@Override
	public void openOptionsMenu() {
		if (!mOptionsMenuOpen && mAttachedToWindow) {
			mOptionsMenuOpen = true;
			super.openOptionsMenu();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		setOptionsMenuGroupState(menu, R.id.stop, true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection.
		switch (item.getItemId()) {
		case R.id.stop:
			post(new Runnable() {

				@Override
				public void run() {
					stopService(new Intent(MenuActivity.this,
							BeaconService.class));
				}
			});
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		mOptionsMenuOpen = false;
		finish();
	}

	/**
	 * Posts a {@link Runnable} at the end of the message loop, overridable for
	 * testing.
	 */
	protected void post(Runnable runnable) {
		mHandler.post(runnable);
	}

	/**
	 * Sets all menu items visible and enabled state that are in the given
	 * group.
	 */
	private static void setOptionsMenuGroupState(Menu menu, int groupId,
			boolean enabled) {
		menu.setGroupVisible(groupId, enabled);
		menu.setGroupEnabled(groupId, enabled);
	}
}
