package com.jdroid.android.crashlytics;

import android.support.v4.util.Pair;

import com.crashlytics.android.Crashlytics;
import com.jdroid.android.analytics.CoreAnalyticsTracker;
import com.jdroid.android.application.AbstractAppModule;
import com.jdroid.android.application.AbstractApplication;
import com.jdroid.java.collections.Lists;

import java.util.List;
import java.util.Map;

import io.fabric.sdk.android.Kit;

public class CrashlyticsAppModule extends AbstractAppModule {

	public static final String MODULE_NAME = CrashlyticsAppModule.class.getName();

	public static CrashlyticsAppModule get() {
		return (CrashlyticsAppModule)AbstractApplication.get().getAppModule(MODULE_NAME);
	}

	private CrashlyticsAppContext crashlyticsAppContext;

	public CrashlyticsAppModule() {
		crashlyticsAppContext = createCrashlyticsAppContext();
	}

	protected CrashlyticsAppContext createCrashlyticsAppContext() {
		return new CrashlyticsAppContext();
	}

	public CrashlyticsAppContext getCrashlyticsAppContext() {
		return crashlyticsAppContext;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		AbstractApplication.get().getDebugContext().addCustomDebugInfoProperty(new Pair<String, Object>("Crashlytics Enabled", crashlyticsAppContext.isCrashlyticsEnabled()));
	}

	@Override
	public List<Kit> createFabricKits() {
		return Lists.<Kit>newArrayList(new Crashlytics());
	}

	@Override
	public List<? extends CoreAnalyticsTracker> createCoreAnalyticsTrackers() {
		return Lists.newArrayList(createCrashlyticsTracker());
	}

	protected CoreAnalyticsTracker createCrashlyticsTracker() {
		return new CrashlyticsCoreAnalyticsTracker();
	}

	@Override
	public void onInitExceptionHandler(Map<String, String> metadata) {
		if (metadata != null) {
			for (Map.Entry<String, String> entry : metadata.entrySet()) {
				if (entry.getValue() != null) {
					Crashlytics.getInstance().core.setString(entry.getKey(), entry.getValue());
				}
			}
		}
	}
}
