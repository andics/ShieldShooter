package com.example.admin.myapplication.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.GameStatics.Utils;
import com.example.admin.myapplication.Variables.Variables;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectActivity extends Activity {
    int seconds, secondsRespond;
    Timer t;
    Button connect;
    TextView stateField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        stateField = (TextView) findViewById(R.id.stateText);
        connect = (Button) findViewById(R.id.connectToServerButton);
    }
    public void register(View v) throws InterruptedException {
        ConnectivityManager m
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(new Utils().hasInternetConnection(m))
        register();
        else
            setText("Please connect to internet!");
    }

    public void register() {
        connect.setEnabled(false);
        connect.setBackgroundResource(R.drawable.round_very_green_disabled);
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
                int port = prefs.getInt("port", 9876);
                TextView nameField = (TextView) findViewById(R.id.nameField);
                TextView ipField = (TextView) findViewById(R.id.ipField);
                try {
                    Log.e("Port", String.valueOf(port));
                    Utils.register(InetAddress.getByName(ipField.getText().toString().trim()), port, nameField.getText().toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        t= new Timer();
        seconds=0;
        secondsRespond= Variables.RESPOND_WAIT;
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (seconds <= Variables.CONNECTION_WAIT) {
                            if (Utils.clientSocket != null && secondsRespond > 0) {
                                if (Utils.clientSocket.isConnected() == true) {
                                    secondsRespond--;
                                    setText("Connecting..." + secondsRespond + " seconds left");
                                    Log.e("Connected", "still counting...");
                                    Log.e("Connected", "true");
                                    if(Utils.connected) {
                                        setText("Connected! Heading to game");
                                        try {
                                            t.cancel();
                                            switchActivity();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                if (secondsRespond == 0) {
                                    Log.e("soconds", "0");
                                    setText("Connecting failed");
                                    connect.setEnabled(true);
                                    connect.setBackgroundResource(R.drawable.round_very_green);
                                    try {
                                        t.cancel();
                                        Utils.clientSocket.close();
                                    } catch (IOException e) {
                                        Log.e("bug", "fail to close the socket on fail");
                                    }
                                }
                            } else if (Utils.clientSocket == null) {
                                setText("Attempting to connect..." + (Variables.CONNECTION_WAIT - seconds) + " seconds left");
                                seconds++;
                            }
                        } else {
                            t.cancel();
                            Log.e("Failed", "Failed");
                            setText("Connecting failed");
                            connect.setEnabled(true);
                            connect.setBackgroundResource(R.drawable.round_very_green);
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public void disconnect() {
        if(Utils.clientSocket!=null) {
            if (Utils.clientSocket.isConnected() == true) {
                try {
                    Log.e("recurse", "Trying begining!");
                    Utils.clientSocket.close();
                    Log.e("recurse","Trying!");
                    Utils.clientSocket=null;
                } catch (IOException e) {
                    Log.e("bug", "fail to close the socket on fail");
                }
            }
        }
    }

    private void switchActivity() throws InterruptedException {
        setText("Connected! Heading to game");
        Thread.sleep(2000);
        startActivity(new Intent(this, game.class));
        finish();
    }

    public void setText(String str) {
        stateField.setText(str);
    }

    private void setTextConnecting() {
        stateField.setText("Connecting...");
    }
}
