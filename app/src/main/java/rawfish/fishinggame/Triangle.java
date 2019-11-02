package rawfish.fishinggame;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import rawfish.fishinggame.GameGLRenderer;

public class Triangle {

    private GameGLRenderer GLRenderer;
    // 정점 버퍼를 설정함
    private FloatBuffer vertexBuffer;

    // 꼭지점당 좌표수는 x,y,z 3개임
    private static final int COORDS_PER_VERTEX = 3;

    // 삼각형의 좌표로 x,y,z축 3개의 꼭지점을 설정함.

    private static float triangleCoords[] = {  // in counterclockwise order:
            0.0f,  0.5f, 0.0f, // top
            -0.5f, -0.5f, 0.0f, // bottom left
            0.5f, -0.5f, 0.0f  // bottom right
    };

    // 버텍스를 표현할 포지션 프로그램코드
    private final String vertexShaderCode = "attribute vec4 vPosition;" +
            "void main() {" +
            "  gl_Position = vPosition;" +
            "}";

    // 색상을 관리
    private float color[] = { 1.0f, 1.0f, 0f, 1.0f };

    // 색상을 표현할 프로그램 코드
    private final String fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private final int mProgram;

    // 포지션 핸들
    private int mPositionHandle;

    // 색상 핸들
    private int mColorHandle;

    // 꼭지점 객수는 전체길이에서 꼭지점의 좌표수로 나눈값임
    private final int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;

    // 꼭지점좌표수 * 4 = 버텍스Stride
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    // 4 bytes per vertex private MainGLRenderer mGLRenderer;

    public Triangle( GameGLRenderer GLRenderer) {
        this.GLRenderer = GLRenderer;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                triangleCoords.length * 4);
        // 9 * 4 = 36

        // use the device hardware’s native byte order
        bb.order(ByteOrder.nativeOrder());

        // create a floating point buffer from the ByteBuffer
        vertexBuffer = bb.asFloatBuffer();

        // add the coordinates to the FloatBuffer
        vertexBuffer.put(triangleCoords);

        // set the buffer to read the first coordinate
        vertexBuffer.position(0);

        // 모양을 나타낼 쉐이더 코드를 로당함
        int vertexShader = GLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);

        // 색상을 나타낼 프래그먼트 쉐이더 코드를 로딩함
        int fragmentShader = GLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // OpenGL ES Program을 생성함
        mProgram = GLES20.glCreateProgram();

        // 쉐이더 코드를 추가함
        GLES20.glAttachShader(mProgram, vertexShader);

        // 프레그먼트 코드를 추가함
        GLES20.glAttachShader(mProgram, fragmentShader);

        // 조합된 OpenGL ES program을 생성함
        GLES20.glLinkProgram(mProgram);

    }
    public void draw() {
        // OpenGL ES 환경하의 프로그램을 사용함
        GLES20.glUseProgram(mProgram);

        // 프로그램으로부터 위치핸들을 얻어옴
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // 삼각형 꼭지점으로 사용할수 있도록 핸들을 사용가능하게 함
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // 삼각형 꼭지점 데이터를 준비함
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // 색상 핸들얼 얻어옴
        // get handle to fragment shader’s vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // 삼각형에 색상을 설정함
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // 삼각형을 그림
        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // 버텍스 배열 사용을 종료함
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}