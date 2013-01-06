package jp.ne.seeken.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.io.FileWriter;

import jp.ne.seeken.serializer.ResponseSerializer;

import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ECOLOR_FORMAT;
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
import android.os.Environment;
import android.app.Activity;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.graphics.*;
	

public class SeekenClietnActivity extends MetaioSDKViewActivity {

	private IGeometry nowLoadingImage;
	private String packageName = null;
	private String dir_path = null;
	private String traking_data_path = null;
	//ムービ用のプレーン
	private IGeometry[] moviePlanes = new IGeometry[5];
	
	private Properties prop = new Properties();
	
	private MetaioSDKCallbackHandler mCallbackHandler;
	
	//Seeken
	private AsynSendImgThread request;
	private YoutubeDownloader yd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// extract all the assets
		try {
			AssetsManager.extractAllAssets(getApplicationContext(), true);
		} catch (IOException e) {
			MetaioDebug.printStackTrace(Log.ERROR, e);
		}

		super.onCreate(savedInstanceState);
		
		//データ保存用のパス
		/*this.dir_path = Environment.getExternalStorageDirectory().getPath() + "/seeken"; // /dataなど
		File f = new File(dir_path);
		f.mkdir();
		this.dir_path += "/";*/
		
		this.packageName = this.getApplicationContext().getPackageName();
		this.dir_path = "/data/data/" + packageName + "/files/";
		
		this.traking_data_path = this.dir_path + "TrackingData.xml";
		
		//config情報
		AssetManager as = getResources().getAssets();
		InputStream is = null;
		try{
			is = as.open("config.properties");
			 prop.load(is);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		////各種スレッド
		//Seeken DB通信用スレッド
		String host = prop.get("seeken.host").toString();
		int port = Integer.parseInt(prop.get("seeken.port").toString());
		request = new AsynSendImgThread(host,port);
		request.start();
		
		//Youube Download用
		this.yd = new YoutubeDownloader(host, port + 1);
		
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

		// 画像取得スレッドのスタート
		this.mThread = new UnifeyeCallbackThread();
		this.mThread.start();

		/*
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
		}*/

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
	
	 /**
	   * rgbのバイト配列をint配列に直す
	   * @param bRGB : RGB配列(バイト)
	   * @return iRGB : RGB配列(int) * alphaは0
	   */
	  private int[] rgbByteArraytoIntArray(byte[] bRGB){
	    int iRGB_length = bRGB.length / 3;
	    int[] iRGB = new int[iRGB_length];
	    int count = 0;
	    for (int i = 0;i < iRGB_length; i++) {
	      int r = bRGB[count];
	      int g = bRGB[count + 1];
	      int b = bRGB[count + 2];
	      iRGB[i] = (0xff000000 | r << 16 | g << 8 | b);
	      count = count + 3;
	    }
	    return iRGB;
	  }
	
	/**
	 * ResponseSerializerをもとに、各種情報を保存
	 * スレッドにする必要ありかもー
	 * @param rs
	 */
	private int[] id_maps = null;
	private void savaResponse(ResponseSerializer rs){
		//trakingDataの保存
		String trackingData = rs.getXML();
		try {
			FileWriter fw = new FileWriter(traking_data_path);
			fw.write(trackingData);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//id_mapsを取得
		int[] pre_id_maps = id_maps;
		id_maps = rs.getIdMaps();
		
		//要らなくなった画像,yotube動画の消去
		if(pre_id_maps != null){
			for(int pre_id : pre_id_maps){
				boolean is_delete = true;
				for(int id : id_maps){
					if(pre_id == id) is_delete = false;
				}
				if(is_delete){
					File f = new File(this.getImagePath(pre_id));
					f.delete();
					f = new File(this.getMoviePath(pre_id));
					f.delete();
				}
			}
		}
		
		//リソースを一旦開放
		for(int i = 0; i < moviePlanes.length; i++){
			if(moviePlanes[i] != null) {
				metaioSDK.unloadGeometry(moviePlanes[i]);
			}
			moviePlanes[i] = null;
		}
		
		//新規画像保存
		for(int i =0; i < id_maps.length; i++){
			byte[] image = rs.getImage(i);
			//imageを保存
			if(image != null){
				int width = rs.getWidth(i);
				int height = rs.getHeight(i);
				Bitmap bitMap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
				int[] argb = rgbByteArraytoIntArray(image);
				bitMap.setPixels(argb, 0, width, 0, 0, width, height);
				try {
					OutputStream fOut = new FileOutputStream(this.getImagePath(id_maps[i]));
					bitMap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   
			}
		}
	}
	
	private String getImagePath(int id) {return dir_path + id + ".jpg"; }
	private String getMoviePath(int id) {return dir_path + id + ".3g2"; }
	
	@Override
	public void onSurfaceDestroyed(){
		this.deleteResoce();
		super.onSurfaceDestroyed();
	}
	
	/**
	 * 画像、動画リソースの消去
	 */
	private void deleteResoce(){
		if(id_maps != null){
		for(int id : id_maps){
				File f = new File(this.getImagePath(id));
				f.delete();
				f = new File(this.getMoviePath(id));
				f.delete();
			}
		}
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
					ResponseSerializer rs = request.getResponse();
					
					//応答がある
					if(rs != null){
						savaResponse(rs);
						//別スレッドでやっても大丈夫か？
						boolean result = metaioSDK.setTrackingConfiguration(traking_data_path);
						//TODO: セットしたタイミングからトラッキングが始まる。そのため、スレッドで回しているとトラッキングが始まらずにずっとセットになってしまう。
						try {
							//0.35秒間だけ待ってやる - 30フレームだけ(30fps)
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(!result){
							Log.d("TrackingData","Create failed");
						}
					}
					is_tracking = false;
				} else {
					if (!is_tracking) {
						request.clearBuffer();
						TrackingValuesVector trackingValues = metaioSDK.getTrackingValues();
						int tv = (int) trackingValues.size();
						for (int i = 0; i < trackingValues.size(); i++) {
							final TrackingValues v = trackingValues.get(i);
							int id = Integer.valueOf(v.getCosName());
							int cosId = v.getCoordinateSystemID();
							String moviePath = getMoviePath(id);
							File f = new File(moviePath);
							//ファイルが存在しているときは、ダウンロードしない
							if(!f.exists())	yd.execute(id, moviePath);
							moviePlanes[cosId - 1] = metaioSDK.createGeometryFromMovie(moviePath);
							moviePlanes[cosId - 1].setScale(new Vector3d(2.0f, 2.0f, 2.0f));
							moviePlanes[cosId - 1].setRotation(new Rotation(0f, 0f, 0f));
							moviePlanes[cosId - 1].setCoordinateSystemID(cosId);
							moviePlanes[cosId - 1].setVisible(true);
							moviePlanes[cosId - 1].startMovieTexture(true);
						}
					}
					is_tracking = true;
				}
			}
		}
	}

	/*
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
	}*/

	private int current_truck_id = -1;

	private class UnifeyeCallbackHandler extends IMetaioSDKCallback {
		@Override
		public void onNewCameraFrame(ImageStruct cameraFrame) {
			ECOLOR_FORMAT a = cameraFrame.getColorFormat();
			String color_format = a.toString();
			request.setRequest(cameraFrame.getWidth(), cameraFrame.getHeight(), cameraFrame.getBuffer(),color_format);
		}

		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
		}
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


	@Override
	protected void onGeometryTouched(IGeometry geometry) {
		// TODO Auto-generated method stub
		
	}

}
