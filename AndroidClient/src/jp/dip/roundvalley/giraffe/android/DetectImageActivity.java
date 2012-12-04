
package jp.dip.roundvalley.giraffe.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class DetectImageActivity extends Activity {
	private static final String TAG = "Sample::Activity";
	private InfomationView mInfoview;
	private CameraPreview mCameraPreview;
	// private MainHandler mHandler = new MainHandler();

	public DetectImageActivity() {
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);

		FrameLayout fl = new FrameLayout(this); // ビューを重ねて表示するためのレイアウト
		fl.setLayoutParams(params);

		mCameraPreview = new CameraPreview(this, new MainHandler());
		mCameraPreview.setLayoutParams(params);
		fl.addView(mCameraPreview);

		mInfoview = new InfomationView(this);
		mInfoview.setLayoutParams(params);
		fl.addView(mInfoview);

		setContentView(fl);
	}

	private class MainHandler extends Handler {
		public void handleMessage(Message msg) {
			int is_detect = msg.arg1;
			if(is_detect != -1) mInfoview.isDetect(true);
			else mInfoview.isDetect(false);
			mInfoview.setExplanation((String)msg.obj);      //検出した画像IDをセット
			mInfoview.invalidate();                  //InfomationViewの更新
			mCameraPreview.restartPreviewCallback(); // Previewのリスタート
		}
	}

}
