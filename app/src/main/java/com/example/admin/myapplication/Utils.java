package com.example.admin.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {

    private static InetAddress ip;
    private static int port;

    private static String name;
    private static List<String> players = new ArrayList<>();

    static String sendData;
    static String receiveData;

    public static PrintWriter outToServer;
    public static BufferedReader inFromServer;

    private static Socket clientSocket;

    private static TextView console;

    public static void register(InetAddress ip, int port, String name) {
        try {
            Utils.ip = ip;
            Utils.port = port;
            Utils.name = name;


            send("reg:" + name);

           new Thread(new Runnable() {
                @Override
                public void run() {
            //        receive();
                }
            }).start();


            //new

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void send(final String str) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket(Utils.ip, Utils.port);
                    outToServer = new PrintWriter(clientSocket.getOutputStream(), true);
                    outToServer.println(str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
            }
    private static void receive() {
        while (true) {
            try {
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 String fromServer = inFromServer.readLine().trim();

                if (fromServer.startsWith("reg:") && fromServer.length() > 4) {
                    String[] firstSplit = fromServer.split(":");
                    Utils.players = Arrays.asList(firstSplit[1].split(","));
                }

                if (fromServer.startsWith("var:")) {
                    String[] firstSplit = fromServer.split(":");
                    String[] secondSplit = firstSplit[1].split("-");
                    Variables.set(secondSplit[0], Integer.parseInt(secondSplit[1]));
                }

                if (fromServer.startsWith("msg:")) {
                    String[] split = fromServer.split(":");
                    Utils.append(split[1].trim());
                    if (split[1].trim().equals("Starting next round")) {
                        inGameActivity.activity.doRound();
                    }
                }

                if (fromServer.startsWith("close")) {
                    clientSocket.close();
                    return;
                }

            } catch (IOException e) {
                e.printStackTrace();
            /*    if(e instanceof SocketException) {
                    try {
                        clientSocket = new Socket(InetAddress.getByName("130.204.8.60"), 9876);
                    } catch (IOException e1) {
                        e.printStackTrace();
                    }
            } */
        }
    }

}


    public static void append(final String str) {

        inGameActivity.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                console.append(str);
            }
        });
    }

    public static void setConsole(TextView console){
        console.setText("");
        Utils.console = console;
    }

    public static String getName() {
        return name;
    }

    public static List<String> getPlayers(){
        return players;
    }

}
