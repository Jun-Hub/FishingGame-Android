package rawfish.fishinggame;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.Editable;

import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedWriter;
import java.io.PrintWriter;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class SearchActivity extends Activity {

    static String id;
    String inDate, inTime, chatMessage, loginPlayer, onetoOneMessage, chatWithWhom;
    ListView listView, playerList;
    TextView searchTextView, chatNumView;
    WaitingChatAdapter waitingChatAdapter;
    ListViewAdapter playerListAdapter;
    AutoCompleteTextView chatEditText;
    boolean whisper = false;
    ArrayList<String> whisperList;
    static ArrayList<String> chatList, roomList;
    ArrayAdapter<String> autoCompleteAdapter;

    ImageView imageView;

    static PrintWriter pw;
    static DataOutputStream dos;
    static PipedWriter pipedWriter = new PipedWriter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/pencil_sketch.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build());

        requestWindowFeature(Window.FEATURE_NO_TITLE);  //상단 타이틀바 없애기
        setContentView(R.layout.search_activity);

        Button searchButton = (Button) findViewById(R.id.searchButton);
        Button chatListButton = (Button) findViewById(R.id.chatListButton);
        searchTextView = (TextView) findViewById(R.id.searchText);
        listView = (ListView) findViewById(R.id.listView);
        playerList = (ListView)findViewById(R.id.playerListView);
        waitingChatAdapter = new WaitingChatAdapter(getApplicationContext(), R.layout.chat_message);
        playerListAdapter = new ListViewAdapter();
        chatEditText = (AutoCompleteTextView)findViewById(R.id.chatEditText);
        Button sendButton = (Button) findViewById(R.id.sendButton);
        chatNumView = (TextView) findViewById(R.id.chatNum);
        imageView = (ImageView) findViewById(R.id.ImageView);

        searchTextView.setVisibility(View.GONE);

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(waitingChatAdapter);
        playerList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        playerList.setAdapter(playerListAdapter);

        Intent intent = getIntent();
        int loginWithWhat = intent.getExtras().getInt("loginWithWhat");
        Log.e("loginWithWhat", "" + loginWithWhat);
        if(loginWithWhat==0) {  //자체로그인 했더라면
            id = intent.getExtras().getString("ID");
        } else if(loginWithWhat==1) {   //페북 로그인 했더라면
            id = intent.getExtras().getString("name");
        } else if(loginWithWhat==2) {   //카카오 로그인 했더라면
            id = intent.getExtras().getString("name");
        }
        Log.e("id", "" + id);

        inDate = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        inTime = new java.text.SimpleDateFormat("HHmmss").format(new java.util.Date());

        //TODO
        //MongoDB에 로그인 로그 남기기
        /*MongoDBThread mongoDBThread = new MongoDBThread();
        mongoDBThread.start();*/

        ChatThread chatThread = new ChatThread();
        chatThread.start();

        chatList = new ArrayList<>();
        roomList = new ArrayList<>();

        //to scroll the list view to bottom on data change
        waitingChatAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(waitingChatAdapter.getCount() - 1);
            }
        });


        //대기자 리스트뷰 클릭 이벤트
        playerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String chatParticipants = playerListAdapter.getPlayerID(position);
                    //자기 아이디를 클릭한게 아니라면
                if(!chatParticipants.equals(SearchActivity.id)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SearchActivity.this);
                    dialog.setTitle("" + playerListAdapter.getPlayerID(position));
                    dialog.setMessage("1:1 채팅하기");
                    dialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(SearchActivity.this, ChatActivity.class);
                            intent.putExtra("roomName", chatParticipants);
                            startActivity(intent);
                        }
                    });
                    dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });


        whisperList = new ArrayList<>();
        autoCompleteAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, whisperList);
        chatEditText.setAdapter(autoCompleteAdapter);

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
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                startActivity(new Intent(SearchActivity.this, GameActivity.class));
                /*pw.println("[SEARCH]" + id);
                pw.flush();

                searchTextView.setVisibility(View.VISIBLE);
                Animation animation = AnimationUtils.loadAnimation(SearchActivity.this, R.anim.translate);
                searchTextView.startAnimation(animation);*/
            }
        });

        chatListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SearchActivity.this, ChatListActivity.class));
            }
        });

        checkPermission();
        checkPermission2();
    }

    @Override   //폰트 설정
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private boolean sendChatMessage() {
        String chatMessage = chatEditText.getText().toString();
        chatEditText.setText("");

        pw.println("[CHAT]" + id + " : " + chatMessage);
        pw.flush();

        return true;
    }

    private void sendWhisperMessage() {
        String chatMessage = chatEditText.getText().toString();
        chatEditText.setText("");

        pw.println("[WHISPER]"+chatMessage);
        pw.flush();
    }

    private class MongoDBThread extends Thread {

        private MongoDBThread() {

        }

        public void run() {
            MongoClient mongoClient = new MongoClient("119.205.220.8", 27017);
            Log.e("몽고DB", "접속 성공");

            //데이터베이스 연결
            DB db = mongoClient.getDB("test");
            //컬렉션 가져오기
            DBCollection collection = db.getCollection("loginLog");

            int idLength = id.length();

            //특정 조건에 맞는 데이터 출력
            BasicDBObject o = new BasicDBObject();
            o.put("ID", id);
            DBCursor cursor = collection.find(o);

            if(!cursor.hasNext()) {//첫 로그인이라면
                //user 테이블에 데이터삽입
                BasicDBObject doc = new BasicDBObject();
                doc.put("Date", inDate);
                doc.put("Time", inTime);
                doc.put("ID", id);
                doc.put("CumulativeNumofLogin", 1);
                collection.insert(doc);
            } else {
                ArrayList<Integer> list = new ArrayList<>();

                while(cursor.hasNext()){
                    //로그 앞부분 자르기
                    String temp = cursor.next().toString().substring(105 + idLength);
                    //Log.e("temp", "" + temp);

                    //자른 로그중 숫자 추출
                    String NumofLogin = temp.replaceAll("[^0-9]", "");;
                    //Log.e("NumofLogin", "" + NumofLogin);

                    int numofLogin = Integer.parseInt(NumofLogin);

                    //리스트에 숫자 추가
                    list.add(numofLogin);
                }

                //리스트 중 최대값 + 1 = 누적 LoginNum
                int cumulativeNum = Collections.max(list);
                cumulativeNum += 1;
                Log.e("cumulativeNum", "" + cumulativeNum);

                //user 테이블에 데이터삽입
                BasicDBObject doc = new BasicDBObject();
                doc.put("Date", inDate);
                doc.put("Time", inTime);
                doc.put("ID", id);
                doc.put("CumulativeNumofLogin", cumulativeNum);
                collection.insert(doc);

                mongoClient.close();
                this.interrupt();
            }
        }
    }

    private class ChatThread extends Thread {

        int i=0;

        private ChatThread() {


        }

        public void run() {
            try {
                Socket socket = new Socket("172.30.1.2", 8500);

                // 2. 데이타 송수신을 위한 i/o stream을 얻어야 한다.
                InputStream is = socket.getInputStream(); // 수신 --> read();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                BufferedInputStream bis = new BufferedInputStream(is);
                OutputStream os = socket.getOutputStream(); // 송신 --> write();
                pw = new PrintWriter(os);
                dos = new DataOutputStream(os);

                Log.e("서버에 보낼 아디 : ", id);

                pw.println(id);    //서버로 자기 닉네임 전달
                pw.flush();

                while (true) {
                    String response = br.readLine();

                    if (response != null) {
                        if (response.startsWith("[CHAT]")) {
                            Log.e("354653", "" + response);
                            chatMessage = response.substring(6);
                            mHandler.sendEmptyMessage(0);

                        }
                        else if(response.startsWith("[CLEAR]")) {
                            Log.e("[CLEAR][CLEAR][CLEAR]", "[CLEAR][CLEAR]");
                            // 메시지 얻어오기
                            Message message = mHandler.obtainMessage();
                            // 메시지 ID 설정
                            message.what = 1;
                            // 메시지 전달
                            mHandler.sendMessage(message);
                        }
                        else if(response.startsWith("[PLAYER]")) {
                            loginPlayer = response.substring(8);
                            Log.e("PLAYERPLAYER", "" + loginPlayer);

                            // 메시지 얻어오기
                            Message message = mHandler.obtainMessage();
                            // 메시지 ID 설정
                            message.what = 2;
                            // 메시지 내용 설정 (Object)
                            message.obj = loginPlayer;
                            // 메시지 전달
                            mHandler.sendMessage(message);

                            //귓속말리스트 추가
                            if(!loginPlayer.equals(id)) {
                                if(!whisperList.contains("@"+loginPlayer)) {
                                    whisperList.add("@"+loginPlayer);
                                }
                            }
                        } else if(response.startsWith("[WHISPER]")) {
                            chatMessage = response.substring(9);
                            if(GameGLRenderer.start) {//게임중이라면
                                mHandler.sendEmptyMessage(4);
                            } else {
                                mHandler.sendEmptyMessage(5);
                            }
                        } else if(response.startsWith("[WHISPERTONULL]")) {
                            mHandler.sendEmptyMessage(3);
                        } else if(response.startsWith("[START]")) {
                            Intent intent = new Intent(SearchActivity.this, GameActivity.class);
                            startActivity(intent);
                            GameGLRenderer.start = true;
                        } else if(response.startsWith("[1:1CHATWITH]")) {
                            //채팅방 리스트에 채팅방 추가
                            chatWithWhom = response.substring(13);
                            Log.e("[1:1CHATWITH]", "" + chatWithWhom);
                            if(ChatListActivity.active) {
                                mHandler.sendEmptyMessage(6);
                            } else {
                                roomList.add(chatWithWhom);
                            }
                        } else if(response.startsWith("[1:1CHAT]")) {
                            onetoOneMessage = response.substring(9);
                            if(ChatActivity.active) {
                                mHandler.sendEmptyMessage(7);
                            } else {
                                chatList.add(onetoOneMessage);
                            }
                            if(ChatListActivity.active) {
                                mHandler.sendEmptyMessage(9);
                            }
                            if(chatList.size()==0) {
                                mHandler.sendEmptyMessage(10);
                            } else {
                                mHandler.sendEmptyMessage(8);
                            }
                        } else if(response.startsWith("[IMAGESIZE]")) {

                            Log.e("IMAGESIZE IMAGESIZE@@", "f ");

                            byte[] imagebuffer = null;
                            int size = 0;

                            byte[] buffer = new byte[10240];

                            int read;

                            while((read = bis.read(buffer)) != -1) {
                                if (imagebuffer == null) {
                                    //처음 4byte에서 비트맵이미지의 총크기를 추출해 따로 저장한다
                                    byte[] sizebuffer = new byte[4];
                                    System.arraycopy(buffer, 0, sizebuffer, 0, sizebuffer.length);
                                    size = getInt(sizebuffer);
                                    read -= sizebuffer.length;

                                    //나머지는 이미지버퍼 배열에 저장한다
                                    imagebuffer = new byte[read];
                                    System.arraycopy(buffer, sizebuffer.length, imagebuffer, 0, read);
                                } else {
                                    //이미지버퍼 배열에 계속 이어서 저장한다
                                    byte[] preimagebuffer = imagebuffer.clone();
                                    imagebuffer = new byte[read + preimagebuffer.length];
                                    System.arraycopy(preimagebuffer, 0, imagebuffer, 0, preimagebuffer.length);

                                    System.arraycopy(buffer, 0, imagebuffer, imagebuffer.length - read, read);
                                }

                                //이미지버퍼 배열에 총크기만큼 다 받아졌다면 이미지를 저장하고 끝낸다
                                if (imagebuffer.length >= size) {
                                    Bundle bundle = new Bundle();
                                    bundle.putByteArray("Data", imagebuffer);

                                    Message msg = mResultHandler.obtainMessage();
                                    msg.setData(bundle);
                                    mResultHandler.sendMessage(msg);

                                    imagebuffer = null;
                                    size = 0;
                                    Log.e("수신 완료@@@@@@@", " s");
                                }
                            }

                        } else if(response.startsWith("[IMAGE]")) {
                            /*File ff = new File("/storage/emulated/0/DCIM/YouCam Perfect/92289.jpg");
                            FileOutputStream output = new FileOutputStream(ff);

                            byte[] buf2 = new byte[1];
                            while (is.read(buf2) > 0) {
                                output.write(buf2);
                                output.flush();
                            }

                            output.close();*/

                            Log.e("57o57ior6@@@@@@@", " ff");
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("search ERROR : ", e.getMessage());
                e.printStackTrace();
            }
            this.interrupt();
        }
    }

    // 핸들러 객체 만들기
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    waitingChatAdapter.add(new ChatMessage(true, false, chatMessage));
                    break;

                case 1:
                    playerListAdapter.removeAll();
                    playerListAdapter.notifyDataSetChanged();
                    Log.e("애미출타나이슴clearclear", "");
                    break;

                case 2:
                    //자기 아이디는 맨위로
                    if(msg.obj.toString().equals(id)) {
                        playerListAdapter.add(0, ""+msg.obj);
                    } else {
                        playerListAdapter.add("" + msg.obj);
                    }
                    playerListAdapter.notifyDataSetChanged();
                    Log.e("애미출타나이슴2222222", "" + msg.obj);
                    break;

                case 3:
                    Toast.makeText(SearchActivity.this, "귓속말 할 상대방이 없습니다", Toast.LENGTH_SHORT).show();
                    break;

                case 4:
                    GameActivity.waitingChatAdapter.add(new ChatMessage(true, true, chatMessage));
                    break;

                case 5:
                    waitingChatAdapter.add(new ChatMessage(true, true, chatMessage));
                    break;

                case 6:
                    ChatListActivity.chatListAdapter.add(0, chatWithWhom, null, null);
                    ChatListActivity.chatListAdapter.notifyDataSetChanged();
                    break;

                case 7:
                    ChatActivity.chatArrayAdapter.add(new ChatMessage(true, false, onetoOneMessage));
                    break;

                case 8:
                    chatNumView.setVisibility(View.VISIBLE);
                    int ii = chatList.size();
                    String chatNum = Integer.toString(ii);
                    chatNumView.setText(chatNum);
                    break;

                case 9:
                    int i = chatList.size();
                    String lastChat = chatList.get(i-1);
                    String chatNumm = Integer.toString(i);
                    ChatListActivity.chatListAdapter.modify(0, lastChat, chatNumm);
                    ChatListActivity.chatListAdapter.notifyDataSetChanged();
                    break;

                case 10:
                    chatNumView.setVisibility(View.GONE);
                    break;

                default:
                    break;
            }
        }
    };

    //이미지뷰에 비트맵을 넣는다
    public Handler mResultHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {

            byte[] data = msg.getData().getByteArray("Data");

            imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("chatList.size", "" + chatList.size());
        if(chatList.size()==0) {
            chatNumView.setVisibility(View.GONE);
        } else {
            chatNumView.setVisibility(View.VISIBLE);
            chatNumView.setText(chatList.size());
        }
    }

    //byte배열을 숫자로 바꾼다
    private int getInt(byte[] data) {
        int s1 = data[0] & 0xFF;
        int s2 = data[1] & 0xFF;
        int s3 = data[2] & 0xFF;
        int s4 = data[3] & 0xFF;

        return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
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

    /**
     * 퍼미션 체크
     */
    private void checkPermission(){

            /* 사용자의 OS 버전이 마시멜로우 이상인지 체크한다. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    /* 사용자 단말기의 권한 중 "위치사용" 권한이 허용되어 있는지 체크한다.
                    *  int를 쓴 이유? 안드로이드는 C기반이기 때문에, Boolean 이 잘 안쓰인다.
                    */
            int permissionResult = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

                    /* ACCESS_FINE_LOCATION의 권한이 없을 때 */
            // 패키지는 안드로이드 어플리케이션의 아이디다.( 어플리케이션 구분자 )
            if (permissionResult == PackageManager.PERMISSION_DENIED) {

                        /* 사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                        * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                        */
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(SearchActivity.this);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 단말기의 \"위치정보\" 권한이 필요합니다. 계속하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
                                    }

                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(SearchActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }

                //최초로 권한을 요청할 때
                else {
                    // ACCESS_FINE_LOCATION 권한을 Android OS 에 요청한다.
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
                }



            }
                    /* ACCESS_FINE_LOCATION의 권한이 있을 때 */
            else {

            }

        }
                /* 사용자의 OS 버전이 마시멜로우 이하일 떄 */
        else {

        }
    }

    private void checkPermission2() {

            /* 사용자의 OS 버전이 마시멜로우 이상인지 체크한다. */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            int permissionResult2 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionResult2 == PackageManager.PERMISSION_DENIED) {

                        /* 사용자가 CALL_PHONE 권한을 한번이라도 거부한 적이 있는 지 조사한다.
                        * 거부한 이력이 한번이라도 있다면, true를 리턴한다.
                        */
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    android.support.v7.app.AlertDialog.Builder dialog = new android.support.v7.app.AlertDialog.Builder(SearchActivity.this);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 단말기의 \"위치정보\" 권한이 필요합니다. 계속하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2000);
                                    }

                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(SearchActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .create()
                            .show();
                }

                //최초로 권한을 요청할 때
                else {
                    // ACCESS_FINE_LOCATION 권한을 Android OS 에 요청한다.
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2000);
                }

            }

                /* ACCESS_FINE_LOCATION의 권한이 있을 때 */
            else {

            }

        }
                /* 사용자의 OS 버전이 마시멜로우 이하일 떄 */
        else {

        }
    }

    /**
     * 사용자가 권한을 허용했는지 거부했는지 체크
     * @param requestCode   1000번
     * @param permissions   개발자가 요청한 권한들
     * @param grantResults  권한에 대한 응답들
     *                    permissions와 grantResults는 인덱스 별로 매칭된다.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {

            /* 요청한 권한을 사용자가 "허용"했다면 인텐트를 띄워라
                내가 요청한 게 하나밖에 없기 때문에. 원래 같으면 for문을 돈다.*/
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                }
            }
            else {
                //Toast.makeText(SearchActivity.this, "앱 설정에서 권한을 허용해야 위치정보 사용이 가능합니다.", Toast.LENGTH_SHORT).show();
            }

        }
        if (requestCode == 2000) {

            /* 요청한 권한을 사용자가 "허용"했다면 인텐트를 띄워라
                내가 요청한 게 하나밖에 없기 때문에. 원래 같으면 for문을 돈다.*/
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                }
            }
            else {
                //Toast.makeText(SearchActivity.this, "앱 설정에서 권한을 허용해야 위치정보 사용이 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
