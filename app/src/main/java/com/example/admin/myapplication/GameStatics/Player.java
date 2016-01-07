package com.example.admin.myapplication.GameStatics;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.admin.myapplication.Activities.game;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.Variables.Variables;

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
    private TextView shotsTextField, shieldsTextField, nameField;
    public RelativeLayout getPlayerLayout() {
        return playerLayout;
    }

    public void setPlayerLayout(RelativeLayout playerLayout, TextView shots, TextView shields, TextView name, int layoutId) {
        this.playerLayout = playerLayout;
        this.shotsTextField = shots;
        this.shieldsTextField = shields;
        this.nameField = name;
        this.layoutId = layoutId;
  //      this.Shots= Variables.allVariables.get("START_AMMO");
    //    this.shieldsInARow = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
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
                shotsTextField.setText(String.valueOf(Shots));
            }
        });
    }
    public void setShots(final int shots) {
        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Shots = shots;
                shotsTextField.setText(String.valueOf(Shots));
            }
        });
    }
    public Player(String name) {
        this.name=name;
    }
}
