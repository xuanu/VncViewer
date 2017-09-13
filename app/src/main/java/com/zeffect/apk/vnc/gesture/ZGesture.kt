package com.qimon.commonlibrary.gesture

import android.androidVNC.VncCanvasActivity
import android.content.Context
import android.util.Log
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.zeffect.apk.vnc.gesture.OnGesture
import com.zeffect.apk.vnc.gesture.WeakHandler

/**
 * Created by Administrator on 2017/9/8.
 * 对手势事件进行封装，有上下左右，单击，双击，长按，move,pressed,up
 *
 * 如果发现onFling没有回调，请设置View.setLongClickable(true)
 */
class ZGesture(context: Context, gesture: OnGesture?) {
    private val TAG = ZGesture::class.java.name
    private val mGestureInterface by lazy { gesture }
    private val mContext by lazy { context }
    /**
     * 通过这个接口，得到触摸事件
     * */
    fun onTouchEvent(event: MotionEvent): Boolean {
        return doGesture(event)
    }

    private var mPoints = Points(0f, 0f)
    private val mDoubleRun by lazy { DoubleRunnable(mGestureInterface) }
    private val mLongPrRun by lazy { LongPreRunnable(mGestureInterface, mDoubleRun, this) }
    /**是否取消一个手指的后续事件**/
    private var cancelOneFingerEvent = false
    /**移动的最后一个点**/
    private var mMoveEndPoints = Points(0f, 0f)

    private fun doGesture(event: MotionEvent): Boolean {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                cancelOneFingerEvent = false
                mPoints.x1 = event.x
                mPoints.y1 = event.y
                mMoveEndPoints.x1 = event.x
                mMoveEndPoints.y1 = event.y
                mGestureInterface?.onDown()
                mLongPrRun.cancel(true)
                WeakHandler(mContext.mainLooper).removeCallbacks(mLongPrRun)
                val doubleCount = mDoubleRun.getCount()
                if (doubleCount >= 2) {
                    mDoubleRun.clearCount()
                    mDoubleRun.isLong(false)
                    mDoubleRun.cancel(true)
                    WeakHandler(mContext.mainLooper).removeCallbacks(mDoubleRun)
                }
                if (doubleCount == 0) {
                    mDoubleRun.cancel(false)
                    mDoubleRun.isLong(false)
                    WeakHandler(mContext.mainLooper).postDelayed(mDoubleRun, ViewConfiguration.getDoubleTapTimeout().toLong())
                }
                mLongPrRun.cancel(false)
                WeakHandler(mContext.mainLooper).postDelayed(mLongPrRun, ViewConfiguration.getLongPressTimeout().toLong())
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mDoubleRun.cancel(true)
                WeakHandler(mContext.mainLooper).removeCallbacks(mDoubleRun)
                mLongPrRun.cancel(true)
                WeakHandler(mContext.mainLooper).removeCallbacks(mLongPrRun)
                cancelOneFingerEvent = true
                //存下第二个点的位置
                mPoints.x2 = event.getX(1)
                mPoints.y2 = event.getY(1)
                mMoveEndPoints.x2 = event.getX(1)
                mMoveEndPoints.y2 = event.getY(1)
            }
            MotionEvent.ACTION_MOVE -> {
                when (event.pointerCount) {
                    1 -> {//一指移动
                        mGestureInterface?.onMove(event)
                        if (Math.abs(event.x - mPoints.x1) > ViewConfiguration.get(mContext).scaledTouchSlop
                                || Math.abs(event.y - mPoints.y1) > ViewConfiguration.get(mContext).scaledTouchSlop) {
                            //移动距离大于感知距离
                            mLongPrRun.cancel(true)
                            WeakHandler(mContext.mainLooper).removeCallbacks(mLongPrRun)
                            mLongPrRun.cancel(true)
                            WeakHandler(mContext.mainLooper).removeCallbacks(mDoubleRun)
                        }
                    }
                    2 -> {//两指计算缩放
                        //应该是和上一次的点比较，而不是按下的点
                        val newPoints = Points(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
                        //算两点间隔吧，大了算放，小了算缩
                        val space = space(mMoveEndPoints, newPoints)
                        Log.e(TAG, "两次间隔：$space")
                        if (Math.abs(space) > ViewConfiguration.get(mContext).scaledTouchSlop) {
                            if (space > 0) {
                                mGestureInterface?.zoomBig(space)
                                Log.e(TAG, "执行放大")
                            } else {
                                mGestureInterface?.zoomSmall(space)
                                Log.e(TAG, "执行缩短")
                            }
                        }
                        mMoveEndPoints.x1 = newPoints.x1;mMoveEndPoints.y1 = newPoints.y1;mMoveEndPoints.x2 = newPoints.x2;mMoveEndPoints.y2 = newPoints.y2
                    }
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
            }
            MotionEvent.ACTION_UP -> {
                mGestureInterface?.onUp()
                //自己抬起来，就不算长按了
                if (mDoubleRun.getIsLong()) {
                    mDoubleRun.clearCount()
                    mDoubleRun.cancel(true)
                    WeakHandler(mContext.mainLooper).removeCallbacks(mDoubleRun)
                }
                mLongPrRun.cancel(true)
                WeakHandler(mContext.mainLooper).removeCallbacks(mLongPrRun)
                if (Math.abs(event.x - mPoints.x1) < ViewConfiguration.get(mContext).scaledTouchSlop
                        && Math.abs(event.y - mPoints.y1) < ViewConfiguration.get(mContext).scaledTouchSlop) {
                    mDoubleRun.addCount()
                    if (mDoubleRun.getIsLong()) mDoubleRun.clearCount()
                } else {
                    if (!cancelOneFingerEvent) {
                        //手松开时，距离大一点
                        val tempX = event.x.minus(mPoints.x1)
                        val tempY = event.y.minus(mPoints.y1)
                        if (Math.abs(tempX) > Math.abs(tempY)) {
                            if (tempX > 0) mGestureInterface?.onRight() else mGestureInterface?.onLeft()
                        } else {
                            if (tempY > 0) mGestureInterface?.onBottom() else mGestureInterface?.onTop()
                        }
                    }
                }
            }
        }
        return true
    }


    class DoubleRunnable(gesture: OnGesture?) : Runnable {
        private var isLong = false
        private var cancel: Boolean = false
        private var clickCount = 0
        private val mDoubleGesture by lazy { gesture }
        override fun run() {
            if (!cancel && !isLong) {
                when (clickCount) {
                    in 2..10 -> mDoubleGesture?.onDoubleUp()
                    1 -> mDoubleGesture?.onSingleUp()
                }
            }
            clickCount = 0
        }

        fun addCount() {
            clickCount++
        }

        fun clearCount() {
            clickCount = 0
        }

        fun getCount(): Int {
            return clickCount
        }

        fun cancel(cancel: Boolean) {
            this.cancel = cancel
        }

        fun isLong(tIsLong: Boolean) {
            this.isLong = tIsLong
            if (isLong) clearCount()
        }

        fun getIsLong(): Boolean {
            return isLong
        }
    }

    class LongPreRunnable(gesture: OnGesture?, doubleRunnable: DoubleRunnable, zGesture: ZGesture) : Runnable {
        private var cancel: Boolean = false
        private val mLongGesture by lazy { gesture }
        private val mlDoubleRun by lazy { doubleRunnable }
        private val mlZesture by lazy { zGesture }
        override fun run() {
            if (!cancel) mLongGesture?.onLong()
            mlDoubleRun.isLong(true)
            mlDoubleRun.cancel(true)
            mlZesture.cancelOneFingerEvent = true
        }

        fun cancel(cancel: Boolean) {
            this.cancel = cancel
        }
    }

    /**双指，两个坐标点**/
    data class Points(var x1: Float, var y1: Float, var x2: Float = 0f, var y2: Float = 0f)

    fun space(p0: Points, p1: Points): Double {
        val p0x = Math.abs(p0.x1 - p0.x2)
        val p0y = Math.abs(p0.y1 - p0.y2)
        val p0sapce = Math.sqrt(p0x * p0x * 1.0 + p0y * p0y)
        val p1x = Math.abs(p1.x1 - p1.x2)
        val p1y = Math.abs(p1.y1 - p1.y2)
        val p1space = Math.sqrt(p1x * p1x * 1.0 + p1y * p1y)
        return p1space - p0sapce
    }

}