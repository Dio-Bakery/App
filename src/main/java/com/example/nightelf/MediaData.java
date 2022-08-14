package com.example.nightelf;

/**
 * 没有用的的测试文件
 */

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MediaData {
    //音频数据写入
    //静态方法不用创建对象就可以调用
    public static boolean saveMediaData(String dataname,byte[] bytes, Context context)
    {
        FileOutputStream fos = null;

        try {
            fos = context.openFileOutput(dataname,Context.MODE_PRIVATE);
            fos.write(bytes);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //用户数据写入
    public static boolean saveUserInfo(String username,String password, Context context)
    {
        String msg = null;
        FileOutputStream fos = null;

        try {
            fos = context.openFileOutput("MyData.txt",Context.MODE_PRIVATE);
            msg = username+":"+password;
            fos.write(msg.getBytes());
            System.out.println("文件保存成功" );
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //用户数据的读取
    public static Map<String,String> getUserInfo(Context context){
        //获取文件的输入流
        FileInputStream fis = null;
        try {
            fis = context.openFileInput("MyData.txt");
            byte[] buffer = new byte[fis.available()];
            fis.read(buffer);//把文件输入流的内容读出来放在buffer中
            String msg = new String(buffer);
            String[] userInfo = msg.split(":");
            Map<String,String> userMap = new HashMap<>();
            userMap.put("username",userInfo[0]);
            userMap.put("password",userInfo[1]);
            return userMap;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
