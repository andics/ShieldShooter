package com.example.admin.myapplication.GameStatics;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.myapplication.Activities.Game;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.Variables.Variables;

/**
 * Created by Genata on 17.12.2015 г..
 */
public class Player extends RuntimeException {
    public String name;
    public int Shots;
    public int wins=0;
    public int shieldsInARow;
    public int layoutId;
    public RelativeLayout playerLayout;
    private TextView shotsTextField, shieldsTextField, nameField, winsField;
    public RelativeLayout getPlayerLayout() {
        return playerLayout;
    }

    public void setPlayerLayout(RelativeLayout playerLayout, TextView shots, TextView shields, TextView name, TextView wins, int layoutId) {
        this.playerLayout = playerLayout;
        this.shotsTextField = shots;
        this.shieldsTextField = shields;
        this.nameField = name;
        this.winsField = wins;
        this.layoutId = layoutId;
        this.Shots= Variables.allVariables.get("START_AMMO");
        this.shieldsInARow = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
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
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                shieldsInARow = ShieldsInARow;
                final ImageView shieldImage = (ImageView) playerLayout.findViewById(R.id.imageView4);
                Utils.scaleAnimationSlow(shieldImage);
                shieldsTextField.setText(String.valueOf(shieldsInARow));
            }
        });
    }
    public void setShots(final int shots) {
            Game.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Shots = shots;
                    final ImageView shootImage = (ImageView) playerLayout.findViewById(R.id.imageView3);
                    Utils.scaleAnimationSlow(shootImage);
                    shotsTextField.setText(String.valueOf(Shots));
                }
            });
        }
    public void setWins(final int winsNum) {
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                wins = winsNum;
                final ImageView cupImage = (ImageView) playerLayout.findViewById(R.id.imageView5);
                Utils.scaleAnimationFast(cupImage);
                winsField.setText(String.valueOf(wins));
            }
        });
    }
    public int getWins() {
        return wins;
    }
    public void restartShields() {
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        shieldsInARow  =  Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
        shieldsTextField.setText(String.valueOf(shieldsInARow));
            }
        });
    }
    public Player(String name) {
        this.name=name;
    }
}
