package jp.ne.seeken.client;

import java.io.IOException;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.ImageStruct;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;

public class SeekenClietnActivity extends MetaioSDKViewActivity {

	private IGeometry mImagePlane;
	private IGeometry mMoviePlane;

	private MetaioSDKCallbackHandler mCallbackHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		// extract all the assets
		try 
		{
			AssetsManager.extractAllAssets(getApplicationContext(), true);
		} 
		catch (IOException e) 
		{
			MetaioDebug.printStackTrace(Log.ERROR, e);
		}
		
		super.onCreate(savedInstanceState);
		mCallbackHandler = new MetaioSDKCallbackHandler();
	}
	
	@Override
	protected int getGUILayout() 
	{
		// TODO: return 0 in case of no GUI overlay
		return R.layout.activity_seeken_clietn;
	}



	@Override
	protected void onStart() 
	{
		super.onStart();
		
		// hide GUI until SDK is ready
		if (!mRendererInitialized)
			mGUIView.setVisibility(View.GONE);
	}
	
	@Override
	protected void loadContent() 
	{
		//コールバックのセット
		metaioSDK.registerCallback(new UnifeyeCallbackHandler());
		
		metaioSDK.requestCameraImage();
		//画像取得スレッドのスタート
		this.mThread = new UnifeyeCallbackThread();
		this.mThread.start();
		
		try
		{
			
			// Load desired tracking data for planar marker tracking
			final String trackingConfigFile = AssetsManager.getAssetPath("Assets2/TrackingData_MarkerlessFast.xml");
			boolean result = metaioSDK.setTrackingConfiguration(trackingConfigFile);
			MetaioDebug.log("Tracking data loaded: " + result);
			
			
			// Loading image geometry
			String imagePath = AssetsManager.getAssetPath("Assets2/frame.png");
			if (imagePath != null)
			{
				mImagePlane = metaioSDK.createGeometryFromImage(imagePath);
				if (mImagePlane != null)
				{
					mImagePlane.setScale(new Vector3d(3.0f,3.0f,3.0f));
					mImagePlane.setCoordinateSystemID(1);
					mImagePlane.setVisible(false);
					MetaioDebug.log("Loaded geometry "+imagePath);
				}
				else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+imagePath);
					finish();
				}
			}
			
			// Loading movie geometry
			String moviePath = AssetsManager.getAssetPath("Assets2/demo_movie.3g2");
			if (moviePath != null)
			{
				mMoviePlane = metaioSDK.createGeometryFromMovie(moviePath, true);
				if (mMoviePlane != null)
				{
					mMoviePlane.setScale(new Vector3d(2.0f,2.0f,2.0f));
					mMoviePlane.setRotation(new Rotation(0f, 0f, (float)-Math.PI/2));
					mImagePlane.setCoordinateSystemID(2);
					mMoviePlane.setVisible(false);
					MetaioDebug.log("Loaded geometry "+moviePath);
				}
				else {
					MetaioDebug.log(Log.ERROR, "Error loading geometry: "+moviePath);
					finish();
				}
			}
			
			mImagePlane.setVisible(true);
			mMoviePlane.setVisible(true);
			mMoviePlane.startMovieTexture(true); // loop = true;
			
			// loading environment maps
			String path = AssetsManager.getAssetPath("Assets2/truck/env_map");
			if (path != null)
			{
				boolean loaded = metaioSDK.loadEnvironmentMap(path);
				MetaioDebug.log("environment mapts loaded: " + loaded);
			}
			else
			{
				MetaioDebug.log(Log.ERROR, "Error loading environment maps at: " + path);
				finish();
			}
			
	
			
		}       
		catch (Exception e)
		{
			
		}
	}
	
	@Override
	protected void onPause(){
		is_run = false;
		super.onPause();
	}
	
	Thread mThread;
	Boolean is_run = true;
	private class UnifeyeCallbackThread extends Thread {
		@Override
		public void run() {
			Boolean is_request = false;

			while (is_run) {
				int gnd = metaioSDK.getNumberOfValidCoordinateSystems(); //トラッキングしている数
				int gnv = metaioSDK.getNumberOfDefinedCoordinateSystems(); //登録されているテンプレートの数
				////トラッキングできていないときは、サーバーに問い合わせてテンプレートの取得
				if (gnd == 0) {
					metaioSDK.requestCameraImage();
				}
			}
		}
	}
	
	private class UnifeyeCallbackHandler extends IMetaioSDKCallback {
		@Override
		public void onNewCameraFrame(ImageStruct cameraFrame) {
			byte[] rgb = cameraFrame.getBuffer();
			int a = 0;
			a = a + 1;
		}
	}
	
  
	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub
		
	}



	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() 
	{
		return mCallbackHandler;
	}
	
	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback 
	{

		@Override
		public void onSDKReady() 
		{
			// show GUI
			runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}
	}
	
}
