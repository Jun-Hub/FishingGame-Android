package tcp;

import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomManager {

	List<GameRoom> roomList; // 방의 리스트

	public RoomManager(){
		roomList = new ArrayList<GameRoom>();
	}
	
	public GameRoom CreateRoom(String _owner){ // 유저가 방을 생성할 때 사용(유저가 방장으로 들어감)
		GameRoom room = new GameRoom(false, _owner);
		roomList.add(room);
		return room;
	}
	
	public GameRoom CreateRoom(GameRoom room){	// 룸을 새로 생성(빈 방)
		roomList.add(room);		
		return room;
	}
	
	public GameRoom CreateRoom(List<String> _userList){
		GameRoom room = new GameRoom(_userList);
		roomList.add(room);
		System.out.println("Room Created!");
		return room;
	}
	
	public void RemoveRoom(GameRoom _room){
		roomList.remove(_room); // 전달받은 룸을 제거
	}
	
}
