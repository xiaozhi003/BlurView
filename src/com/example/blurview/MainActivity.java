package com.example.blurview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.enrique.stackblur.StackBlurManager;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity {

	private static final String TAG = "MainActivity";

	private ImageView mImageView;
	private TextView mTextView;
	private View mBlurView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initViews();
	}

	private void initViews() {
		mBlurView = findViewById(R.id.blurView);
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
						new BlurTask().execute(bmp);
						return true;
					}
				});
		
		// 图片缩放动画
		ObjectAnimator imageAnimator = ObjectAnimator.ofFloat(mImageView,
				"scaleX", 1.0f, 1.1f).setDuration(2000);
		ObjectAnimator imageAnimator1 = ObjectAnimator.ofFloat(mImageView,
				"scaleY", 1.0f, 1.1f).setDuration(2000);
		imageAnimator.start();
		imageAnimator1.start();
		imageAnimator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animation) {
				textAnimation();
			}
		});
		// 由于图片放大相应的模糊的View也需要同样放大
		mBlurView.setScaleX(1.1f);
		mBlurView.setScaleY(1.1f);
	}

	/**
	 * 模糊动画和字体动画效果
	 */
	private void textAnimation() {
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
		textAnimatorSet.setDuration(2000);
		textAnimatorSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				Intent intent = new Intent(MainActivity.this, BlurActivity.class);
				startActivity(intent);
				MainActivity.this.finish();
			}
		});
		Log.i(TAG, "AnimatorSet=" + textAnimatorSet);
		textAnimatorSet.start();
	}

	/**
	 * 模糊图像
	 */
	@SuppressLint("NewApi")
	private Bitmap blur(Bitmap bkg, View view) {

		float radius = 2;
		float scaleFactor = 8;

		// 创建需要模糊的Bitmap
		Bitmap overlay = Bitmap.createBitmap(
				(int) (view.getMeasuredWidth() / scaleFactor),
				(int) (view.getMeasuredHeight() / scaleFactor),
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(overlay);
		canvas.translate(-view.getLeft() / scaleFactor, -view.getTop()
				/ scaleFactor);
		canvas.scale(1 / scaleFactor, 1 / scaleFactor);
		Paint paint = new Paint();
		paint.setFlags(Paint.FILTER_BITMAP_FLAG);
		canvas.drawBitmap(bkg, 0, 0, paint);

		// 模糊Bitmap
		StackBlurManager stackBlurManager = new StackBlurManager(overlay);
		stackBlurManager.process((int) radius);
		overlay = stackBlurManager.returnBlurredImage();

		// RenderScript rs = RenderScript.create(MainActivity.this);
		//
		// Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
		// ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs,
		// overlayAlloc.getElement());
		// blur.setInput(overlayAlloc);
		// blur.setRadius(radius);
		// blur.forEach(overlayAlloc);
		// overlayAlloc.copyTo(overlay);
		// rs.destroy();
		return overlay;
	}

	/**
	 * 模糊异步任务
	 */
	private class BlurTask extends AsyncTask<Bitmap, Void, Bitmap> {

		@Override
		protected Bitmap doInBackground(Bitmap... params) {
			return blur(params[0], mBlurView);
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			super.onPostExecute(result);
			mBlurView.setBackground(new BitmapDrawable(getResources(), result));
		}
	}
}
