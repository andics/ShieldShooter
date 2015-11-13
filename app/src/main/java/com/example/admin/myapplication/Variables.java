package com.example.admin.myapplication;

/**
 * Created by Admin on 07/11/2015.
 */
public class Variables {

    public static int MIN_PLAYERS;
    public static int MAX_PLAYERS;
    public static int ROUND_DELAY;
    public static int WAIT_FOR_PLAYERS;
    public static int MAX_AMMO;
    public static int MAX_SHIELDS_IN_A_ROW;

    public static int get(String name){
        switch (name){
            case "MIN_PLAYERS":
                return MIN_PLAYERS;
            case "MAX_PLAYERS":
                return MAX_PLAYERS;
            case "ROUND_DELAY":
                return ROUND_DELAY;
            case "WAIT_FOR_PLAYERS":
                return WAIT_FOR_PLAYERS;
            case "MAX_AMMO":
                return MAX_AMMO;
            case "MAX_SHIELDS_IN_A_ROW":
                return MAX_SHIELDS_IN_A_ROW;
        }
        return 0;
    }

    public static void set(String name, int value){
        switch (name){
            case "MIN_PLAYERS":
                 MIN_PLAYERS = value;
            case "MAX_PLAYERS":
                MAX_PLAYERS = value;
            case "ROUND_DELAY":
                ROUND_DELAY = value;
            case "WAIT_FOR_PLAYERS":
                WAIT_FOR_PLAYERS = value;
            case "MAX_AMMO":
                MAX_AMMO = value;
            case "MAX_SHIELDS_IN_A_ROW":
                MAX_SHIELDS_IN_A_ROW = value;
        }
    }

}
