package com.example.proyectoredquiz;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.checkerframework.checker.units.qual.C;

import java.security.Provider;

public abstract class BroadcastService extends Service {
    private String TAG = "BroadcastServise";
    public static final String COUNTDOWN_BR = "com.example.proyectoredquiz";
    Intent intent = new Intent(COUNTDOWN_BR);
    CountDownTimer countDownTimer = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Sarting timer...");
        countDownTimer = new CountDownTimer(60000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished);
                intent.putExtra("countdow", millisUntilFinished);
                sendBroadcast(intent);
            }

            @Override
            public void onFinish() {

            }
        };
        countDownTimer.start();
    }

    @Override
    public void onDestroy() {
        countDownTimer.cancel();
        super.onDestroy();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public abstract void onReceive(Context context, Intent intent);
}
