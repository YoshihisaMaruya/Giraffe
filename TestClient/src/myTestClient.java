import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import jp.dip.roundvalley.RequestSerializer;
import jp.dip.roundvalley.ResponseSerializer;




public class myTestClient {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws Exc 
	 */
	public static void main(String[] args) throws Exception {
		int port = 5003;
		String host = "192.168.72.6";
		
		
		Socket socket = new Socket(host,port);
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		
		//Test Request
		int request_data_array_size = 460800; //B
		byte[] request_data = new byte[request_data_array_size];
		for(int i = 0; i < request_data_array_size; i++){
			request_data[i] = 0x00; 
		}
		//while(true){
		long start = System.currentTimeMillis(); //ŽžŠÔ‘ª’è—p
		RequestSerializer request = new RequestSerializer(640, 480, request_data);
		out.writeObject(request); //send
		out.flush();
		System.out.println("Request Time = " + (System.currentTimeMillis() - start) + "[ms]");
		//END
		
		//Test Response
		start = System.currentTimeMillis(); //ŽžŠÔ‘ª’è—p
		ResponseSerializer response = (ResponseSerializer) in.readObject(); //response
		System.out.println("Response Time = " + (System.currentTimeMillis() - start) + "[ms]");
		System.out.println("Response Data Size = " + response.getIntData().length);
		//}
		//End
	}

}
