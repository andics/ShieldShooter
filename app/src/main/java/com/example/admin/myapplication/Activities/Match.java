package com.example.admin.myapplication.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.admin.myapplication.GameStatics.Utils;
import com.example.admin.myapplication.R;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.example.admin.myapplication.GameStatics.Utils.getName;

public class Match extends Activity {

    public final int POPUP_WIDTH = 320;
    public final int POPUP_HEIGHT = 500;
    private String spinnerSelected="1";
    private TextView nameField;
    public static Match activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_match);

        Spinner dropdown = (Spinner)findViewById(R.id.spinnerPeople);
        final String[] items = new String[]{"1", "2", "3", "4"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
        Spinner s = (Spinner) findViewById(R.id.spinnerPeople);
        nameField = (TextView) findViewById(R.id.matchNameField);
        activity = this;
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelected = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private PopupWindow pwindo;
    public void showPopUp(View v) {
        Utils.setName(nameField.getText().toString());
        matchMe();
        LayoutInflater inflater = (LayoutInflater) Match.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.matching_layout, null);
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.hi);

        pwindo = new PopupWindow(layout, convertToDps(POPUP_WIDTH), convertToDps(POPUP_HEIGHT), true);
        pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
        View v1 = pwindo.getContentView();

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v1, "alpha", .1f, 1f);
        fadeIn.setDuration(300);
        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn);
        mAnimationSet.start();
    }
    public void matchMe() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ip = String.valueOf(Utils.getDefaultsString("settings", "mmip", Match.this));
                    Log.e("mmip",ip);
                    Utils.matchMakingIp = InetAddress.getByName(ip);
                    //213.191.179.135
                    Log.e("Match making", Utils.matchMakingIp.getHostAddress());
                    Utils.register(Utils.matchMakingIp, 47252, getName(), spinnerSelected);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void switchActivity() throws InterruptedException {
        startActivity(new Intent(this, Game.class));
        finish();
    }
    public void dismissPopUp(View v) {
        Utils.disconnectFromMM();
        View v1 = pwindo.getContentView();
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v1, "alpha",  1f, 0f);
        fadeOut.setDuration(200);
        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeOut);

        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                pwindo.dismiss();
            }
        });
        mAnimationSet.start();
    }
    public int convertToDps(int dps) {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);
        return pixels;
    }
    public void state(String str) {

    }
}
