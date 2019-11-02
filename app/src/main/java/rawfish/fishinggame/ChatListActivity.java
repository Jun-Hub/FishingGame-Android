package rawfish.fishinggame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

public class ChatListActivity extends Activity {

    static ChatListViewAdapter chatListAdapter;
    static boolean active = false;
    ListView chatListView;
    String chatParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.chatlist_activity);

        active = true;

        Intent intent = getIntent();
        chatParticipants = intent.getStringExtra("roomName");
        String lastChat = intent.getStringExtra("lastChat");

        chatListView = (ListView) findViewById(R.id.chatListView);
        chatListAdapter = new ChatListViewAdapter();
        chatListView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        chatListView.setAdapter(chatListAdapter);

        if(!(chatParticipants==null)&&!(lastChat==null)) {
            chatListAdapter.add(0, chatParticipants, lastChat, null);
        }

        //채팅리스트에 담겨져있던 채팅을 다 추가해주고, 초기화
        for(int i=0; i<SearchActivity.roomList.size(); i++) {
            if(!(chatListAdapter.getCount()==0)) {  //채팅방리스트가 비어있지않다면
                for(int j=0; j<chatListAdapter.getCount(); j++) {
                    // 현존하는 채팅방리스트와 룸리스트가 다르다면 채팅방 리스트에 채팅방추가
                    if (!chatListAdapter.getPlayerID(j).equals(SearchActivity.roomList.get(i))) {
                        chatListAdapter.add(0, SearchActivity.roomList.get(i), null, null);
                    }
                }
            } else {
                chatListAdapter.add(0, SearchActivity.roomList.get(i), null, null);
            }
        }
        SearchActivity.roomList.clear();

        //채팅방의 가장 최근 채팅 보여주기
        if(!(SearchActivity.chatList.size()==0)) {
            int i = SearchActivity.chatList.size();
            String lastChatt = SearchActivity.chatList.get(i-1);
            String chatNum = Integer.toString(i);
            chatListAdapter.modify(0, lastChatt, chatNum);
        }

        //채팅 목록이 하나도 없다면!
        if(chatListAdapter.getCount()==0) {
            setContentView(R.layout.nullchatlist_activity);
        }

        chatListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(chatParticipants==null) {
                    chatParticipants = chatListAdapter.getPlayerID(position);
                }
                Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
                intent.putExtra("roomName", chatParticipants);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }
}
