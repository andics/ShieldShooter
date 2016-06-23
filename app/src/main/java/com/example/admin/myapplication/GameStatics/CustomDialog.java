package com.example.admin.myapplication.GameStatics;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.admin.myapplication.R;

/**
 * Created by Genata on 20.3.2016 г..
 */
public class CustomDialog extends Dialog implements
        android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public Runnable r;
    public String text, hint1, hint2;
    public Button yes, no;
    public static TextView tv, editText, editText2;

    public CustomDialog(Activity a, Runnable r, String text, String hint1, String hint2) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.text=text;
        this.hint1=hint1;
        this.hint2=hint2;
        this.r=r;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
        yes = (Button) findViewById(R.id.btn_yes);
        no = (Button) findViewById(R.id.btn_no);
        tv = (TextView) findViewById(R.id.txt_dia);
        editText = (TextView) findViewById(R.id.dialogTV1);
        editText2 = (TextView) findViewById(R.id.dialogTV2);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        tv.setText(text);
        d = this;
        editText.setHint(hint1);
        editText2.setHint(hint2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_yes:
                r.run();
                break;
            case R.id.btn_no:
                dismiss();
                break;
            default:
                break;
        }
    }
}
