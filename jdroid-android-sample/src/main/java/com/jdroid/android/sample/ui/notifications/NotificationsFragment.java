package com.jdroid.android.sample.ui.notifications;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

import com.jdroid.android.application.AbstractApplication;
import com.jdroid.android.fragment.AbstractFragment;
import com.jdroid.android.notification.NotificationBuilder;
import com.jdroid.android.notification.NotificationUtils;
import com.jdroid.android.sample.R;
import com.jdroid.java.concurrent.ExecutorUtils;
import com.jdroid.java.date.DateUtils;
import com.jdroid.java.utils.IdGenerator;
import com.jdroid.java.utils.StringUtils;

public class NotificationsFragment extends AbstractFragment {

	private EditText notificationName;
	private EditText contentTitle;
	private EditText contentText;
	private EditText largeIconUrlEditText;
	private CheckBox largeIconDrawable;
	private EditText urlEditText;

	@Override
	public Integer getContentFragmentLayout() {
		return R.layout.notifications_fragment;
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		notificationName = findView(R.id.notificationName);
		notificationName.setText("myNotification");

		contentTitle = findView(R.id.contentTitle);
		contentTitle.setText("Title example");

		contentText = findView(R.id.contentText);
		contentText.setText("Description example");

		largeIconUrlEditText = findView(R.id.largeIconUrl);
		largeIconUrlEditText.setText("http://jdroidframework.com/images/gradle.png");

		urlEditText = findView(R.id.url);
		urlEditText.setText("http://jdroidframework.com/uri");

		largeIconDrawable = findView(R.id.largeIconDrawable);
		largeIconDrawable.setChecked(false);

		findView(R.id.sendNotification).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				ExecutorUtils.execute(new Runnable() {
					@Override
					public void run() {
						NotificationBuilder builder = new NotificationBuilder(notificationName.getText().toString());
						builder.setSmallIcon(AbstractApplication.get().getNotificationIconResId());

						if (largeIconDrawable.isChecked()) {
							builder.setLargeIcon(R.drawable.marker);
						} else {
							String largeIconUrl = largeIconUrlEditText.getText().toString();
							if (StringUtils.isNotEmpty(largeIconUrl)) {
								builder.setLargeIcon(largeIconUrl);
							}
						}

						builder.setContentTitle(contentTitle.getText().toString());
						builder.setContentText(contentText.getText().toString());
						builder.setSingleTopUrl(urlEditText.getText().toString());
						builder.setWhen(DateUtils.nowMillis());
						builder.setBlueLight();
						builder.setDefaultSound();

						NotificationUtils.sendNotification(IdGenerator.getIntId(), builder);
					}
				});
			}
		});

		findView(R.id.cancelNotifications).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				NotificationUtils.cancelAllNotifications();
			}
		});
	}
}
