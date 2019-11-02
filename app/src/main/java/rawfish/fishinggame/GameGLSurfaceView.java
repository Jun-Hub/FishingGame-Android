package rawfish.fishinggame;

import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GameGLSurfaceView extends GLSurfaceView {

    private final GameGLRenderer Renderer;

    public GameGLSurfaceView(GameActivity activity, int width, int height) {
        super(activity.getApplicationContext());
        setEGLContextClientVersion(2);  //OpenGL ES 버전 정의

        Renderer = new GameGLRenderer(activity, width, height);
        setRenderer(Renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);   //계속 그린다
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        Renderer.onTouchEvent(event);

        return true;
    }
}

