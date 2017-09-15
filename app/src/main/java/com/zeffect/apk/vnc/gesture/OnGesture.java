package com.zeffect.apk.vnc.gesture;

import android.view.MotionEvent;

/**
 * Created by Administrator on 2017/9/9.
 */

public abstract class OnGesture {
    /**
     * 上滑
     **/
    public void onTopUp() {
    }

    public void onTopUp(MotionEvent pEvent) {
    }

    /**
     * 下滑
     */
    public void onBottomUp() {
    }

    public void onBottomUp(MotionEvent pEvent) {

    }

    /***左滑**/
    public void onLeftUp() {
    }

    public void onLeftUp(MotionEvent pEvent) {
    }

    /**
     * 右滑
     **/
    public void onRightUp() {
    }

    public void onRightUp(MotionEvent pEvent) {
    }

    /**
     * 按下
     **/
    public void onDown() {
    }

    public void onDown(MotionEvent pEvent) {
    }


    /**
     * 抬起
     **/
    public void onUp() {
    }

    public void onUp(MotionEvent pEvent) {
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

    public void onSingleUp(MotionEvent pEvent) {
    }

    /**
     * 双击
     **/
    public void onDoubleUp() {
    }

    public void onDoubleUp(MotionEvent pEvent) {
    }

    /**
     * 长按
     */
    public void onLongClick() {
    }

    public void onLongClick(MotionEvent pEvent) {
    }

    //**********************双指手势区

    /***
     * 双指放大
     * @param pDouble 两次间隔
     */
    public void zoomBig(Double pDouble) {

    }

    /***
     * 缩小
     * @param pDouble 两次间隔
     * **/
    public void zoomSmall(Double pDouble) {

    }

    public void on2TopMove(Float pFloat) {
    }

    public void on2BottomMove(Float pFloat) {
    }

    public void on2LeftMove(Float pFloat) {
    }

    public void on2RightMove(Float pFloat) {
    }

}
