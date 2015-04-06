package com.example.blurview;

import com.enrique.stackblur.StackBlurManager;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";

	private ImageView mImageView;
	private TextView mTimeTextView;
	private TextView mTextView;
	private View mBlurView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBlurView = findViewById(R.id.blurView);
		mTimeTextView = (TextView) findViewById(R.id.timeTextView);
		mTextView = (TextView) findViewById(R.id.blurTextView);
		mImageView = (ImageView) findViewById(R.id.imageView);
		mImageView.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {

					@Override
					public boolean onPreDraw() {
						mImageView.getViewTreeObserver()
						.removeOnPreDrawListener(this);
						mImageView.buildDrawingCache();
						Bitmap bmp = mImageView.getDrawingCache();
//						 new BlurTask(bmp, mTextView).execute();
						new BlurThread(bmp, mBlurView).start();

						return true;
					}
				});
		ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(mImageView,
				"scaleX", 1.0f, 1.1f).setDuration(2000);
		ObjectAnimator imageAnimator1 = ObjectAnimator.ofFloat(mImageView,
				"scaleY", 1.0f, 1.1f).setDuration(2000);
		imageAnimator.start();
		imageAnimator1.start();
		imageAnimator.addListener(new AnimatorListenerAdapter() {
			
			@Override
			public void onAnimationEnd(Animator animation) {
//				mImageView.setImageBitmap(null);
				enterAnimation();
			}
			
		});

		mBlurView.setScaleX(1.1f);
		mBlurView.setScaleY(1.1f);
	}

	private void enterAnimation() {
		ObjectAnimator textAnimator1 = ObjectAnimator.ofFloat(mTextView,
				"alpha", 0f, 1f);
		ObjectAnimator textAnimator2 = ObjectAnimator.ofFloat(mTextView,
				"scaleX", 1.2f, 1f);
		ObjectAnimator textAnimator3 = ObjectAnimator.ofFloat(mTextView,
				"scaleY", 1.2f, 1f);
		ObjectAnimator blurAnimator = ObjectAnimator.ofFloat(mBlurView,
				"alpha", 0f, 0.8f);

		AnimatorSet textAnimatorSet = new AnimatorSet();
		textAnimatorSet.playTogether(textAnimator1, textAnimator2,
				textAnimator3, blurAnimator);
		textAnimatorSet.setDuration(1500);
		textAnimatorSet.start();
	}

	@SuppressLint("NewApi")
	private Bitmap blur(Bitmap bkg, View view) {

		float radius = 2;
		float scaleFactor = 8;

		Bitmap overlay = Bitmap.createBitmap(
				(int) (view.getMeasuredWidth() / scaleFactor),
				(int) (view.getMeasuredHeight() / scaleFactor),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlay);
		Log.d(TAG, "left:" + view.getLeft() + ";top:" + view.getTop());
		canvas.translate(-view.getLeft() / scaleFactor, -view.getTop()
				/ scaleFactor);
		canvas.scale(1 / scaleFactor, 1 / scaleFactor);
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(bkg, 0, 0, paint);
		
		StackBlurManager stackBlurManager = new StackBlurManager(overlay);
		stackBlurManager.process((int)radius);
//		stackBlurManager.processNatively((int)radius);
//		stackBlurManager.processRenderScript(MainActivity.this, radius);
		overlay = stackBlurManager.returnBlurredImage();
		
//		RenderScript rs = RenderScript.create(MainActivity.this);
//
//		Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
//		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs,
//				overlayAlloc.getElement());
//		blur.setInput(overlayAlloc);
//		blur.setRadius(radius);
//		blur.forEach(overlayAlloc);
//		overlayAlloc.copyTo(overlay);
//		rs.destroy();
		return overlay;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class BlurThread extends Thread {

		private View view;
		private Bitmap bkg;

		long startMs;

		public BlurThread(Bitmap bkg, View view) {
			this.bkg = bkg;
			this.view = view;
		}

		@Override
		public void run() {
			super.run();
			startMs = System.currentTimeMillis();
			final Bitmap result = blur(bkg, view);
			view.post(new Runnable() {

				@Override
				public void run() {
					view.setBackground(new BitmapDrawable(getResources(),
							result));
				}
			});
			mTimeTextView.post(new Runnable() {

				@Override
				public void run() {
					mTimeTextView.setText("cost "
							+ (System.currentTimeMillis() - startMs) + "ms");
				}
			});
		}
	}

	private class BlurTask extends AsyncTask<Void, Void, Bitmap> {

		private View view;
		private Bitmap bkg;

		long startMs;

		public BlurTask(Bitmap bkg, View view) {
			this.bkg = bkg;
			this.view = view;
		}

		@Override
		protected Bitmap doInBackground(Void... params) {
			startMs = System.currentTimeMillis();
			return blur(bkg, view);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			view.setBackground(new BitmapDrawable(getResources(), result));
			mTimeTextView.setText("cost "
					+ (System.currentTimeMillis() - startMs) + "ms");
		}
	}
}
