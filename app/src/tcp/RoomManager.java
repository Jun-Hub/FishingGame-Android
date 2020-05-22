package tcp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomManager {

	List<GameRoom> roomList; // 방의 리스트
//    HashMap<Boolean, GameRoom> roomMap;	//룸의 룸 고유번호와 해당 룸을 담을 해쉬맵 생성

	public RoomManager(){
		roomList = new ArrayList<GameRoom>();
//		roomMap = new HashMap<Boolean, GameRoom>();
	}
	
//	public GameRoom CreateRoom(){ // 룸을 새로 생성(빈 방)
//		GameRoom room = new GameRoom();
//		roomList.add(room);
//		roomMap.put(2, room);
//		System.out.println("Room Created!");
//		return room;
//	}
	
	public GameRoom CreateRoom(String _owner){ // 유저가 방을 생성할 때 사용(유저가 방장으로 들어감)
		GameRoom room = new GameRoom(false, _owner);
		roomList.add(room);
		System.out.println("Room Created!");
		return room;
	}
	
	public GameRoom CreateRoom(GameRoom room){	// 룸을 새로 생성(빈 방)
		roomList.add(room);
//		roomMap.put(room.full, room);
		System.out.println("Room Created!");
		
		return room;
	}
	
	public GameRoom CreateRoom(List<String> _userList){
		GameRoom room = new GameRoom(_userList);
		roomList.add(room);
		System.out.println("Room Created!");
		return room;
	}
	
	public void RemoveRoom(GameRoom _room){
		roomList.remove(_room); // 전달받은 룸을 제거한다.
		System.out.println("Room Deleted!");
	}
	
//	public static int RoomCount(){ return roomList.size();} // 룸의 크기를 리턴해
}