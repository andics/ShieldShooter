package com.example.admin.myapplication.Variables;

import java.util.HashMap;

/**
 * Created by Admin on 07/11/2015.
 */
public class Variables {
    public static HashMap<String, Integer> allVariables = new HashMap<String, Integer>();
    public static int RESPOND_WAIT = 10;
    public static int CONNECTION_WAIT = 10;
    public static void set(String name, int value){
        allVariables.put(name,value);
    }

}
