package com.jdroid.android.google.analytics;

import android.support.annotation.WorkerThread;

import com.jdroid.java.analytics.AnalyticsTracker;

import java.util.concurrent.Executor;

public class AbstractGoogleAnalyticsTracker implements AnalyticsTracker {

	@Override
	public Boolean isEnabled() {
		return GoogleAnalyticsAppModule.get().getGoogleAnalyticsAppContext().isGoogleAnalyticsEnabled();
	}

	@WorkerThread
	protected GoogleAnalyticsHelper getGoogleAnalyticsHelper() {
		return GoogleAnalyticsAppModule.get().getGoogleAnalyticsHelper();
	}

	@Override
	public Executor getExecutor() {
		return getGoogleAnalyticsHelper().getExecutor();
	}
}
