package com.jdroid.android.sample.ui.google.admob;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.ads.AdSize;
import com.jdroid.android.fragment.FragmentHelper;
import com.jdroid.android.google.admob.AdViewType;
import com.jdroid.android.google.admob.helpers.BaseAdViewHelper;
import com.jdroid.android.google.admob.helpers.NativeExpressAdViewHelper;
import com.jdroid.android.recycler.AbstractRecyclerFragment;
import com.jdroid.android.recycler.RecyclerViewAdapter;
import com.jdroid.android.recycler.RecyclerViewType;
import com.jdroid.android.sample.R;
import com.jdroid.android.sample.application.AndroidAppContext;
import com.jdroid.android.sample.ui.recyclerview.SimpleRecyclerFragment;
import com.jdroid.android.sample.usecase.SampleItemsUseCase;
import com.jdroid.java.collections.Lists;

import java.util.List;

public class AdRecyclerFragment extends AbstractRecyclerFragment {

	private SampleItemsUseCase sampleItemsUseCase;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sampleItemsUseCase = new SampleItemsUseCase();
	}

	@Override
	public void onStart() {
		super.onStart();
		registerUseCase(sampleItemsUseCase, this, FragmentHelper.UseCaseTrigger.ONCE);
	}

	@Override
	public void onStop() {
		super.onStop();
		unregisterUseCase(sampleItemsUseCase, this);
	}

	@Override
	public void onFinishUseCase() {
		executeOnUIThread(new Runnable() {
			@Override
			public void run() {
				List<RecyclerViewType> recyclerViewTypes = Lists.<RecyclerViewType>newArrayList(new StringRecyclerViewType(), new MyAdViewType());

				List<Object> items = Lists.newArrayList();
				for(String each : sampleItemsUseCase.getItems()) {
					items.add(each);
					if (each.equals("three")) {
						BaseAdViewHelper baseAdViewHelper = new NativeExpressAdViewHelper();
						baseAdViewHelper.setAdSize(new AdSize(AdSize.FULL_WIDTH, 80));
						baseAdViewHelper.setAdUnitId(AndroidAppContext.SAMPLE_SMALL_NATIVE_AD_EXPRESS_AD_UNIT_ID);
						items.add(baseAdViewHelper);
					}
				}
				setAdapter(new RecyclerViewAdapter(recyclerViewTypes, items));
				dismissLoading();
			}
		});
	}

	public class StringRecyclerViewType extends RecyclerViewType<String, SimpleRecyclerFragment.StringViewHolder> {

		@Override
		protected Integer getLayoutResourceId() {
			return R.layout.item;
		}

		@Override
		protected Class<String> getItemClass() {
			return String.class;
		}

		@Override
		public RecyclerView.ViewHolder createViewHolderFromView(View view) {
			SimpleRecyclerFragment.StringViewHolder viewHolder = new SimpleRecyclerFragment.StringViewHolder(view);
			viewHolder.textView = findView(view, R.id.name);
			return viewHolder;
		}

		@Override
		public void fillHolderFromItem(String item, SimpleRecyclerFragment.StringViewHolder holder) {
			holder.textView.setText(item);
		}

		@Override
		public AbstractRecyclerFragment getAbstractRecyclerFragment() {
			return AdRecyclerFragment.this;
		}

		@Override
		public void onItemSelected(String item, View view) {
			getAdapter().removeItem(item);
		}
	}

	public class MyAdViewType extends AdViewType {

		@Override
		protected Integer getLayoutResourceId() {
			return R.layout.native_ad_express;
		}

		@Override
		public AbstractRecyclerFragment getAbstractRecyclerFragment() {
			return AdRecyclerFragment.this;
		}
	}
}
