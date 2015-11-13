package com.example.admin.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.TextView;

import java.net.InetAddress;

public class ConnectActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
    }

    public void register(View v){
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

        startActivity(new Intent(this, inGameActivity.class));

    }

}
