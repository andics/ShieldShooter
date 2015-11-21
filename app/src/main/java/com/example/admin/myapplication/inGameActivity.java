package com.example.admin.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.admin.myapplication.Utils.*;

public class inGameActivity extends ActionBarActivity {

    TextView console;
    TextView timerText;
    public static inGameActivity activity;
    public int seconds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game);
        console = (TextView) findViewById(R.id.console);
        activity = this;
        Utils.setConsole(console);
    }

    public void finishAndRestart() {
        startActivity(new Intent(this,ConnectActivity.class));
        finish();
    }

    public void doRound() {
        Timer t= new Timer();
        seconds=0;
        timerText=(TextView) findViewById(R.id.timer);
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                seconds++;
                if(seconds<=Variables.ROUND_DELAY) {
                    timerText.setText(Variables.ROUND_DELAY-seconds + " seconds left");
                } else {
                    super.cancel();
                }
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
