package com.example.admin.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;

import static com.example.admin.myapplication.Utils.*;

public class inGameActivity extends ActionBarActivity {

    TextView console;
    Timer t;
    TextView timerText;
    public static inGameActivity activity;
    public int seconds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        console = (TextView) findViewById(R.id.console);
        activity = this;
        Utils.setUnits(console, (Button) findViewById(R.id.shootButton), (Button) findViewById(R.id.shieldButton), (Button) findViewById(R.id.reloadButton));
    }



    public void finishAndRestart() {
        startActivity(new Intent(this,ConnectActivity.class));
        finish();
    }

    public void doRound() {
        t= new Timer();
        seconds=0;
        timerText=(TextView) findViewById(R.id.timer);
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seconds++;
                        if (seconds <= Variables.ROUND_DELAY) {
                            timerText.setText(Variables.ROUND_DELAY - seconds + " seconds left");
                        } else {
                            t.cancel();
                        }
                    }
                });
                }
            },0,1000);
    }

    public void shoot(View v){
        startActivity(new Intent(this,TargetsActivity.class));
    }

    public void reload(View v){
        send("reload:" + getName());
    }

    public void shield(View v){
        send("shield:" + getName());
    }

}
