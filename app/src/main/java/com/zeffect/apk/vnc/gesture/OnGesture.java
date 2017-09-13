package com.zeffect.apk.vnc.gesture;

import android.view.MotionEvent;

/**
 * Created by Administrator on 2017/9/9.
 */

public abstract class OnGesture {
    /**
     * 上滑
     **/
    public void onTop() {
    }

    /**
     * 下滑
     */
    public void onBottom() {
    }

    /***左滑**/
    public void onLeft() {
    }

    /**
     * 右滑
     **/
    public void onRight() {
    }

    /**
     * 按下
     **/
    public void onDown() {
    }

    /**
     * 抬起
     **/
    public void onUp() {
    }

    /**
     * 移动
     **/
    public void onMove(MotionEvent event) {
    }

    /**
     * 单击
     **/
    public void onSingleUp() {
    }

    /**
     * 双击
     **/
    public void onDoubleUp() {
    }

    /**
     * 按下
     */
    public void onPressed() {
    }

    /**
     * 长按
     */
    public void onLong() {
    }

    /***
     * 双指放大
     */
    public void zoomBig(Double pDouble) {

    }

    /***
     * 缩小
     * **/
    public void zoomSmall(Double pDouble) {

    }

}
