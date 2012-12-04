package jp.dip.roundvalley.giraffe.android;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import jp.dip.roundvalley.ResponseSerializer;
import jp.dip.roundvalley.giraffe.android.thread.*;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback, Camera.AutoFocusCallback {
	private static final String TAG = "CameraPreview";

	private Camera mCamera;
	private SurfaceHolder mHolder;
	private int mFrameWidth;
	private int mFrameHeight;
	private Handler mHandler;
	private AsynSendImgThread sender;
	private final String host = "192.168.72.6";
	private final int port = 5003;

	public CameraPreview(Context context, Handler handler) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		mHandler = handler;
		this.sender = new AsynSendImgThread();
		this.sender.setSocket(host, port);
		this.sender.start();
		Log.i(TAG, "Instantiated new " + this.getClass());
	}

	@Override //SurfaceHolder.Callback
	public void surfaceChanged(SurfaceHolder _holder, int format, int width,
			int height) {
		Log.i(TAG, "surfaceCreated");
		if (mCamera != null) {
			Camera.Parameters params = mCamera.getParameters();
			List<Camera.Size> sizes = params.getSupportedPreviewSizes();
			mFrameWidth = width;
			mFrameHeight = height;

			// selecting optimal camera preview size
			{
				double minDiff = Double.MAX_VALUE;
				for (Camera.Size size : sizes) {
					if (Math.abs(size.height - height) < minDiff) {
						mFrameWidth = size.width;
						mFrameHeight = size.height;
						minDiff = Math.abs(size.height - height);
					}
				}
			}

			params.setPreviewSize(mFrameWidth, mFrameHeight);
			mCamera.setParameters(params);
			mCamera.startPreview();
		}
	}

	@Override //SurfaceHolder.Callback
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mCamera.setOneShotPreviewCallback(mCallback);
	}

	private String response_message = null;
	private PreviewCallback mCallback = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			//TODO: 検出が遅い!
			Message msg = new Message();
			sender.setRequest(mFrameWidth, mFrameHeight, data);
			ResponseSerializer response = sender.getResponse();
			if(response != null){
				response_message = response.getResultMessage();
				Log.i("ResponseMessage",response_message);
				int[] widths = new int[1];
				int[] heights = new int[1];
				int[][] rgbas = new int[1][];
				widths[0] = response.getWidth();
				heights[0] = response.getHeight();
				rgbas[0] = response.getIntData();
				setTrainingImages(widths,heights, rgbas,1);
			}
			long start = System.currentTimeMillis(); //時間測定用
			msg.arg1 = detectImage(mFrameWidth, mFrameHeight, data); // 学習画像を検出したら、その画像のIDを返す。検出できなければ-1を返す。
			Log.i("DetectResult", "id=" + msg.arg1);
			Log.i("DetectImageTime",(System.currentTimeMillis() -start) + "[ms]");
			msg.obj = response_message;
			mHandler.sendMessage(msg); // 検出結果をDetectImageActivityへ伝える
		}
	};
	
	//カメラを再度起動
	public void restartPreviewCallback(){
		this.requestLayout();
		this.invalidate();
		if (mCamera != null) {
			// PreviewCallbackの再セット
			mCamera.setOneShotPreviewCallback(mCallback);
			mCamera.startPreview();
		}
	}

	@Override //SurfaceHolder.Callback
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		if (mCamera != null) {
			synchronized (this) {
				mCamera.stopPreview();
				mCamera.setPreviewCallback(null);
				mCamera.release();
				mCamera = null;
			}
		}
	}
	
	//
	public native int detectImage(int width, int height, byte[] data);
	public native void setTrainingImage(int widht,int height,int[] rgbas);
	public native void setTrainingImages(int[] widths, int[] heights, int[][] rgbas, int imageNum);

	static {
		System.loadLibrary("native_sample"); //ネイティブライブラリの読み込み
	}
	

    @Override
    //タッチ時に呼び出されるメソッド
    public boolean onTouchEvent(MotionEvent event) {
        //1本の指でタッチされたときに実行
        if (event.getAction()==MotionEvent.ACTION_DOWN) {
            //オートフォーカス機能を呼び出す
        	mCamera.autoFocus(this);
        }
        //それ以外のときはtrueを返して処理をすすめる
        return true;
    }
	@Override //Camera.AutoFocusCallback
	public void onAutoFocus(boolean success, Camera camera) {
		mCamera.autoFocus(null);
	}
}