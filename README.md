# androidVNC
Exported from https://code.google.com/p/android-vnc-viewer/.

> 修改之后，图片**没有放大缩放**操作，请注意。修改了手势操作，屏蔽了Google的手势操作。

##### 一些记录
- [VncCanvasActivity.class](https://github.com/xuanu/VncViewer/blob/master/app/src/main/java/android/androidVNC/VncCanvasActivity.java)：获取连接信息，并设置其它手势操作。
- [VncCanvas.class](https://github.com/xuanu/VncViewer/blob/master/app/src/main/java/android/androidVNC/VncCanvas.java):展示内容，并且自动更新。所以的方法基本上都在这里。
- [ZGesture.class](https://github.com/xuanu/VncViewer/blob/master/app/src/main/java/com/zeffect/apk/vnc/gesture/ZGesture.kt)：手势监听，自己写的，基本上常用的都有。
##### VncCanvas的一些方法记录
- VncCanvas#refreshPoint(x,y);//自己写的一个方法，刷新鼠标的位置
- VncCanvas#sendMetaKey(MetaKeyBean);//发送键值，注意不要在UI线程。
- VncCanvas#sendUpMetaKey();//手指抬起来的时候，要中断一些按键事件。
> 我复写的时候用到的事件应该就这几个了。
##### 其它的一些记录
- RfbProto#writePointerEvent(x,y,flag,mousebtn);//鼠标按键盘最后会调用这个
- RfbProto#writeKeyEvent(int,int,boolean);//键盘按键盘会调用这个
> 其它的一些刷新啊，我就完全没看懂了
