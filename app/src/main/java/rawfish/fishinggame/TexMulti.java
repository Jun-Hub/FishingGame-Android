package rawfish.fishinggame;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

// 텍스쳐 출력
public class TexMulti {
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

    private int[] mHandleBitmap;
    private int mBitmapCount = 0;
    private Bitmap mBitmap[];

    private float[] mVertices;
    // 이미지의 가로, 세로 설정 private float mWidth = 0;
    private int mWidth;
    private float mHeight = 0;

    private float[] mTranslationMatrix = new float[16];
    private float[] mScaleMatrix = new float[16];
    private float[] mRotationMatrix = new float[16];
    private float[] mMVPMatrix = new float[16];
    private float[] mMVPMatrix2 = new float[16];
    private float[] mMVPMatrix3 = new float[16];

    private float mPosX = 0;
    private float mPosY = 0;
    int mAngle = 0;
    float mScale = 1;

    // 생성자
    public TexMulti(int programImage) {
        mProgramImage = programImage;
        mPositionHandle = GLES20.glGetAttribLocation(mProgramImage, "vPosition");
        mTexCoordLoc = GLES20.glGetAttribLocation(mProgramImage, "a_texCoord");
        mtrxhandle = GLES20.glGetUniformLocation(mProgramImage, "uMVPMatrix");
        mSamplerLoc = GLES20.glGetUniformLocation(mProgramImage, "s_texture");

    }

    // 이미지핸들, 가로, 세로 값을 받아와 설정

    public void setBitmap(int[] handle, int width, int height) {
        mBitmapCount = handle.length;
        this.mWidth = width;
        this.mHeight = height;
        setupBuffer();
        mHandleBitmap = handle;
    }

    public float getPosX(){
        return this.mPosX;
    }
    public float getPosY(){
        return this.mPosY;
    }
    public void setPosX(float posX) {
        this.mPosX = posX;
    }
    public void setPosY(float posY) {
        this.mPosY = posY;
    }
    public void setAngle(int angle) {
        this.mAngle = angle;
    }
    public int getAngle() {
        return this.mAngle;
    }
    public void setScale(float scale) {
        this.mScale = scale;
    }
    public float getScale() {
        return this.mScale;
    }

    // 이미지 처리를 위한 버퍼를 설정함.
    public void setupBuffer(){
        mVertices = new float[] { mWidth/ (-2), mHeight/2, 0.0f,
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
    int mCount = 0;
    int mImageIndex = 0;

    public void draw(float[] m) {
        mCount++;
        if(mCount % 400 < 100){
            mImageIndex = 0;
        }
        else if(mCount % 400 < 200){
            mImageIndex = 1;
        }
        else if(mCount % 400 < 300){
            mImageIndex = 2;
        }
        else {
            mImageIndex = 3;
        }
        /*
        mPosX++;
        mPosY++;
        if(mPosX > 500){
            mPosX = -500;
            mPosY = -500;
        }
        mAngle++;
        if(mAngle == 360){
            mAngle = 0;
        }
        mScale = mScale + 0.01f;
        if(mScale > 1.5f){
            mScale = 0.5f;
        }
        */
        Matrix.setIdentityM(mRotationMatrix, 0);
        Matrix.setIdentityM(mScaleMatrix, 0);
        Matrix.setIdentityM(mTranslationMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix, 0);
        Matrix.setIdentityM(mMVPMatrix2, 0);
        Matrix.setIdentityM(mMVPMatrix3, 0);

        Matrix.translateM(mTranslationMatrix, 0, mPosX, mPosY, 0);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        Matrix.scaleM(mScaleMatrix, 0, mScale, mScale, 1.0f);


        Matrix.multiplyMM(mMVPMatrix, 0, m, 0, mTranslationMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix2, 0, mMVPMatrix, 0, mRotationMatrix, 0);
        Matrix.multiplyMM(mMVPMatrix3, 0, mMVPMatrix2, 0, mScaleMatrix, 0);

        //Matrix.setIdentityM(m, 0);
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 0,
                mVertexBuffer);
        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT, false, 0, mUvBuffer);
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, mMVPMatrix3, 0);

        // 투명한 배경을 처리한다.
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mHandleBitmap[mImageIndex]);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mIndices.length,
                GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);

    }
}
