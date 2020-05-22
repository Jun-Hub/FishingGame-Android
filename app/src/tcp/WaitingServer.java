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
	// 연결할 포트를 지정합니다.
	private static final int PORT = 8500;
	// 스레드 풀의 최대 스레드 개수를 지정합니다.
	private static final int THREAD_CNT = 20;
	public static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);	
	public static HashMap<String, Socket> waitingMap = new HashMap<>();
	static HashMap<String, Socket> playingMap = new HashMap<>();	//유저의 닉네임와 소켓 정보를 담을 해쉬맵 생성
	public static ArrayList<String> userList = new ArrayList<>();
    static RoomManager roomManager = new RoomManager(); // 클래스 시작 시 한번만 생성해야 한다.
	
	public static void main(String[] args) {
				
		System.out.println("====================[Waiting112Server]====================");        
		
		try {
			// 서버소켓 생성
			ServerSocket serverSocket = new ServerSocket(PORT);	
			
			// 소켓서버가 종료될때까지 무한루프
			while (true) {
				// 소켓 접속 요청이 올때까지 대기합니다.
				Socket clientSocket = serverSocket.accept();

				// 룸을 일단 만들어버린 후에 2명이 차면 스레드로 넘겨서 게임 진행
				
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
					System.out.println("얘한테 : "+ key); 
					OutputStream outputStream = waitingMap.get(key).getOutputStream(); // 송신
					PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
					printWriter2.println("[CLEAR]clear");
					printWriter2.flush();
					
					for(String id : waitingMap.keySet()){
						printWriter.println("[PLAYER]" + id);
						System.out.println("이걸보냄 : "+ id); 
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
							//if(WaitingServer.playingMap.containsKey(whispertoWhom)) {	//귓말 상대가 플레이 중일경우
								OutputStream outputStream2 = WaitingServer.playingMap.get(whispertoWhom).getOutputStream(); // 송신
								PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
								printWriter2.println("[WHISPER]" + userNickName + " : " + whisperMsg);
								printWriter2.flush();
							//} else if(WaitingServer.waitingMap.containsKey(whispertoWhom)) {	//귓말 상대가 대기실일 경우
							//	OutputStream outputStream2 = WaitingServer.waitingMap.get(whispertoWhom).getOutputStream();
							//	PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
							//	printWriter2.println("[WHISPER]" + userNickName + " : " + whisperMsg);
							//	printWriter2.flush();
							//}							
						} else {						
							pw.println("[WHISPERTONULL]");
							pw.flush();
						}
					} else if(msgFromClient.startsWith("[START]")) {
						String ownerNickName = msgFromClient.substring(7);
						//게임 진행 유저목록에 넣어주고
						//WaitingServer.playingMap.put(userNickName, WaitingServer.waitingMap.get(userNickName));
						//WaitingServer.playingMap.put(ownerNickName, WaitingServer.waitingMap.get(ownerNickName));
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
					
					
					
					
					/*else if(msgFromClient.startsWith("[SEARCH]")) {
						
						String userNickName = msgFromClient.substring(8);
						
						System.out.println(userNickName + "님 접속");
						System.out.println("");
						
						WaitingServer.playingMap.put(userNickName, clientSocket);		//해쉬맵에 유저 닉네임과 소켓정보 담기
						
						//유저가 들어갈 방이 있나 새로 생성해야하나 검색하는 과정		
						for(int i=0; i<WaitingServer.roomManager.roomList.size(); i++) {	//roomList를 돌며 
							
							int j =0;	//roomList를 다 돌았는지 확인용 변수
												
							if(WaitingServer.roomManager.roomList.get(i).full == false) {	//대기방이 있을경우
								
								GameRoom waitingRoom = WaitingServer.roomManager.roomList.get(i);	//대기방
								
								waitingRoom.AddUser(userNickName);	//대기방에 유저 추가(Room의 유저리스트에 추가)
																		
								waitingRoom.full = true;	//2명의 유저로 꽉 찻다는 걸 알려줌
								
								System.out.println("*system : " + userNickName + "님 대기방에 입장 후 게임 시작");
								System.out.println("*system : " + waitingRoom.GetOwner() + "님 새로운 유저 입장하여 게임 시작");
								
								Socket ownerSocket = WaitingServer.playingMap.get(waitingRoom.GetOwner());	//대기방에 있던 방 주인 소켓						
								OutputStream ownerOutput = ownerSocket.getOutputStream();
								PrintWriter ownerPw = new PrintWriter(ownerOutput);
														
								pw.println("[START]");
								ownerPw.println("[START]");
								pw.flush();
								ownerPw.flush();
								
								WaitingServer.waitingMap.remove(userNickName);
								WaitingServer.waitingMap.remove(waitingRoom.GetOwner());
								//게임시작한 유저를 각 플레이어들한테 알려줘서 대기자리스트 다시 업뎃	
								Iterator<String> iterator = WaitingServer.waitingMap.keySet().iterator();
								while (iterator.hasNext()) {
									String key = (String) iterator.next();
									System.out.println("얘한테 : "+ key); 
									OutputStream outputStream = WaitingServer.waitingMap.get(key).getOutputStream(); // 송신
									PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
									PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"));
									printWriter2.println("[CLEAR]clear");
									printWriter2.flush();
									
									for(String id : WaitingServer.waitingMap.keySet()){
										printWriter.println("[PLAYER]" + id);
										System.out.println("이걸보냄 : "+ id); 
										printWriter.flush();
							        }
								}
								
								try{	//방에있는 두 유저 모두 스레드 시작함으로써 겜 시작
									// 요청이 오면 스레드 풀의 스레드로 소켓을 넣어줍니다.
									// 이후는 스레드 내에서 처리합니다.
									
									WaitingServer.threadPool.execute(new PlayingLogic(ownerSocket, waitingRoom, 0));
									WaitingServer.threadPool.execute(new PlayingLogic(clientSocket, waitingRoom, 1));
									PlayingLogic playingLogic = new PlayingLogic(ownerSocket, waitingRoom, 0);
									playingLogic.start();									
									PlayingLogic playingLogic2 = new PlayingLogic(clientSocket, waitingRoom, 1);
									playingLogic2.start();
																		
								}catch(Exception e){
									e.printStackTrace();
								}
								
								break;
							}
							
							j++;
												
							if(j == WaitingServer.roomManager.roomList.size()) {	//roomList를 다 돌았는데도 대기방이 나오지 않았다면, 새로운 방 생성
								GameRoom room = new GameRoom(false, userNickName);	//유저가 방을 만들어서, 동시에 방의 유저리스트에 방장으로 추가됨
								
								WaitingServer.roomManager.CreateRoom(room);	//룸매니저의 해쉬맵에 룸 추가
								
								System.out.println("*system : " + userNickName + "님 대기중...");
								pw.println("*system : 다른 유저를 기다리는 중....");
								pw.flush();
															
								
								break;
							}
						}	
						
						if(WaitingServer.roomManager.roomList.size() == 0) {	//만들어진 방이 하나도 없다면
							GameRoom room = new GameRoom(false, userNickName);	//유저가 방을 만들어서, 동시에 방의 유저리스트에 방장으로 추가됨
							
							WaitingServer.roomManager.CreateRoom(room);	//룸매니저의 리스트에 룸 추가
							
							System.out.println("*system : " + userNickName + "님 대기중...\n");
						}
						
					}*/
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
			System.out.println("클라 소켓 close 완료");
		} catch (IOException e) {
			System.out.println("close에러");
			e.printStackTrace();
		}
	}
}

/*class PlayingLogic implements Runnable {
	
	private Socket clientSocket = null;
	private GameRoom playingRoom = null;
	private String clientNickName = null;
	private String enemyNickName = null;
	private int playerNum;
	
	InputStream myIs = null;
	BufferedReader myBr = null;
	OutputStream enemyOs, myOs = null;
	PrintWriter enemyPw, myPw = null;
	
	public PlayingLogic(Socket clientSocket, GameRoom playingRoom, int playerNum) {

		this.clientSocket = clientSocket;
		this.playingRoom = playingRoom;
		this.playerNum = playerNum;

		try {
			// 3. 소켓으로 부터 송수신을 위한 i/o stream 을 얻기
			
			int enemyNum=6969;

			if (playerNum == 0) {
				enemyNum = 1;
			} else if (playerNum == 1) {
				enemyNum = 0;
			}

			clientNickName = playingRoom.userList.get(playerNum);
			enemyNickName = playingRoom.userList.get(enemyNum);

			Socket enemySocket = WaitingServer.playingMap.get(enemyNickName);

			enemyOs = enemySocket.getOutputStream();
			enemyPw = new PrintWriter(new OutputStreamWriter(enemyOs, "UTF-8"));

			myIs = clientSocket.getInputStream(); // 수신 --> read();
			myBr = new BufferedReader(new InputStreamReader(myIs, "UTF-8"));
			myOs = clientSocket.getOutputStream(); // 송신 --> write();
			myPw = new PrintWriter(myOs);

		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {

		String msgFromClient = null;

		// 클라이언트가 획득한 점수
		int myFish = 0;

		try {
			
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
			if (playerNum == 0) {
				myPw.println("[SPEAR]1");
				myPw.flush();
			} else if (playerNum == 1) {
				myPw.println("[SPEAR]2");
				myPw.flush();
			}
			
			FishThread fishThread = new FishThread();
			fishThread.start();

			while (true) { // 게임로직

				msgFromClient = myBr.readLine();

				if (msgFromClient.startsWith("[SPEAR_X]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
				}
				if (msgFromClient.startsWith("[SPEAR_Y]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
				}
				
				if (msgFromClient.startsWith("[COLLISION]")) {
					System.out.println("클라로부터 [COLLISION]받았다...." + msgFromClient + "\n");
					enemyPw.println(msgFromClient);
					enemyPw.flush();
					System.out.println("상대한테 [COLLISION]보냈다...." + msgFromClient + "\n");

					String whatFish = msgFromClient.substring(11);
					int whatFishNum = Integer.parseInt(whatFish);

					if (whatFishNum == 6) {
						if (myFish > 0)
							myFish -= 1;
					} else if (whatFishNum == 8) {
						if (myFish > 0)
							myFish -= 1;
					} else if (whatFishNum == 9) {
						if (myFish > 0)
							myFish -= 1;
					} else {
						myFish += 1;
					}
				} else if (msgFromClient.startsWith("[GAMEOVER]")) {
					System.out.println("=========== G A M E  O V E R ===========");
					// 상대방한테 내가 얻은 점수 보내주기
					enemyPw.println("[GAMEOVER]" + myFish);
					enemyPw.flush();
					
					try {	//패킷이 오가는 시간을 벌기 위한 close직전 대기시간
						Thread.sleep(3500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
					break;
				}
				
				if(msgFromClient.startsWith("[CHAT]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
					System.out.println("" + msgFromClient);				
				} 
				if(msgFromClient.startsWith("[WHISPER]")) {
					String whisper = msgFromClient.substring(9);
					int sub = whisper.indexOf(" ") + 1;
					String whispertoWhom = whisper.substring(1, sub-1);
					System.out.println("귓말 대상 : " + whispertoWhom);
					String whisperMsg = whisper.substring(sub);
					System.out.println("귓말 내용 : " + whisperMsg);
					
					if(WaitingServer.userList.contains(whispertoWhom)) {
						//귓말대상이 접속해 있을경우
						myPw.println("[WHISPER]" + clientNickName + " : " + whisperMsg);
						myPw.flush();
						if(WaitingServer.playingMap.containsKey(whispertoWhom)) {	//귓말 상대가 플레이 중일경우
							OutputStream outputStream2 = WaitingServer.playingMap.get(whispertoWhom).getOutputStream(); // 송신
							PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
							printWriter2.println("[WHISPER]" + clientNickName + " : " + whisperMsg);
							printWriter2.flush();
						} else if(WaitingServer.waitingMap.containsKey(whispertoWhom)) {	//귓말 상대가 대기실일 경우
							OutputStream outputStream2 = WaitingServer.waitingMap.get(whispertoWhom).getOutputStream();
							PrintWriter printWriter2 = new PrintWriter(new OutputStreamWriter(outputStream2, "UTF-8"));
							printWriter2.println("[WHISPER]" + clientNickName + " : " + whisperMsg);
							printWriter2.flush();
						}
					} else {						
						myPw.println("[WHISPERTONULL]");
						myPw.flush();
					}
				}

				 
			}

			System.out.println(clientNickName + "님이 잡은 물고기 : " + myFish + "마리");
			WaitingServer.roomManager.RemoveRoom(playingRoom); // 게임이 끝났으므로 해당방도 삭제
			playingRoom = null; // 방 객체 삭제
			
			String inDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
	        String inTime = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
			
			MongoClient mongoClient = new MongoClient("119.205.220.8", 27017);
			System.out.println("몽고DB 접속 성공");

            //데이터베이스 연결
            DB db = mongoClient.getDB("test");
            //컬렉션 가져오기
            DBCollection collection = db.getCollection("playLog");

            int idLength = clientNickName.length();

            //특정 조건에 맞는 데이터 출력
            BasicDBObject o = new BasicDBObject();
            o.put("ID", clientNickName);
            DBCursor cursor = collection.find(o);

            if(!cursor.hasNext()) {//첫 플레이라면
                //user 테이블에 데이터삽입
                BasicDBObject doc = new BasicDBObject();
                doc.put("Date", inDate);
                doc.put("Time", inTime);
                doc.put("ID", clientNickName);
                doc.put("NumofPlaying", 1);
                doc.put("PlayWithWho", enemyNickName);
                collection.insert(doc);
            } else {
                ArrayList<Integer> list = new ArrayList<>();

                while(cursor.hasNext()){
                    //로그 앞부분 자르기
                    String temp = cursor.next().toString().substring(105 + idLength);
                    System.out.println("temp : "+ temp);

                    //자른 로그중 숫자 추출
                    String NumofPlay = temp.replaceAll("[^0-9]", "");;
                    System.out.println("NumofPlay : " + NumofPlay);
                    
                    int numofPlay = Integer.parseInt(NumofPlay);

                    //리스트에 숫자 추가
                    list.add(numofPlay);
                }

                //리스트 중 최대값 + 1 = 누적 playNum
                int cumulativeNum = Collections.max(list);
                cumulativeNum += 1;
                System.out.println("cumulativeNum : "+ cumulativeNum);

                //user 테이블에 데이터삽입
                BasicDBObject doc = new BasicDBObject();
                doc.put("Date", inDate);
                doc.put("Time", inTime);
                doc.put("ID", clientNickName);
                doc.put("NumofPlaying", cumulativeNum);
                doc.put("PlayWithWho", enemyNickName);
                collection.insert(doc);
                
                mongoClient.close();
            }

		} catch (IOException e) {
			System.out.println("데이타 송수신에러");
			e.printStackTrace();
		} finally {
			close();
		}
	}
	public void close(){
		  try {
		  // 4. 소켓 닫기 --> 연결 끊기
		   enemyOs.close();
		   enemyPw.close();
		   myIs.close();
		   myBr.close();
		   myOs.close();
		   myPw.close();
		   clientSocket.close();
		   System.out.println("클라 소켓 close 완료");
		  } catch(IOException e) {
		   System.out.println("close에러");
		   e.printStackTrace();
		  }
	}
	
	private class FishThread extends Thread {
		
		private FishThread() {
			
		}
		
		public void run() {
			for (int i = 0; i < 10; i++) {

				try {
					Thread.sleep(3 * 1000); // 밀리세컨즈이므로 1000을 곱한다
					
					myPw.println("[FISH]");
					myPw.flush();
					
					//System.out.println("물고기 생성\n");
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.interrupt();
		}
	}
}*/




				
