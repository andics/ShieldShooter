package com.example.admin.myapplication.Activities;

import static com.example.admin.myapplication.GameStatics.Utils.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.admin.myapplication.R;
import com.example.admin.myapplication.GameStatics.Utils;
import com.example.admin.myapplication.Variables.Variables;

public class TargetsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_targets);
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, Utils.getPlayers());

        ListView lv = (ListView) findViewById(R.id.listViewId);
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(TargetsActivity.this, inGameActivity.class);
                String selectedPlayer = Utils.getPlayers().get(position);
                send("shoot:" + selectedPlayer.trim());
                inGameActivity inGameActivityConstructor = new inGameActivity();
                inGameActivityConstructor.shots= Variables.get("MAX_SHIELDS_IN_A_ROW");
                Utils.dissableButtons(null ,"You played shoot this round");
                startActivity(intent);
            }
        });
    }
}
