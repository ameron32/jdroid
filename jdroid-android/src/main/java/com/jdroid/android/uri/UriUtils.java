package com.jdroid.android.uri;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.jdroid.android.application.AbstractApplication;
import com.jdroid.android.intent.IntentUtils;
import com.jdroid.android.utils.AppUtils;
import com.jdroid.java.utils.RandomUtils;
import com.jdroid.java.utils.StringUtils;

public class UriUtils {

	private static final String ORIGINAL_URI = "originalUri";
	private static final String RANDOM_PARAMETER = "rnd";

	public static Intent createIntent(Context context, String url, String referrer) {
		Intent intent;
		if (url != null) {
			intent = new Intent();
			intent.putExtra(ORIGINAL_URI, url);
			intent.setData(UriUtils.addRandomParam(Uri.parse(url)));
			intent.setPackage(AppUtils.getApplicationId());
			if (!IntentUtils.isIntentAvailable(intent)) {
				intent = new Intent(context, AbstractApplication.get().getHomeActivityClass());
				AbstractApplication.get().getExceptionHandler().logHandledException("Url is not valid: " + url);
			}
		} else {
			intent = new Intent(context, AbstractApplication.get().getHomeActivityClass());
			AbstractApplication.get().getExceptionHandler().logHandledException("Missing url to create intent");
		}
		ReferrerUtils.setReferrer(intent, referrer);
		return intent;
	}

	public static Uri addRandomParam(Uri uri) {
		String uriString = uri.toString();
		StringBuilder builder = new StringBuilder(uriString);
		if (uri.getPathSegments().isEmpty() && StringUtils.isEmpty(uri.getQuery()) && !uriString.endsWith("/")) {
			builder.append("/");
		}
		if (StringUtils.isEmpty(uri.getQuery())) {
			builder.append("?");
		} else{
			builder.append("&");
		}
		builder.append(RANDOM_PARAMETER);
		builder.append("=");
		builder.append(RandomUtils.getInt());
		return Uri.parse(builder.toString());
	}


	public static Uri getUri(Intent intent) {
		Uri uri = intent.getData();
		if (uri != null) {
			if (intent.hasExtra(ORIGINAL_URI)) {
				uri = Uri.parse(intent.getStringExtra(ORIGINAL_URI));
			}
		}
		return uri;
	}

	public static Uri getUri(Activity activity) {
		return getUri(activity.getIntent());
	}
}
