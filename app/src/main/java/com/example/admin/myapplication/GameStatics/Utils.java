package com.example.admin.myapplication.GameStatics;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.Activities.ConnectActivity;
import com.example.admin.myapplication.Activities.game;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.Variables.Variables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils extends ActionBarActivity {

    private static InetAddress ip;
    private static int port;

    private static String name;
    public static List<Player> players = new ArrayList<>();

    private static TextView console, shotsCurrentText, maxShotsText, shieldsLeftText;

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
        Log.e("Sent", str);
        if(clientSocket!=null) {
            if (clientSocket.isConnected() && !clientSocket.isClosed())
                outToServer.println(str);
        } else
            disconnected();
    }

    private static void receive() {
        new Thread(new Runnable() {
            public void run() {
                while (!clientSocket.isClosed()) {
                    try {
                        String fromServer = inFromServer.readLine();
                        if (fromServer != null) {
                            Log.e("Recieved",fromServer);
                            if (fromServer.startsWith("reg:") && fromServer.length() > 4) {
                                String[] firstSplit = fromServer.split(":");
                                if(game.isFirstRound) {
                                    Utils.players.clear();
                                    for (String playerName : Arrays.asList(firstSplit[1].split(","))) {
                                        Utils.players.add(new Player(playerName));
                                        append(playerName + " joined the game.");
                                    }
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
                                if (split[1].trim().equals("newRound")) {
                                    Log.e("newRound","mhm");
                                    game.activity.runOnUiThread(new Runnable() {
                                           @Override
                                       public void run() {
                                               game.activity.doRound(Variables.allVariables.get("ROUND_DELAY"));
                                                }
                                            });
                                    Utils.append("Starting next round");
                                } else {
                                    Utils.append(split[1]);
                                }
                            }
                            if (fromServer.startsWith("shield:")) {
                                String[] firstSplit = fromServer.split(":");
                                append(firstSplit[1] + " played shield this round");
                                Player p = getPlayer(firstSplit[1]);
                                p.setShieldsInARow(Integer.parseInt(firstSplit[2]));
                            }
                            if (fromServer.startsWith("shoot:")) {
                                final String[] firstSplit = fromServer.split(":");
                                if(!firstSplit[1].equals(getName())) {
                                    Player p = getPlayer(firstSplit[1]);
                                    p.setShots(p.getShots() - 1);
                                    p.setShieldsInARow(Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW"));
                                } else {
                                    append("You shot" + firstSplit[2]);
                                }
                                if(firstSplit[3].equals("fail")) {
                                    Log.e("fail", "fail");
                                    append(firstSplit[1] + " tried to shoot " + firstSplit[2] + ", but " + firstSplit[2] + " defended");
                                } else if(firstSplit[3].equals("success")) {
                                    Log.e("success", firstSplit[2]);
                                    append(firstSplit[1] + " shot " + firstSplit[2]);
                                  //  if()
                                    game.activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(firstSplit[2].equals(name))
                                                disableButtons(null, "You have been shot by: " + firstSplit[1]);
                                            else
                                                game.activity.removePlayer(firstSplit[2]);
                                        }
                                    });
                                }
                            }

                            if (fromServer.startsWith("reload:")) {
                                String[] firstSplit = fromServer.split(":");
                                append(firstSplit[1] + " reloaded this round");
                                Player p = getPlayer(firstSplit[1]);
                                p.setShots(p.getShots()+1);
                                p.setShieldsInARow(Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW"));
                            }

                            if (fromServer.startsWith("shot:")) {
                                String[] firstSplit = fromServer.split(":");
                                disableButtons(null, "You have been shot by: " + firstSplit[1]);
                            }

                            if (fromServer.startsWith("close")) {
                                clientSocket.close();
                                append("You have been disconnected from the server!");
                                disconnected();
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

    public static void disconnect() {
        try {
            send("close");
            clientSocket.close();
            game.activity.finishAndRestart();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void disconnected() {
        append("You have been disconnected from the server!");
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        game.activity.finishAndRestart();
    }

    public static void append(final String str) {

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

    public boolean hasInternetConnection(ConnectivityManager m) {
        NetworkInfo activeNetworkInfo = m.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return connected;
    }

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static int getDefaults(String key, String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        int keyValue = prefs.getInt(value, 9876);
        return keyValue;
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    public static void enableButtons(final String button, final String str) {
        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (button != null) {
                    switch (button) {
                        case "shoot":
                            shoot.setBackgroundResource(R.drawable.round_left_blue_fusster);
                            shoot.setEnabled(true);
                        case "shield":
                            shield.setBackgroundColor(Color.parseColor("#576cc7"));
                            shield.setEnabled(true);
                        case "reload":
                            reload.setBackgroundResource(R.drawable.round_right_blue_fusster);
                            reload.setEnabled(true);
                    }
                } else {
                    shoot.setBackgroundResource(R.drawable.round_left_blue_fusster);
                    shield.setBackgroundColor(Color.parseColor("#576cc7"));
                    reload.setBackgroundResource(R.drawable.round_right_blue_fusster);
                    shoot.setEnabled(true);
                    shield.setEnabled(true);
                    reload.setEnabled(true);
                }
                if (str != null)
                    append(str);
            }
        });
    }

    public static void disableButtons(final String button, final String str) {
        game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (button != null) {
                    switch (button) {
                        case "shoot":
                            shoot.setBackgroundResource(R.drawable.round_up_blue_fusster);
                            shoot.setEnabled(false);
                        case "shield":
                            shield.setBackgroundColor(Color.parseColor("#FF748D9A"));
                            shield.setEnabled(false);
                        case "reload":
                            reload.setBackgroundResource(R.drawable.round_down_blue_fusster);
                            reload.setEnabled(false);
                    }
                } else {
                    shoot.setBackgroundResource(R.drawable.round_left_grey_fusster);
                    shield.setBackgroundColor(Color.parseColor("#FF748D9A"));
                    reload.setBackgroundResource(R.drawable.round_right_grey_fusster);
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

    public static List<Player> getPlayers(){
        return players;
    }
    public static Player getPlayer(String name) {
        for(Player p:Utils.getPlayers()) {
            if(p.getName().equals(name))
                return p;
        }
        return null;
    }
    public static Player getPlayerByLayoutId(int id) {
        for(Player p:Utils.getPlayers()) {
            if(p.playerLayout.getId()==id)
                return p;
        }
        return null;
    }
}
