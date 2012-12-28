package jp.ne.seeken.client;

import java.lang.Thread;
import java.util.LinkedList;
import java.util.Queue;
import java.net.*;
import java.io.*;

import jp.dip.roundvalley.RequestSerializer;
import jp.dip.roundvalley.ResponseSerializer;

import android.util.Log;

public class AsynSendImgThread extends Thread {

	private Queue<RequestSerializer> requestBuffer = new LinkedList<RequestSerializer>(); //requset用のバッファ
	private final int max_request_buffer = 5;
	private Queue<ResponseSerializer> responseBuffer = new LinkedList<ResponseSerializer>(); //response用のバッファ
	private final int max_response_buffer = 5; //response bufferの最大数

	private int set_count = 1; //setImgが呼ばれた回数 
	private final int request_count = 3; //サーバーにリクエストを投げるset_countの回数

	//通信用
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private Boolean is_close = false;

	public AsynSendImgThread() {

	}

	public void setSocket(String host, int port) {
		try {
			this.socket = new Socket(host, port);
			this.out = new ObjectOutputStream(this.socket.getOutputStream());
			this.in = new ObjectInputStream(this.socket.getInputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setRequest(int widht, int height, byte[] data) {
		if (set_count < request_count) {
			set_count++;
			return;
		}
		//Log.i("SetRequest","SetRequest");
		RequestSerializer is = new RequestSerializer(widht, height, data);
		if(this.max_request_buffer < requestBuffer.size()){
			this.requestBuffer.poll();
		}
		this.requestBuffer.add(is);
		set_count = 1; 
	}

	public ResponseSerializer getResponse() {
		//Log.i("GetResponse","GetResponse");
		if(this.responseBuffer.size() == 0) return null;
		else return this.responseBuffer.poll(); 
	}

	public void close() {
		this.is_close = true;
	}
	
	private int[] rgbByteArraytorgbIntArray(byte[] bRGB) {
		int iRGB_length = bRGB.length / 3;
		int[] iRGB = new int[iRGB_length];
		int count = 0;
		for(int i = 0; i < iRGB_length; i++){
			int r = bRGB[count];
			int g = bRGB[count + 1];
			int b = bRGB[count + 2];
			iRGB[i] = (0xff000000 | r << 16 | g << 8 | b);
		    count += 3;
		}
		return iRGB;
	}

	@Override
	public void run() {
		try {
			while (!is_close) {
				if (this.requestBuffer.size() == 0) continue; // send bufferがないときは何も送らない

				Log.i("Connection Strat","Connection Start");
				long start = System.currentTimeMillis(); //時間測定用
				
				RequestSerializer request = this.requestBuffer.poll();
				

				Log.i("Request Size", "width=" + request.getWidth() + "height=" + request.getHeight() + "size=" + request.getByteData().length  + "[B]");
				
				this.out.writeObject(request); //send
				this.out.flush();
				
				Log.i("Request Time", (System.currentTimeMillis() - start) + "[ms]");
				
				start = System.currentTimeMillis(); //時間測定用
				ResponseSerializer response = (ResponseSerializer) this.in.readObject(); //response
				
				long stop = System.currentTimeMillis(); //ここまで
				
				Log.i("Response Time", (stop - start) + "[ms]");
				Log.i("Response Size", "width=" + response.getWidth() + "height=" + response.getHeight() + "size=" + response.getByteData().length + "[B]");
				
				 //nullがぞうが見つからなかった時 or 更新の必要がないとき
				if (response != null){
					if ( (max_response_buffer -1 ) < this.responseBuffer.size()) {
						this.responseBuffer.poll();
					}
					//TODO: TMP
					ResponseSerializer tmp_response = new ResponseSerializer(response.getResultMessage(), response.getWidth(), response.getHeight(), rgbByteArraytorgbIntArray(response.getByteData()));
					this.responseBuffer.add(tmp_response);
				}	
			}
			this.out.writeObject(null); //コネクションをcloseするためのメッセージ
			this.out.close();
		    this.in.close();
			this.socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
