package rawfish.fishinggame;

import android.util.Log;

// 화면 설정 클래스
public class ScreenConfig {

    public int mDeviceWidth;
    public int mDeviceHeight;
    public int mVirtualWidth;
    public int mVirtualHeight;

    // 기본설정
    public ScreenConfig(int deviceWidth , int deviceHeight){
        this.mDeviceWidth = deviceWidth;
        this.mDeviceHeight = deviceHeight;
    }
    // 가상 폭 설정
    public void setSize(int width, int height){
        mVirtualWidth = width;
        mVirtualHeight = height;
    }
    // X 좌표 설정
    public int getX(int x){

        return (int)( x * mVirtualWidth/mDeviceWidth );
    }
    // Y좌표 설정
    public int getY(int y){
        //Log.e("", ">>" + y + "::" + mVirtualHeight + "::" + mDeviceHeight );
        //Log.e("", ">>" + (mVirtualHeight - (int)( y *  mVirtualHeight/mDeviceHeight)) );

        return  mVirtualHeight - (int)( y *  mVirtualHeight/mDeviceHeight);

    }
}


