package jp.ne.seeken.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValues;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;

public class SeekenClietnActivity extends MetaioSDKViewActivity {

	private IGeometry nowLoadingImage;
	private IGeometry mMoviePlane;
	private IGeometry dMP1;
	private IGeometry dMP2;
	private String packageName = null;
	private String dir_path = null;

	private MetaioSDKCallbackHandler mCallbackHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// extract all the assets
		try {
			AssetsManager.extractAllAssets(getApplicationContext(), true);
		} catch (IOException e) {
			MetaioDebug.printStackTrace(Log.ERROR, e);
		}

		super.onCreate(savedInstanceState);
		this.packageName = this.getApplicationContext().getPackageName();
		this.dir_path = "/data/data/" + packageName + "/files/";
		mCallbackHandler = new MetaioSDKCallbackHandler();
	}

	@Override
	protected int getGUILayout() {
		// TODO: return 0 in case of no GUI overlay
		return R.layout.activity_seeken_clietn;
	}

	@Override
	protected void onStart() {
		super.onStart();

		// hide GUI until SDK is ready
		if (!mRendererInitialized)
			mGUIView.setVisibility(View.GONE);
	}

	@Override
	protected void loadContent() {
		final String trackingConfigFile = AssetsManager
				.getAssetPath("Assets2/TrackingData_MarkerlessFast.xml");
		boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);

		// コールバックのセット
		UnifeyeCallbackHandler ch = new UnifeyeCallbackHandler();
		metaioSDK.registerCallback(ch);

		metaioSDK.requestCameraImage();
		// 画像取得スレッドのスタート
		this.mThread = new UnifeyeCallbackThread();
		this.mThread.start();

		//ローディング画像の読み込み
		String nowLoadingImage_path = AssetsManager.getAssetPath("Assets2/now_loading.png");
		if (nowLoadingImage != null) {
			nowLoadingImage = metaioSDK
					.createGeometryFromImage(nowLoadingImage_path);
			if (nowLoadingImage != null) {
				nowLoadingImage.setScale(new Vector3d(3.0f, 3.0f, 3.0f));
				nowLoadingImage.setVisible(false);
				MetaioDebug.log("Loaded geometry " + nowLoadingImage);
			} else {
				MetaioDebug.log(Log.ERROR, "Error loading geometry: "
						+ nowLoadingImage);
				finish();
			}
		}

		/*
		 * try {
		 * 
		 * // Load desired tracking data for planar marker tracking final String
		 * trackingConfigFile =
		 * AssetsManager.getAssetPath("Assets2/TrackingData_MarkerlessFast.xml"
		 * ); boolean result =
		 * metaioSDK.setTrackingConfiguration(trackingConfigFile);
		 * MetaioDebug.log("Tracking data loaded: " + result);
		 * 
		 * 
		 * // Loading image geometry String imagePath =
		 * AssetsManager.getAssetPath("Assets2/frame.png"); if (imagePath !=
		 * null) { mImagePlane = metaioSDK.createGeometryFromImage(imagePath);
		 * if (mImagePlane != null) { mImagePlane.setScale(new
		 * Vector3d(3.0f,3.0f,3.0f)); mImagePlane.setCoordinateSystemID(1);
		 * mImagePlane.setVisible(false);
		 * MetaioDebug.log("Loaded geometry "+imagePath); } else {
		 * MetaioDebug.log(Log.ERROR, "Error loading geometry: "+imagePath);
		 * finish(); } }
		 * 
		 * // Loading movie geometry String moviePath =
		 * AssetsManager.getAssetPath("Assets2/a.3g2"); if (moviePath != null) {
		 * mMoviePlane = metaioSDK.createGeometryFromMovie(moviePath, false); if
		 * (mMoviePlane != null) { mMoviePlane.setScale(new
		 * Vector3d(2.0f,2.0f,2.0f)); mMoviePlane.setRotation(new Rotation(0f,
		 * 0f, 0f)); mMoviePlane.setCoordinateSystemID(2);
		 * mMoviePlane.setVisible(false);
		 * MetaioDebug.log("Loaded geometry "+moviePath); } else {
		 * MetaioDebug.log(Log.ERROR, "Error loading geometry: "+moviePath);
		 * finish(); } }
		 * 
		 * mImagePlane.setVisible(true); mMoviePlane.setVisible(true);
		 * mMoviePlane.startMovieTexture(true); // loop = true;
		 * 
		 * // loading environment maps String path =
		 * AssetsManager.getAssetPath("Assets2/truck/env_map"); if (path !=
		 * null) { boolean loaded = metaioSDK.loadEnvironmentMap(path);
		 * MetaioDebug.log("environment mapts loaded: " + loaded); } else {
		 * MetaioDebug.log(Log.ERROR, "Error loading environment maps at: " +
		 * path); finish(); } } catch (Exception e) {
		 * 
		 * }
		 */
	}

	@Override
	protected void onPause() {
		is_run = false;
		super.onPause();
	}

	Thread mThread;
	Boolean is_run = true;

	private class UnifeyeCallbackThread extends Thread {
		@Override
		public void run() {
			Boolean is_request = false;
			Boolean is_tracking = false;

			while (is_run) {
				int gnd = metaioSDK.getNumberOfValidCoordinateSystems(); // トラッキングしている数
				int gnv = metaioSDK.getNumberOfDefinedCoordinateSystems(); // 登録されているテンプレートの数
				// //トラッキングできていないときは、サーバーに問い合わせてテンプレートの取得
				if (gnd == 0) {
					metaioSDK.requestCameraImage();
					is_tracking = false;
				} else {
					if (!is_tracking) {
						metaioSDK.getTrackingValues();
					}
					is_tracking = true;
				}
			}
		}
	}

	private class DownloadThread extends Thread {
		private IGeometry dMP;
		private int id;
		private int coordinateSystemID;
		private String url;

		public DownloadThread(IGeometry dMP, int id, int coordinateSystemID,
				String url) {
			this.dMP = dMP;
			this.id = id;
			this.coordinateSystemID = coordinateSystemID;
			this.url = url;
		}

		@Override
		public void run() {
			// String moviePath =
			// AssetsManager.getAssetPath("Assets2/demo_movie.3g2");
			String moviePath = "/data/data/" + packageName + "/files/" + id
					+ ".3g2";
			File file = new File(moviePath);

			// ファイルが存在していない(ダウンロードが完了してない)
			if (!file.exists()) {
				YoutubeDownloader yd = new YoutubeDownloader();
				FileOutputStream out = null;

				try {
					out = new FileOutputStream(file);
					yd.getLink(this.url, "UTF-8");
					yd.execute(out);
					dMP = metaioSDK.createGeometryFromMovie(moviePath, false);
					dMP.setScale(new Vector3d(2.0f, 2.0f, 2.0f));
					dMP.setRotation(new Rotation(0f, 0f, 0f));
					dMP.setCoordinateSystemID(coordinateSystemID);
					dMP.setVisible(true);
					dMP.startAnimation();
					// out = openFileOutput(dir_Path, MODE_PRIVATE);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	private int current_truck_id = -1;

	private class UnifeyeCallbackHandler extends IMetaioSDKCallback {
		@Override
		public void onNewCameraFrame(ImageStruct cameraFrame) {
			byte[] rgb = cameraFrame.getBuffer();
			int a = 0;
			a = a + 1;
		}

		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
			int tv = (int) trackingValues.size();
			for (int i = 0; i < trackingValues.size(); i++) {
				final TrackingValues v = trackingValues.get(i);
				int id = Integer.valueOf(v.getCosName());
				int cosId = v.getCoordinateSystemID();
				nowLoadingImage.setCoordinateSystemID(cosId);
				nowLoadingImage.setVisible(true);
				if (id == 1) {
				
					DownloadThread th = new DownloadThread(dMP1, id,
							v.getCoordinateSystemID(),
							"http://www.youtube.com/watch?v=iGfLqqjHh3U");
					th.start();
				} else if (id == 2) {
					DownloadThread th = new DownloadThread(dMP2, id,
							v.getCoordinateSystemID(),
							"http://www.youtube.com/watch?feature=fvwp&v=aijv8nWShek&NR=1");
					th.start();
				}
			}
		}
	}

	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub

	}

	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {

		@Override
		public void onSDKReady() {
			// show GUI
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}
	}

}
