package com.crashapps.soundservice;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.View;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Perroloco on 1/24/16.
 */
public class PLSoundPoolManager {

    private static PLSoundPoolManager instance;

    private SoundPool soundPool;
    boolean loaded = false;
    AudioManager audioManager;
    int volume;
    HashMap<Integer, Integer> soundIds;

    private void PLSoundPoolManager(){

    }

    public static PLSoundPoolManager get(){
        if(instance == null)
            instance = new PLSoundPoolManager();
        return instance;
    }

    public void init(Context context) {
        audioManager = (AudioManager) context.getApplicationContext().getSystemService(context.AUDIO_SERVICE);
        volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

        // Load the sounds
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });

        soundIds = new HashMap<Integer, Integer>();

        soundIds.put(soundPool.load(context, R.raw.f1, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f2, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f3, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f4, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f5, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f6, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f7, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f8, 1), 0);
        soundIds.put(soundPool.load(context, R.raw.f9, 1), 0);

    }

    public void playSound() {
        // Is the sound loaded does it already play?
        if (loaded) {
            Random r = new Random();
            Object[] values = soundIds.keySet().toArray();
            int randomValue = (int)values[r.nextInt(values.length)];

            soundPool.play(randomValue, volume, volume, 1, 0, 1f);
        }
    }

    public void playLoop() {
        // Is the sound loaded does it already play?
        if (loaded) {

            Random r = new Random();
            Object[] values = soundIds.values().toArray();
            int randomValue = (int)values[r.nextInt(values.length)];
            // the sound will play for ever if we put the loop parameter -1
            soundPool.play(randomValue, volume, volume, 1, -1, 1f);
        }
    }

    public void stopSound(View v) {
        soundPool.autoPause();
    }
}
