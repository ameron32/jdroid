package com.jdroid.android.google.inappbilling;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.jdroid.android.application.AbstractApplication;
import com.jdroid.android.activity.AbstractFragmentActivity;
import com.jdroid.android.exception.DefaultExceptionHandler;
import com.jdroid.android.exception.DialogErrorDisplayer;
import com.jdroid.android.fragment.AbstractFragment;
import com.jdroid.java.exception.ErrorCodeException;
import com.jdroid.java.utils.LoggerUtils;

import org.slf4j.Logger;

import java.io.Serializable;
import java.util.List;

public class InAppBillingHelperFragment extends AbstractFragment implements InAppBillingClientListener {
	
	private static final String MANAGED_PRODUCT_TYPES = "managedProductTypes";
	private static final String SUBSCRIPTIONS_PRODUCT_TYPES = "subscriptionsProductTypes";
	private static final String SILENT_MODE = "silentMode";
	
	private static final Logger LOGGER = LoggerUtils.getLogger(InAppBillingHelperFragment.class);
	
	private InAppBillingClient inAppBillingClient;
	private List<ProductType> managedProductTypes;
	private List<ProductType> subscriptionsProductTypes;
	private Boolean silentMode;
	
	public static void add(FragmentActivity activity,
			Class<? extends InAppBillingHelperFragment> inAppBillingHelperFragmentClass, Boolean silentMode,
			Fragment targetFragment) {
		add(activity, inAppBillingHelperFragmentClass, InAppBillingAppModule.get().getInAppBillingContext().getManagedProductTypes(),
				InAppBillingAppModule.get().getInAppBillingContext().getSubscriptionsProductTypes(), silentMode, targetFragment);
	}
	
	public static void add(FragmentActivity activity,
			Class<? extends InAppBillingHelperFragment> inAppBillingHelperFragmentClass,
			List<ProductType> managedProductTypes, List<ProductType> subscriptionsProductTypes, Boolean silentMode,
			Fragment targetFragment) {
		
		if (!managedProductTypes.isEmpty() || (!subscriptionsProductTypes.isEmpty() && (get(activity) == null))) {
			AbstractFragmentActivity abstractFragmentActivity = (AbstractFragmentActivity)activity;
			InAppBillingHelperFragment inAppBillingHelperFragment = abstractFragmentActivity.instanceFragment(
				inAppBillingHelperFragmentClass, null);
			inAppBillingHelperFragment.setTargetFragment(targetFragment, 0);
			
			Bundle args = new Bundle();
			args.putSerializable(MANAGED_PRODUCT_TYPES, (Serializable)managedProductTypes);
			args.putSerializable(SUBSCRIPTIONS_PRODUCT_TYPES, (Serializable)subscriptionsProductTypes);
			args.putBoolean(SILENT_MODE, silentMode);
			inAppBillingHelperFragment.setArguments(args);
			
			FragmentTransaction fragmentTransaction = abstractFragmentActivity.getSupportFragmentManager().beginTransaction();
			fragmentTransaction.add(0, inAppBillingHelperFragment, InAppBillingHelperFragment.class.getSimpleName());
			fragmentTransaction.commit();
		}
		
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		managedProductTypes = getArgument(MANAGED_PRODUCT_TYPES);
		subscriptionsProductTypes = getArgument(SUBSCRIPTIONS_PRODUCT_TYPES);
		silentMode = getArgument(SILENT_MODE);
		
		inAppBillingClient = new InAppBillingClient(getActivity());
		
		// TODO The use cases logic should be replicated here. With the current approach, if an error happens on while
		// the app is on the background, when the fragment is resumed, the error dialog is not displayed to the user
		inAppBillingClient.setInAppBillingClientListener(this);
		inAppBillingClient.startSetup();
	}
	
	/**
	 * @see com.jdroid.android.google.inappbilling.InAppBillingClientListener#onSetupFinished()
	 */
	@Override
	public void onSetupFinished() {
		inAppBillingClient.queryInventory(managedProductTypes, subscriptionsProductTypes);
	}
	
	public InAppBillingListener getInAppBillingListener() {
		return (InAppBillingListener)getTargetFragment();
	}
	
	@Override
	public void onSetupFailed(ErrorCodeException errorCodeException) {
		AbstractApplication.get().getExceptionHandler().logHandledException(errorCodeException);
		if (!silentMode) {
			createErrorDisplayer(errorCodeException).displayError(errorCodeException);
		}
	}
	
	@Override
	public void onQueryInventoryFinished(Inventory inventory) {
		InAppBillingListener inAppBillingListener = getInAppBillingListener();
		if (inAppBillingListener != null) {
			inAppBillingListener.onProductsLoaded(inventory.getProducts());
			for (Product each : inventory.getProductToConsume()) {
				inAppBillingClient.consume(each);
			}
		}
	}
	
	@Override
	public void onQueryInventoryFailed(ErrorCodeException errorCodeException) {
		AbstractApplication.get().getExceptionHandler().logHandledException(errorCodeException);
		if (!silentMode) {
			errorCodeException.setDescription(getString(R.string.jdroid_failedToLoadPurchases));
			createErrorDisplayer(errorCodeException).displayError(errorCodeException);
		}
	}
	
	public void launchPurchaseFlow(Product product) {
		inAppBillingClient.launchInAppPurchaseFlow(getActivity(), product.getId(), product.getDeveloperPayload());
		InAppBillingAppModule.get().getAnalyticsSender().trackInAppBillingPurchaseTry(product);
	}
	
	@Override
	public void onPurchaseFinished(final Product product) {
		InAppBillingListener inAppBillingListener = getInAppBillingListener();
		if (inAppBillingListener != null) {
			inAppBillingListener.onPurchased(product);
			if (product.isWaitingToConsume()) {
				inAppBillingClient.consume(product);
			}
		}
	}
	
	@Override
	public void onPurchaseFailed(ErrorCodeException errorCodeException) {
		if (DefaultExceptionHandler.matchAnyErrorCode(errorCodeException, InAppBillingErrorCode.USER_CANCELED)) {
			LOGGER.warn(errorCodeException.getMessage());
		} else if (DefaultExceptionHandler.matchAnyErrorCode(errorCodeException, InAppBillingErrorCode.DEVELOPER_ERROR,
			InAppBillingErrorCode.ITEM_UNAVAILABLE)) {
			AbstractApplication.get().getExceptionHandler().logHandledException(errorCodeException);
		} else {
			DialogErrorDisplayer.markAsNotGoBackOnError(errorCodeException);
			AbstractApplication.get().getExceptionHandler().logHandledException(errorCodeException);
			createErrorDisplayer(errorCodeException).displayError(errorCodeException);
		}
	}
	
	@Override
	public void onConsumeFinished(Product product) {
		InAppBillingListener inAppBillingListener = getInAppBillingListener();
		if (inAppBillingListener != null) {
			inAppBillingListener.onConsumed(product);
		}
	}
	
	@Override
	public void onConsumeFailed(ErrorCodeException errorCodeException) {
		AbstractApplication.get().getExceptionHandler().logHandledException(errorCodeException);
		if (!silentMode) {
			createErrorDisplayer(errorCodeException).displayError(errorCodeException);
		}
	}
	
	/**
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (inAppBillingClient != null) {
			// Pass on the activity result to the helper for handling
			if (!inAppBillingClient.handleActivityResult(requestCode, resultCode, data)) {
				// not handled, so handle it ourselves (here's where you'd perform any handling of activity results not
				// related to in-app billing...
				super.onActivityResult(requestCode, resultCode, data);
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	/**
	 * @see com.jdroid.android.fragment.AbstractFragment#onDestroy()
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (inAppBillingClient != null) {
			inAppBillingClient.dispose();
			inAppBillingClient = null;
		}
	}
	
	public static InAppBillingHelperFragment get(FragmentActivity activity) {
		return ((AbstractFragmentActivity)activity).getFragment(InAppBillingHelperFragment.class);
	}
	
	public static void removeTarget(FragmentActivity activity) {
		InAppBillingHelperFragment inAppBillingHelperFragment = InAppBillingHelperFragment.get(activity);
		if (inAppBillingHelperFragment != null) {
			inAppBillingHelperFragment.setTargetFragment(null, 0);
		}
	}
}
