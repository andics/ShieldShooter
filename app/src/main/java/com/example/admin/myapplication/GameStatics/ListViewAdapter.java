package com.example.admin.myapplication.GameStatics;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.inputmethodservice.Keyboard;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.admin.myapplication.R;

import com.example.admin.myapplication.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Genata on 15.3.2016 г..
 */
public class ListViewAdapter extends BaseAdapter{
    String [] result, players;
    Context context;
    int [] imageId;
    View view;
    List<Holder> holder = new ArrayList<Holder>();
    private static LayoutInflater inflater=null;
    public ListViewAdapter(MainActivity.PageFragment mainActivity, String[] prgmNameList, String[] players, int[] prgmImages) {
        result=prgmNameList;
        this.players=players;
        context=mainActivity.getContext();
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        View v;
        TextView title;
        TextView players;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.server_layout, null);
        }

        Holder h = new Holder();
        h.img = (ImageView) view.findViewById(R.id.imageView7);
        h.players = (TextView) view.findViewById(R.id.textView5);
        h.title = (TextView) view.findViewById(R.id.textView1);
        h.title.setText(result[position]);
        h.players.setText(players[position]);
        h.img.setImageResource(imageId[position]);
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
                                Toast.makeText(context, "You LongClicked " + result[position], Toast.LENGTH_LONG).show();
                                //remove item
                                result[position]=null;
                                players[position]=null;
                                view=null;
                                imageId[position]=0;
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
                return false;
            }
        });
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked " + result[position], Toast.LENGTH_LONG).show();
            }
        });
        holder.add(h);
        return view;
    }
}