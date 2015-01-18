package com.elmz.drift;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Created by El1t on 1/18/15.
 */

public class PreferencesActivity extends PreferenceActivity {
	Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
		LinearLayout content = (LinearLayout) root.getChildAt(0);
		LinearLayout toolbarContainer = (LinearLayout) View.inflate(this, R.layout.toolbar_layout, null);

		root.removeAllViews();
		toolbarContainer.addView(content);
		root.addView(toolbarContainer);

		mToolbar = (Toolbar) toolbarContainer.findViewById(R.id.toolbar);
		mToolbar.setTitle("Settings");
		mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.preference_headers, target);
	}

	@Override
	protected boolean isValidFragment(String fragmentName) {
		return Prefs1Fragment.class.getName().equals(fragmentName) |
				Prefs2Fragment.class.getName().equals(fragmentName);
	}

	public static class Prefs1Fragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preference_alert);
		}
	}

	public static class Prefs2Fragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Can retrieve arguments from headers XML.
//			Log.i("args", "Arguments: " + getArguments());

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.preference_openbci);
		}
	}
}
