package rawfish.fishinggame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

// 랜더링
public class GameGLRenderer implements Renderer {
    // 매트릭스
    private final float[] mMtrxProjection = new float[16];
    private final float[] mMtrxView = new float[16];
    private final float[] mMtrxProjectionAndView = new float[16];
    // 프로그램
    private static int mProgramImage;
    // 프로그램
    long mLastTime;
    // 디바이스의 넓이, 높이
    private static int mDeviceWidth = 0;
    private static int mDeviceHeight = 0;
    // 주 액티비티
    private GameActivity mActivity;
    private Context mContext;

    private ScreenConfig screenConfig;

    private WallPaper wallPaper, popup, maNumber, uNumber, maGetScore, uGetScore;
    private Text searchText;
    private Tex[] fishes = new Tex[9];
    private Tex spongebob;
    private Spear spear, iceSpear, mySpear, enemySpear;

    private ResourceLoading resourceLoading;

    static boolean start = false;  //상대방이랑 매칭됬는지 여부
    public boolean end = false; //게임이 끝났는지 여부
    private boolean spearSet = false;   //내 창 종류가 정해졌는지 여부
    //static PrintWriter printWriter; //서버로 보낼 writer
    static int mySpearNum; //내 spear종류
    private int deadFishes = 0; //죽은 생선 종류
    private int myScore, enemyScore = 0;

    //사운드 관련
    private SoundPool soundPool;
    private int finishedGame, scream, spearThrow;
    private MediaPlayer bgm;

    static PrintWriter printWriter;

    //채팅메시지
    static String chatMessage = null;

    // 생성자
    public GameGLRenderer(GameActivity activity, int width, int height) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mLastTime = System.currentTimeMillis() + 100;
        mDeviceWidth = width;
        mDeviceHeight = height;
    }
    // 멈춤
    public void onPause() {
    }
    // 재시작
    public void onResume() {
        mLastTime = System.currentTimeMillis();
    }
    // 서피스뷰 변경
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, (int) mDeviceWidth, (int) mDeviceHeight);
        Matrix.setIdentityM(mMtrxProjection, 0);
        Matrix.setIdentityM(mMtrxView, 0);
        Matrix.setIdentityM(mMtrxProjectionAndView, 0);
        Matrix.orthoM(mMtrxProjection, 0, 0, 1000, 0, 1600, 0, 50);
        Matrix.setLookAtM(mMtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        Matrix.multiplyMM(mMtrxProjectionAndView, 0, mMtrxProjection, 0, mMtrxView, 0);
    }

    // 서피스뷰 생성
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        GLES20.glClearColor(0.9f, 0.97f, 0.99f, 1);

        screenConfig = new ScreenConfig(mDeviceWidth,mDeviceHeight);
        screenConfig.setSize(1000, 1600);   //터치이벤트를 받을 객체

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vs_Image);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fs_Image);
        mProgramImage = GLES20.glCreateProgram();

        GLES20.glAttachShader(mProgramImage, vertexShader);
        GLES20.glAttachShader(mProgramImage, fragmentShader);
        GLES20.glLinkProgram(mProgramImage);
        GLES20.glUseProgram(mProgramImage);

        float scale = mContext.getResources().getDisplayMetrics().density;
        resourceLoading = new ResourceLoading(mActivity, mContext, scale);

        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        // 각각의 재생하고자하는 음악을 미리 준비한다
        finishedGame = soundPool.load(mActivity, R.raw.finish_game, 1);
        scream = soundPool.load(mActivity, R.raw.scream, 1);
        spearThrow = soundPool.load(mActivity, R.raw.spear_throw, 1);

        bgm = MediaPlayer.create(mActivity, R.raw.first_run);
        bgm.setVolume(0.2f, 0.2f);

        HangulBitmap hangulBitmap = new HangulBitmap(mActivity);
        String searchStr = "상대방을  검색중입니다...";
        Bitmap  tempBmp = Bitmap.createBitmap((int) (48 * 6), (int) (48), Bitmap.Config.ARGB_8888);
        hangulBitmap.GetBitmap(tempBmp, searchStr, 38, Color.BLACK, -1, 1);
        int imageHandle = getImageHandle(tempBmp);
        searchText = new Text(mProgramImage);
        searchText.setBitmap(imageHandle, 900, 150);
        searchText.setPos(550, 800);
        searchText.setMoving(true);  //텍스트 움직이게하기

        wallPaper = new WallPaper( mProgramImage );
        for(int i=0; i< 9; i++){
            fishes[i] = new Tex(mProgramImage);
        }
        spongebob = new Tex(mProgramImage);
        spear = new Spear(mProgramImage);
        iceSpear = new Spear(mProgramImage);
        popup = new WallPaper(mProgramImage);
        maNumber = new WallPaper(mProgramImage);
        uNumber = new WallPaper(mProgramImage);
        maGetScore = new WallPaper(mProgramImage);
        uGetScore = new WallPaper(mProgramImage);

        maGetScore.setIsScorePlMa(true);
        uGetScore.setIsScorePlMa(true);

        NetworkThread networkThread = new NetworkThread();
        networkThread.start();

        resourceLoading.loadingResource(wallPaper, fishes, spongebob, spear, iceSpear, popup);
        wallPaper.setPos(500, 800);
        wallPaper.setIsActive(true);

        fishes[0].setPos(100, 700);
        fishes[1].setPos(200, 800);
        fishes[2].setPos(300, 900);
        fishes[3].setPos(400, 1000);
        fishes[4].setPos(150, 1100);
        fishes[5].setPos(600, 1200);
        fishes[6].setPos(700, 1300);
        fishes[7].setPos(800, 1400);
        fishes[8].setPos(900, 1500);
        spongebob.setPos(1000, 1600);

        //5~10 / 3~8 / 800 ~ 1200
        fishes[0].setSpeed(6, 3, 850);
        fishes[1].setSpeed(8, 4, 800);
        fishes[2].setSpeed(-4, 2, 1000);
        fishes[3].setSpeed(-8, -5, 1000);
        fishes[4].setSpeed(-3, 2, 1200);
        fishes[5].setSpeed(8, 0, 850);
        fishes[6].setSpeed(1, 4, 800);
        fishes[7].setSpeed(-9, -4, 1550);
        fishes[8].setSpeed(2, -1, 1300);
        spongebob.setSpeed(-14, 0, 850);

        popup.setPos(500, 800);
        maNumber.setPos(750, 670);
        uNumber.setPos(750, 600);
    }

    // 쉐이더 이미지
    public static final String vs_Image =
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec2 a_texCoord;" +
            "varying vec2 v_texCoord;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  v_texCoord = a_texCoord;" + "}";

    public static final String fs_Image =
            "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
            "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
            "}";
    // 쉐이더 로딩
    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
    // 그리기 호출
    @Override
    public void onDrawFrame(GL10 unused) {

        long now = System.currentTimeMillis();

        if (mLastTime > now)
            return;

        long elapsed = now - mLastTime;
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(1.0f, 0.607f, 0.039f, 1);

        // 전체화면
        GLES20.glViewport(0, 0, (int) mDeviceWidth, (int) mDeviceHeight);
        Matrix.orthoM(mMtrxProjection, 0, 0, 1000, 0, 1600, 0, 50);
        Matrix.multiplyMM(mMtrxProjectionAndView, 0, mMtrxProjection, 0, mMtrxView, 0);
        GLES20.glClearColor(1.0f, 0.607f, 0.039f, 1);

        if(start) {
            RenderGame(mMtrxProjectionAndView);
            //내위치 서버로 전송
            if(spearSet) {    //내 창 종류가 정해졌다면
                collision();    //충돌 메소드

                printWriter.println("[SPEAR_X]" + mySpear.getPosX());
                printWriter.println("[SPEAR_Y]" + mySpear.getPosY());
                printWriter.flush();
            }
        } else if(end) {
            wallPaper.draw(mMtrxProjectionAndView);
            popup.draw(mMtrxProjectionAndView);
            maNumber.draw(mMtrxProjectionAndView);
            uNumber.draw(mMtrxProjectionAndView);
        } else{
            searchText.draw(mMtrxProjectionAndView);
        }

        mLastTime = now;
    }

    private int mPointerID;

    public boolean onTouchEvent(MotionEvent event){

        int x = (int)event.getX();
        int y = (int)event.getY();
        int chgX = screenConfig.getX(x);
        int chgY = screenConfig.getY(y);
        int action = event.getAction();

        switch(action & MotionEvent.ACTION_MASK){

            case MotionEvent.ACTION_UP:{
                if((chgX >= -100 && chgX <= 1000) && (chgY >= 0 && chgY <= 300)) {
                    mySpear.setFire(true);    //spear 발사!
                    soundPool.play(spearThrow, 1, 1, 0, 0, 1);
                }
            }
            case MotionEvent.ACTION_DOWN:{
                if(((chgX >= -100 && chgX <= 1000) && (chgY >= 0 && chgY <= 300)) && !mySpear.isFire) {
                    mySpear.setPos(chgX, chgY);
                }

            }
            case MotionEvent.ACTION_MOVE:{
                if(((chgX >= -100 && chgX <= 1000) && (chgY >= 0 && chgY <= 300)) && !mySpear.isFire) {
                    mySpear.setPos(chgX, chgY);
                }
            }
        }
        return true;
    }

    private void selectTouch(int x, int y){

        if (mySpear.isSelected(x, y)) {   //spear가 터치됬다면
        }
    }

    private void collision() {  //객체간 충돌

        for(int i=0; i<9; i++) {

            if (fishes[i] != null) { //물고기들이 죽어서 삭제되지 않았다면

                if (mySpear.isCollided(fishes[i].mPosX, fishes[i].mPosY)) {   //spear에 물고기가 닿았다면
                    if (mySpearNum == 1) { //내 창이 아이스라면
                        mySpear.setPos(850, 150);
                    } else if (mySpearNum == 2) {  // 내 창이 아이언이라면
                        mySpear.setPos(150, 150);
                    }
                    mySpear.setFire(false);
                }

                if (fishes[i].isCollided(mySpear.mPosX, mySpear.mPosY + 300)) {    //fish에 spear가 닿앗다면

                    //서버로 물고기 맞춘 패킷 보내기
                    printWriter.println("[COLLISION]" + i);
                    printWriter.flush();

                    Log.e("[COLLISION]을 보냇다!!!!", "[COLLISION]" + i);

                    fishes[i].setBitmap(ResourceLoading.mHandleExp, 135, 135);

                    if (mySpearNum == 1) {
                        mySpear.setPos(850, 150);
                    } else if (mySpearNum == 2) {
                        mySpear.setPos(150, 150);
                    }
                    mySpear.setFire(false);

                    soundPool.play(scream, 1, 1, 0, 0, 1);

                    ExplosionThread explosionThread = new ExplosionThread(i);
                    explosionThread.start();

                    if(i==6 || i==8) {

                        GetScoreThread getScoreThread = new GetScoreThread(maGetScore, -1);
                        getScoreThread.start(); //1점 마이너스 되는 효과 스레드
                        //1점 마이너스 효과는 일어나지만,
                        if(myScore>0)
                        myScore -= 1;   //실제 점수는 0점 밑으로 떨어지지 않음
                    }
                    else {
                        myScore += 1;

                        GetScoreThread getScoreThread = new GetScoreThread(maGetScore, 1);
                        getScoreThread.start(); //1점 플러스되는 효과 스레드
                    }
                }
            }

            if (spongebob != null) {
                if (spongebob.isCollided(mySpear.mPosX, mySpear.mPosY + 300)) {
                    //서버로 스폰지밥 맞춘 패킷 보내기
                    printWriter.println("[COLLISION]" + 9);
                    printWriter.flush();

                    Log.e("[COLLISION]을 보냇다!!!!", "[COLLISION]" + 9);

                    spongebob.setBitmap(ResourceLoading.mHandleBobDead, 173, 237);

                    if (mySpearNum == 1) {
                        mySpear.setPos(850, 150);
                    } else if (mySpearNum == 2) {
                        mySpear.setPos(150, 150);
                    }
                    mySpear.setFire(false);

                    soundPool.play(scream, 1, 1, 0, 0, 1);

                    ExplosionThread explosionThread = new ExplosionThread(10);
                    explosionThread.start();

                    GetScoreThread getScoreThread = new GetScoreThread(maGetScore, -1);
                    getScoreThread.start(); //1점 마이너스 되는 효과 스레드
                    //1점 마이너스 효과는 일어나지만,

                    if(myScore>0)
                    myScore -= 1;   //실제 점수는 0점 밑으로 떨어지지 않음
                }
            }
        }
    }

    // 이미지 핸들
    private int getImageHandle(Bitmap bitmap){

        int[] texturenames = new int[1];

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glGenTextures(1, texturenames, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texturenames[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        return texturenames[0];
    }

    // 그리기 시작
    private void RenderGame(float[] m) {

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1);
        //Matrix.orthoM(mMtrxProjection, 0, 0, mDeviceWidth, 0, mDeviceHeight, 0, 50);
        Matrix.orthoM(mMtrxProjection, 0, 0, 1000, 0, 1600, 0, 50);
        Matrix.multiplyMM(mMtrxProjectionAndView, 0, mMtrxProjection, 0, mMtrxView, 0);
        wallPaper.draw(mMtrxProjectionAndView);

        for(int i=0; i<9; i++) {
            if(fishes[i] != null) //활성화됬다면 그리기
                fishes[i].draw(mMtrxProjectionAndView);
        }

        if(spongebob != null) {
            spongebob.draw(mMtrxProjectionAndView);
        }
        spear.draw(mMtrxProjectionAndView);
        iceSpear.draw(mMtrxProjectionAndView);
        maGetScore.draw(mMtrxProjectionAndView);
        uGetScore.draw(mMtrxProjectionAndView);
    }


    private class NetworkThread extends Thread {

        String response = null;		//서버로부터 받을 응답
        int i = 0;

        private NetworkThread() {
        }

        public void run() {
            try {
                Socket socket = new Socket("172.30.1.2", 9000);

                // 2. 데이타 송수신을 위한 i/o stream을 얻어야 한다.
                InputStream is = socket.getInputStream(); // 수신 --> read();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                OutputStream os = socket.getOutputStream(); // 송신 --> write();
                PrintWriter pw = new PrintWriter(os);

                Log.e("서버에 보낼 아디 : ", SearchActivity.id);

                pw.println(SearchActivity.id);    //서버로 자기 닉네임 전달
                pw.flush();

                printWriter = pw;

                while (true) {

                    response = br.readLine();

                        if(response.startsWith("[START]")) {
                            start = true;
                            bgm.start();

                            //대기실서버에 알려줘서 대기실사람리스트 업뎃하게 하기
                            SearchActivity.pw.println(response);
                            SearchActivity.pw.flush();
                        }
                        if (response.startsWith("[SPEAR]")) {
                            String spearNum = response.substring(7);
                            mySpearNum = Integer.parseInt(spearNum);

                            if (mySpearNum == 1) {
                                mySpear = iceSpear;
                                enemySpear = spear;

                                mySpear.setPos(850, 150);
                                enemySpear.setPos(150, 150);
                                maGetScore.setPos(920, 150);
                                uGetScore.setPos(80, 150);
                                maGetScore.setOriginalPosInfo(920, 150);
                                uGetScore.setOriginalPosInfo(80, 150);
                            } else if (mySpearNum == 2) {
                                mySpear = spear;
                                enemySpear = iceSpear;

                                mySpear.setPos(150, 150);
                                enemySpear.setPos(850, 150);
                                maGetScore.setPos(80, 150);
                                uGetScore.setPos(920, 150);
                                maGetScore.setOriginalPosInfo(80, 150);
                                uGetScore.setOriginalPosInfo(920, 150);
                            }

                            spearSet = true;

                        } else if (response.startsWith("[FISH]") && i < 10) {

                            //Log.e("[FISH][FISH][FISH]", "" + response);

                            if (i == 9) {
                                spongebob.setIsActive(true);
                            } else {
                                fishes[i].setIsActive(true);
                            }
                            i++;

                        } else if (response.startsWith("[SPEAR_X]")) {
                            String spearX = response.substring(9);
                            int spearXtoInt = Integer.parseInt(spearX);
                            enemySpear.setPosX(spearXtoInt);
                        } else if (response.startsWith("[SPEAR_Y]")) {
                            String spearY = response.substring(9);
                            int spearYtoInt = Integer.parseInt(spearY);
                            enemySpear.setPosY(spearYtoInt);
                        } else if (response.startsWith("[CHAT]")) {
                            Log.e("354653", "" + response);
                            chatMessage = response.substring(6);
                            mHandler.sendEmptyMessage(0);
                        } else if (response.startsWith("[COLLISION]")) {
                            String colliedUnit = response.substring(11);
                            int colliedUnitNum = Integer.parseInt(colliedUnit);

                            if (colliedUnitNum == 9) {
                                spongebob.setBitmap(ResourceLoading.mHandleBobDead, 173, 237);

                                if (mySpearNum == 1) {
                                    mySpear.setPos(850, 150);
                                } else if (mySpearNum == 2) {
                                    mySpear.setPos(150, 150);
                                }
                                mySpear.setFire(false);

                                soundPool.play(scream, 1, 1, 0, 0, 1);

                                ExplosionThread explosionThread = new ExplosionThread(10);
                                explosionThread.start();

                                GetScoreThread getScoreThread = new GetScoreThread(uGetScore, -1);
                                getScoreThread.start(); //상대방 점수 -1
                            } else {
                                fishes[colliedUnitNum].setBitmap(ResourceLoading.mHandleExp, 135, 135);

                                if (mySpearNum == 1) {
                                    mySpear.setPos(850, 150);
                                } else if (mySpearNum == 2) {
                                    mySpear.setPos(150, 150);
                                }
                                mySpear.setFire(false);

                                soundPool.play(scream, 1, 1, 0, 0, 1);

                                ExplosionThread explosionThread = new ExplosionThread(colliedUnitNum);
                                explosionThread.start();

                                if(colliedUnitNum==6 || colliedUnitNum==8) {
                                    GetScoreThread getScoreThread = new GetScoreThread(uGetScore, -1);
                                    getScoreThread.start(); //상대방 점수 -1
                                } else {
                                    GetScoreThread getScoreThread = new GetScoreThread(uGetScore, 1);
                                    getScoreThread.start(); //상대방 점수 +1
                                }
                            }
                        } else if (response.startsWith("[GAMEOVER]")) {
                            printWriter.println("[GAMEOVER]");
                            printWriter.flush();
                            SearchActivity.pw.println("[GAMEOVER]");
                            SearchActivity.pw.flush();

                            String enemySco = response.substring(10);
                            int enemyScoretoInt = Integer.parseInt(enemySco);
                            enemyScore = enemyScoretoInt;

                            Log.e("d" + myScore, "as" + enemyScore);

                            maNumber.setBitmap(ResourceLoading.mHandleNum[myScore], 44, 60);
                            uNumber.setBitmap(ResourceLoading.mHandleNum[enemyScore], 44, 60);
                            maNumber.setIsActive(true);
                            uNumber.setIsActive(true);

                            end = true;
                            start = false;

                            bgm.stop();
                            soundPool.play(finishedGame, 1, 1, 0, 0, 1);

                            break;
                        }

                }
                //게임이 끝나면
                gameoverHandler.sendEmptyMessage(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.interrupt();
        }
    }

    private class ExplosionThread extends Thread {

        int i;

        private ExplosionThread(int i) {
            this.i = i;
        }

        public void run() {
            try {
                Thread.sleep(500);
                if(i==10) {
                    spongebob.setIsActive(false);
                    //spongebob.setIsNull(true);
                    spongebob=null;
                }else {
                    fishes[i].setIsActive(false);
                    //fishes[i].setIsNull(true);
                    fishes[i]=null;
                }
            }
            catch (InterruptedException e) {e.printStackTrace();}

            deadFishes += 1;

            //몬스터 다 잡았는지 검사
            if(deadFishes == 10) {
                popup.setIsActive(true);
                printWriter.println("[GAMEOVER]");
                printWriter.flush();
                /*SearchActivity.pw.println("[GAMEOVER]");
                SearchActivity.pw.flush();*/
            }
            this.interrupt();
        }
    }

    //점수 획득 표시 쓰레드
    private class GetScoreThread extends Thread {

        WallPaper whoGetScore;
        int score;

        private GetScoreThread(WallPaper whoGetScore, int score) {
            this.whoGetScore = whoGetScore;
            this.score = score;
        }

        public void run() {

            whoGetScore.setIsActive(true);


            if(score==1) {
                whoGetScore.setBitmap(ResourceLoading.mHandlePlus1, 100, 118);
            } else if(score==-1) {
                whoGetScore.setBitmap(ResourceLoading.mHandleMinus1, 100, 127);
            }

            try{Thread.sleep(2000);}
            catch (InterruptedException e) {e.printStackTrace();}

            whoGetScore.setOriginalPos();   //다시 아래로 원위치
            whoGetScore.setIsActive(false);

            this.interrupt();
        }
    }

    // 핸들러 객체 만들기
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            GameActivity.waitingChatAdapter.add(new ChatMessage(true, false, chatMessage));
        }
    };

    // 핸들러 객체 만들기
    private Handler gameoverHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            GameActivity.listView.setVisibility(View.GONE);
            GameActivity.chatButton.setVisibility(View.GONE);
        }
    };
}