package com.jdroid.android.firebase.remoteconfig;

import com.jdroid.android.analytics.CoreAnalyticsTracker;
import com.jdroid.android.application.AbstractAppModule;
import com.jdroid.android.application.AbstractApplication;
import com.jdroid.android.debug.PreferencesAppender;
import com.jdroid.java.collections.Lists;

import java.util.List;

public class FirebaseRemoteConfigAppModule extends AbstractAppModule {

	public static final String MODULE_NAME = FirebaseRemoteConfigAppModule.class.getName();

	public static FirebaseRemoteConfigAppModule get() {
		return (FirebaseRemoteConfigAppModule)AbstractApplication.get().getAppModule(MODULE_NAME);
	}

	private FirebaseRemoteConfigDebugContext firebaseRemoteConfigDebugContext;

	public FirebaseRemoteConfigDebugContext getFirebaseRemoteConfigDebugContext() {
		synchronized (AbstractApplication.class) {
			if (firebaseRemoteConfigDebugContext == null) {
				firebaseRemoteConfigDebugContext = createFirebaseRemoteConfigDebugContext();
			}
		}
		return firebaseRemoteConfigDebugContext;
	}

	protected FirebaseRemoteConfigDebugContext createFirebaseRemoteConfigDebugContext() {
		return new FirebaseRemoteConfigDebugContext();
	}

	@Override
	public List<PreferencesAppender> getPreferencesAppenders() {
		return getFirebaseRemoteConfigDebugContext().getPreferencesAppenders();
	}

	@Override
	public List<? extends CoreAnalyticsTracker> createCoreAnalyticsTrackers() {
		return Lists.newArrayList(new FirebaseRemoteConfigTracker());
	}
}
