package jp.ne.seeken.serializer;

import java.io.Serializable;

public class RequestSerializer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private byte[] data = null;
	private int width;
	private int height;
	private String color_format;
	
	/**
	 * 
	 * @param widht
	 * @param height
	 * @param data
	 * @param data_format
	 */
	public RequestSerializer(int widht,int height,byte[] data,String color_format) {
		this.width = widht;
		this.height = height;
		this.data = data;
		this.color_format = color_format;
	}
	
	public byte[] data() {
		return this.data;
	}
	
	
	public int width(){
		return this.width;
	}
	
	public int height(){
		return this.height;
	}
	
	public String color_format(){
		return this.color_format;
	}
}