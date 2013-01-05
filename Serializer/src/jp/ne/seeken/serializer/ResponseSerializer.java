package jp.ne.seeken.serializer;

import java.io.Serializable;

public class ResponseSerializer implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String xml = null;
	private byte[][] images = new byte[5][];
	private int[] id_maps = new int[5];
	private int[] widths = new int[5];
	private int[] heights = new int[5];
	
	public ResponseSerializer(String xml,int[] id_maps,int[] widths,int[] heights,byte[][] images){
		this.xml = xml;
		this.id_maps = id_maps;
		this.images = images;
		this.widths = widths;
		this.heights = heights;
	}
	
	public String getXML(){
		return xml;
	}
	
	public byte[] getImage(int id){
		return images[id];
	}
	
	public int[] getIdMaps(){
		return id_maps;
	}
	
	public int getId(int id){
		return id_maps[id];
	}
	
	public int getHeight(int id){
		return heights[id];
	}
	
	public int getWidth(int id){
		return widths[id];
	}
}
