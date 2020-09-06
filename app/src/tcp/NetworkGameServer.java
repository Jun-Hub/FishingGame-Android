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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class NetworkGameServer {
	// 연결할 포트
	private static final int PORT = 9000;
	// 스레드 풀의 최대 스레드 개수
	private static final int THREAD_CNT = 10;
	private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);
	
	static HashMap<String, Socket> userMap = new HashMap<String, Socket>();	//유저의 닉네임와 소켓 정보를 담을 해쉬맵 생성
    static RoomManager roomManager = new RoomManager();
	
	public static void main(String[] args) {
				
		
		try {
			// 서버소켓 생성
			ServerSocket serverSocket = new ServerSocket(PORT);	
			
			while(true){
				// 소켓 접속 요청이 올때까지 대기
				Socket clientSocket = serverSocket.accept();
				
				//룸을 일단 만들어버린 후에 2명이 차면 스레드로 넘겨서 게임 진행
				
				// 3. 소켓으로 부터 송수신을 위한 i/o stream 을 얻기
				InputStream is = clientSocket.getInputStream(); //수신 --> read();
				BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				OutputStream os = clientSocket.getOutputStream(); //송신 --> write();
			  	PrintWriter pw = new PrintWriter(os);
			  	
				String userNickName = null;

				userNickName = br.readLine();
																
				userMap.put(userNickName, clientSocket);		//해쉬맵에 유저 닉네임과 소켓정보 담기
				
				//유저가 들어갈 방이 있나 새로 생성해야하나 검색하는 과정		
				for(int i=0; i<roomManager.roomList.size(); i++) {	//roomList를 돌며 
					
					int j =0;	//roomList를 다 돌았는지 확인용 변수
										
					if(roomManager.roomList.get(i).full == false) {	//대기방이 있을경우
						
						GameRoom waitingRoom = roomManager.roomList.get(i);	//대기방
						
						waitingRoom.AddUser(userNickName);	//대기방에 유저 추가(Room의 유저리스트에 추가)
						//clientMap.get(userNickName).EnterRoom(waitingRoom);	//룸에 유저 입장(유저의 속한방 정보에 해당 룸 등록)					
																
						waitingRoom.full = true;	//2명의 유저로 꽉 찻다는 걸 알려줌
						
						Socket ownerSocket = userMap.get(waitingRoom.GetOwner());	//대기방에 있던 방 주인 소켓						
						OutputStream ownerOutput = ownerSocket.getOutputStream();
						PrintWriter ownerPw = new PrintWriter(ownerOutput);
												
						pw.println("[START]" + waitingRoom.GetOwner());
						ownerPw.println("[START]" + userNickName);
						pw.flush();
						ownerPw.flush();
						
						try{
							threadPool.execute(new GameLogic(ownerSocket, waitingRoom, 0));
							threadPool.execute(new GameLogic(clientSocket, waitingRoom, 1));
							
							ownerPw.println("[SPEAR]1");
							ownerPw.flush();
							pw.println("[SPEAR]2");
							pw.flush();
						}catch(Exception e){
							e.printStackTrace();
						}
						
						break;
					}
					
					j++;
										
					if(j == roomManager.roomList.size()) {	//roomList를 다 돌았는데도 대기방이 나오지 않았다면, 새로운 방 생성
						GameRoom room = new GameRoom(false, userNickName);	//유저가 방을 만들어서, 동시에 방의 유저리스트에 방장으로 추가됨
						
						roomManager.CreateRoom(room);	//룸매니저의 해쉬맵에 룸 추가
						//clientMap.get(userNickName).EnterRoom(room);	//룸에 유저 입장(유저의 속한방 정보에 해당 룸 등록)
						
						pw.println("*system : 다른 유저를 기다리는 중....");
						pw.flush();
						
						break;
					}
				}	
				
				if(roomManager.roomList.size() == 0) {	//만들어진 방이 하나도 없다면
					GameRoom room = new GameRoom(false, userNickName);	//유저가 방을 만들어서, 동시에 방의 유저리스트에 방장으로 추가됨
					
					roomManager.CreateRoom(room);	//룸매니저의 리스트에 룸 추가
					//clientMap.get(userNickName).EnterRoom(room);	//룸에 유저 입장(유저의 속한방 정보에 해당 룸 등록)
					
				} 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
 
//소켓 처리용 래퍼 클래스입니다.
class GameLogic implements Runnable {

	private Socket clientSocket = null;
	private GameRoom playingRoom = null;
	private String clientNickName = null;
	private String enemyNickName = null;
	private int playerNum;
	
	InputStream myIs = null;
	BufferedReader myBr = null;
	OutputStream enemyOs, myOs = null;
	PrintWriter enemyPw, myPw = null;

	public GameLogic(Socket clientSocket, GameRoom playingRoom, int playerNum) {

		this.clientSocket = clientSocket;
		this.playingRoom = playingRoom;
		this.playerNum = playerNum;

		try {
			// 3. 소켓으로 부터 송수신을 위한 i/o stream 을 얻기
			
			int enemyNum=6868;

			if (playerNum == 0) {
				enemyNum = 1;
			} else if (playerNum == 1) {
				enemyNum = 0;
			}

			clientNickName = playingRoom.userList.get(playerNum);
			enemyNickName = playingRoom.userList.get(enemyNum);

			Socket enemySocket = NetworkGameServer.userMap.get(enemyNickName);

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
				
				if(msgFromClient.startsWith("[CHAT]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();
					System.out.println("" + msgFromClient);				
				}

				if (msgFromClient.startsWith("[COLLISION]")) {
					enemyPw.println(msgFromClient);
					enemyPw.flush();

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
				}	else if (msgFromClient.startsWith("[GAMEOVER]")) {
					// 상대방한테 내가 얻은 점수 보내주기
					enemyPw.println("[GAMEOVER]" + myFish);
					enemyPw.flush();
					
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}					
					break;
				} 
			}

			NetworkGameServer.roomManager.RemoveRoom(playingRoom); // 게임이 끝났으므로 해당방도 삭제
			playingRoom = null; // 방 객체 삭제
			
			String inDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
	        String inTime = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());
			
			MongoClient mongoClient = new MongoClient("119.205.220.8", 27017);

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
		  }catch(IOException e) {
		   System.out.println("close에러");
		   e.printStackTrace();
		  }
	}
	
	public class FishThread extends Thread {
		
		public FishThread() {
			
		}
		
		public void run() {
			for (int i = 0; i < 10; i++) {

				try {
					Thread.sleep(3 * 1000);
					
					myPw.println("[FISH]");
					myPw.flush();
										
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			this.interrupt();
		}
	}
}
