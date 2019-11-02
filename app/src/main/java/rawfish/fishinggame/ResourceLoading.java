package rawfish.fishinggame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;

// 리소스 관리
public class ResourceLoading {

    private Context mContext;
    private Activity mActivity;

    // 유닛 객체
    private static int mHandleTex;
    public static int[] mHandleFishes = new int[9];
    public static int mHandleExp, mHandleBobDead, mHandlePlus1, mHandleMinus1, mHandleTp;
    private static int mHandleSpear, mHandleIceSpear, mHandleBob, mHandlePopup;
    public static int[] mHandleNum = new int[10];

    // 화면확대축소관리
    private float mScale = 0;

    // 리소스로딩 생성자
    public ResourceLoading(Activity activity, Context context, float scale){
        mActivity = activity;
        mContext = context;
        mScale = scale;
    }

    // 리소스 로딩
    public void loadingResource(WallPaper wallPaper, Tex[] fishes, Tex spongebob, Spear spear, Spear iceSpear, WallPaper popup){

        Bitmap bmpWallPaper = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/sea", null, mContext.getPackageName()));
        mHandleTex = getImageHandle(bmpWallPaper);
        bmpWallPaper.recycle();
        wallPaper.setBitmap(mHandleTex, 1500, 2400);

        // 버튼 1
        Bitmap bmpFish1 = BitmapFactory.decodeResource(
                mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/fish1", null, mContext.getPackageName()));
        mHandleFishes[0] = getImageHandle(bmpFish1);
        bmpFish1.recycle();
        fishes[0].setBitmap(mHandleFishes[0], 200, 125);
        //unit.setBitmap(mHandleUnit,150,300);
        // 버튼 2
        Bitmap bmpFish2 = BitmapFactory.decodeResource(
                mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/fish2", null, mContext.getPackageName()));
        mHandleFishes[1] = getImageHandle(bmpFish2);
        bmpFish2.recycle();
        fishes[1].setBitmap(mHandleFishes[1], 186, 124);
        //unit.setBitmap(mHandleUnit,150,300);

        // 버튼 3
        Bitmap bmpFish3 = BitmapFactory.decodeResource(
                mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/fish3", null, mContext.getPackageName()));
        mHandleFishes[2] = getImageHandle(bmpFish3);
        bmpFish3.recycle();
        fishes[2].setBitmap(mHandleFishes[2], 600, 213);
        //unit.setBitmap(mHandleUnit,150,300);
        // 버튼 4
        Bitmap bmpFish4 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/fish4", null, mContext.getPackageName()));
        mHandleFishes[3] = getImageHandle(bmpFish4);
        bmpFish4.recycle();
        fishes[3].setBitmap(mHandleFishes[3], 153, 122);
        //unit.setBitmap(mHandleUnit,150,300);

        Bitmap bmpFish5 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/fish5", null, mContext.getPackageName()));
        mHandleFishes[4] = getImageHandle(bmpFish5);
        bmpFish5.recycle();
        fishes[4].setBitmap(mHandleFishes[4], 226, 84);
        //unit.setBitmap(mHandleUnit,150,300);

        Bitmap bmpFish6 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/crab", null, mContext.getPackageName()));
        mHandleFishes[5] = getImageHandle(bmpFish6);
        bmpFish6.recycle();
        fishes[5].setBitmap(mHandleFishes[5], 115, 115);
        //unit.setBitmap(mHandleUnit,150,300);

        Bitmap bmpFish7 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/sea_horse", null, mContext.getPackageName()));
        mHandleFishes[6] = getImageHandle(bmpFish7);
        bmpFish7.recycle();
        fishes[6].setBitmap(mHandleFishes[6], 60, 130);
        //unit.setBitmap(mHandleUnit,150,300);

        Bitmap bmpFish8 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/shark", null, mContext.getPackageName()));
        mHandleFishes[7] = getImageHandle(bmpFish8);
        bmpFish8.recycle();
        fishes[7].setBitmap(mHandleFishes[7], 450, 225);
        //unit.setBitmap(mHandleUnit,150,300);

        Bitmap bmpFish9 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/turtle", null, mContext.getPackageName()));
        mHandleFishes[8] = getImageHandle(bmpFish9);
        bmpFish9.recycle();
        fishes[8].setBitmap(mHandleFishes[8], 186, 91);
        //unit.setBitmap(mHandleUnit,150,300);

        Bitmap bmpBob = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/spongebob", null, mContext.getPackageName()));
        mHandleBob = getImageHandle(bmpBob);
        bmpBob.recycle();
        spongebob.setBitmap(mHandleBob, 173, 162);
        //unit.setBitmap(mHandleUnit,150,300);

        //물고기 죽음 효과 비트맵
        Bitmap bmpExplosion = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/explosion_small", null, mContext.getPackageName()));
        mHandleExp = getImageHandle(bmpExplosion);
        bmpExplosion.recycle();

        //스폰지밥 뒤지는 비트맵
        Bitmap bmpBobDead = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/spongebob2", null, mContext.getPackageName()));
        mHandleBobDead = getImageHandle(bmpBobDead);
        bmpBobDead.recycle();

        Bitmap bmpSpear = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/iron_spear", null, mContext.getPackageName()));
        mHandleSpear = getImageHandle(bmpSpear);
        bmpSpear.recycle();
        spear.setBitmap(mHandleSpear, 80, 400);

        Bitmap bmpIceSpear = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/ice_spear", null, mContext.getPackageName()));
        mHandleIceSpear = getImageHandle(bmpIceSpear);
        bmpIceSpear.recycle();
        iceSpear.setBitmap(mHandleIceSpear, 80, 400);

        Bitmap bmpPopup = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/popup", null, mContext.getPackageName()));
        mHandlePopup = getImageHandle(bmpPopup);
        bmpPopup.recycle();
        popup.setBitmap(mHandlePopup, 890, 550);  //홤금비! 1:1.618

        //점수용 숫자
        Bitmap bmp0 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num0", null, mContext.getPackageName()));
        mHandleNum[0] = getImageHandle(bmp0);
        bmp0.recycle();

        Bitmap bmp1 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num1", null, mContext.getPackageName()));
        mHandleNum[1] = getImageHandle(bmp1);
        bmp1.recycle();

        Bitmap bmp2 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num2", null, mContext.getPackageName()));
        mHandleNum[2] = getImageHandle(bmp2);
        bmp2.recycle();

        Bitmap bmp3 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num3", null, mContext.getPackageName()));
        mHandleNum[3] = getImageHandle(bmp3);
        bmp3.recycle();

        Bitmap bmp4 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num4", null, mContext.getPackageName()));
        mHandleNum[4] = getImageHandle(bmp4);
        bmp4.recycle();

        Bitmap bmp5 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num5", null, mContext.getPackageName()));
        mHandleNum[5] = getImageHandle(bmp5);
        bmp5.recycle();

        Bitmap bmp6 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num6", null, mContext.getPackageName()));
        mHandleNum[6] = getImageHandle(bmp6);
        bmp6.recycle();

        Bitmap bmp7 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num7", null, mContext.getPackageName()));
        mHandleNum[7] = getImageHandle(bmp7);
        bmp7.recycle();

        Bitmap bmp8 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num8", null, mContext.getPackageName()));
        mHandleNum[8] = getImageHandle(bmp8);
        bmp8.recycle();

        Bitmap bmp9 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/num9", null, mContext.getPackageName()));
        mHandleNum[9] = getImageHandle(bmp9);
        bmp9.recycle();

        Bitmap bmpPlus1 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/plus1", null, mContext.getPackageName()));
        mHandlePlus1 = getImageHandle(bmpPlus1);
        bmpPlus1.recycle();

        Bitmap bmpMinus1 = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/minus1", null, mContext.getPackageName()));
        mHandleMinus1 = getImageHandle(bmpMinus1);
        bmpMinus1.recycle();

        /*Bitmap bmpTransparency = BitmapFactory.decodeResource(mContext.getResources(),
                mContext.getResources().getIdentifier("drawable/transparency", null, mContext.getPackageName()));
        mHandleTp = getImageHandle(bmpTransparency);
        bmpTransparency.recycle();*/
    }

    // 이미지 핸들 반환
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
}
