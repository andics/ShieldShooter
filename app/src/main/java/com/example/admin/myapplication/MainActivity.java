package com.example.admin.myapplication;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.app.ActionBar.LayoutParams;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.example.admin.myapplication.Activities.ConnectActivity;
import com.example.admin.myapplication.Activities.game;
import com.example.admin.myapplication.GameStatics.Player;
import com.example.admin.myapplication.GameStatics.Utils;
import com.example.admin.myapplication.Variables.Variables;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import at.markushi.ui.CircleButton;

public class MainActivity extends FragmentActivity {

    int seconds, secondsRespond;
    Timer t;
    CircleButton connect;
    TextView stateField, portSettingsField;
    PopupWindow popUp;
    LinearLayout layout;
    TextView tv;
    LayoutParams params;
    LinearLayout mainLayout;
    ViewPager viewPager;
    boolean click = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(this.getSupportFragmentManager()));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);
        stateField = (TextView) findViewById(R.id.stateText);
        connect = (CircleButton) findViewById(R.id.connectToServerButton);
    }

    public void register(View v) throws InterruptedException {
        ConnectivityManager m
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(new Utils().hasInternetConnection(m))
            register();
        else
            setText("Please connect to internet!");

    }

    public void register() {
        connect.setEnabled(false);
        connect.setColor(Color.parseColor("#FF344869"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                int port = Utils.getDefaults("settings", "port", MainActivity.this);
                TextView nameField = (TextView) findViewById(R.id.nameField);
                TextView ipField = (TextView) findViewById(R.id.ipField);
                try {
                    Log.e("Port", String.valueOf(port));
                    Utils.register(InetAddress.getByName(ipField.getText().toString().trim()), port, nameField.getText().toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        t= new Timer();
        seconds=0;
        secondsRespond= Variables.RESPOND_WAIT;
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (seconds <= Variables.CONNECTION_WAIT) {
                            if (Utils.clientSocket != null && secondsRespond > 0) {
                                if (Utils.clientSocket.isConnected() == true) {
                                    secondsRespond--;
                                    setText("Connecting..." + secondsRespond + " seconds left");
                                    Log.e("Connected", "still counting...");
                                    Log.e("Connected", "true");
                                    if(Utils.connected) {
                                        setText("Connected! Heading to game");
                                        try {
                                            t.cancel();
                                            switchActivity();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                                if (secondsRespond == 0) {
                                    Log.e("soconds", "0");
                                    setText("Connecting failed");
                                    connect.setEnabled(true);
                                    connect.setBackgroundResource(R.drawable.round_very_green);
                                    try {
                                        t.cancel();
                                        Utils.clientSocket.close();
                                    } catch (IOException e) {
                                        Log.e("bug", "fail to close the socket on fail");
                                    }
                                }
                            } else if (Utils.clientSocket == null) {
                                setText("Attempting to connect..." + (Variables.CONNECTION_WAIT - seconds) + " seconds left");
                                seconds++;
                            }
                        } else {
                            t.cancel();
                            Log.e("Failed", "Failed");
                            setText("Connecting failed");
                            connect.setEnabled(true);
                            connect.setColor(Color.parseColor("#FF55A3CD"));
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public void disconnect() {
        if(Utils.clientSocket!=null) {
            if (Utils.clientSocket.isConnected() == true) {
                try {
                    Log.e("recurse", "Trying begining!");
                    Utils.clientSocket.close();
                    Log.e("recurse","Trying!");
                    Utils.clientSocket=null;
                } catch (IOException e) {
                    Log.e("bug", "fail to close the socket on fail");
                }
            }
        }
    }

    private void switchActivity() throws InterruptedException {
        setText("Connected! Heading to game");
        Thread.sleep(2000);
        startActivity(new Intent(this, game.class));
        finish();
    }

    public void setText(String str) {
        stateField.setText(str);
    }

    private void setTextConnecting() {
        stateField.setText("Connecting...");
    }

    public void saveSettings(View v){
        TextView portTextView = (TextView) findViewById(R.id.portTextField);
        if(Utils.isNumeric(portTextView.getText().toString())) {
            if(Integer.parseInt(portTextView.getText().toString())>0 && Integer.parseInt(portTextView.getText().toString())<65535) {
                int port = Integer.parseInt(portTextView.getText().toString());
                SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
                SharedPreferences.Editor edit = preferences.edit();

                edit.putInt("port", port);
                edit.apply();
                Toast.makeText(MainActivity.this, "Settings saved!!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Please enter a valid port number!", Toast.LENGTH_LONG).show();
        }

    }

    public void goToConnectScreen(View v){
        ConnectivityManager m
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(new Utils().hasInternetConnection(m)) {
            startActivity(new Intent(this, ConnectActivity.class));
            finish();
        } else {
            Toast.makeText(MainActivity.this, "Please make sure you are connected to internet!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class SampleFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter{


        private String tabTitles[] = new String[] { "Game", "Settings" };
        final int PAGE_COUNT = 2;

        public SampleFragmentPagerAdapter(android.support.v4.app.FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            PageFragment p = new PageFragment();
            return p.newInstance(position + 1);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }}

    public class PageFragment extends Fragment {
        private int mPage;
        public static final String ARG_PAGE = "ARG_PAGE";

        public PageFragment newInstance(int page) {
            Bundle args = new Bundle();
            args.putInt(ARG_PAGE, page);
            PageFragment fragment = new PageFragment();
            fragment.setArguments(args);
            return fragment;
        }

        // Inflate the fragment layout we defined above for this fragment
// Set the associated text for the title
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mPage = getArguments().getInt(ARG_PAGE);
            if(mPage==1) {
                View view = inflater.inflate(R.layout.fragment_a_game, container, false);
                stateField = (TextView) view.findViewById(R.id.stateText);
                connect = (CircleButton) view.findViewById(R.id.connectToServerButton);
                return view;
            } else {
                View view = inflater.inflate(R.layout.fragment_b_settings, container, false);
                portSettingsField = (TextView) view.findViewById(R.id.portTextField);
                Log.e("test", "testing");
                portSettingsField.setText(String.valueOf(Utils.getDefaults("settings", "port", MainActivity.this)));
                return view;
            }
        }
    }
}
