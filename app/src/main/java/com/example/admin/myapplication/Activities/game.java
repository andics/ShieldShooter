package com.example.admin.myapplication.Activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.content.ClipData;
import android.content.ClipDescription;
import android.graphics.drawable.Drawable;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

import com.example.admin.myapplication.GameStatics.ChooseGameMode;
import com.example.admin.myapplication.GameStatics.Player;
import com.example.admin.myapplication.GameStatics.Utils;
import com.example.admin.myapplication.MainActivity;
import com.example.admin.myapplication.R;
import com.example.admin.myapplication.Variables.Variables;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.net.InetAddress;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.admin.myapplication.GameStatics.Utils.getPlayerByLayoutId;
import static com.example.admin.myapplication.GameStatics.Utils.send;
import static com.example.admin.myapplication.GameStatics.Utils.*;

public class Game extends Activity {
        /** Called when the activity is first created. */
        private Button shoot, shield, reload;
        public static int shields;
        public static int shots=0;
        public static int wins=0;
        public int newUiOptions;
        public static boolean isFirstRound=true, isFirstResume=true, activityInitialized=false;
        public int PLAYER_WIDTH=128, PLAYER_HEIGHT=85;
        private static RelativeLayout gameContainer;
        private static final String IMAGEVIEW_TAG = "shootButton";
        private int secondsTicked, currentProgress;
        CircularProgressBar timer;
        Timer t;
        public static Game activity;
        TextView timeLeft, shieldsLeftText, shotsCurrentText, gameStateText, nameText,  winsText;
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_game);
            checkForSDKVersion();
            initialize();
            regIfMatchFound();
        }
        @Override
        public void onDestroy() {
            super.onDestroy();
            Utils.send("close");
            Utils.disconnect();
            this.finish();
        }
        @Override
        public void onBackPressed() {

        }
        @Override
        public void onResume() {
            this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
            super.onResume();
        }

        @Override
        public void onStop() {
            super.onStop();
            activityInitialized = false;
            isFirstRound=true;
            interrupted=false;
        }

        public static void regIfMatchFound() {
            if(matchFound)
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        register(foundServerIp, foundServerPort, getName(), null);
                    }
                }).start();
        }

    public void checkForSDKVersion() {
            newUiOptions = this.getWindow().getDecorView().getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= 16) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }
            if (Build.VERSION.SDK_INT >= 18) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            this.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }
    public void initialize() {
        PLAYER_WIDTH = convertToDps(PLAYER_WIDTH);
        PLAYER_HEIGHT = convertToDps(PLAYER_HEIGHT);
        timer = (CircularProgressBar)findViewById(R.id.timer);
        timeLeft = (TextView) findViewById(R.id.timerText);
        shoot = (Button) findViewById(R.id.shootButton);
        shotsCurrentText = (TextView) findViewById(R.id.shotsTextField);
        shieldsLeftText = (TextView) findViewById(R.id.shieldsTextField);
        gameStateText = (TextView) findViewById(R.id.gameStateText);
        winsText =  (TextView) findViewById(R.id.wins);
        nameText = (TextView) findViewById(R.id.yourNameText);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/gothic.TTF");
        gameStateText.setTypeface(face);
        nameText.setTypeface(face);
        gameContainer = (RelativeLayout) findViewById(R.id.game);
        shoot.setTag(IMAGEVIEW_TAG);
        activity = this;
        shield = (Button) findViewById(R.id.shieldButton);
        reload = (Button) findViewById(R.id.reloadButton);
        TextView c = (TextView) findViewById(R.id.console);
        c.setMovementMethod(new ScrollingMovementMethod());
        Utils.setUnits((TextView) findViewById(R.id.console), (TextView) findViewById(R.id.gameStateText), (TextView) findViewById(R.id.wins), (Button) findViewById(R.id.shootButton), (Button) findViewById(R.id.shieldButton), (Button) findViewById(R.id.reloadButton));
        shoot.setOnLongClickListener(new MyClickListener());
        activityInitialized=true;
        wins=0;
        isFirstRound=true;
        disableButtons("all", null);
    }
    public void newDoRound(View v) {
        Player p = Utils.getPlayer("Pesho");
        if(!isFirstRound)
        p.fadeIn();
        doRound(15);
        Utils.state("Testing");
    }
    public void killPesho(View v) {
        Player p = Utils.getPlayer("Pesho");
            p.setShieldsInARow(2);
        Utils.append("Hello");
        Utils.state("Hello " + String.valueOf(R.id.src_in));
        Log.e("kill", "pesho");
        Utils.state("Hello from the other siiiide");
        p.setWins(2);
    }
    public void restartGame() {
        isAlive=true;
        setShields(Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW"));
        setShots(Variables.allVariables.get("START_AMMO"));
        for(Player p: Utils.players) {
            p.setShieldsInARow(Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW"));
            p.setShots(Variables.allVariables.get("START_AMMO"));
            p.fadeIn();
        }
    }
    public void finishAndRestart() {
        startActivity(new Intent(this, ChooseGameMode.class));
        finish();
    }
    public void doRound(final int sec) {

        //Drawing player blocks
        try {
            t.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
            interrupted=false;
            secondsTicked = 0;
            currentProgress = 0;
            t = new Timer();
        if(isAlive) {
            Log.e("IsAlive", "In doRound true");
            if (isFirstRound) {
                Utils.state("A new round in progress");
                shields = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
                shots = Variables.allVariables.get("START_AMMO");
                wins = 0;
                isFirstRound = false;
                drawPlayersBlocks(Utils.getPlayers(), Utils.getPlayers().size());
                nameText.setText("Playing as " + Utils.getName());
                shoot.setOnLongClickListener(new MyClickListener());
            }

            if (shots > 0) {
                Utils.enableButtons("shoot", null);
            }
            if (shields > 0) {
                Log.e("SHIELDS", String.valueOf(shields));
                Utils.enableButtons("shield", null);
            } else {
                shield.setBackgroundColor(Color.parseColor("#FF748D9A"));
                shield.setEnabled(false);
            }
            if (shots < Variables.allVariables.get("MAX_AMMO")) {
                Log.e("SHOTS", String.valueOf(shots));
                Utils.enableButtons("reload", null);
            } else {
                reload.setBackgroundResource(R.drawable.round_right_grey_fusster);
                reload.setEnabled(false);
            }
            setShots(shots);
            setShields(shields);
            setWins(wins);
        }

        Drawable d = getResources().getDrawable(R.drawable.fusster_color);
        timer.setColor(((ColorDrawable) d).getColor());
        timer.setProgress(currentProgress);
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (secondsTicked <= sec && !interrupted) {
                            timeLeft.setText(Integer.toString(sec - secondsTicked));
                            currentProgress += 100 / sec;
                            timer.setProgressWithAnimation(currentProgress + 100 / sec);
                            secondsTicked++;
                        } else {
                            Log.e("Interrupted", "In doRound Timer else " + secondsTicked + " " + sec + " " + interrupted);
                            if (!interrupted)
                                timeLeft.setText("Time's up");
                            timer.setProgressWithAnimation(100);
                            Utils.disableButtons("all", null);
                            t.cancel();
                        }
                    }
                });
            }
        }, 0, 1000);
        }
    public void shoot(String name) {
        Utils.send("shoot:" + name);
        shots--;
        setShots(shots);
        endTurn("You played shoot this round!");
        restartShields();
        Log.e("Pressed", "shoot");
    }

    public void reload(View v) {
        shots++;
        setShots(shots);
        restartShields();
        send("reload");
        endTurn("You reloaded this round");
        Log.e("Pressed", "reload");
    }

    public void shield(View v) {
        send("shield");
        shields--;
        setShields(shields);
        endTurn("You played defence this round");
        Log.e("Pressed", "shield");
    }

    public void removePlayer(String str) {
        Player p = Utils.getPlayer(str);
        p.fadeOut();
    }
    public void disconnectPlayer(String str) {
        Player p = Utils.getPlayer(str);
        p.fadeOut();
        gameContainer.removeView(Utils.getPlayer(str).getPlayerLayout());
        Utils.players.remove(Utils.getPlayer(str));
    }

    public void restartShields() {
        shields = Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW");
    }
    public void exit(View v) {
        Utils.send("exit");
        Utils.disconnect();
        this.finish();
    }
    public void setShots(int shots) {
        Game.shots =shots;
        shotsCurrentText.setText(String.valueOf(Game.shots));
    }
    public void setShields(int shields) {
        Game.shields =shields;
        shieldsLeftText.setText(String.valueOf(Game.shields));
    }
    public void setWins(int wins) {
        Game.wins =wins;
        winsText.setText(String.valueOf(Game.wins));
    }
    public void endTurn(String str) {
        Utils.disableButtons("all", str);
        Drawable d = getResources().getDrawable(R.drawable.fusster_color_disabled);
        timer.setColor(((ColorDrawable) d).getColor());
    }

    public void interruptTimer() {
        currentProgress=100;
        interrupted=true;
        secondsTicked=Variables.allVariables.get("ROUND_DELAY");
    }

    public void makeToast(String str) {
        Context context = Game.activity.getApplicationContext();
        Toast.makeText(context, str,
                Toast.LENGTH_LONG).show();
    }

    public synchronized void drawPlayersBlocks(List<Player> players, int size) {
        //Spaghetti code incoming
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        int leftMargin=0, id=0;
        for(Player p : players) {
            if(size==1)
                leftMargin+=(getScreenWidth()/2-PLAYER_WIDTH/2);
            View view = inflater.inflate(R.layout.player_layout, null);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(PLAYER_WIDTH, PLAYER_HEIGHT);
            params.leftMargin = leftMargin;
            params.topMargin = 120;
            final RelativeLayout item = (RelativeLayout) view.findViewById(R.id.container);
            item.setId(id);

            TextView shots = (TextView) item.findViewById(R.id.playerShotsTextView);
            shots.setText(String.valueOf(Variables.allVariables.get("START_AMMO")));
            TextView shields = (TextView) item.findViewById(R.id.playerShieldsLeftTextView);
            shields.setText(String.valueOf(Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW")));
            TextView name = (TextView) item.findViewById(R.id.playerName);
            TextView wins = (TextView) item.findViewById(R.id.playerVictoriesTextView);
            wins.setText(String.valueOf(0));
            p.setPlayerLayout(item, shots, shields, name, wins, id);

            name.setText(p.getName());
            item.setOnDragListener(new MyDragListener());
            gameContainer.addView(item, params);
            item.setAlpha(0f);
            p.fadeIn();
            Log.e("fk", "android");
            p.setShots(Integer.parseInt(String.valueOf(Variables.allVariables.get("START_AMMO"))));
            p.setShieldsInARow(Integer.parseInt(String.valueOf(Variables.allVariables.get("MAX_SHIELDS_IN_A_ROW"))));
            if(size!=1)
                leftMargin+=(getScreenWidth()-PLAYER_WIDTH)/(size-1);
            id++;
        }
    }

    public int convertToDps(int dps) {
        final float scale = getApplicationContext().getResources().getDisplayMetrics().density;
        int pixels = (int) (dps * scale + 0.5f);
        return pixels;
    }

    public int getScreenHeight() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        return height;
    }
    public int getScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return width;
    }

    private final class MyClickListener implements OnLongClickListener {

        // called when the item is long-clicked
        @Override
        public boolean onLongClick(View view) {
            // TODO Auto-generated method stub

            // create it from the object's tag
            ClipData.Item item = new ClipData.Item((CharSequence)view.getTag());

            String[] mimeTypes = { ClipDescription.MIMETYPE_TEXT_PLAIN };
            ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);
            DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

            view.startDrag( data, //data to be dragged
                    shadowBuilder, //drag shadow
                    view, //local data about the drag and drop operation
                    0   //no needed flags
            );


            view.setVisibility(View.INVISIBLE);
            return true;
        }
    }

    class MyDragListener implements OnDragListener {
        boolean successfullyShotAPerson=false;
        Drawable normalShape = getResources().getDrawable(R.drawable.round_left_blue_fusster);
        Drawable targetShape = getResources().getDrawable(R.drawable.round_left_grey_fusster);

        @Override
        public synchronized boolean onDrag(View v, DragEvent event) {

            // Handles each of the expected events
            switch (event.getAction()) {

                //signal for the start of a drag and drop operation.
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;

                //the drag point has entered the bounding box of the View
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(Color.parseColor("#576cc7"));	//change the shape of the view
                    break;

                //the user has moved the drag shadow outside the bounding box of the View
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundColor(Color.WHITE);	//change the shape of the view back to normal
                    break;

                //drag shadow has been released,the drag point is within the bounding box of the View
                case DragEvent.ACTION_DROP:
                    // if the view is the bottomlinear, we accept the drag item
                    if(getPlayerByLayoutId(v.getId())!=null) {
                        //change the text
                        Player p = getPlayerByLayoutId(v.getId());
                        Utils.append("You tried to shoot " + p.getName());
                        Context context = getApplicationContext();
                        shoot(p.getName());
                        Toast.makeText(context, "You tried to shoot " + p.getName(),
                                Toast.LENGTH_LONG).show();
                    } else {
                        Context context = getApplicationContext();
                        Toast.makeText(context, "Please select a valid target",
                                Toast.LENGTH_LONG).show();
                        break;
                    }
                    break;

                //the drag and drop operation has concluded.
                case DragEvent.ACTION_DRAG_ENDED:
                    final View droppedView = (View) event.getLocalState();
                    final View vF = v;
                    droppedView.post(new Runnable() {
                        @Override
                        public void run() {
                            vF.setBackgroundColor(Color.WHITE);    //go back to normal shape
                            droppedView.setVisibility(View.VISIBLE);
                        }
                    });
                default:
                    break;
            }
            return true;
        }
    }

}







/*        int y = ll.getHeight() / 2;
        int x = ll.getWidth() / 2;
        secondsTicked=0;
        Paint paintBig = new Paint();
        paintBig.setColor(Color.parseColor("#B4C0C0"));
        Paint paintSmall = new Paint();
        paintSmall.setColor(Color.parseColor("#FFFFFF"));
        Paint paintTiming = new Paint();
        paintTiming.setColor(Color.RED);
        //drawing timer circles
        Bitmap bg = Bitmap.createBitmap(x*2, y*2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);
        RectF timerBig = new RectF(0, 0, x*2, y*2);
        RectF timerSmall = new RectF(width/2, width/2, x*2-width/2, y*2-width/2);
        RectF timing = new RectF(x, y*2, x*2-width/2, y*2-width/2);
        canvas.drawOval(timerBig, paintBig);
        canvas.drawOval(timerSmall, paintSmall);
        //start timing
        t= new Timer();
        ll.setBackgroundDrawable(new BitmapDrawable(bg));*/
