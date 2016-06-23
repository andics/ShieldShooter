package com.example.admin.myapplication.GameStatics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.Activities.Game;
import com.example.admin.myapplication.Activities.Match;
import com.example.admin.myapplication.MainActivity;
import com.example.admin.myapplication.R;

public class ChooseGameMode extends Activity {

    public final int POPUP_WIDTH = 320;
    public final int POPUP_HEIGHT = 500;
    LayoutInflater inflater;
    static ChooseGameMode activity;
    View view;
    TextView ipTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_game_mode);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/gothic.TTF");
        ((TextView) findViewById(R.id.Slogan)).setTypeface(face);
        inflater = (LayoutInflater) ChooseGameMode.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.settings_layout, null);
        ipTextView = (TextView) view.findViewById(R.id.mmIpSettings);
        ipTextView.setText(String.valueOf(Utils.getDefaultsString("settings", "mmip", ChooseGameMode.this)));
        activity=this;
    }
    @Override
    public void onResume() {
        super.onResume();
        Game.isFirstRound=true;
    }
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }
    public void directConnect(View v) {
        startActivity(new Intent(this, MainActivity.class));
    }
    public void matchMaking(View v) {
        startActivity(new Intent(this, Match.class));
    }
    private PopupWindow pwindo;
    public void saveSettingsMM(View v) {
        if(Utils.validIP(String.valueOf(ipTextView.getText().toString()))) {
                String mmIP = String.valueOf(ipTextView.getText().toString());
                SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();

                edit.putString("mmip", mmIP);
                edit.apply();
                Toast.makeText(ChooseGameMode.this, "Settings saved!!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(ChooseGameMode.this, "Please enter a valid ip address!", Toast.LENGTH_LONG).show();
        }
    }
    public void dismissSettings(View v) {
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
    public void showMMSettings(View v) {
        RelativeLayout layout = (RelativeLayout) view.findViewById(R.id.hiSettings);

        pwindo = new PopupWindow(layout, convertToDps(POPUP_WIDTH), convertToDps(POPUP_HEIGHT), true);
        pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);
        View v1 = pwindo.getContentView();

        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v1, "alpha", .1f, 1f);
        fadeIn.setDuration(300);
        final AnimatorSet mAnimationSet = new AnimatorSet();

        mAnimationSet.play(fadeIn);
        mAnimationSet.start();
    }
    public int convertToDps(int dps) {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);
        return pixels;
    }
}
