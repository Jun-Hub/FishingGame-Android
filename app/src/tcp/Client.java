package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Scanner;
import org.json.simple.JSONObject;
import com.google.gson.Gson;

public class Client {

	private GameRoom room; 
	private Socket socket;
	private String nickName = ""; // 유저의 닉네임

	
	public Client() { // 아무런 정보가 없는 깡통 유저를 만들 때

	}

	public Client(Socket socket) { // 닉네임 정보만 가지고 생성(방이 부여안되고, 서치중일 때)
		this.socket = socket;
	}

	public Client(Socket socket, GameRoom room) { // 닉네임 정보만 가지고 생성(방바로 부여..)
		this.socket = socket;
		this.room = room;
	}

	public void runningClient() {
		// 테이타를 송수신하기 위한 소켓을 준비

		Socket socket = null; // 1. 소켓객체 생성
		
		try {		
			socket = new Socket("localhost", 9000); // IP,Port 

			// 2. 데이타 송수신을 위한 i/o stream을 얻어야 한다.
			InputStream is = socket.getInputStream(); // 수신 --> read();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			OutputStream os = socket.getOutputStream(); // 송신 --> write();
			PrintWriter pw = new PrintWriter(os);
			
			BufferedReader stringReader = new BufferedReader(new InputStreamReader(System.in));
			
			String json = "";
			Gson gson = new Gson();	//Client 객체를 전달하기 위한 Gson 생성
			
			json = gson.toJson(this);	//자기 자신을 Json데이터로 변경
			
			pw.println(json);	//서버로 json전달
			pw.println(this.nickName);	//서버로 자기 닉네임 전달
			pw.flush();
			
			ResponseThread responseThread = new ResponseThread(br);
			responseThread.start();		//서버로부터 응답받아서 출력하는 쓰레드

		} catch (UnknownHostException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			 try {
			 // 4. 소켓 닫기 --> 연결 끊기
				 if(socket!=null)
					 socket.close();
				 if(scanner!=null)
					 scanner.close();
			 }catch(IOException e) {e.printStackTrace();}
		}
	}
	
	public static void main(String[] args) {

		Client client = new Client();
		client.runningClient();
		
	}
	
	// Thread 클래스를 상속받은 클래스를 생성
	class ResponseThread extends Thread{
		
		BufferedReader bufferedReader;
		String response = null;		//서버로부터 받을 응답
		
		public ResponseThread(BufferedReader bufferedReader) {
			this.bufferedReader = bufferedReader;
		}

		public void run(){
			
			while (true) {
				try {
					response = bufferedReader.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				if(response.equals("GAME OVER\n")) {
					break;
				}
			}
			this.interrupt();
		}
	}

	public void EnterRoom(GameRoom _room) {
		this.room = _room; // 유저가 속한 방을 룸으로 변경한다.(중요)
	}
}
