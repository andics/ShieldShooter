package com.example.admin.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
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
            new Thread(new Runnable() {
                @Override
                public void run() {
                    receive();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("BUG", "Socket failed to connect!");
        }
    }

    //That method is called many times after "send("reg:" + name);"
    public static void send(String str) {
        outToServer.println(str);
    }

    private static void receive() {
        try {
            while (connectedToServer) {
                if (clientSocket.isClosed() != true && clientSocket!=null && inFromServer!=null) {
                    if (inFromServer.readLine() != null) {
                        String fromServer = inFromServer.readLine();

                        if (fromServer.startsWith("reg:") && fromServer.length() > 4) {
                            String[] firstSplit = fromServer.split(":");
                            Utils.players = Arrays.asList(firstSplit[1].split(","));
                            for(String player: Utils.players) {
                                append(player + " joined the game");
                            }
                        }

                        if (fromServer.startsWith("var:")) {
                            String[] firstSplit = fromServer.split(":");
                            String[] secondSplit = firstSplit[1].split("-");
                            Variables.set(secondSplit[0], Integer.parseInt(secondSplit[1].trim()));
                        }

                        if (fromServer.startsWith("msg:")) {
                            String[] split = fromServer.split(":");
                            if (split[1].equals("newRound")) {
                                inGameActivity.activity.doRound();
                                Utils.append("Starting next round");
                            } else {
                                Utils.append(split[1]);
                            }
                        }

                        if (fromServer.startsWith("shot:"))
                        {
                            String[] firstSplit = fromServer.split(":");
                            dissableButtons(firstSplit[1]);
                        }

                        if (fromServer.startsWith("close")) {
                            clientSocket.close();
                            append("You have been disconnected from the server!");
                            Thread.sleep(2500);
                            inGameActivity.activity.finishAndRestart();
                            return;
                        }
                        if (fromServer.startsWith("connected")) {
                            connected = true;
                        }
                    }
                } else break;
            }
        } catch (SocketException e) {
            e.printStackTrace();
            try {
                inFromServer.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            Log.e("Null receive", "Disconnected");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static void append(final String str) {

        inGameActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        /*        Spannable word = new SpannableString("Your message");
                word.setSpan(new ForegroundColorSpan(Color.BLUE), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                console.colosetSpan(new ForegroundColorSpan(Color.RED), 0, 5, 0); */
                console.append(str + "\n");
            }
        });
    }

    private static void dissableButtons(final String str) {
        inGameActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                append("You have been shot by: "+ str + "..." + "REKT KIDDO 2 EZ GTFO");
                shoot.setBackgroundResource(R.drawable.round_up_blue_fusster);
                shield.setBackgroundColor(Color.parseColor("#FF748D9A"));
                reload.setBackgroundResource(R.drawable.round_down_blue_fusster);
                shoot.setEnabled(false);
                shield.setEnabled(false);
                reload.setEnabled(false);
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
