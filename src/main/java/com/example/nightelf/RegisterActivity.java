package com.example.nightelf;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RegisterActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initViews();//初始化控件

    }

    private final int HANDLER_MSG_TELL_RECV = 0x124;
    private EditText Pwd, UserID,Name;
    private ImageView client_submit;
    private String flag ;


    Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            //设置一个弹框
            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
            builder.setMessage("服务器回复：" + msg.obj);
            //创建弹框 并展示
            builder.create().show();
        }
    };


    private void startNetThread(final String host, final int port, final String UserID,final String Pwd,final String Name) {
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象
                    Socket socket = new Socket(host, port);



                    //获取客户端对象的输出流

                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write("REGISTER".getBytes());//发送控制指令，启用登录功能


                    String UserInfo = new String("ID,"+UserID+",PWD,"+Pwd+",Name,"+Name);
                    System.out.println("Register::start："+UserInfo);
                    outputStream.write(UserInfo.getBytes());
                    outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去
                    System.out.println("打印客户端中的内容：" + socket);



                    //拿到客户端输入流
                    InputStream is = socket.getInputStream();
                    //接收信息
                    byte[] bytes1 = new byte[1024];
                    //回应数据
                    int n = is.read(bytes1);
                    flag = new String(bytes1, 0, n);
                    System.out.println(flag );

                    //关闭流
                    is.close();
                    //关闭客户端
                    socket.close();
//                    System.out.println(flag.startsWith("PASS") );
                    if(flag.startsWith("pass"))//不能放在socket关掉之前
                    {
                        Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, "注册成功");
                        msg.sendToTarget();
                        sleep(300);
                        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                        startActivity(intent);
                    }
                    else if (flag.startsWith("already"))
                    {
                        //以对话框的形式显示接收到的信息
                    Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, "该账号已被注册");
                    msg.sendToTarget();
                    }
                    else
                    {
                        Message msg = handler.obtainMessage(HANDLER_MSG_TELL_RECV, "网络错误");
                        msg.sendToTarget();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //启动线程
        }.start();
    }


    //初始化控件
    private void initViews() {
        UserID =findViewById(R.id.editTextNumber);
        Pwd = findViewById(R.id.editTextPassword);
        Name = findViewById(R.id.editTextName);
        client_submit = findViewById(R.id.client_submit);
    }


    //注册按钮
    public void client_submitonCallClicked(View view) {
        String UserID_String = UserID.getText().toString();
        String Pwd_String = Pwd.getText().toString();
        String Name_String = Name.getText().toString();
        startNetThread(getString(R.string.commonHost),Integer.parseInt(getString(R.string.commonPort)),UserID_String,Pwd_String,Name_String);
    }

    //返回按钮
    public void backLogin(View view) {
        Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(intent);
    }
}