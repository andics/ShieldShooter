package com.example.admin.myapplication.GameStatics;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.example.admin.myapplication.Activities.game;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.Variables.Variables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils extends ActionBarActivity {

    private static InetAddress ip;
    private static int port;

    private static String name;
    private static List<String> players = new ArrayList<>();

    private static TextView console;

    private static Button shoot;
    private static Button shield;
    private static Button reload;

    public static boolean connectedToServer, connected=false;

    final static String COLOR_RED = "#E13131";
    final static String COLOR_BLACK = "#000000";
    final static String COLOR_BLUE = "#38B9E8";

    public static PrintWriter outToServer;
    public static Socket clientSocket;
    public static BufferedReader inFromServer;

    public static void register(InetAddress ip, int port, String name) {
        try {
            clientSocket = new Socket(ip, port);
            if (clientSocket.isConnected() == true) Log.e("BUG", "connected");
            else Log.e("BUG", "not connected");
            Utils.ip = ip;
            Utils.port = port;
            Utils.name = name;
            outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            connectedToServer=true;

            send("reg:" + name);
            receive();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BUG", "Socket failed to connect!");
        }
    }

    //That method is called many times after "send("reg:" + name);"
    public static void send(String str)
    {
        if(clientSocket.isConnected() && !clientSocket.isClosed())
        outToServer.println(str);
        else
            disconnect();
    }

    private static void receive() {
        new Thread(new Runnable() {
            public void run() {
                while (!clientSocket.isClosed()) {
                    try {
                        String fromServer = inFromServer.readLine();
                        if (fromServer != null) {
                            if (fromServer.startsWith("reg:") && fromServer.length() > 4) {
                                String[] firstSplit = fromServer.split(":");
                                Utils.players = Arrays.asList(firstSplit[1].split(","));
                                for (String player : Utils.players) {
                                    append(player + " joined the game");
                                }
                            }

                            if (fromServer.startsWith("var:")) {
                                String[] firstSplit = fromServer.split(":");
                                String[] secondSplit = firstSplit[1].split("-");
                                Variables.allVariables.put(secondSplit[0], Integer.parseInt(secondSplit[1].trim()));
                                Log.e(secondSplit[0], secondSplit[1].trim());
                            }

                            if (fromServer.startsWith("msg:")) {
                                String[] split = fromServer.split(":");
                                if (split[1].equals("newRound")) {
                                    game.activity.runOnUiThread(new Runnable() {
                                           @Override
                                       public void run() {
                                               game.activity.doRound(15);
                                                }
                                            });
                                    Utils.append("Starting next round");
                                } else {
                                    Utils.append(split[1]);
                                }
                            }

                            if (fromServer.startsWith("shot:")) {
                                String[] firstSplit = fromServer.split(":");
                                dissableButtons(null , "You have been shot by: "+ firstSplit[1]);
                            }

                            if (fromServer.startsWith("close")) {
                                clientSocket.close();
                                append("You have been disconnected from the server!");
                                disconnect();
                                return;
                            }
                            if (fromServer.startsWith("connected")) {
                                   Log.e("RECIEVED", "CONNECTED");
                                connected = true;
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        disconnect();
                    }
                }
            }
        }).start();
    }

    private static void disconnect() {
        append("You have been disconnected from the server!");
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        game.activity.finishAndRestart();
    }

    private static void append(final String str) {

        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        /*        Spannable word = new SpannableString("Your message");
                word.setSpan(new ForegroundColorSpan(Color.BLUE), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                console.colosetSpan(new ForegroundColorSpan(Color.RED), 0, 5, 0); */
                console.append("\n" + str + "\n");
            }
        });
    }

    public static void enableButtons(final String button, final String str) {
        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(button!=null) {
                    switch (button) {
                        case "shoot":
                            shoot.setBackgroundResource(R.drawable.round_blue_fusster);
                            shoot.setEnabled(true);
                        case "shield":
                            shield.setBackgroundResource(R.drawable.round_blue_fusster);
                            shield.setEnabled(true);
                        case "reload":
                            reload.setBackgroundResource(R.drawable.round_blue_fusster);
                            reload.setEnabled(true);
                    }
                } else {
                        shoot.setBackgroundResource(R.drawable.round_blue_fusster);
                        shield.setBackgroundResource(R.drawable.round_blue_fusster);
                        reload.setBackgroundResource(R.drawable.round_blue_fusster);
                        shoot.setEnabled(true);
                        shield.setEnabled(true);
                        reload.setEnabled(true);
                    }
                if(str!=null)
                    append(str);
            }
        });
    }

    public static void dissableButtons(final String button, final String str) {
        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (button != null) {
                    switch (button) {
                        case "shoot":
                            shoot.setBackgroundResource(R.drawable.round_up_blue_fusster);
                            shoot.setEnabled(false);
                        case "shield":
                            shield.setBackgroundColor(Color.parseColor("#6E6E6E"));
                            shield.setEnabled(false);
                        case "reload":
                            reload.setBackgroundResource(R.drawable.round_down_blue_fusster);
                            reload.setEnabled(false);
                    }
                } else {
                    shoot.setBackgroundResource(R.drawable.round_up_blue_fusster);
                    shield.setBackgroundColor(Color.parseColor("#FF748D9A"));
                    reload.setBackgroundResource(R.drawable.round_down_blue_fusster);
                    shoot.setEnabled(false);
                    shield.setEnabled(false);
                    reload.setEnabled(false);
                }
                if (str != null)
                    append(str);
            }
        });
    }

    public static void setUnits(TextView console, Button shoot, Button shield, Button reload){
        console.setText("");
        Utils.console = console;
        Utils.shoot = shoot;
        Utils.shield = shield;
        Utils.reload = reload;
    }

    public static String getName() {
        return name;
    }

    public static List<String> getPlayers(){
        return players;
    }
}
