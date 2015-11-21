package com.example.admin.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

public class ConnectActivity extends ActionBarActivity {
    int seconds, secondsRespond=5;
    Timer t;
    TextView stateField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        stateField = (TextView) findViewById(R.id.stateText);
    }
    public void register(View v) throws InterruptedException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                TextView nameField = (TextView) findViewById(R.id.nameField);
                TextView ipField = (TextView) findViewById(R.id.ipField);
                try {
                    Utils.register(InetAddress.getByName(ipField.getText().toString().trim()), 9876, nameField.getText().toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        t= new Timer();
        seconds=0;
        secondsRespond=6;
                t.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (seconds <= Variables.CONNECTION_WAIT) {
                                    if (Utils.clientSocket != null && secondsRespond > 0) {
                                        secondsRespond--;
                                        setText("Connecting..." + secondsRespond + " seconds left");
                                        Log.e("Connected", "still counting...");
                                        if (Utils.connected == true) {
                                            Log.e("Connected","true");
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
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                disconnect();
                                            }
                                        }).start();
                                    }
                                    if (Utils.clientSocket == null) {
                                        setText("Attempting to connect..." + (Variables.CONNECTION_WAIT - seconds) + " seconds left");
                                        seconds++;
                                    }
                                } else {
                                    t.cancel();
                                    Log.e("Failed", "Failed");
                                    setText("Connecting failed");
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
                    Log.e("recurse","Trying begining!");
                    Utils.connectedToServer=false;
                    Utils.inFromServer.close();
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
        Thread.sleep(4000);
        startActivity(new Intent(this, inGameActivity.class));
        finish();
    }

    public void setText(String str) {
        stateField.setText(str);
    }

    private void setTextConnecting() {
        stateField.setText("Connecting...");
    }
}
