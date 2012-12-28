package jp.ne.seeken.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


import net.arnx.jsonic.JSON;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.util.Log;



public class YoutubeDownloader {

	private String link = null;
	/**
	 * @param args
	 */
	public YoutubeDownloader(){
		
	}
	
	/**
	 * 直接Youtubeにアクセスしてから、解析を行う(じゃないとダメらしい!!...めんど)
	 * @param url
	 * @param encode
	 */
	public void getLink(final String url,final String encode) {
    	String html = null;
    	//ナマのhtml取得
    	try{
    		URI uri = new URI(url);
    		HttpGet request = new HttpGet(uri);
    		DefaultHttpClient httpClient = new DefaultHttpClient();
    		html = httpClient.execute(request,new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					 switch (response.getStatusLine().getStatusCode()) {
			          case HttpStatus.SC_OK:
			            // レスポンスデータをエンコード済みの文字列として取得する
			            return EntityUtils.toString(response.getEntity(), encode);
			          case HttpStatus.SC_NOT_FOUND:
			            return null;
			          default:
			            return null;
			          }
				}
    			
			});
    	}
    	catch(Exception e){
 
    	}
    	//ダウンロードリンクの取得
    	try{
    		URI uri = new URI("http://roundvalley.dip.jp/youtube/");
    		HttpPost request = new HttpPost(uri);
    		DefaultHttpClient httpClient = new DefaultHttpClient();
    		List<NameValuePair> post_params = new ArrayList<NameValuePair>();
    		post_params.add(new BasicNameValuePair("html", html));
    		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post_params, encode);
    		request.setEntity(entity);
    	   		
    		link = httpClient.execute(request,new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response)
						throws ClientProtocolException, IOException {
					 switch (response.getStatusLine().getStatusCode()) {
			          case HttpStatus.SC_OK:
			            // レスポンスデータをエンコード済みの文字列として取得する
			        	  String s =  EntityUtils.toString(response.getEntity(), encode);
			        	  Log.d("link",s);
			              List<LinkedHashMap> maps = (List<LinkedHashMap>)JSON.decode(s);
			              for(LinkedHashMap  map : maps){
			            	  String type = (String) map.get("type");
			            	  if(type.equals("Medium Quality - 176x144")) return (String) map.get("url");
			              }
			          case HttpStatus.SC_NOT_FOUND:
			            return null;
			          default:
			            return null;
			          }
				}
    			
			});
    	}
    	catch(Exception e){
 
    	}
	}
	
	public void execute(OutputStream out) {
		
		
		InputStream in = null;

		try {
		    URL url = new URL(link);
		    in = url.openStream();

		    byte[] buf = new byte[1024];
		    int len = 0;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }

		    out.flush();

		} catch (Exception e) {

		} finally {
		    if (out != null) {
		        try {
		            out.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }

		    if (in != null) {
		        try {
		            in.close();
		        } catch (IOException e) {
		            e.printStackTrace();
		        }
		    }
		}
	}

}
