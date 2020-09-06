package tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class WaitingServer {
	// 연결할 포트
	private static final int PORT = 8500;
	// 스레드 풀의 최대 스레드 개수
	private static final int THREAD_CNT = 20;
	public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);	
	public static HashMap<String, Socket> waitingMap = new HashMap<>();
	static HashMap<String, Socket> playingMap = new HashMap<>();	//유저의 닉네임와 소켓 정보를 담을 해쉬맵 생성
	public static ArrayList<String> userList = new ArrayList<>();
    static RoomManager roomManager = new RoomManager(); 
	
	public static void main(String[] args) {
				
		
		try {
			// 서버소켓 생성
			ServerSocket serverSocket = new ServerSocket(PORT);	
			
			// 소켓서버가 종료될때까지 무한루프
			while (true) {
				Socket clientSocket = serverSocket.accept();
				
				// 3. 소켓으로 부터 송수신을 위한 i/o stream 을 얻기
				InputStream is = clientSocket.getInputStream(); // 수신 -->	// read();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

				String userNickName = null;
				userNickName = br.readLine();
				System.out.println(userNickName + "님 로그인\n");
				
				waitingMap.put(userNickName, clientSocket);
				playingMap.put(userNickName, clientSocket);
				userList.add(userNickName);
				
				//로그인한 유저를 각 플레이어들한테 알려주기	
				Iterator<String> iterator = waitingMap.keySet().iterator();
				while (iterator.hasNext()) {
					String key = (String) iterator.next();
					OutputStream outputStream = waitingMap.get(key).getOutputStream(); // 송신
					PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					printWriter2.println("[CLEAR]clear");
					printWriter2.flush();
					
					for(String id : waitingMap.keySet()){
						printWriter.println("[PLAYER]" + id);
						printWriter.flush();
			        }
				}
				threadPool.execute(new WaitingChat(userNickName, clientSocket));				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//소켓 처리용 래퍼 클래스입니다.
class WaitingChat implements Runnable{

	String userNickName = null;
	Socket clientSocket = null;
	
	InputStream is = null;
	BufferedReader br = null;
	OutputStream os = null;
	PrintWriter pw = null;

	public WaitingChat(String userNickName, Socket clientSocket) {
		this.userNickName = userNickName;
		this.clientSocket = clientSocket;
		
		try {
			this.is = clientSocket.getInputStream();
			this.br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			this.os = clientSocket.getOutputStream();
			this.pw = new PrintWriter(new OutputStreamWriter(os, "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		String msgFromClient = null;

		try {	
			while (true) {

				msgFromClient = br.readLine();
				
				if(msgFromClient != null){

					if (msgFromClient.startsWith("[CHAT]")) {
						Iterator<String> iterator = WaitingServer.waitingMap.keySet().iterator();
						while (iterator.hasNext()) { // 로그인한 유저들한테 메시지 돌리기
							String key = (String) iterator.next();
							OutputStream outputStream = WaitingServer.waitingMap.get(key).getOutputStream(); // 송신
							PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							printWriter.println(msgFromClient);
							printWriter.flush();
						}
						System.out.println("" + msgFromClient);
					} else if(msgFromClient.startsWith("[WHISPER]")) {
						String whisper = msgFromClient.substring(9);
						int sub = whisper.indexOf(" ") + 1;
						String whispertoWhom = whisper.substring(1, sub-1);
						System.out.println("귓말 대상 : " + whispertoWhom);
						String whisperMsg = whisper.substring(sub);
						System.out.println("귓말 내용 : " + whisperMsg);
						
						if(WaitingServer.userList.contains(whispertoWhom)) {
							//귓말대상이 접속해 있을경우
							pw.println("[WHISPER]" + userNickName + " : " + whisperMsg);
							pw.flush();
							
							OutputStream outputStream2 = WaitingServer.playingMap.get(whispertoWhom).getOutputStream(); // 송신
							PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
							printWriter2.println("[WHISPER]" + userNickName + " : " + whisperMsg);
							printWriter2.flush();
								
						} else {						
							pw.println("[WHISPERTONULL]");
							pw.flush();
						}
					} else if(msgFromClient.startsWith("[START]")) {
						String ownerNickName = msgFromClient.substring(7);
						//게임 진행 유저목록에 넣어주고
						WaitingServer.playingMap.put(userNickName, WaitingServer.waitingMap.get(userNickName));
						WaitingServer.playingMap.put(ownerNickName, WaitingServer.waitingMap.get(ownerNickName));
						//대기실 유저목록에서는 명단 삭제
						WaitingServer.waitingMap.remove(userNickName);
						WaitingServer.waitingMap.remove(ownerNickName);
						//게임시작한 유저를 각 플레이어들한테 알려줘서 대기자리스트 다시 업뎃	
						Iterator<String> iterator = WaitingServer.waitingMap.keySet().iterator();
						while (iterator.hasNext()) {
							String key = (String) iterator.next();
							OutputStream outputStream = WaitingServer.waitingMap.get(key).getOutputStream(); // 송신
							PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
							printWriter2.println("[CLEAR]clear");
							printWriter2.flush();
							
							for(String id : WaitingServer.waitingMap.keySet()){
								printWriter.println("[PLAYER]" + id);
								printWriter.flush();
					        }
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}
	
	public void close() {
		try {
			// 4. 소켓 닫기 --> 연결 끊기
			is.close();
			br.close();
			os.close();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
