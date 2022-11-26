package com.example.doetest;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
public class SubPage1 extends AppCompatActivity {
    private SlidrInterface slidr;
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layoutpage1);
        slidr = Slidr.attach(this);
    }



}
