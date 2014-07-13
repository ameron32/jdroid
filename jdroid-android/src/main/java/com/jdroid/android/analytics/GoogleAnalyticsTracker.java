package com.jdroid.android.analytics;

import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import android.app.Activity;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.HitBuilders.AppViewBuilder;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.HitBuilders.TransactionBuilder;
import com.google.android.gms.analytics.Logger.LogLevel;
import com.google.android.gms.analytics.Tracker;
import com.jdroid.android.AbstractApplication;
import com.jdroid.android.analytics.ExperimentHelper.Experiment;
import com.jdroid.android.analytics.ExperimentHelper.ExperimentVariant;
import com.jdroid.android.inappbilling.Product;
import com.jdroid.android.social.AccountType;
import com.jdroid.android.social.SocialAction;
import com.jdroid.android.utils.AndroidUtils;
import com.jdroid.android.utils.SharedPreferencesHelper;
import com.jdroid.java.collections.Maps;
import com.jdroid.java.utils.LoggerUtils;

/**
 * 
 * @author Maxi Rosson
 */
public class GoogleAnalyticsTracker extends AbstractAnalyticsTracker {
	
	private static final Logger LOGGER = LoggerUtils.getLogger(GoogleAnalyticsTracker.class);
	
	public static final String NOTIFICATION_CATEGORY = "notification";
	private static final String ADS_CATEGORY = "ads";
	private static final String CLICK_ACTION = "click";
	
	private Map<String, Integer> customDimensionsMap = Maps.newHashMap();
	private Map<String, Integer> customMetricsMap = Maps.newHashMap();
	private Tracker tracker;
	private Boolean firstTrackingSent = false;
	
	public enum CustomDimension {
		LOGIN_SOURCE,
		INSTALLATION_SOURCE,
		APP_LOADING_SOURCE,
		DEVICE_TYPE;
	}
	
	public GoogleAnalyticsTracker() {
		if (isEnabled()) {
			GoogleAnalytics analytics = GoogleAnalytics.getInstance(AbstractApplication.get());
			if (AbstractApplication.get().getAppContext().isGoogleAnalyticsDebugEnabled()) {
				analytics.getLogger().setLogLevel(LogLevel.VERBOSE);
			} else {
				analytics.getLogger().setLogLevel(LogLevel.ERROR);
			}
			tracker = analytics.newTracker(AbstractApplication.get().getAppContext().getGoogleAnalyticsTrackingId());
			tracker.setSessionTimeout(300);
			init(customDimensionsMap, customMetricsMap);
		}
	}
	
	protected void init(Map<String, Integer> customDimensionsMap, Map<String, Integer> customMetricsMap) {
		// Do nothing
	}
	
	/**
	 * @see com.jdroid.android.analytics.AnalyticsTracker#isEnabled()
	 */
	@Override
	public Boolean isEnabled() {
		return AbstractApplication.get().getAppContext().isGoogleAnalyticsEnabled();
	}
	
	/**
	 * @see com.jdroid.android.analytics.AbstractAnalyticsTracker#onActivityStart(android.app.Activity,
	 *      com.jdroid.android.analytics.AppLoadingSource, java.lang.Object)
	 */
	@Override
	public void onActivityStart(Activity activity, AppLoadingSource appLoadingSource, Object data) {
		
		synchronized (GoogleAnalyticsTracker.class) {
			
			AppViewBuilder appViewBuilder = new HitBuilders.AppViewBuilder();
			if (appLoadingSource != null) {
				addCustomDimension(appViewBuilder, CustomDimension.APP_LOADING_SOURCE, appLoadingSource.getName());
			}
			
			if (!firstTrackingSent) {
				addCustomDimension(appViewBuilder, CustomDimension.DEVICE_TYPE, AndroidUtils.getDeviceType());
				
				for (Entry<Experiment, ExperimentVariant> entry : ExperimentHelper.getExperimentsMap().entrySet()) {
					Experiment experiment = entry.getKey();
					ExperimentVariant experimentVariant = entry.getValue();
					Integer customDimensionIndex = experiment.getCustomDimensionIndex();
					if (customDimensionIndex != null) {
						addCustomDimension(appViewBuilder, customDimensionIndex, experiment.getId() + "-"
								+ experimentVariant.getId());
					}
				}
				
				String installationSource = SharedPreferencesHelper.getOldDefault().loadPreference(
					AbstractApplication.INSTALLATION_SOURCE);
				if (installationSource != null) {
					addCustomDimension(appViewBuilder, CustomDimension.INSTALLATION_SOURCE, installationSource);
					
					onAppLoadTrack(appViewBuilder);
					firstTrackingSent = true;
				}
			}
			onActivityStartTrack(appViewBuilder);
			sendScreenView(appViewBuilder, activity.getClass().getSimpleName());
		}
		
	}
	
	protected void onAppLoadTrack(AppViewBuilder appViewBuilder) {
		// Do nothing
	}
	
	protected void onActivityStartTrack(AppViewBuilder appViewBuilder) {
		// Do nothing
	}
	
	/**
	 * @see com.jdroid.android.analytics.AbstractAnalyticsTracker#trackInAppBillingPurchase(com.jdroid.android.inappbilling.Product)
	 */
	@Override
	public void trackInAppBillingPurchase(Product product) {
		HitBuilders.TransactionBuilder transactionBuilder = new HitBuilders.TransactionBuilder();
		transactionBuilder.setTransactionId(product.getPurchase().getOrderId());
		transactionBuilder.setAffiliation("Google Play");
		transactionBuilder.setRevenue(product.getPrice());
		transactionBuilder.setCurrencyCode(product.getCurrencyCode());
		sendTransaction(transactionBuilder.build());
		
		HitBuilders.ItemBuilder itemBuilder = new HitBuilders.ItemBuilder();
		itemBuilder.setTransactionId(product.getPurchase().getOrderId());
		itemBuilder.setName(product.getProductType().getProductId());
		itemBuilder.setCategory(product.getProductType().isConsumable() ? "consumable" : "notConsumable");
		itemBuilder.setSku(product.getProductType().getProductId());
		itemBuilder.setQuantity(1);
		itemBuilder.setPrice(product.getPrice());
		itemBuilder.setCurrencyCode(product.getCurrencyCode());
		sendTransaction(itemBuilder.build());
	}
	
	/**
	 * @see com.jdroid.android.analytics.AbstractAnalyticsTracker#trackNotificationDisplayed(java.lang.String)
	 */
	@Override
	public void trackNotificationDisplayed(String notificationName) {
		sendEvent(NOTIFICATION_CATEGORY, "display", notificationName);
	}
	
	/**
	 * @see com.jdroid.android.analytics.AnalyticsTracker#trackNotificationOpened(java.lang.String)
	 */
	@Override
	public void trackNotificationOpened(String notificationName) {
		sendEvent(NOTIFICATION_CATEGORY, "open", notificationName);
	}
	
	/**
	 * @see com.jdroid.android.analytics.AbstractAnalyticsTracker#trackRemoveAdsBannerClicked()
	 */
	@Override
	public void trackRemoveAdsBannerClicked() {
		sendEvent(ADS_CATEGORY, CLICK_ACTION, "removeAds");
	}
	
	/**
	 * @see com.jdroid.android.analytics.AbstractAnalyticsTracker#trackRateMeBannerClicked()
	 */
	@Override
	public void trackRateMeBannerClicked() {
		sendEvent(ADS_CATEGORY, CLICK_ACTION, "rateMe");
	}
	
	/**
	 * @see com.jdroid.android.analytics.AnalyticsTracker#trackSocialInteraction(com.jdroid.android.social.AccountType,
	 *      com.jdroid.android.social.SocialAction, java.lang.String)
	 */
	@Override
	public void trackSocialInteraction(AccountType accountType, SocialAction socialAction, String socialTarget) {
		tracker.send(new HitBuilders.SocialBuilder().setNetwork(accountType.getFriendlyName()).setAction(
			socialAction.getName()).setTarget(socialTarget).build());
	}
	
	protected void addCustomDimension(AppViewBuilder appViewBuilder, CustomDimension customDimension, String dimension) {
		addCustomDimension(appViewBuilder, customDimension.name(), dimension);
	}
	
	protected void addCustomDimension(AppViewBuilder appViewBuilder, String customDimension, String dimension) {
		Integer index = customDimensionsMap.get(customDimension);
		addCustomDimension(appViewBuilder, index, dimension);
	}
	
	protected void addCustomDimension(AppViewBuilder appViewBuilder, Integer index, String dimension) {
		if (index != null) {
			LOGGER.debug("Added custom dimension: " + index + " - " + dimension);
			appViewBuilder.setCustomDimension(index, dimension);
		}
	}
	
	protected void addCustomDimension(EventBuilder eventBuilder, Map<String, String> customDimensions) {
		if (customDimensions != null) {
			for (Entry<String, String> entry : customDimensions.entrySet()) {
				addCustomDimension(eventBuilder, entry.getKey(), entry.getValue());
			}
		}
	}
	
	protected void addCustomDimension(EventBuilder eventBuilder, CustomDimension customDimension, String dimension) {
		addCustomDimension(eventBuilder, customDimension.name(), dimension);
	}
	
	protected void addCustomDimension(EventBuilder eventBuilder, String customDimension, String dimension) {
		Integer index = customDimensionsMap.get(customDimension);
		addCustomDimension(eventBuilder, index, dimension);
	}
	
	protected void addCustomDimension(EventBuilder eventBuilder, Integer index, String dimension) {
		if (index != null) {
			LOGGER.debug("Added custom dimension: " + index + " - " + dimension);
			eventBuilder.setCustomDimension(index, dimension);
		}
	}
	
	protected void addCustomDimension(TransactionBuilder transactionBuilder, Map<String, String> customDimensions) {
		if (customDimensions != null) {
			for (Entry<String, String> entry : customDimensions.entrySet()) {
				addCustomDimension(transactionBuilder, entry.getKey(), entry.getValue());
			}
		}
	}
	
	protected void addCustomDimension(TransactionBuilder transactionBuilder, String customDimension, String dimension) {
		Integer index = customDimensionsMap.get(customDimension);
		addCustomDimension(transactionBuilder, index, dimension);
	}
	
	protected void addCustomDimension(TransactionBuilder transactionBuilder, Integer index, String dimension) {
		if (index != null) {
			LOGGER.debug("Added custom dimension: " + index + " - " + dimension);
			transactionBuilder.setCustomDimension(index, dimension);
		}
	}
	
	protected void addCustomMetric(AppViewBuilder appViewBuilder, String customDimensionKey, Float metric) {
		Integer index = customMetricsMap.get(customDimensionKey);
		addCustomMetric(appViewBuilder, index, metric);
	}
	
	protected void addCustomMetric(AppViewBuilder appViewBuilder, Integer index, Float metric) {
		if (index != null) {
			LOGGER.debug("Added custom metric: " + index + " - " + metric);
			appViewBuilder.setCustomMetric(index, metric);
		}
	}
	
	public void sendScreenView(AppViewBuilder appViewBuilder, String screenName) {
		tracker.setScreenName(screenName);
		tracker.send(appViewBuilder.build());
	}
	
	public void sendScreenView(String screenName) {
		tracker.setScreenName(screenName);
		tracker.send(new HitBuilders.AppViewBuilder().build());
	}
	
	public void sendEvent(String category, String action, String label, Long value) {
		tracker.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).setValue(
			value).build());
	}
	
	public void sendEvent(String category, String action, String label) {
		sendEvent(category, action, label, (Map<String, String>)null);
	}
	
	public void sendEvent(String category, String action, String label, Map<String, String> customDimensions) {
		HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
		addCustomDimension(eventBuilder, customDimensions);
		tracker.send(eventBuilder.setCategory(category).setAction(action).setLabel(label).build());
		LOGGER.debug("Sent event. Category [" + category + "] Action [" + action + "] Label [" + label + "]");
	}
	
	public void sendTransaction(Map<String, String> params) {
		tracker.send(params);
	}
	
	public void dispatchLocalHits() {
		GoogleAnalytics.getInstance(AbstractApplication.get()).dispatchLocalHits();
	}
}
