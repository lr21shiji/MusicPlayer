package com.attackt.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

/**
 * 处理媒体按键的广播接收器
 * 
 * @author mh
 * */
public class MediaButtonReceiver extends BroadcastReceiver {
	private static final String TAG = MediaButtonReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent key = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (key.getAction() == KeyEvent.ACTION_DOWN) {
				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Context.TELEPHONY_SERVICE);
				if (tm.getCallState() == TelephonyManager.CALL_STATE_IDLE) {
					Log.i(TAG, "OnReceive, getKeyCode = " + key.getKeyCode());

					switch (key.getKeyCode()) {
					case KeyEvent.KEYCODE_HEADSETHOOK:
						context.startService(new Intent(
								MusicService.ACTION_PLAY));
						break;
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
						context.startService(new Intent(
								MusicService.ACTION_PREVIOUS));
						break;
					case KeyEvent.KEYCODE_MEDIA_NEXT:
						context.startService(new Intent(
								MusicService.ACTION_NEXT));
						break;
					}
				}
			}
		}
	}
}
