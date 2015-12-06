package com.example.admin.myapplication.Activities;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.example.admin.myapplication.R;

public class game extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        TextView tv = (TextView) findViewById(R.id.gameState);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/gothic.TTF");
        tv.setTypeface(face);
    }

}
