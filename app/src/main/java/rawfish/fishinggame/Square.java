package rawfish.fishinggame;

import android.opengl.GLES20;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import rawfish.fishinggame.GameGLRenderer;

public class Square {

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private final String mVertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String mFragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" + "void main() {" +
            "  gl_FragColor = vColor;" + "}";

    static final int COORDS_PER_VERTEX = 3;

    float mSquareCoords[] = {
            -0.5f,  0.5f, 0.0f,  // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, -0.5f, 0.0f,	// bottom right
            0.5f,  0.5f, 0.0f };
    // top right
    float mColor[] = { 0.6f, 0.68f, 0.7f, 1.0f };

    private short mDrawOrder[] = { 0, 1, 2, 0, 2, 3 };

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private final int mVertexCount = mSquareCoords.length / COORDS_PER_VERTEX;

    private final int mVertexStride = COORDS_PER_VERTEX * 4;
    GameGLRenderer GLRenderer;

    public Square(GameGLRenderer GLRenderer) {

        this.GLRenderer = GLRenderer;
        ByteBuffer bb = ByteBuffer.allocateDirect(
                mSquareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mSquareCoords);
        mVertexBuffer.position(0);
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                mDrawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(mDrawOrder);
        mDrawListBuffer.position(0);
        int vertexShader = GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw() {

        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, mVertexStride, mVertexBuffer);
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, mDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
