package com.example.admin.myapplication.GameStatics;

import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.myapplication.Activities.game;
import com.example.admin.myapplication.R;

import org.w3c.dom.Text;

/**
 * Created by Genata on 17.12.2015 г..
 */
public class Player {
    public String name;
    public int Shots;
    public int shieldsInARow;
    public int layoutId;
    public RelativeLayout playerLayout;
    public ScaleAnimation scaleUp, scaleDown;
    private TextView shotsTextField, shieldsTextField, nameField;
    public RelativeLayout getPlayerLayout() {
        return playerLayout;
    }

    public void setPlayerLayout(RelativeLayout playerLayout, TextView shots, TextView shields, TextView name, int layoutId) {
        this.playerLayout = playerLayout;
   //     this.playerLayout.setAnimationStyle(R.style.Animation);
        this.shotsTextField = shots;
        this.shieldsTextField = shields;
        this.nameField = name;
        this.layoutId = layoutId;
        scaleUp = new ScaleAnimation(1, 1.2f, 1, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(500);
        scaleDown = new ScaleAnimation(1.2f, 1, 1.2f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDown.setDuration(500);
  //      this.Shots= Variables.allVariables.get("START_AMMO");
    //    this.shieldsInARow = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
    }

    public void fadeOut() {
        this.playerLayout.animate()
                .translationYBy(0)
                .translationY(120)
                .alphaBy(100)
                .alpha(0)
                .setDuration(1000);
    }
    public void fadeIn() {
        this.playerLayout.animate()
                .translationYBy(0)
                .translationY(-120)
                .alphaBy(0)
                .alpha(100)
                .setDuration(1000);
    }

    public  String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getShieldsInARow() {
        return shieldsInARow;
    }
    public int getShots() {
        return Shots;
    }
    public void setShieldsInARow(final int ShieldsInARow) {
        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shieldsInARow = ShieldsInARow;
                final ImageView shieldImage = (ImageView) playerLayout.findViewById(R.id.imageView4);
                scaleUp.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        shieldImage.startAnimation(scaleDown);
                        shieldsTextField.setText(String.valueOf(shieldsInARow));
                    }
                });
                shieldImage.startAnimation(scaleUp);
            }
        });
    }
    public void setShots(final int shots) {
        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Shots = shots;
                final ImageView shootImage = (ImageView) playerLayout.findViewById(R.id.imageView3);
                scaleUp.setAnimationListener(new Animation.AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        shootImage.startAnimation(scaleDown);
                        shieldsTextField.setText(String.valueOf(shieldsInARow));
                    }
                });
                shootImage.startAnimation(scaleUp);
                shotsTextField.setText(String.valueOf(Shots));
            }
        });
    }
    public Player(String name) {
        this.name=name;
    }
}
