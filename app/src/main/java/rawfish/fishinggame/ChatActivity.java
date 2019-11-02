package rawfish.fishinggame;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ChatActivity extends Activity {

    static ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private Button buttonSend, buttonGallery;
    private String roomName;
    private boolean sendExp = false;
    static boolean active = false;
    final int REQ_CODE_SELECT_IMAGE=100;
    Bitmap image_bitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        roomName = intent.getStringExtra("roomName");

        setContentView(R.layout.chat_activity);

        active = true;

        buttonSend = (Button) findViewById(R.id.buttonSend);
        //buttonGallery = (Button) findViewById(R.id.buttonGalley);
        listView = (ListView) findViewById(R.id.listView1);
        TextView roomNameText = (TextView) findViewById(R.id.roomName);
        roomNameText.setText(roomName);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.chat_message);
        listView.setAdapter(chatArrayAdapter);

        //채팅리스트에 담겨져있던 채팅을 다 추가해주고, 초기화
        for(int i=0; i<SearchActivity.chatList.size(); i++) {
            chatArrayAdapter.add(new ChatMessage(true, false, SearchActivity.chatList.get(i)));
        }
        SearchActivity.chatList.clear();

        chatText = (EditText) findViewById(R.id.chatText);
        chatText.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    return sendChatMessage();
                }
                return false;
            }
        });
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(!chatText.getText().toString().equals("")) {
                    Log.e("chatArrayAdapter", " "+ chatArrayAdapter.getCount());
                    /*if(chatArrayAdapter.getCount()==0) {
                        SearchActivity.pw.println("[1:1CHATWITH]"+roomName);
                        SearchActivity.pw.flush();
                    }*/
                    sendChatMessage();
                }
            }
        });

        /*buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼 클릭시 처리로직
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
                intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
            }
        });*/

        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
    }

    private boolean sendChatMessage(){
        if(!sendExp) {
            SearchActivity.pw.println("[1:1CHATWITH]"+roomName);
            SearchActivity.pw.flush();
            sendExp=true;
        }
        SearchActivity.pw.println("[CHATROOMNAME]"+roomName);
        SearchActivity.pw.flush();

        SearchActivity.pw.println("[1:1CHAT]"+chatText.getText().toString());
        SearchActivity.pw.flush();

        chatArrayAdapter.add(new ChatMessage(false, false, chatText.getText().toString()));
        chatText.setText("");

        return true;
    }

    //비트맵의 byte배열을 얻는다
    public byte[] getImageByte(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

        return out.toByteArray();
    }
    //숫자를 byte형태로 바꾼다
    private byte[] getByte(int num) {
        byte[] buf = new byte[4];
        buf[0] = (byte)( (num >>> 24) & 0xFF );
        buf[1] = (byte)( (num >>> 16) & 0xFF );
        buf[2] = (byte)( (num >>>  8) & 0xFF );
        buf[3] = (byte)( (num >>>  0) & 0xFF );

        return buf;
    }

    @Override
    protected void onStop() {
        super.onStop();
        active = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(sendExp) {
            Intent intent = new Intent(ChatActivity.this, ChatListActivity.class);
            intent.putExtra("roomName", roomName);
            intent.putExtra("lastChat", chatArrayAdapter.getItem(chatArrayAdapter.getCount()-1).get());
            startActivity(intent);
        } else {
            startActivity(new Intent(ChatActivity.this, ChatListActivity.class));
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        //Toast.makeText(getBaseContext(), "resultCode : "+resultCode,Toast.LENGTH_SHORT).show();

        if(requestCode == REQ_CODE_SELECT_IMAGE)
        {
            if(resultCode==Activity.RESULT_OK)
            {
                try {
                    //Uri에서 이미지 이름을 얻어온다.
                    String imagePath = getImageNameToUri(data.getData());

                    //이미지 데이터를 비트맵으로 받아온다.
                    image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    //배치해놓은 ImageView에 set

                    Toast.makeText(getBaseContext(), "path : "+imagePath , Toast.LENGTH_SHORT).show();
                    Log.e("path", imagePath);

                    SearchActivity.pw.println("[CHATROOMNAME]"+roomName);
                    SearchActivity.pw.flush();

                    //이미지를 불려온다
                    byte[] dataa = getImageByte(image_bitmap);

                    //비트맵이미지의 총크기를 4byte배열에 담아서 먼저 보낸다 (이때 꼭 4byte일 필요는 없다. 마음내키는대로~)
                    //총크기를 보내는 이유는 비트맵이미지가 전부 전송된 시점을 클라이언트에 알리기 위함이다
                    byte[] size = getByte(dataa.length);
                    String sizeStr = new String(size);

                    SearchActivity.pw.println("[IMAGESIZE]"+sizeStr);
                    SearchActivity.pw.flush();

                    /*SearchActivity.dos.write(size, 0, size.length);
                    SearchActivity.dos.flush();*/

                    String dataStr = new String(dataa);

                    SearchActivity.pw.println("[IMAGE]"+dataStr);
                    SearchActivity.pw.flush();
                    //실제 데이터를 보낸다
                    /*SearchActivity.dos.write(dataa, 0, dataa.length);
                    SearchActivity.dos.flush();*/

                    Log.e("이미지 서버로 전송완료@@@@", "shfsfhsfh");

                    //서버로 파일전송
                    /*DataInputStream dis = new DataInputStream(new FileInputStream(new File(imagePath)));

                    byte[] buf = new byte[1024];
                    while(dis.read(buf)>0) {
                        SearchActivity.dos.write(buf);
                        SearchActivity.dos.flush();
                    }

                    SearchActivity.dos.close();*/

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getImageNameToUri(Uri data) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgPath;
    }
}
