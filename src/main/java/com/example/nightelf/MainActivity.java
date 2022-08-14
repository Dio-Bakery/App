package com.example.nightelf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login1(View view) {
        Toast.makeText(getApplicationContext(),"登录成功",Toast.LENGTH_SHORT);
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        //执行意图
        startActivity(intent);
    }
}


