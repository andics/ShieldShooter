package com.example.admin.myapplication.GameStatics;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.myapplication.Activities.Game;
import com.example.admin.myapplication.Activities.Match;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.Variables.Variables;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Utils extends ActionBarActivity {

    private static InetAddress ip;
    private static int port;
    private static int round=1;
    private static int secondsTicked=0;
    private static int currentProgress=0;

    private static String name;
    public static List<Player> players = new ArrayList<>();
    public static List<String> triedToKill = new ArrayList<>();

    private static TextView console, shotsCurrentText, maxShotsText, shieldsLeftText, gameStateText, winsText;

    private static Button shoot;
    private static Button shield;
    private static Button reload;

    public static boolean connectedToServer, connected=false, matchFound=false, interrupted=false, isAlive=true;

    public static int foundServerPort;
    public static InetAddress matchMakingIp;
    public static InetAddress foundServerIp;

    final static String COLOR_RED = "#E13131";
    final static String COLOR_BLACK = "#000000";
    final static String COLOR_BLUE = "#38B9E8";

    public static PrintWriter outToServer;
    public static Socket clientSocket;
    public static BufferedReader inFromServer;

    public static void register(InetAddress ip, int port, String name, String matchNum) {
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
            if(matchNum==null)
            send("reg:" + name);
            else
                send("reg:" + name + ":" + matchNum);
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
                                    Utils.players.clear();
                                    for (String playerName : Arrays.asList(firstSplit[1].split(","))) {
                                        Utils.players.add(new Player(playerName));
                                        append(playerName + " joined the game.");
                                    }
                            }

                            if (fromServer.startsWith("var:")) {
                                String[] firstSplit = fromServer.split(":");
                                String[] secondSplit = firstSplit[1].split("-");
                                Variables.allVariables.put(secondSplit[0], Integer.parseInt(secondSplit[1].trim()));
                                Log.e(secondSplit[0], secondSplit[1].trim());
                            }

                            if (fromServer.startsWith("server:")) {
                                String[] firstSplit = fromServer.split(":");
                                clientSocket.close();
                                foundServerIp = InetAddress.getByName(firstSplit[1]);
                                foundServerPort = Integer.parseInt(firstSplit[2]);
                                Match.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Match.activity.switchActivity();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                          //      register(InetAddress.getByName(firstSplit[1]), Integer.parseInt(firstSplit[2]), getName(), null);
                                matchFound=true;
                            }

                            if(fromServer.equals("gameOver")) {
                                    state("Game over. Going back to main screen");
                                Game.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Game.activity.finishAndRestart();
                                    }
                                });
                            }

                            if (fromServer.startsWith("noWinner")) {
                                isAlive=true;
                                Game.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Game.activity.restartGame();
                                        Game.activity.interruptTimer();
                                        state("No winners last round");
                                    }
                                });
                            }

                            if (fromServer.startsWith("winner:")) {
                                round=0;
                                final String[] firstSplit = fromServer.split(":");
                                isAlive=true;
                                if(firstSplit[1].equals(name)) {
                                    Game.activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            winsText.setText(String.valueOf(Game.wins));
                                            Log.e("restart", "restart");
                                            Game.activity.restartGame();
                                            Game.activity.interruptTimer();
                                        }
                                    });
                                    state("You won the last round");
                                    Game.wins++;
                                } else {
                                    Player p = getPlayer(firstSplit[1]);
                                    p.setWins(p.getWins()+1);
                                    Game.activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.e("restart", "restart");
                                            Game.activity.restartGame();
                                            Game.activity.interruptTimer();
                                        }
                                    });
                                    state(firstSplit[1] + " won the last round");
                                }
                            }

                            if (fromServer.startsWith("disc:")) {
                                final String[] firstSplit = fromServer.split(":");
                                Log.e("Lenght",String.valueOf(firstSplit.length));
                                if(firstSplit.length==3) {
                                    if (getPlayer(firstSplit[1]) != null) {
                                        Game.activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Game.activity.disconnectPlayer(firstSplit[1]);
                                                Log.e("Toast", String.valueOf(firstSplit.length));
                                                Game g = new Game();
                                                g.makeToast(firstSplit[1] + " disconnected from the game");
                                            }
                                        });
                                        state(firstSplit[1] + " disconnected from the game");
                                        append(firstSplit[1] + " disconnected from the game. Reason: " + firstSplit[2]);
                                    }
                                } else {
                                    Runnable r = new Runnable() {
                                        @Override
                                        public void run() {
                                            disconnect();
                                        }
                                    };
                                    setUpATimer(3, "You've been disconnected from game!", r);
                                }
                            }

                            if (fromServer.startsWith("msg:")) {
                                String[] split = fromServer.split(":");
                                if (split[1].trim().equals("newRound")) {
                                    interrupted=false;
                                    Game.activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Game.activity.doRound(Variables.allVariables.get("ROUND_DELAY"));
                                        }
                                    });
                                    if(round>0)
                                    Utils.append("-End of round " + String.valueOf(round) + "-");
                                    Utils.append("Starting next round");
                                    Utils.logTriedToKill();
                                    round++;
                                }
                                if(!split[1].trim().equals("newGame") && !split[1].trim().equals("newRound")) {
                                    Utils.append(split[1]);
                                }
                            }
                            if (fromServer.startsWith("shield:")) {
                                Utils.logTriedToKill();
                                String[] firstSplit = fromServer.split(":");
                                append(firstSplit[1] + " played shield this round");
                                Player p = getPlayer(firstSplit[1]);
                                p.setShieldsInARow(Integer.parseInt(firstSplit[2]));
                            }
                            if (fromServer.startsWith("waitPlayers:")) {
                                String[] firstSplit = fromServer.split(":");
                                setUpATimer(Integer.valueOf(firstSplit[1]), "Starting a game", null);
                                Log.e("wait", "players");
                                state("Waiting for additional players to connect");
                            }
                            if (fromServer.startsWith("shoot:")) {
                                final String[] firstSplit = fromServer.split(":");
                                if (!firstSplit[1].equals(getName())) {
                                    Player p = getPlayer(firstSplit[1]);
                                    p.setShots(p.getShots() - 1);
                                    p.restartShields();
                                }
                                if (firstSplit[3].equals("fail")) {
                                    Log.e("fail", "fail");
                                    if (firstSplit[2].equals(name)) {
                                        triedToKill.add(firstSplit[1]);
                                    }
                                    append(firstSplit[1] + " tried to shoot " + firstSplit[2] + ", but " + firstSplit[2] + " defended");
                                } else if (firstSplit[3].equals("success")) {
                                    Log.e("success", firstSplit[2]);
                                    if(firstSplit[1].equals(getName())) {
                                        append("You" + " shot " + firstSplit[2]);
                                        Log.e("shot", firstSplit[2]);
                                        Game.activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Game g = new Game();
                                                g.makeToast("You" + " shot " + firstSplit[2]);
                                            }
                                        });
                                    }
                                    else
                                    append(firstSplit[1] + " shot " + firstSplit[2]);
                                    if (firstSplit[2].equals(name)) {
                                        Game.activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                disableButtons("all", null);
                                                Utils.state("You have been shot by: " + firstSplit[1] + " and now you're spectating");
                                                isAlive=false;
                                                Log.e("ded", "son");
                                            }
                                        });
                                    } else {
                                        Game.activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Game.activity.removePlayer(firstSplit[2]);
                                            }
                                        });
                                    }
                                }
                            }

                            if (fromServer.startsWith("reload:")) {
                                String[] firstSplit = fromServer.split(":");
                                if(!firstSplit[1].equals(getName())) {
                                    append(firstSplit[1] + " reloaded this round");
                                    Player p = getPlayer(firstSplit[1]);
                                    if(p!=null) {
                                        p.setShots(Integer.parseInt(firstSplit[2]));
                                        p.restartShields();
                                    } else
                                        disconnect();
                                }
                            }

                            if (fromServer.startsWith("shot:")) {
                                String[] firstSplit = fromServer.split(":");
                                disableButtons("all", "You have been shot by: " + firstSplit[1]);
                            }
                            if (fromServer.startsWith("waitRound:")) {
                                logTriedToKill();
                                isAlive=true;
                                String[] firstSplit = fromServer.split(":");
                                Game.activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Game.activity.interruptTimer();
                                    }
                                });
                                setUpATimer(Integer.parseInt(firstSplit[1]), null, null);
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
                }catch(IOException e){
                    e.printStackTrace();
                    if (clientSocket.isConnected())
                        if(Game.activityInitialized=true)
                        disconnect();
                        else
                            disconnectFromMM();
                }
            }
        }
    }).start();
}

    public static void logTriedToKill() {
        String string = triedToKill.toString();
        Log.e("In ", "logTriedToKill");
        if (!triedToKill.isEmpty()) {
            Log.e("In ","Tried to kill");
            state(string.substring(1, string.length() - 1) + " tried to shoot you");
            triedToKill.clear();
        }
    }

    public static void disconnect() {
        try {
            if(Game.activityInitialized) {
                Game.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Disconnected"," from the game");
                        Toast.makeText(Game.activity, "You have been disconnected from the server", Toast.LENGTH_LONG);
                    }
                });
                Game.activity.finishAndRestart();
            }
            clientSocket.close();
            Game.isFirstRound=true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void disconnectFromMM() {
        try {
            if(clientSocket!=null)
            clientSocket.close();
            else
                clientSocket=null;
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
        Game.activity.finishAndRestart();
    }

    public static void setUpATimer(final int sec, final String str, final Runnable r) {
        final Timer t = new Timer();
        final CircularProgressBar timer = (CircularProgressBar) Game.activity.findViewById(R.id.timer);
        final TextView timeLeft = (TextView) Game.activity.findViewById(R.id.timerText);
        if(str!=null)
        state(str);
        restartTimerProgress();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Game.activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (secondsTicked <= sec - 2) {
                            timer.setColor(getColorFromRes(R.drawable.fusster_green));
                            timeLeft.setText(Integer.toString(sec - secondsTicked -2));
                            currentProgress += 100 / sec;
                            timer.setProgress(currentProgress);
                            secondsTicked++;
                        } else {
                            if(r!=null)
                            r.run();
                            t.cancel();
                            Log.e("sdfsd", "sdfsd");
                            interrupted = false;
                            secondsTicked = 0;
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public static boolean validIP (String ip) {
        try {
            if ( ip == null || ip.isEmpty() ) {
                return false;
            }

            String[] parts = ip.split( "\\." );
            if ( parts.length != 4 ) {
                return false;
            }

            for ( String s : parts ) {
                int i = Integer.parseInt( s );
                if ( (i < 0) || (i > 255) ) {
                    return false;
                }
            }
            if ( ip.endsWith(".") ) {
                return false;
            }

            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public static boolean isPort(String value)
    {
        if (value==null)
            return false;

        if (isNumeric(value))
        {
                if (Integer.parseInt(value) < 65536 && Integer.parseInt(value) > 0)
                    return true;
                else
                    return false;
        }

        return false;
    }

    public static void restartTimerProgress() {
        final CircularProgressBar timer = (CircularProgressBar) Game.activity.findViewById(R.id.timer);
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timer.setProgress(0);
            }
        });
    }

    public static void append(final String str) {

        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                console.append("\n" + str + "\n");
            }
        });
    }

    public static int getColorFromRes(int res) {
        try {
            Drawable d = Game.activity.getResources().getDrawable(res);
            return ((ColorDrawable) d).getColor();
        } catch(NullPointerException e) {
            return 0;
        }
    }

    public static void state(final String str) {
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            gameStateText.setText(str);
            }
        });
    }

    public boolean hasInternetConnection(ConnectivityManager m) {
        NetworkInfo activeNetworkInfo = m.getActiveNetworkInfo();
        boolean connected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return connected;
    }

    public static void fadeIn(View v) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(v, "alpha", 1f, .3f);
        fadeOut.setDuration(2000);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(v, "alpha", .3f, 1f);
        fadeIn.setDuration(2000);

        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeIn);
        mAnimationSet.start();
    }

    public static void scaleAnimationSlow(final View v) {
        final ScaleAnimation scaleUp, scaleDown;
        scaleUp = new ScaleAnimation(1, 1.2f, 1, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(500);
        scaleDown = new ScaleAnimation(1.2f, 1, 1.2f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDown.setDuration(500);
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                        v.startAnimation(scaleDown);
                    }
                });
                v.startAnimation(scaleUp);
            }
        });
    }

    public static void scaleAnimationFast(final View v) {
        final ScaleAnimation scaleUpFaster, scaleDownFaster;
        scaleUpFaster = new ScaleAnimation(1, 1.7f, 1, 1.7f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUpFaster.setDuration(200);
        scaleDownFaster = new ScaleAnimation(1.7f, 1, 1.7f, 1, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDownFaster.setDuration(200);
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scaleUpFaster.setAnimationListener(new Animation.AnimationListener() {

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
                        v.startAnimation(scaleDownFaster);
                    }
                });
                v.startAnimation(scaleUpFaster);
            }
        });
    }

    public static void setDefaults(String key, String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static int getDefaultsInt(String key, String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        int keyValue = prefs.getInt(value, 9876);
        return keyValue;
    }

    public static String getDefaultsString(String key, String value, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        String keyValue = prefs.getString(value, "matchmaking.fusster.eu");
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
        Log.e("Buttons enabled",button + ", " + str);
        Game.activity.runOnUiThread(new Runnable() {
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
                } else if(button==null) {
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
        Log.e("Buttons disabled",button + ", " + str);
        Game.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (button != null) {
                    switch (button) {
                        case "shoot":
                            shoot.setBackgroundResource(R.drawable.round_left_grey_fusster);
                            shoot.setEnabled(false);
                        case "shield":
                            shield.setBackgroundColor(Color.parseColor("#FF748D9A"));
                            shield.setEnabled(false);
                        case "reload":
                            reload.setBackgroundResource(R.drawable.round_right_grey_fusster);
                            reload.setEnabled(false);
                    }
                } if(button.equals("all")) {
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

    public static void setUnits(TextView console, TextView gameStateText, TextView wins, Button shoot, Button shield, Button reload){
        console.setText("");
        Utils.console = console;
        Utils.winsText = wins;
        Utils.gameStateText = gameStateText;
        Utils.shoot = shoot;
        Utils.shield = shield;
        Utils.reload = reload;
    }

    public static String getName() {
        return name;
    }
    public static void setName(String str) {
        name = str;
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
