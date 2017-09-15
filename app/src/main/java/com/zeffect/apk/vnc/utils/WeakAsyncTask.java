package com.zeffect.apk.vnc.utils;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * 防止内存泄露的AsyncTask
 * <pre>
 *      author  ：zzx
 *      e-mail  ：zhengzhixuan18@gmail.com
 *      time    ：2017/07/14
 *      desc    ：
 *      version:：1.0
 * </pre>
 *
 * @author zzx
 */

public abstract class WeakAsyncTask<Params, Progress, Result, WeakTarget> extends AsyncTask<Params, Progress, Result> {
    protected final WeakReference<WeakTarget> mTarget;

    public WeakAsyncTask(WeakTarget pWeakTarget) {
        if (pWeakTarget == null) {
            throw new NullPointerException("weak target is null");
        }
        mTarget = new WeakReference<WeakTarget>(pWeakTarget);
    }


    @Override
    protected final void onPreExecute() {
        final WeakTarget target = mTarget.get();
        if (target != null) {
            this.onPreExecute(target);//运行前的准备
        }
    }

    protected void onPreExecute(WeakTarget pTarget) {
    }


    @Override
    protected final Result doInBackground(Params... params) {
        final WeakTarget target = mTarget.get();
        if (target != null) {
            return this.doInBackground(target, params);//后台运行中
        } else {
            return null;
        }
    }

    protected abstract Result doInBackground(WeakTarget pTarget, Params... params);


    @Override
    protected final void onPostExecute(Result pResult) {
        final WeakTarget target = mTarget.get();
        if (target != null) {
            this.onPostExecute(target, pResult);
        }
    }


    protected void onPostExecute(WeakTarget pTarget, Result pResult){};

    @Override
    protected final void onProgressUpdate(Progress... values) {
        final WeakTarget target = mTarget.get();
        if (target != null) {
            this.onProgressUpdate(target, values);
        }
    }

    protected void onProgressUpdate(WeakTarget pTarget, Progress... values) {
    }

}
