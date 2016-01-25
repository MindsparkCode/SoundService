package com.crashapps.soundservice;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SoundService.SoundServiceListener{


    private static final String TAG = "MainActivity";

    @Bind(R.id.service_status)
    TextView service_status;

    @Bind(R.id.buttonsContainer)
    LinearLayout buttonsContainer;

    @Bind(R.id.coordinator)
    CoordinatorLayout coordinator;

    @Bind(R.id.sound)
    ImageView soundImage;

    private ServiceReadyReceiver serviceReadyReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        checkServiceStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        reigsterReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (serviceReadyReceiver != null)
            unregisterReceiver(serviceReadyReceiver);
    }

    private void reigsterReceiver(){
        if (serviceReadyReceiver == null)
            serviceReadyReceiver = new ServiceReadyReceiver();
        IntentFilter intentFilter = new IntentFilter(SoundService.SOUND_SERVICE_READY);
        registerReceiver(serviceReadyReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        animateLayout();
    }

    private void animateLayout(){
        for(int i = 0; i < buttonsContainer.getChildCount(); i++){
            TranslateAnimation anim = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -3, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            anim.setDuration(900);
            anim.setInterpolator(new DecelerateInterpolator());
            anim.setStartOffset(i*100);
            buttonsContainer.getChildAt(i).startAnimation(anim);
        }
    }

    private void checkServiceStatus(){
        service_status.setVisibility(View.VISIBLE);
        soundImage.setVisibility(View.INVISIBLE);
        if(isMyServiceRunning(SoundService.class)){
            service_status.setText("Service Running...");
            SoundService.get().registerListener(this);
        }else{
            service_status.setText("Service Stopped");
        }
    }

    @OnClick(R.id.bt_startService)
    public void simpleStart(){
        if(isMyServiceRunning(SoundService.class)) {
            Snackbar.make(coordinator, "Service is already running...", Snackbar.LENGTH_SHORT)
                    .show();
            checkServiceStatus();
        }else {
            startService(new Intent(this, SoundService.class));
        }
    }

    @OnClick(R.id.bt_startDelayed)
    public void startDelayed(){
        pickDelay();
    }

    @OnClick(R.id.bt_stopService)
    public void stopService(){
        if(isMyServiceRunning(SoundService.class)) {
            stopService(new Intent(this, SoundService.class));
        }
        checkServiceStatus();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPlaySound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                service_status.setVisibility(View.INVISIBLE);
                soundImage.setVisibility(View.VISIBLE);
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkServiceStatus();
                    }
                });
            }
        }, 500);
    }

    @Override
    public void onServiceDestroyed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkServiceStatus();
            }
        });
    }

    @Override
    public void nextSound(final long delay) {
        Log.e(TAG, "Next sound: " + delay);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                service_status.setText("Next sound in "+delay+"s.");
            }
        });
    }


    private void pickDelay(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 1);

        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        View multiPickerLayout = LayoutInflater.from(this).inflate(R.layout.delaypicker, null);
        final DatePicker multiPickerDate = (DatePicker) multiPickerLayout.findViewById(R.id.multipicker_date);
        final TimePicker multiPickerTime = (TimePicker) multiPickerLayout.findViewById(R.id.multipicker_time);

        multiPickerDate.updateDate(mYear,mMonth,mDay);

        multiPickerTime.setCurrentHour(mHour);
        multiPickerTime.setCurrentMinute(mMinute);
        try {
            multiPickerDate.setMinDate(System.currentTimeMillis() + 10000);
        }catch(Exception e){
            e.printStackTrace();
        }

        DialogInterface.OnClickListener dialogButtonListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_NEGATIVE: {
                        Log.e(TAG, "Cancel delay");
                        dialog.dismiss();
                        break;
                    }
                    case DialogInterface.BUTTON_POSITIVE: {
                        service_status.setText("Scheduling service...");
                        if(isMyServiceRunning(SoundService.class)) {
                            stopService(new Intent(MainActivity.this, SoundService.class));
                        }
                        Intent i =new Intent(MainActivity.this, SoundService.class);
                        long scheduledMillis = calculateTime(multiPickerDate, multiPickerTime);
                        if(scheduledMillis - System.currentTimeMillis() < 1000){
                            Snackbar.make(coordinator, "This app cannot travel back in time...", Snackbar.LENGTH_SHORT).show();
                            break;
                        }
                        Snackbar.make(coordinator, "Starting service at "+scheduledMillis, Snackbar.LENGTH_SHORT).show();
                        i.putExtra("schedule", scheduledMillis);
                        startService(i);
                        break;
                    }
                    default: {
                        Log.d(TAG, "Uncaught event... closing dialog");
                        dialog.dismiss();
                        break;
                    }
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(multiPickerLayout);
        builder.setPositiveButton("Set", dialogButtonListener);
        builder.setNegativeButton("Cancel", dialogButtonListener);
        builder.show();
    }

    private long calculateTime(DatePicker d, TimePicker t){
        Calendar calendar = Calendar.getInstance();
        calendar.set(d.getYear(), d.getMonth(), d.getDayOfMonth(),
                t.getCurrentHour(), t.getCurrentMinute(), 0);
        return calendar.getTimeInMillis();
    }

    //BroadcastReceiver

    private class ServiceReadyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SoundService.SOUND_SERVICE_READY)) {
                SoundService.get().registerListener(MainActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        service_status.setText("Service Running...");
                    }
                });
            }
        }
    }

}
