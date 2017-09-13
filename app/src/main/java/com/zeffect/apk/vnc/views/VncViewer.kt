package com.zeffect.apk.vnc.views

import android.androidVNC.*
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.ImageView
import com.antlersoft.android.bc.BCFactory
import java.io.IOException

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
class VncViewer(context: Context, attrs: AttributeSet) : ImageView(context, attrs) {
    private val TAG = "VncViewer"
    private var mConnect: ConnectionBean? = null
    private val rfb by lazy { RfbProto(mConnect?.address, mConnect?.port ?: 0) }

    fun initVnc(connectionBean: ConnectionBean) {
        if (connectionBean == null) return
        this.mConnect = connectionBean
        try {
            connectAndAuthenticate(mConnect!!)
        } catch (e: Exception) {

        }
    }

    @Throws(Exception::class)
    private fun connectAndAuthenticate(connection: ConnectionBean) {
        Log.i(TAG, "Connecting to " + connection.getAddress() + ", port " + connection.getPort() + "...")
        // <RepeaterMagic>
        if (connection.getUseRepeater() && connection.getRepeaterId() != null && connection.getRepeaterId().length > 0) {
            Log.i(TAG, "Negotiating repeater/proxy connection")
            val protocolMsg = ByteArray(12)
            rfb.`is`.read(protocolMsg)
            val buffer = ByteArray(250)
            System.arraycopy(connection.getRepeaterId().toByteArray(), 0, buffer, 0, connection.getRepeaterId().length)
            rfb.os.write(buffer)
        }
        // </RepeaterMagic>
        rfb.readVersionMsg()
        Log.i(TAG, "RFB server supports protocol version " + rfb.serverMajor + "." + rfb.serverMinor)
        rfb.writeVersionMsg()
        Log.i(TAG, "Using RFB protocol version " + rfb.clientMajor + "." + rfb.clientMinor)
        var bitPref = 0
        if (connection.userName.isNotEmpty())
            bitPref = bitPref or 1
        Log.d("debug", "bitPref=" + bitPref)
        val secType = rfb.negotiateSecurity(bitPref)
        val authType: Int
        if (secType == RfbProto.SecTypeTight) {
            rfb.initCapabilities()
            rfb.setupTunneling()
            authType = rfb.negotiateAuthenticationTight()
        } else if (secType == RfbProto.SecTypeUltra34) {
            rfb.prepareDH()
            authType = RfbProto.AuthUltra
        } else {
            authType = secType
        }

        when (authType) {
            RfbProto.AuthNone -> {
                Log.i(TAG, "No authentication needed")
                rfb.authenticateNone()
            }
            RfbProto.AuthVNC -> {
                Log.i(TAG, "VNC authentication needed")
                rfb.authenticateVNC(connection.password)
            }
            RfbProto.AuthUltra -> rfb.authenticateDH(connection.userName, connection.password)
            else -> throw Exception("Unknown authentication scheme " + authType)
        }
    }


    @Throws(IOException::class)
    internal fun doProtocolInitialisation(dx: Int, dy: Int) {
        rfb.writeClientInit()
        rfb.readServerInit()

        Log.i(TAG, "Desktop name is " + rfb.desktopName)
        Log.i(TAG, "Desktop size is " + rfb.framebufferWidth + " x " + rfb.framebufferHeight)

        var useFull = false
        val capacity = BCFactory.getInstance().bcActivityManager.getMemoryClass(Utils.getActivityManager(context))
        if (mConnect.getForceFull() == BitmapImplHint.AUTO) {
            if (rfb.framebufferWidth * rfb.framebufferHeight * FullBufferBitmapData.CAPACITY_MULTIPLIER <= capacity * 1024 * 1024)
                useFull = true
        } else
            useFull = mConnect.getForceFull() == BitmapImplHint.FULL
        if (!useFull)
            bitmapData = LargeBitmapData(rfb, this, dx, dy, capacity)
        else
            bitmapData = FullBufferBitmapData(rfb, this, capacity)
        mouseX = rfb.framebufferWidth / 2
        mouseY = rfb.framebufferHeight / 2

        setPixelFormat()
    }

    @Throws(IOException::class)
    private fun setPixelFormat() {
        pendingColorModel.setPixelFormat(rfb)
        bytesPerPixel = pendingColorModel.bpp()
        colorPalette = pendingColorModel.palette()
        colorModel = pendingColorModel
        pendingColorModel = null
    }
}