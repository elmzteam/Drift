package com.elmz.drift;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;

public class SoundHelper {
	public static void alarm(Context context) {
		final Uri sound;
		final SharedPreferences sp = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
		if (sp.getBoolean("alert", true)) {
			final String path = sp.getString("ringtone", null);
			if (path != null) {
				sound = Uri.parse(path);
			} else {
				sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			}
			RingtoneManager.getRingtone(context, sound).play();
			if (sp.getBoolean("vibrate", true)) {
				final Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
				// Vibrate for 500 milliseconds
				v.vibrate(500);
			}
		}
	}
}
