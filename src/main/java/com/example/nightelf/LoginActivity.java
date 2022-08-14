package com.example.nightelf;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private final int HANDLER_MSG_TELL_RECV = 0x124;
    private EditText Pwd, UserID;
    private Button client_submit;
    private String flag ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();//初始化控件
        initEvent();

    }


    //登录按键
    private void initEvent() {
        client_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String UserID_String = UserID.getText().toString();
                String Pwd_String = Pwd.getText().toString();

                startNetThread(getString(R.string.commonHost),Integer.parseInt(getString(R.string.commonPort)),UserID_String,Pwd_String);
//                Intent intent = new Intent(LoginActivity.this,WaveActivity.class);
//                startActivity(intent);
            }
        });
    }



    //发送登录信息的线程
    private void startNetThread(final String host, final int port, final String UserID,final String Pwd) {
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象
                    Socket socket = new Socket(host, port);

                    //获取客户端对象的输出流
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write("LOGIN".getBytes());//发送控制指令，启用登录功能
                    String UserInfo = "ID,"+UserID+",PWD,"+Pwd;
                    outputStream.write(UserInfo.getBytes());

                    outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去

                    System.out.println("打印客户端中的内容：" + socket);

                    //拿到客户端输入流
                    InputStream is = socket.getInputStream();
                    //接收信息
                    byte[] bytes1 = new byte[1024];
                    //回应数据
                    int n = is.read(bytes1);
//                    System.out.println("Login：n:" + n);
                    flag = new String(bytes1, 0, n);
                    System.out.println(flag );


                    //关闭流
                    is.close();
                    //关闭客户端
                    socket.close();
                    System.out.println(flag.startsWith("pass") );
                    if(flag.startsWith("pass"))//不能放在socket关掉之前
                    {

                        loginSuccessful();
                    }
                    else
                    {
                        Toast.makeText(LoginActivity.this,"账号或密码输入错误",Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //启动线程
        }.start();
    }
    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            //设置一个弹框
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setMessage("服务器回复：" + msg.obj);
            //创建弹框 并展示
            builder.create().show();
        }
    };

    //初始化控件
    private void initViews() {
        UserID =findViewById(R.id.editTextNumber);
        Pwd = findViewById(R.id.editTextTextPassword);
        client_submit = findViewById(R.id.client_submit);
    }
    public void loginSuccessful()
    {
        //以对话框的形式显示接收到的信息
        Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, "登录成功");
        msg.sendToTarget();
        Intent intent = new Intent(LoginActivity.this,WaveActivity.class);
//        Intent intent = new Intent(LoginActivity.this,audioTestActivity.class);
        //执行意图
        startActivity(intent);
    }

    //注册按钮
    public void register(View view) {
        Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(intent);
    }
}