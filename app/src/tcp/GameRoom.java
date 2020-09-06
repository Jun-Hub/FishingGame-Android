package tcp;

import java.util.ArrayList;
import java.util.List;

public class GameRoom {

	List<String> userList;
	String roomOwner; // 방장
	String roomName; // 방 이름
	int roomNumber; //방 번호
	int ownerFish, clientFish;	//두 유저가 잡은 물고기 수
	int fishEa = 10;
	
	Boolean full = false;	//대기방인지의 여부

	public GameRoom(Boolean full) { // 아무도 없는 방을 생성할 때
		userList = new ArrayList<String>();
		this.full = full;
	}

	public GameRoom(Boolean full, String _user) { // 유저가 방을 만들때
		userList = new ArrayList<String>();
		userList.add(_user); // 유저를 추가시킨 후
		this.roomOwner = _user; // 방장을 유저로 만든다.
		this.full = full;
	}

	public GameRoom(List<String> _userList) { // 유저 리스트가 방을 생성할
		this.userList = _userList; // 유저리스트 복사
		this.roomOwner = userList.get(0); // 첫번째 유저를 방장으로 설정
	}

	public void AddUser(String userNickName) {
		userList.add(userNickName);
	}


	
	@SuppressWarnings("unused")
	public void Broadcast(byte[] data) {
		for (String user : userList) { // 방에 속한 유저의 수만큼 반복
			// 각 유저에게 데이터를 전송하는 메서드 호출
			// ex) user.SendData(data);
			
			try {
				user.sock.getOutputStream().write(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void SetOwner(String _user) {
		this.roomOwner = _user; // 특정 사용자를 방장으로 변경한다.
	}

	public void SetRoomName(String _name) { // 방 이름을 설정
		this.roomName = _name;
	}
	
	public String GetUserByNickName(String _nickName){ // 닉네임을 통해서 방에 속한 유저를 리턴함
		
		for(String user : userList){
			if(user.equals(_nickName)){
				return user; // 유저를 찾았다면
			}
		}
		return null; // 찾는 유저가 없다면
	}

	public String GetRoomName() { // 방 이름을 가져옴
		return roomName;
	}
	
	public int GetRoomNumber() { // 방 번호를 가져옴
		return roomNumber;
	}

	public int GetUserSize() { // 유저의 수를 리턴
		return userList.size();
	}

	public String GetOwner() { // 방장을 리턴
		return roomOwner;
	}
}
