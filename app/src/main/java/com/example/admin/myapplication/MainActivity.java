package com.example.admin.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.app.ActionBar.LayoutParams;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.example.admin.myapplication.Activities.Game;
import com.example.admin.myapplication.GameStatics.CustomDialog;
import com.example.admin.myapplication.GameStatics.ListViewAdapter;
import com.example.admin.myapplication.GameStatics.Obtainer;
import com.example.admin.myapplication.GameStatics.ServerItem;
import com.example.admin.myapplication.GameStatics.Utils;
import com.example.admin.myapplication.Variables.Variables;
import com.yalantis.phoenix.PullToRefreshView;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

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
    TextView ipField;
    public static MainActivity mainActivity;
    ListView serversView;
    Context c;
    ViewPager viewPager;
    ArrayList prgmName;
    CustomDialog cdd;
    public static int prgmImage=R.drawable.logo_cropped;
    public static String [] prgmNameList={"Let Us C","c++","JAVA"};
    public static String [] players={"3","5","7"};
    final Long REFRESH_DELAY = 1000L;
    PullToRefreshView mPullToRefreshView;
    boolean click = true;

    List<Integer> ports;
    List<String> addresses;
    List<ServerItem> serverItems;
    ArrayList<HashMap<String,String>> items;
    
    private Object lock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new SampleFragmentPagerAdapter(this.getSupportFragmentManager()));

        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);

        c=this;

        mainActivity=this;

        tabsStrip.setViewPager(viewPager);
        stateField = (TextView) findViewById(R.id.stateText);
        connect = (CircleButton) findViewById(R.id.connectToServerButton);
        ipField = (TextView) findViewById(R.id.ipField);
    }

    public void register(View v) throws InterruptedException {
        ConnectivityManager m
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        ipField = (TextView) findViewById(R.id.ipField);
        if(new Utils().hasInternetConnection(m))
            if(validIP(String.valueOf(ipField.getText())))
                register();
            else
                setText("Please enter a valid IP address");
        else
            setText("Please connect to internet!");
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
            return !ip.endsWith(".");

        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    public void register() {
        connect.setEnabled(false);
        connect.setColor(Color.parseColor("#FF344869"));
        new Thread(new Runnable() {
            @Override
            public void run() {
                int port = Utils.getDefaultsInt("settings", "port", MainActivity.this);
                TextView nameField = (TextView) findViewById(R.id.nameField);
                try {
                    Log.e("Port", String.valueOf(port));
                    Utils.register(InetAddress.getByName(ipField.getText().toString().trim()), port, nameField.getText().toString().trim(), null);
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
                                    if (Utils.connected) {
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
                                    connect.setBackgroundColor(Utils.getColorFromRes(R.drawable.fusster_color_disabled));
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

    public void removeServer(int i) {
        serversView.removeViewAt(i);
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
        startActivity(new Intent(this, Game.class));
        finish();
    }

    public void setText(String str) {
        stateField.setText(str);
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
            } else {
                Toast.makeText(MainActivity.this, "Please enter a valid port number between 1 and 65535!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Please enter a valid port number!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void actuallyAddServer(String ip, String port) {
        String server = ip + ":" + port;
        if(Utils.getDefaultsString("servers","servers",MainActivity.this).equals("matchmaking.fusster.eu"))
            Utils.setDefaults("servers", server, MainActivity.this);
        else {
            String currentServers = Utils.getDefaultsString("servers","servers",MainActivity.this);
            Utils.setDefaults("servers", currentServers + "," + server, MainActivity.this);
        }

    }

    public void refreshServers() {
        items = new ArrayList<HashMap<String,String>>();
        Log.e("test", "Libraryy");
        Log.e("Servers value", String.valueOf(Utils.getDefaultsString("servers", "servers", MainActivity.this)));
        if(!Utils.getDefaultsString("servers","servers",MainActivity.this).equals("matchmaking.fusster.eu")) {
            synchronized (lock) {
                String[] servers = Utils.getDefaultsString("servers", "servers", MainActivity.this).split(",");
                Log.e("Servers split 1 size", String.valueOf(servers.length));
                serverItems = new ArrayList<ServerItem>();
                ports = new ArrayList<Integer>();
                addresses = new ArrayList<String>();
                for(String s : servers)  {
                    String[] splitter = s.split(":");
                    ports.add(Integer.parseInt(splitter[1]));
                    addresses.add(splitter[0]);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized(lock){
                            serverItems = Obtainer.obtainAll(addresses, ports, 1);
                            for(ServerItem s : serverItems) {
                                HashMap<String, String> map1 = new HashMap<String, String>();
                                Log.e("Server ", s.getIp());
                                Log.e("Server ", s.getIp() + ":" + s.getPort() + ", " + s.getName());
                                if(!s.getName().equals("noServer")) {
                                    map1.put("title", s.getName());
                                    map1.put("players", String.valueOf(s.getPlayers()) + "/" + String.valueOf(s.getMaxPlayers()));
                                    map1.put("address", s.getIp() + ":" + String.valueOf(s.getPort()));
                                    items.add(map1);
                                } else {
                                    map1.put("title", "Server offline");
                                    map1.put("players", "");
                                    map1.put("address", s.getIp() + ":" + String.valueOf(s.getPort()));
                                    items.add(map1);
                                }
                            }
                            lock.notify();
                        }
                    }
                }, "Obtainer Thread").start();
                try {
                    lock.wait(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
  //      Utils.setDefaults("servers", "194.145.63.12:1243,192.168.20.20:47247,130.204.8.60:47247,85.11.164.201:47247", MainActivity.this);
        MyListAdapter myListAdapter = new MyListAdapter(MainActivity.this, items, prgmImage);
        serversView.setAdapter(myListAdapter);
        serversView.setOnCreateContextMenuListener(this);
    }

    public void addServer(View v){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                String ip = cdd.editText.getText().toString();
                String port = cdd.editText2.getText().toString();
                if(Utils.validIP(ip)) {
                    if(Utils.isPort(port)) {
                        actuallyAddServer(ip, port);
                        refreshServers();
                        cdd.dismiss();
                    } else
                        Toast.makeText(MainActivity.this, "Please enter a valid port number between 1 and 65535", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Please enter a valid ip address", Toast.LENGTH_LONG).show();
                }
            }
        };
        cdd=new CustomDialog(MainActivity.this, r, "Add a new server", "Address", "Port");
        cdd.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        cdd.show();
        cdd.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    public class SampleFragmentPagerAdapter extends android.support.v4.app.FragmentPagerAdapter{


        private String tabTitles[] = new String[] { "Game", "Settings", "Servers" };
        final int PAGE_COUNT = 3;

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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            mPage = getArguments().getInt(ARG_PAGE);
            if(mPage==1) {
                View view = inflater.inflate(R.layout.fragment_a_game, container, false);
                stateField = (TextView) view.findViewById(R.id.stateText);
                connect = (CircleButton) view.findViewById(R.id.connectToServerButton);
                return view;
            } else if(mPage==2) {
                View view = inflater.inflate(R.layout.fragment_b_settings, container, false);
                portSettingsField = (TextView) view.findViewById(R.id.portTextField);
                portSettingsField.setText(String.valueOf(Utils.getDefaultsInt("settings", "port", MainActivity.this)));
                return view;
            } else {
                View view = inflater.inflate(R.layout.fragment_c_servers, container, false);
                mPullToRefreshView = (PullToRefreshView) view.findViewById(R.id.pull_to_refresh);
                serversView =(ListView) view.findViewById(R.id.list_view);
                refreshServers();
                mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mPullToRefreshView.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Refreshing", Toast.LENGTH_SHORT).show();
                                mPullToRefreshView.setRefreshing(false);
                            }
                        }, REFRESH_DELAY);
                    }
                });
                portSettingsField.setText(String.valueOf(Utils.getDefaultsInt("settings", "port", MainActivity.this)));
                return view;
            }
        }
    }
    private class MyListAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<HashMap<String, String>> items;
        int prgmImage;

        public MyListAdapter(Context mainActivity, ArrayList<HashMap<String, String>> items, int prgmImage) {
            this.context = mainActivity;
            this.items = items;
            this.prgmImage = prgmImage;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View view = convertView;

            if (view == null) {
                LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.server_layout, null);
            }

            TextView title = (TextView) view.findViewById(R.id.textView1);
            TextView players = (TextView) view.findViewById(R.id.textView5);
            TextView address = (TextView) view.findViewById(R.id.serverAddressView);
            ImageView img = (ImageView) view.findViewById(R.id.imageView7);

            final HashMap<String, String> item = items.get(position);
            title.setText(item.get("title"));
            players.setText(item.get("players"));
            address.setText(item.get("address"));
            img.setBackgroundResource(this.prgmImage);

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    new AlertDialog.Builder(context)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Remove")
                            .setMessage("Are you sure you want to remove this server?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(context, "You Clicked " + item.get("title"), Toast.LENGTH_LONG).show();
                                    items.remove(position);
                                    notifyDataSetChanged();
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                    return false;
                }
            });

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    Toast.makeText(context, "You Clickedd " + item.get("title") + " " + item.get("address"), Toast.LENGTH_LONG).show();
                }
            });

            return view;
        }
    }
}
