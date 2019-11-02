package rawfish.fishinggame;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

// 텍스쳐 출력
public class WallPaper {
    // 기본적인 이미지 처리를 위한 변수 private static float mVertices[];
    private static short mIndices[];
    private static float mUvs[];
    private static int mProgramImage;
    private int mPositionHandle;

    private int mTexCoordLoc;
    private int mtrxhandle;
    private int mSamplerLoc;

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mUvBuffer;

    // 비트맵 이미지 핸들관리 (여러건 처리를 위해 배열로 정의)

    protected int[] mHandleBitmap;
    private int mBitmapCount = 0;
    private Bitmap mBitmap[];

    private float[] mVertices;
    // 이미지의 가로, 세로 설정 private float mWidth = 0;
    private int mWidth;
    private float mHeight = 0;

    // 여러개의 이미지 중 화면에 표시할 인덱스번호
    protected int mBitmapState = 0;

    // 현재의 위치정보
    protected float mPosX = 0;
    protected float mPosY = 0;

    // 이동하려는 위치정보
    protected float mTargetX;
    protected float mTargetY;

    //기억해야될 원래 위치정보(점수 효과전용)
    protected float oPosX = 0;
    protected float oPosY = 0;

    // 이미지의 확대, 축소 설정
    protected float mScaleX = 1.0f;
    protected float mScaleY = 1.0f;

    // 매트릭스변환을 위한 변수
    protected final float[] mMVPMatrix = new float[16];
    protected final float[] mMVPMatrix2 = new float[16];
    protected float[] mRotationMatrix = new float[16];
    protected float[] mScaleMatrix = new float[16];
    protected float[] mTranslationMatrix = new float[16];

    //현재 객체의 활성화 여부
    protected boolean mIsActive = false;

    //현재 객체가 점수획득 표시인지
    protected boolean isScorePlMa = false;

    // 생성자
    public WallPaper(int programImage) {
        mProgramImage = programImage;
        mPositionHandle = GLES20.glGetAttribLocation(mProgramImage, "vPosition");
        mTexCoordLoc = GLES20.glGetAttribLocation(mProgramImage, "a_texCoord");
        mtrxhandle = GLES20.glGetUniformLocation(mProgramImage, "uMVPMatrix");
        mSamplerLoc = GLES20.glGetUniformLocation(mProgramImage, "s_texture");
    }

    // 이미지핸들 배열, 가로,세로 값을 받아와 설정

    public void setBitmap(int handle[], int width, int height) {
        mBitmapCount = handle.length;
        this.mWidth = width;
        this.mHeight = height;
        setupBuffer();
        mHandleBitmap = new int[mBitmapCount];
        mHandleBitmap = handle;
        mBitmapState = 0;

    }

    // 이미지핸들, 가로, 세로 값을 받아와 설정
    public void setBitmap(int handle, int width, int height) {
        mBitmapCount = 1;
        this.mWidth = width;
        this.mHeight = height;
        setupBuffer();
        mHandleBitmap = new int[mBitmapCount];
        mHandleBitmap[0] = handle;
        mBitmapState = 0;
    }

    // 객체의 활성화여부를 설정함

    public void setIsActive(boolean isActive){
        this.mIsActive = isActive;
    }

    public void setIsScorePlMa(boolean isScorePlMa) {
        this.isScorePlMa = isScorePlMa;
    }

    // 위치정보를 설정함

    public void setPos(float posX, float posY) {
        this.mPosX = posX;
        this.mPosY = posY;
        this.mTargetX = posX;
        this.mTargetY = posY;
    }

    public void setOriginalPosInfo(float oPosX, float oPosY) {
        this.oPosX = oPosX;
        this.oPosY = oPosY;
    }

    public void setOriginalPos() {
        this.mPosX = oPosX;
        this.mPosY = oPosY;
        this.mTargetX = oPosX;
        this.mTargetY = oPosY;
    }

    // 이미지 처리를 위한 버퍼를 설정함.
    public void setupBuffer(){
        mVertices = new float[] {
                mWidth/ (-2), mHeight/2, 0.0f,
                mWidth/(-2), mHeight/ (-2), 0.0f,
                mWidth/2, mHeight/(-2), 0.0f,
                mWidth/2, mHeight /2, 0.0f,
        };

        mIndices = new short[] {0, 1, 2, 0, 2, 3};
        // The order of vertexrendering.
        ByteBuffer bb = ByteBuffer.allocateDirect(mVertices.length * 4);
        bb.order(ByteOrder.nativeOrder());

        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mVertices);
        mVertexBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(mIndices.length * 2);

        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(mIndices);
        mDrawListBuffer.position(0);

        mUvs = new float[] {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f,
                1.0f, 0.0f
        };

        ByteBuffer bbUvs = ByteBuffer.allocateDirect(mUvs.length * 4);
        bbUvs.order(ByteOrder.nativeOrder());
        mUvBuffer = bbUvs.asFloatBuffer();
        mUvBuffer.put(mUvs);
        mUvBuffer.position(0);
    }

    // 그리기

    void draw(float[] m) {

        if(!mIsActive) {
            return; //활성화 상태가 아니라면 그리지 않음
        }

        if(isScorePlMa) {
            mPosY += 1.5f;
        }

        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.translateM(mTranslationMatrix, 0, mPosX, mPosY, 0);
        Matrix.scaleM(mScaleMatrix, 0, this.mScaleX, this.mScaleY, 1.0f);
        Matrix.multiplyMM(mMVPMatrix, 0, m, 0, mTranslationMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix2, 0, mMVPMatrix, 0, mScaleMatrix, 0);

        //Matrix.setIdentityM(m, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mUvBuffer);
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mMVPMatrix, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHandleBitmap[0]);
        // 투명한 배경을 처리한다.
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        // 이미지 핸들을 바인드 한다. (추후 변경할 예정임)
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndices.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
    }
}