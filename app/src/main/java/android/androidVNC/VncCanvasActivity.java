/* 
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */

//
// CanvasView is the Activity for showing VNC Desktop.
//
package android.androidVNC;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.qimon.commonlibrary.gesture.ZGesture;
import com.zeffect.apk.vnc.gesture.OnGesture;
import com.zeffect.apk.vnc.gesture.WeakAsyncTask;

import java.util.List;

import cn.zeffect.apk.vnc.R;

public class VncCanvasActivity extends Activity {
    public static final String FIT_SCREEN_NAME = "FIT_SCREEN";
    public static final String TOUCH_ZOOM_MODE = "TOUCH_ZOOM_MODE";
    public static final String TOUCHPAD_MODE = "TOUCHPAD_MODE";
    private final static String TAG = "VncCanvasActivity";
    AbstractInputHandler inputHandler;
    VncCanvas vncCanvas;
    VncDatabase database;
    private ConnectionBean connection;
    private ZGesture mGesture;
    private Context mContext;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mContext = this;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        database = new VncDatabase(this);

        Intent i = getIntent();
        connection = new ConnectionBean();
        Uri data = i.getData();
        if ((data != null) && (data.getScheme().equals("vnc"))) {
            String host = data.getHost();
            // This should not happen according to Uri contract, but bug introduced in Froyo (2.2)
            // has made this parsing of host necessary
            int index = host.indexOf(':');
            int port;
            if (index != -1) {
                try {
                    port = Integer.parseInt(host.substring(index + 1));
                } catch (NumberFormatException nfe) {
                    port = 0;
                }
                host = host.substring(0, index);
            } else {
                port = data.getPort();
            }
            if (host.equals(VncConstants.CONNECTION)) {
                if (connection.Gen_read(database.getReadableDatabase(), port)) {
                    MostRecentBean bean = androidVNC.getMostRecent(database.getReadableDatabase());
                    if (bean != null) {
                        bean.setConnectionId(connection.get_Id());
                        bean.Gen_update(database.getWritableDatabase());
                    }
                }
            } else {
                connection.setAddress(host);
                connection.setNickname(connection.getAddress());
                connection.setPort(port);
                List<String> path = data.getPathSegments();
                if (path.size() >= 1) {
                    connection.setColorModel(path.get(0));
                }
                if (path.size() >= 2) {
                    connection.setPassword(path.get(1));
                }
                connection.save(database.getWritableDatabase());
            }
        } else {

            Bundle extras = i.getExtras();

            if (extras != null) {
                connection.Gen_populate((ContentValues) extras
                        .getParcelable(VncConstants.CONNECTION));
            }
            if (connection.getPort() == 0)
                connection.setPort(5900);

            // Parse a HOST:PORT entry
            String host = connection.getAddress();
            if (host.indexOf(':') > -1) {
                String p = host.substring(host.indexOf(':') + 1);
                try {
                    connection.setPort(Integer.parseInt(p));
                } catch (Exception e) {
                }
                connection.setAddress(host.substring(0, host.indexOf(':')));
            }
        }
        setContentView(R.layout.canvas);
        vncCanvas = (VncCanvas) findViewById(R.id.vnc_canvas);
        vncCanvas.setLongClickable(true);
        vncCanvas.initializeVncCanvas(connection, new Runnable() {
            public void run() {
            }
        });
        mGesture = new ZGesture(this, new MyOnGesture());
        vncCanvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGesture.onTouchEvent(event);
            }
        });
    }

    class MyOnGesture extends OnGesture {
        ZGesture.Points mPoints = new ZGesture.Points(0, 0, 0, 0);

        @Override
        public void onSingleUp() {
            sendKey(MetaKeyBean.keyMouseLeftClick);
        }

        @Override
        public void onDoubleUp() {
            sendKey(MetaKeyBean.keyMouseLeftClick);
            sendKey(MetaKeyBean.keyMouseLeftClick);
        }

        @Override
        public void onLongClick() {
            sendKey(MetaKeyBean.keyMouseRightClick);
        }

        @Override
        public void onDown(MotionEvent pEvent) {
            mPoints.setX1(pEvent.getX());
            mPoints.setY1(pEvent.getY());
            vncCanvas.refreshPoint((int) pEvent.getX(), (int) pEvent.getY());
        }

        @Override
        public void onMove(MotionEvent event) {
            //没有超过感知距离的就算了
            float tempW = Math.abs(mPoints.getX1() - event.getX());
            float tempH = Math.abs(mPoints.getY1() - event.getY());
            if ((tempW > ViewConfiguration.get(mContext).getScaledTouchSlop() || tempH > ViewConfiguration.get(mContext).getScaledTouchSlop())) {
                vncCanvas.processPointerEvent(event, true);
            }
        }

        @Override
        public void on2TopMove(Float pFloat) {
            sendKey(MetaKeyBean.mouseScrollUp);
        }

        @Override
        public void on2BottomMove(Float pFloat) {
            sendKey(MetaKeyBean.mouseScrollDown);
        }

        @Override
        public void onUp() {
            vncCanvas.sendUpMetaKey();
        }
    }


    public void sendKey(MetaKeyBean temp) {
        new WeakAsyncTask<MetaKeyBean, Void, Void, Context>(mContext) {
            @Override
            protected Void doInBackground(Context pContext, MetaKeyBean... params) {
                vncCanvas.sendMetaKey(params[0]);
                return null;
            }
        }.execute(temp);
    }

    ConnectionBean getConnection() {
        return connection;
    }

    @Override
    protected void onStop() {
        vncCanvas.disableRepaints();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        vncCanvas.enableRepaints();
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            vncCanvas.closeConnection();
            vncCanvas.onDestroy();
            database.close();
        }
    }
}
