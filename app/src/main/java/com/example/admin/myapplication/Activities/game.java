package com.example.admin.myapplication.Activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.myapplication.R;

import java.util.Timer;
import java.util.TimerTask;

public class game extends Activity {
        /** Called when the activity is first created. */
        LinearLayout ll;
        int secondsTicked;
        TextView timeLeft = (TextView) findViewById(R.id.timerText);
        Timer t;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_game);
            TextView tv = (TextView) findViewById(R.id.gameState);
            Typeface face = Typeface.createFromAsset(getAssets(),
                    "fonts/gothic.TTF");
            tv.setTypeface(face);
            ll = (LinearLayout) findViewById(R.id.timer);
}
    public void newDoRound(View v) {
        doRound(50, 15);
    }
    public void doRound(int width,final int sec) {
        int y = ll.getHeight() / 2;
        int x = ll.getWidth() / 2;
        secondsTicked=0;
        Paint paintBig = new Paint();
        paintBig.setColor(Color.parseColor("#B4C0C0"));
        Paint paintSmall = new Paint();
        paintSmall.setColor(Color.parseColor("#FFFFFF"));
        Bitmap bg = Bitmap.createBitmap(x*2, y*2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        RectF timerBig = new RectF(0, 0, x*2, y*2);
        RectF timerSmall = new RectF(width/2, width/2, x*2-width/2, y*2-width/2);
        canvas.drawOval(timerBig, paintBig);
        canvas.drawOval(timerSmall, paintSmall);
        t= new Timer();
        ll.setBackgroundDrawable(new BitmapDrawable(bg));
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(secondsTicked<sec) {
                            secondsTicked++;

                            timeLeft.setText(sec-secondsTicked);
                        } else {
                            t.cancel();
                        }
                    }
                });
                }
        }, 0, 1000);
        }
    }
