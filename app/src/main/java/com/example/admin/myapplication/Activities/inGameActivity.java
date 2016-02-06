package com.example.admin.myapplication.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.GameStatics.Utils;
import com.example.admin.myapplication.Variables.Variables;

import java.util.Timer;
import java.util.TimerTask;

import static com.example.admin.myapplication.GameStatics.Utils.*;

//THIS ACTIVITY IS NEVER USED! Contains some useful code tough
public class inGameActivity extends Activity {
    private int shields;
    public int shots;
    TextView console;
    Timer t;
    TextView timerText;
    TextView shotsCurrentText, maxShotsText, shieldsLeftText;
    public static inGameActivity activity;
    public int seconds;
    private boolean isFirstRound;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_in_gamesdfsdf);
//        console = (TextView) findViewById(R.id.console);
//        activity = this;
//        Utils.setUnits(console, (Button) findViewById(R.id.shootButton), (Button) findViewById(R.id.shieldButton), (Button) findViewById(R.id.reloadButton));
//        shotsCurrentText=(TextView) findViewById(R.id.Shots);
//        maxShotsText=(TextView) findViewById(R.id.maxShots);
//        shieldsLeftText=(TextView) findViewById(R.id.shieldsLeft);
//        Utils.dissableButtons(null, null);
//        shots = 0;
//        isFirstRound=true;
//    }



    public void finishAndRestart() {
        startActivity(new Intent(this,ConnectActivity.class));
        finish();
    }

    public void doRound() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                                if (isFirstRound) {
                                    maxShotsText.setText(String.valueOf(Variables.allVariables.get("MAX_AMMO")));
                                    shields = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
                                    isFirstRound = false;
                                }
                                t = new Timer();
                                seconds = 0;
                                timerText = (TextView) findViewById(R.id.timer);
                                Log.e("RIP", "Well, you tried");
                                if (shields > 0) {
                                    Utils.enableButtons("shield", null);
                                    shieldsLeftText.setText(String.valueOf(shields));
                                }
                                if (shots > 0) {
                                    Utils.enableButtons("shield", null);
                                    shotsCurrentText.setText(String.valueOf(shots));
                                }
                                if (shots < Variables.allVariables.get("MAX_AMMO")) {
                                    Utils.enableButtons("reload", null);
                                }
                                t.scheduleAtFixedRate(new TimerTask() {
                                    @Override
                                    public void run() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                seconds++;
                                                if (seconds <= Variables.allVariables.get("ROUND_DELAY")) {
                                                    timerText.setText(Variables.allVariables.get("ROUND_DELAY") - seconds + " seconds left");
                                                } else {
                                                    t.cancel();
                                                }
                                            }
                                        });
                                    }
                                }, 0, 1000);
                            }
                                        });
                                    }

//                                    public void shoot(View v) {
//                                        shields = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
//                                        startActivity(new Intent(this, TargetsActivity.class));
//                                    }
//
//                                    public void reload(View v) {
//                                        send("reload");
//                                        shots++;
//                                        shields = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
//                                        Utils.dissableButtons(null, "You reloaded this round");
//                                    }
//
//                                    public void shield(View v) {
//                                        send("shield");
//                                        shields--;
//                                        Utils.dissableButtons(null, "You played defence this round");
//                                    }

                                }
