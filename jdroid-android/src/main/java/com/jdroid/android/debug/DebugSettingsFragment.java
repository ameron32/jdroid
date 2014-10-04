package com.jdroid.android.debug;

import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.view.View;
import android.widget.ListView;
import com.jdroid.android.R;
import com.jdroid.android.fragment.AbstractPreferenceFragment;
import com.jdroid.android.gcm.GcmMessage;
import com.jdroid.java.collections.Lists;
import com.jdroid.java.utils.CollectionUtils;

public class DebugSettingsFragment extends AbstractPreferenceFragment {
	
	/**
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(getDebugPreferences());
	}
	
	/**
	 * @see android.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		initDebugGcmMessages();
		DebugExperimentsHelper.initPreferences(getActivity(), getPreferenceScreen());
		DebugLogHelper.initPreferences(getActivity(), getPreferenceScreen());
		DebugImageLoaderHelper.initPreferences(getActivity(), getPreferenceScreen());
		DebugHttpCacheHelper.initPreferences(getActivity(), getPreferenceScreen());
		DebugCrashHelper.initPreferences(getActivity(), getPreferenceScreen());
		
		ListView listView = ((ListView)findView(android.R.id.list));
		
		View debugInfoView = new DebugInfoView(getActivity());
		listView.addFooterView(debugInfoView);
		
		View customDebugInfoView = getCustomDebugInfoView();
		if (customDebugInfoView != null) {
			listView.addFooterView(debugInfoView);
		}
	}
	
	protected void initDebugGcmMessages() {
		
		final List<? extends GcmMessage> gcmMessages = getGcmMessages();
		if (CollectionUtils.isNotEmpty(gcmMessages)) {
			PreferenceCategory preferenceCategory = new PreferenceCategory(getActivity());
			preferenceCategory.setTitle(R.string.gcmSettings);
			getPreferenceScreen().addPreference(preferenceCategory);
			
			ListPreference preference = new ListPreference(getActivity());
			preference.setTitle(R.string.emulateGcmMessageTitle);
			preference.setDialogTitle(R.string.emulateGcmMessageTitle);
			preference.setSummary(R.string.emulateGcmMessageDescription);
			List<CharSequence> entries = Lists.newArrayList();
			for (GcmMessage entry : gcmMessages) {
				entries.add(entry.getMessageKey());
			}
			preference.setEntries(entries.toArray(new CharSequence[0]));
			preference.setEntryValues(entries.toArray(new CharSequence[0]));
			preference.setPersistent(false);
			preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					
					GcmMessage gcmMessage = null;
					for (GcmMessage each : gcmMessages) {
						if (each.getMessageKey().equals(newValue.toString())) {
							gcmMessage = each;
							break;
						}
					}
					
					Intent intent = getEmulatedGcmMessageIntent(gcmMessage);
					if (intent != null) {
						gcmMessage.handle(intent);
					}
					
					return false;
				}
			});
			preferenceCategory.addPreference(preference);
		}
	}
	
	protected Intent getEmulatedGcmMessageIntent(GcmMessage gcmMessage) {
		return null;
	}
	
	protected List<? extends GcmMessage> getGcmMessages() {
		return null;
	}
	
	protected Integer getDebugPreferences() {
		return R.xml.debug_preferences;
	}
	
	protected View getCustomDebugInfoView() {
		return null;
	}
}