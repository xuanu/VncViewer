package com.zeffect.apk.vnc.ui

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import cn.zeffect.apk.vnc.R
import com.zeffect.apk.vnc.views.VncViewer
import org.jetbrains.anko.find

/**
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/09/13
 *      desc    ：
 *      version:：1.0
 * </pre>
 * @author zzx
 */
class VncActivity : Activity() {
    private val mVncViewer by lazy { find<VncViewer>(R.id.vnc_viewer) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.layout_vnc)
    }
}