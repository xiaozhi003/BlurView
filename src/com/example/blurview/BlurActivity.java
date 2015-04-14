package com.example.blurview;

import com.enrique.stackblur.StackBlurManager;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class BlurActivity extends ActionBarActivity {
	
	private static final String TAG = "BlurActivity";
	
	private Button mJavaBtn;
	private Button mRsBtn;
	private Button mNativeBtn;
	private TextView mTimeTv;
	private TextView mBlurTv;
	private ImageView mBlurIv;
	private SeekBar mRadiusSb;
	
	private Bitmap bmp;
	float radius = 20;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blur);
		
		initViews();
	}

	private void initViews() {
		mJavaBtn = (Button) findViewById(R.id.javaBtn);
		mRsBtn = (Button) findViewById(R.id.rsBtn);
		mNativeBtn = (Button) findViewById(R.id.nativeBtn);
		mTimeTv = (TextView) findViewById(R.id.timeTv);
		mBlurTv = (TextView) findViewById(R.id.blurTv);
		mBlurIv = (ImageView) findViewById(R.id.blurIv);
		mRadiusSb = (SeekBar) findViewById(R.id.radiusSb);
		mRadiusSb.setMax(100);
		
		mJavaBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				blur(bmp, mBlurTv,v);
			}
		});
		mRsBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				blur(bmp, mBlurTv,v);
			}
		});
		mNativeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				blur(bmp, mBlurTv,v);
			}
		});
		mBlurIv.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {
				mBlurIv.getViewTreeObserver().removeOnPreDrawListener(this);
				mBlurIv.buildDrawingCache();
				bmp = mBlurIv.getDrawingCache();
				blur(bmp, mBlurTv,mBlurTv);
				return true;
			}
		});
		mRadiusSb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				radius = seekBar.getProgress();
				blur(bmp, mBlurTv, mRadiusSb);
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
			}
		});
	}
	
	private void blur(Bitmap bkg, View view,View v){
		float scaleFactor = 1;
		long start = System.currentTimeMillis();
		
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
		switch (v.getId()) {
		case R.id.javaBtn:
			stackBlurManager.process((int) radius);
			break;
		case R.id.rsBtn:
			stackBlurManager.processRenderScript(BlurActivity.this, radius);
			break;
		case R.id.nativeBtn:
			stackBlurManager.processNatively((int)radius);
			break;
		default:
			stackBlurManager.processNatively((int)radius);
			break;
		}
		overlay = stackBlurManager.returnBlurredImage();
		mBlurTv.setBackground(new BitmapDrawable(getResources(),overlay));
		mTimeTv.setText((System.currentTimeMillis() - start) + "ms");
	}

}
