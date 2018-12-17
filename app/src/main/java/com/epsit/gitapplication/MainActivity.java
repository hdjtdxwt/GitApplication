package com.epsit.gitapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onClick(View view){
        Toast.makeText(getApplicationContext(), "onClick" ,Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SettingActivity.class));
    }
}
