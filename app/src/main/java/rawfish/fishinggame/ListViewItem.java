package rawfish.fishinggame;

public class ListViewItem {  //데이터 처리 클래스

    private String playerID;
    private String chatMessage;
    private String lastChatTime;
    private String chatNum=null;

    public void setPlayerID(String playerID) {
        this.playerID = playerID;
    }

    public String getPlayerID() {
        return playerID;
    }

    public void setChatMessage(String chatMessage) {
        this.chatMessage = chatMessage;
    }

    public String getChatMessage() {
        return chatMessage;
    }

    public void setLastChatTime(String lastChatTime) {
        this.lastChatTime = lastChatTime;
    }

    public String getLastChatTime() {
        return lastChatTime;
    }

    public void setChatNum(String chatNum) {
        this.chatNum = chatNum;
    }

    public String getChatNum() {
        return chatNum;
    }
}