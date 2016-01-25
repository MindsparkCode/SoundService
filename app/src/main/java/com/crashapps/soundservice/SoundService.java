package com.crashapps.soundservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Random;

/**
 * Created by Perroloco on 1/24/16.
 */
public class SoundService extends Service {

    public static final String SOUND_SERVICE_READY = "sound service ready";
    private static final String TAG = "SoundService";
    private SoundServiceListener listener = null;

    PLSoundPoolManager spManager;
    Handler sHandler;
    Random random;

    private static SoundService instance = null;

    public static SoundService get(){
        return instance;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "OnCreate");
        spManager = PLSoundPoolManager.get();
        random = new Random();
        sHandler = new Handler();
        instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OnStartCommand");

        spManager.init(this);
        long delay = intent.getLongExtra("delay", 0);
        postSoundDelayed(delay);
        sendBroadcast(new Intent(SOUND_SERVICE_READY));

        return super.onStartCommand(intent, flags, startId);
    }

    private void postSoundDelayed(long delay){
        Log.d(TAG, "postSoundDelayed");
        if(delay == 0) {
            delay = 4000 + random.nextInt(20000);
        }
        sHandler.postDelayed(new SoundRunnable(), delay);
        if(listener != null)
            listener.nextSound(delay);
    }

    private class SoundRunnable implements Runnable{

        @Override
        public void run() {
            Log.d(TAG, "SoundRunnable run()");
            Log.d("SoundService", "Incoming fart...");
            if(listener != null)
                listener.onPlaySound();
            spManager.playSound();
            postSoundDelayed(0);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sHandler.removeCallbacksAndMessages(null);
        if(listener != null)
            listener.onServiceDestroyed();
    }

    public void registerListener(SoundServiceListener l){
        listener = l;
    }

    protected interface SoundServiceListener{
        void onPlaySound();
        void onServiceDestroyed();
        void nextSound(long delay);
    }

}
