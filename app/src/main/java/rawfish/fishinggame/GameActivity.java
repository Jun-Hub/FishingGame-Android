package rawfish.fishinggame;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.lang.ref.WeakReference;

public class GameActivity extends Activity {

    GameGLSurfaceView GLSurfaceView;
    InputMethodManager imm;

    AutoCompleteTextView chatEditText;
    static Button chatButton, sendButton;

    static WaitingChatAdapter waitingChatAdapter;
    static ListView listView;

    private boolean side, whisper = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //상단 타이틀바 없애기

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   // 조명 항상켜기
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);    //전체화면

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;

        GLSurfaceView = new GameGLSurfaceView(this, width, height);
        setContentView(GLSurfaceView);  //OpenGl화면으로 가기

        //Opengl ES화면 위로 안드로이드 레이아웃 화면 오버레이
        setContentView(GLSurfaceView);

        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout linear = (LinearLayout)inflater.inflate(R.layout.game_activity, null);

        LinearLayout.LayoutParams paramlinear = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                                                            LinearLayout.LayoutParams.MATCH_PARENT);
        this.addContentView(linear, paramlinear);//이 부분이 레이아웃을겹치는 부분

        listView = (ListView) findViewById(R.id.listView1);

        waitingChatAdapter = new WaitingChatAdapter(getApplicationContext(), R.layout.chat_message);
        listView.setAdapter(waitingChatAdapter);
        chatEditText = (AutoCompleteTextView)findViewById(R.id.chatEditText);
        chatButton = (Button) findViewById(R.id.chatButton);
        sendButton = (Button) findViewById(R.id.sendButton);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        //채팅쪽은 안보이다가
        chatEditText.setVisibility(View.GONE);
        sendButton.setVisibility(View.GONE);

        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override   //상단 채팅하기버튼 클릭하면 보이기
            public void onClick(View v) {

                chatEditText.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.VISIBLE);
                chatEditText.requestFocus();
                //키보드 보이게 하는 부분
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });

        //KeyboardThread keyboardThread = new KeyboardThread();
        //keyboardThread.start();

        chatEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    chatEditText.setVisibility(View.GONE);
                    sendButton.setVisibility(View.GONE);
                    imm.hideSoftInputFromWindow(chatEditText.getWindowToken(), 0);
                    return sendChatMessage();
                }
                return false;
            }
        });

        //채팅AutoCompleteTextView 변화감지
        chatEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 입력되는 텍스트에 변화가 있을 때
                String text = s.toString();
                String strColor = "#c512e1";
                if(text.startsWith("@")) {
                    chatEditText.setTextColor(Color.parseColor(strColor));
                    if(text.contains(" ")) {
                        whisper = true;
                    }
                } else {
                    chatEditText.setTextColor(Color.BLACK);
                    whisper = false;
                }
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // 입력이 끝났을 때
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // 입력하기 전에
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(whisper) {
                    sendWhisperMessage();
                } else {
                    sendChatMessage();
                }
                chatEditText.setVisibility(View.GONE);
                sendButton.setVisibility(View.GONE);
                imm.hideSoftInputFromWindow(chatEditText.getWindowToken(), 0);
            }
        });

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(waitingChatAdapter);

        //to scroll the list view to bottom on data change
        waitingChatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(waitingChatAdapter.getCount() - 1);
            }
        });

        //채팅 리스트뷰 롱클릭 이벤트
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                String chatMsg = waitingChatAdapter.getItem(position).message;
                int sub = chatMsg.indexOf(" ");
                String whisperfromWho = chatMsg.substring(0, sub);

                chatEditText.setVisibility(View.VISIBLE);
                sendButton.setVisibility(View.VISIBLE);
                chatEditText.requestFocus();
                //키보드 보이게 하는 부분
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                chatEditText.setText("@" + whisperfromWho + " ");

                return false;
            }
        });
    }

    private boolean sendChatMessage(){
        String chatMessage = chatEditText.getText().toString();
        waitingChatAdapter.add(new ChatMessage(side, false, SearchActivity.id + " : " + chatEditText.getText().toString()));
        chatEditText.setText("");
        side = !side;

        GameGLRenderer.printWriter.println("[CHAT]" + SearchActivity.id + " : " + chatMessage);
        GameGLRenderer.printWriter.flush();

        return true;
    }

    private void sendWhisperMessage() {
        String chatMessage = chatEditText.getText().toString();
        chatEditText.setText("");

        SearchActivity.pw.println("[WHISPER]"+chatMessage);
        SearchActivity.pw.flush();
    }

    // 핸들러 객체 만들기
    private class MyHandler extends Handler {
        private WeakReference<GameActivity> mActivity;

        private MyHandler(GameActivity activity) {
            mActivity = new WeakReference<GameActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            GameActivity activity = mActivity.get();
            if (activity != null) {
                    chatEditText.setVisibility(View.GONE);
                    sendButton.setVisibility(View.GONE);
            }
        }
    }

    //    @Override
//    protected void onStop() {
//        // TODO 어딘가에 쓰기
//        super.onStop();
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}





