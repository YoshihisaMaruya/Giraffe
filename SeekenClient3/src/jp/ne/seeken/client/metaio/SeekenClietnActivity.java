package jp.ne.seeken.client.metaio;

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

import jp.ne.seeken.client.CommunicationThread;
import jp.ne.seeken.client.R;
import jp.ne.seeken.client.YoutubeDownloader;
import jp.ne.seeken.client.R.layout;
import jp.ne.seeken.serializer.RequestSerializer;
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

	private String packageName = null;
	private String dir_path = null;
	private String traking_data_path = null;
	// ムービ用のプレーン
	private IGeometry[] moviePlanes = new IGeometry[5];

	private Properties prop = new Properties();

	private MetaioSDKCallbackHandler mCallbackHandler;

	// Seeken
	private CommunicationThread ct;
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

		// データ保存用のパス
		/*
		 * this.dir_path = Environment.getExternalStorageDirectory().getPath() +
		 * "/seeken"; // /dataなど File f = new File(dir_path); f.mkdir();
		 * this.dir_path += "/";
		 */
		this.packageName = this.getApplicationContext().getPackageName();
		this.dir_path = "/data/data/" + packageName + "/files/";

		this.traking_data_path = this.dir_path + "TrackingData.xml";

		// config情報
		AssetManager as = getResources().getAssets();
		InputStream is = null;
		try {
			is = as.open("config.properties");
			prop.load(is);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// //各種スレッド
		// Seeken DB通信用スレッド
		String host = prop.get("seeken.host").toString();
		int port = Integer.parseInt(prop.get("seeken.port").toString());
		ct = new CommunicationThread(host, port);
		ct.start();

		// Youube Download用
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
		// コールバックのセット
		UnifeyeCallbackHandler ch = new UnifeyeCallbackHandler();
		metaioSDK.registerCallback(ch);

		// 画像取得スレッドのスタート
		this.tw = new TrackingWatcher();
		this.tw.start();
	}

	@Override
	protected void onPause() {
		is_run = false;
		super.onPause();
	}

	/**
	 * rgbのバイト配列をint配列に直す
	 * 
	 * @param bRGB
	 *            : RGB配列(バイト)
	 * @return iRGB : RGB配列(int) * alphaは0
	 */
	private int[] rgbByteArraytoIntArray(byte[] bRGB) {
		int iRGB_length = bRGB.length / 3;
		int[] iRGB = new int[iRGB_length];
		int count = 0;
		for (int i = 0; i < iRGB_length; i++) {
			int r = bRGB[count];
			int g = bRGB[count + 1];
			int b = bRGB[count + 2];
			iRGB[i] = (0xff000000 | r << 16 | g << 8 | b);
			count = count + 3;
		}
		return iRGB;
	}

	/**
	 * リスポンス情報をローカルに保存
	 * TODO: I/O処理がとてつもなく遅い。高速化する部分
	 * @param rs
	 */
	private int[] id_maps = null;
	private void savaResponse(ResponseSerializer rs) {
		// トラッキングの停止
		metaioSDK.pauseTracking();
		
		// trakingDataの保存
		String trackingData = rs.getXML();
		try {
			FileWriter fw = new FileWriter(traking_data_path);
			fw.write(trackingData);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// id_mapsを取得
		int[] pre_id_maps = id_maps;
		id_maps = rs.getIdMaps();

		// いらなくなった画像,yotube動画の消去
		if (pre_id_maps != null) {
			for (int pre_id : pre_id_maps) {
				boolean is_delete = true;
				for (int id : id_maps) {
					if (pre_id == id)
						is_delete = false;
				}
				if (is_delete) {
					File f = new File(this.getImagePath(pre_id));
					f.delete();
					f = new File(this.getMoviePath(pre_id));
					f.delete();
				}
			}
		}

		// metaio geometryリソースを一旦開放
		for (int i = 0; i < moviePlanes.length; i++) {
			if (moviePlanes[i] != null) {
				metaioSDK.unloadGeometry(moviePlanes[i]);
			}
			moviePlanes[i] = null;
		}

		//新規画像保存
		for (int i = 0; i < id_maps.length; i++) {
			byte[] image = rs.getImage(i);
			//imageがローカルになければ、ローカルに保存
			if (image != null) {
				int width = rs.getWidth(i);
				int height = rs.getHeight(i);
				Bitmap bitMap = Bitmap.createBitmap(width, height,
						Bitmap.Config.ARGB_8888);
				int[] argb = rgbByteArraytoIntArray(image);
				bitMap.setPixels(argb, 0, width, 0, 0, width, height);
				try {
					OutputStream fOut = new FileOutputStream(
							this.getImagePath(id_maps[i]));
					bitMap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//トラッキングの再開
		metaioSDK.resumeTracking();
	}

	private String getImagePath(int id) {
		return dir_path + id + ".jpg";
	}

	private String getMoviePath(int id) {
		return dir_path + id + ".3g2";
	}

	@Override
	public void onSurfaceDestroyed() {
		this.deleteResoce();
		this.is_run = false;
		super.onSurfaceDestroyed();
	}

	/**
	 * 画像、動画リソースの消去
	 */
	private void deleteResoce() {
		if (id_maps != null) {
			for (int id : id_maps) {
				File f = new File(this.getImagePath(id));
				f.delete();
				f = new File(this.getMoviePath(id));
				f.delete();
			}
		}
	}

	/**
	 * トラッキングレート分、スレッドをとめる
	 */
	private void waitTrackingFrameRate() {
		// トラッキングレートの分だけ待ってみる
		try {
			float trackingFrameRate = metaioSDK.getTrackingFrameRate();
			Thread.sleep((long) metaioSDK.getTrackingFrameRate());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * トラッキングしている数を監視するスレッド。トラッキングを感知したら、直ちにリソースの変更を停止。アクションをセット。
	 * スレッドを停止させる為にはis_runフラグをfalseに
	 */
	Boolean is_run = true;
	TrackingWatcher tw;
	private class TrackingWatcher extends Thread {
		Boolean is_first_request = true;

		@Override
		public void run() {
			ResponseSerializer rs;
			while (is_run) {
				//TODO : xmlファイルをセットしていなくても動くのか？
				int gnd = metaioSDK.getNumberOfValidCoordinateSystems(); // トラッキングしている数
				// //トラッキングを開始したら、
				if (gnd != 0) {
					// //トラッキングデータをセット
					TrackingValuesVector trackingValues = metaioSDK
							.getTrackingValues();
					for (int i = 0; i < trackingValues.size(); i++) {
						final TrackingValues v = trackingValues.get(i);
						int id = Integer.valueOf(v.getCosName());
						int cosId = v.getCoordinateSystemID();
						String moviePath = getMoviePath(id);
						File f = new File(moviePath);
						// ファイルが存在しているときは、ダウンロードしない
						if (!f.exists())
							yd.execute(id, moviePath);
						moviePlanes[cosId - 1] = metaioSDK
								.createGeometryFromMovie(moviePath);
						moviePlanes[cosId - 1].setScale(new Vector3d(2.0f,
								2.0f, 2.0f));
						moviePlanes[cosId - 1].setRotation(new Rotation(0f, 0f,
								0f));
						moviePlanes[cosId - 1].setCoordinateSystemID(cosId);
						moviePlanes[cosId - 1].setVisible(true);
						moviePlanes[cosId - 1].startMovieTexture(true);
					}

					//バッファをクリア
					ct.clearBuffer();

					//フラグを元に戻す
					is_first_request = true;

					//トラッキングが終了するまで、待つ
					while (true) {
						waitTrackingFrameRate();
						gnd = metaioSDK.getNumberOfValidCoordinateSystems(); // トラッキングしている数
						if (gnd == 0)
							break;
					}
				} else {
					//一回目は応答があるまで待つ(TrackingDataがないと、トラッキングを開始してもしても意味がない)
					if (is_first_request) {
						metaioSDK.requestCameraImage();
						while ((rs = ct.pull()) == null);
						savaResponse(rs);
					} else {
						metaioSDK.requestCameraImage();
						rs = ct.pull();
						if (rs != null)
							savaResponse(rs);
					}
					
					//トラッキングファイルをセット
					boolean trakingConfig = metaioSDK
							.setTrackingConfiguration(traking_data_path);
					if (!trakingConfig) {
						Log.v("TrackingWatcher TrakingConfig", "false");
					}
					
					//次からはリスポンスがある
					is_first_request = false;
					
					//トラッキングが始まるように、フレームレート分待ってやる
					waitTrackingFrameRate();
				}
			}
		}
	}

	/**
	 * Meataio Callback
	 * 
	 * @author maruyayoshihisa
	 * 
	 */
	private class UnifeyeCallbackHandler extends IMetaioSDKCallback {
		@Override
		public void onNewCameraFrame(ImageStruct cameraFrame) {
			ECOLOR_FORMAT a = cameraFrame.getColorFormat();
			String color_format = a.toString();
			ct.push(new RequestSerializer(cameraFrame.getWidth(), cameraFrame
					.getHeight(), cameraFrame.getBuffer(), color_format));
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
