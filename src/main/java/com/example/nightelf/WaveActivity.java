package com.example.nightelf;

import static java.lang.System.arraycopy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


public class WaveActivity extends AppCompatActivity {

    //绘图部分定义

    MySurfaceView IU;

    //传输音频
    private ImageButton Audio;
    private boolean record_flag = false;//用于表示是否在录音，如果是正在录音

    //智能家居传感器
    private TextView fire ;
    private TextView lamp ;
    private String alarm_flag;
    private boolean alarm_booleanFlag =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave);
        IU = findViewById(R.id.IU);//绘制波形的对话框

        initViews();//初始化控件
        AudioBtnEvent();
        sensorView();


    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Wave : onStart ");
        wave_flag = true;//重新打开波形接收
        isOut = false;
        alarm_booleanFlag =false; //重新显示波形页面时，重新读取报警信息
        change.setText("居家模式");
        waveDataEvent();
        alarmEvent();

    }

    byte[] bytes1 = new byte[96];//用于存放接收到的数据
    byte[] bytes2 = new byte[96];//用于存放接收到的数据
    byte[] bytes3 = new byte[2048];//用于存放接收到的数据
    boolean [] flag_wave_data = new boolean[3];
    boolean wave_flag = true;
    private void waveDataEvent() {

                System.out.println("waveDataStart ");

                new Thread() {
                    @Override
                    public void run() {
                        try {
                            //创建客户端对象
/**
如果波形是socket只连接一次的长链接，把这边开起来，现在因为连的是华为云所以用的是请求一次回复一帧数据的形式
 */
//                            Socket socket = new Socket(getString(R.string.waveHost), Integer.parseInt(getString(R.string.wavePort)));
//
//                            //获取客户端对象的输出流
//                            OutputStream outputStream = socket.getOutputStream();
//                            outputStream.write("WAVEDATA".getBytes());//发送控制指令，启用接收波形功能
//                            outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去
//
//                            //拿到客户端输入流
//                            InputStream is = socket.getInputStream();
//                            //接收信息

                        //这里写入子线程需要做的工作
                        flag_wave_data[0] = true;
                        flag_wave_data[1] = true;
                        flag_wave_data[2] = true;
                        int repeat = 0;

                        while (wave_flag)//判断是不是在wave页面，如果到了sos页面就停止接收
                      {
                          //启动网络线程处理数据
                          if(IU.flag )//flag为true的时候接收，为false的时候绘制
                          {
                            startNetThread(getString(R.string.waveHost),Integer.parseInt(getString(R.string.wavePort)));
//                              System.out.println("打印客户端中的内容：" + socket);
                              //System.out.println(Arrays.equals(bytes1,bytes2));
                              //回应数据
//                              int n = is.read(bytes1);
//                              System.out.println("n：" + n);
                              if( Arrays.equals(bytes1,bytes2))//因为手机比较快，如果相同就跳过
                              {
                                  repeat = repeat +1;
                                  if(repeat >= 10)//这说明bytes1已经连续10次没有收到信息，添加时延以免内存溢出
                                  {
                                      try {
                                          sleep(20);
                                      } catch (InterruptedException e) {
                                          e.printStackTrace();
                                      }
                                  }

                                  continue;//如果收不到数据会内存溢出
                              }
                              else
                              {
                                  IU.rec_Data(bytes1);
//                                  System.out.println("Wave : draw wave");
                                  arraycopy(bytes1, 0, bytes2,0, 96);
                                  repeat = 0;
                              }
                              try {
                                  sleep(5);
                              } catch (InterruptedException e) {
                                  e.printStackTrace();
                              }
                          }
                      }
////关闭流
//                        is.close();
//                        //关闭客户端
//                        socket.close();
                        } catch (Exception e) {
//                    startNetThreadFlag = true;

                            e.printStackTrace();
                        }
                    }
                }.start();
    }

    private void  alarmEvent() {


        System.out.println("WAVE::alarmEvent Start ");

        new Thread() {
            @Override
            public void run() {
                //这里写入子线程需要做的工作
                int index = 0;

                while (true)
                {
                    if(alarm_booleanFlag)//只有警报响起才监听
                    {
                        break;
                    }
                    else
                    {
                        AlarmThread(getString(R.string.commonHost),Integer.parseInt(getString(R.string.commonPort)));
                        index++;
                       if(index >10)
                       {
                           index = 0;
                           sensorThread(getString(R.string.sensorHost),Integer.parseInt(getString(R.string.sensorPort)));//获取传感器信息
                           //更新传感器信息

                       }

                        if(alarm_rec_flag)
                        {
                            alarm_flag = new String(Alarmbytes, 0, 6);
                            if(alarm_flag.startsWith("sos"))//不能放在socket关掉之前
                            {
                                alarm_booleanFlag = true;
                                wave_flag = false;//关闭wave波形接收
                                alarm_rec_flag = false;//防止多开
                                showInfo("检测到跌倒，请及时救护");
                                Intent intent = new Intent(WaveActivity.this,SosActivity.class);
                                startActivity(intent);
                            }
                            else if( alarm_flag.startsWith("invade") && isOut)
                            {
                                wave_flag = false;//关闭wave波形接收
                                alarm_booleanFlag = true;
                                alarm_rec_flag = false;//防止多开
//                        showInfo("检测到入侵，请及时关注");在通知栏显示，但是好像会导致多开
                                Intent intent = new Intent(WaveActivity.this,InvadeActivity.class);
                                startActivity(intent);
                            }
                            else
                            {
                                System.out.println(" Wave::ERR ");
                            }
                        }

                    }
                    try {
                        sleep(400);//如果不停一下，会疯狂开很多的线程

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }



    private void AudioBtnEvent() {
        Audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(record_flag)//如果按下时正在录音，则停止录音，并将录音内容发送出去
                {
                    Audio.setImageResource(R.drawable.play);//将播放的图标切换为开始
                    System.out.println("WAVE::Audio  Stop ");
                    AudioStop();
                    //startNetThread(host,Integer.parseInt(port));
                    record_flag = false;
                }
                else//开始录音
                {
                    System.out.println("WAVE::Audio start");

                    Audio.setImageResource(R.drawable.stop);//将播放的图标切换为暂停
//                    AudioSelectThread(getString(R.string.audioControlHost), Integer.parseInt(getString(R.string.audioControlPort)));
                    AudioStart();
                    record_flag = true;
                }
            }
        });
    }





    public static int byte2Int(byte[] b) {
        int intValue = 0;
        for (int i = 0; i < b.length; i++) {
            intValue += (b[i] & 0xFF) << (8 * (3 - i));
        }
        return intValue;
    }

    public static byte[] intToBytes2(int n){
        byte[] b = new byte[4];
        for(int i = 0;i < 4;i++){
            b[i] = (byte)(n >> (24 - i * 8));
        }
        return b;
    }

//  接收波形的线程
    private void startNetThread(final String host, final int port) {
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象

                        Socket socket = new Socket(host, port);

                        //获取客户端对象的输出流
                        OutputStream outputStream = socket.getOutputStream();
                        outputStream.write("WAVEDATA".getBytes());//发送控制指令，启用接收波形功能
                        outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去
                        outputStream.flush();
//                        System.out.println("打印客户端中的内容：" + socket);
                        //拿到客户端输入流
                        InputStream is = socket.getInputStream();
                        int n = is.read(bytes1);
//                        System.out.println("n：" + n);

                        //关闭流
                        is.close();
                        //关闭客户端
                        socket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            //启动线程
        }.start();
    }

    //接收传感器信息
    private void sensorThread(final String host, final int port) {
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象

                    Socket socket = new Socket(host, port);
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write("security".getBytes());//发送控制指令，启用接收波形功能

                    outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去


                    //获取客户端对象的输出流

                    System.out.println("打印客户端中的内容：" + socket);

                    //拿到客户端输入流
                    InputStream is = socket.getInputStream();
                    //接收信息

                    byte[] tempBytes = new byte[1024];
                    //回应数据
                    int n = is.read(tempBytes);
                    if(n!=-1)
                    {
                        String test = new String(tempBytes);
                        String[] split = test.split(",");
                        Random r =new Random();
                        fire.post(new Runnable() {
                            @Override
                            public void run() {
                                lamp.setText("温度"+split[0]+"°C "+"湿度"+split[1]);
                                fire.setText("气体浓度"+split[2]+"ppm 正常");
                            }
                        });
                    }

                    //关闭流
                    is.close();
                    //关闭客户端
                    socket.close();

                } catch (Exception e) {
//                    startNetThreadFlag = true;
                    e.printStackTrace();
                }
            }
            //启动线程
        }.start();
    }

    boolean AlarmThreadFlag = true;
    boolean alarm_rec_flag = false;//false说明还没有一个线程收到回复
    byte []Alarmbytes = new byte[10];
    private void AlarmThread(final String host, final int port) {
        new Thread() {
            public void run() {
                try {
                    //创建客户端对象
                        Socket socket = new Socket(host, port);

                        //获取客户端对象的输出流
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write("APP".getBytes());//发送控制指令，查询是否有警报

                    String alarm;
                        if(isOut)
                        {
                            alarm = "out";
                        }
                        else
                        {
                            alarm = "home";
                        }
                        System.out.println("VideoChatView::alarm：" + alarm);
                        outputStream.write(alarm.getBytes());//发送控制指令，查询是否有警报
                        outputStream.flush();
                        //为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去
                        System.out.println("WAVE：：ALARM socket start" );
                        System.out.println("WAVE：： socket：" + socket);

                        //拿到客户端输入流
                        InputStream is = socket.getInputStream();
                        //接收信息

                        //回应数据
                        int n = is.read(Alarmbytes);

                        if(n!=-1)
                            alarm_rec_flag = true;

                        //关闭流
                        is.close();
                        //关闭客户端
                        socket.close();

                } catch (Exception e) {
                    AlarmThreadFlag =true ;
                    e.printStackTrace();
                }
            }
            //启动线程
        }.start();
    }

    private void sensorView(){

        //烟雾传感器
//        Random r =new Random();
//        int fire10 = 920+r.nextInt(5);
//        int fire1 = r.nextInt(100) ;
//        fire.setText(fire10+"."+fire1+" ppm 正常");
//        //光敏传感器
//        lamp.setText("环境光较强  检测走动");
        lamp.setText("温度"+"   °C "+"湿度");
        fire.setText("气体浓度"+"    ppm 正常");


    }


    //初始化控件
    private void initViews() {

        Audio = findViewById(R.id.Audio);
        change = findViewById(R.id.change);
        fire = findViewById(R.id.fire);
        lamp =(TextView) findViewById(R.id.lamp);
    }


    Button change = null;
    boolean isOut = false;

    //模式切换按钮，按下后改变模式，居家模式检测摔倒，外出模式有行为就报警
    public void changeMode(View view) {
        if(isOut)
        {
            change.setText("居家模式");
            isOut = false;
        }
        else
        {
            //现在为居家模式，按下后变为外出模式
            change.setText("外出模式");
            isOut = true;
        }
    }

    //在手机下拉信息栏显示一个通知
public void showInfo( String Info) {
    Intent intent = new Intent(this, SosActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
    String channelId = createNotificationChannel("my_channel_ID", "my_channel_NAME", NotificationManager.IMPORTANCE_HIGH);
    NotificationCompat.Builder notification = new NotificationCompat.Builder(this, channelId)
            .setContentTitle("警报")
            .setContentText(Info)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.logo)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true);
    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
    notificationManager.notify(100, notification.build());



}
    private String createNotificationChannel(String channelID, String channelNAME, int level) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);
            manager.createNotificationChannel(channel);
            return channelID;
        } else {
            return null;
        }
    }


    /**
    音频收发
     */

    private void AudioStart() //开启通信
    {
        System.out.println("Audio Start ");
        creatAudioRecord();//初始化录音发送线程
        initData();//初始化接收播放线程
        startPlay();//开启接收播放线程
        startRecord();//开启录音发送线程

    }

    private void AudioStop() //开启通信
    {
        System.out.println("Audio Start ");
        stopRecord();//关闭录音发送线程
        stopPlay();//关闭接收播放线程
    }

    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 14000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
//    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private Button Start;
    private Button Stop;
    private AudioRecord audioRecord;
    private boolean isRecord = false;// 设置正在录制的状态


    private void creatAudioRecord() {
        // 获得缓冲区字节大小
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        // 创建AudioRecord对象
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
    }


    private void startRecord() {
        audioRecord.startRecording();
        // 让录制状态为true
        isRecord = true;
        // 开启音频文件写入线程
        new Thread(new WaveActivity.AudioRecordThread()).start();
    }

    private void stopRecord() {
        close();
    }

    private void close() {
        if (audioRecord != null) {
            System.out.println("stopRecord");
            isRecord = false;//停止文件写入
            audioRecord.stop();
            audioRecord.release();//释放资源
            audioRecord = null;
        }
    }

    class AudioRecordThread implements Runnable {
        @Override
        public void run() {
            writeDateTOFile();//往文件中写入裸数据
        }
    }

    /**
     * 这里将数据写入文件，但是并不能播放，因为AudioRecord获得的音频是原始的裸音频，
     * 如果需要播放就必须加入一些格式或者编码的头信息。但是这样的好处就是你可以对音频的 裸数据进行处理，比如你要做一个爱说话的TOM
     * 猫在这里就进行音频的处理，然后重新封装 所以说这样得到的音频比较容易做一些音频的处理。
     */

    //能连续不断的音频输出
    private void writeDateTOFile() {
        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
        byte[] audiodata = new byte[bufferSizeInBytes];
        FileOutputStream fos = null;
        int readsize = 0;

//        String host = "192.168.43.77";//client_host_ip.getText().toString();
//        String port = "8090";//;client_port.getText().toString();
        try {
            //创建客户端对象
            Socket socket = new Socket(getString(R.string.aoHost), Integer.parseInt(getString(R.string.aoPort)));

            System.out.println("SEND START ");

            //读取刚刚录制的音频文件内数据

            //获取客户端对象的输出流
            OutputStream outputStream = socket.getOutputStream();
//                    outputStream.write("PCMSEND".getBytes());//发送控制指令，启用传输音频功能


            outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去


            System.out.println("打印客户端中的内容：" + socket);

            //发送文件大小
            byte[] fileSize = new byte[4];
            fileSize = intToBytes2(bufferSizeInBytes * 40);
            System.out.println("WAVE:: fileSize:" + bufferSizeInBytes * 40);
//                    outputStream.write(fileSize);

            int count = 0;
            while (isRecord == true) {
                readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
//                        System.out.println("audio:: bufferSizeInBytes:"+bufferSizeInBytes);
                if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
                    try {
                        //fos.write(audiodata);
                        outputStream.write(audiodata);
                        outputStream.flush();
                        count = count + 1;

//                        Thread.sleep(100);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (count == 39) {
//                            outputStream.write(fileSize);
                    System.out.println("audio:: send data");
                    count = 0;
                }
//                        System.out.println("audio:: count:"+count);

            }
            //关闭客户端
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onDestroy() {
        close();
        super.onDestroy();
    }


    //播放
    private AudioTrack mAudioTrack;
    private DataInputStream mDis;//播放文件的数据流
    private Thread mRecordThread;
    private boolean isStart = false;
//    private volatile static AudioTrackManager mInstance;

    //音频流类型
    private static final int mStreamType = AudioManager.STREAM_VOICE_CALL;
    //指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
//    AudioManager.STREAM_MUSIC：用于音乐播放的音频流。
//    AudioManager.STREAM_SYSTEM：用于系统声音的音频流。
//    AudioManager.STREAM_RING：用于电话铃声的音频流。
//    STREAM_VOICE_CALL：用于电话通话的音频流。
//



    private static final int mSampleRateInHz=16000 ;
    //指定捕获音频的声道数目。在AudioFormat类中指定用于此的常量
    private static final int mChannelConfig= AudioFormat.CHANNEL_OUT_STEREO; //单声道
    //指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
    //因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实。
    private static final int mAudioFormat=AudioFormat.ENCODING_PCM_16BIT;
    //指定缓冲区大小。调用AudioRecord类的getMinBufferSize方法可以获得。
    private int mMinBufferSize;
    //STREAM的意思是由用户在应用程序通过write方式把数据一次一次得写到audiotrack中。这个和我们在socket中发送数据一样，
    // 应用层从某个地方获取数据，例如通过编解码得到PCM数据，然后write到audiotrack。
    private static int mMode = AudioTrack.MODE_STREAM;




    private void initData(){
        //根据采样率，采样精度，单双声道来得到frame的大小。
        mMinBufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz,mChannelConfig, mAudioFormat);//计算最小缓冲区
        //注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
        //创建AudioTrack
        mAudioTrack= new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(mSampleRateInHz)
                        .setChannelMask(mChannelConfig)
                        .build())
                .setBufferSizeInBytes(mMinBufferSize)
                .build();

    }


    /**
     * 销毁线程方法
     */
    private void destroyThread() {
        try {
            isStart = false;
            if (null != mRecordThread && Thread.State.RUNNABLE == mRecordThread.getState()) {
                try {
                    Thread.sleep(500);
                    mRecordThread.interrupt();
                } catch (Exception e) {
                    mRecordThread = null;
                }
            }
            mRecordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mRecordThread = null;
        }
    }

    /**
     * 启动播放线程
     */
    private void startThread() {
        destroyThread();
        isStart = true;
        if (mRecordThread == null) {
            mRecordThread = new Thread(recordRunnable);
            mRecordThread.start();
        }
    }

    /**
     * 播放线程
     * 从socket
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {


            try {
                Thread.sleep(500);//不然和8090同时开会出问题
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                //创建客户端对象

                Socket socket = new Socket(getString(R.string.aiHost),Integer.parseInt(getString(R.string.aiPort)));
                System.out.println("打印客户端中的内容：" + socket);
                System.out.println("WAVE::AudioTrack   recordRunnable START ");
                //获取客户端对象的输出流
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write("PCMREC".getBytes());//发送控制指令，启用传输音频功能

                outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去
                System.out.println("WAVE::AudioTrack  Thread run");
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);//设置线程优先级
                byte[] tempBuffer = new byte[mMinBufferSize];
                int readCount = 0;
                //拿到客户端输入流
                InputStream is = socket.getInputStream();

                int stopflag = 0;
                while (stopflag<10) {

                    readCount = is.read(tempBuffer);
                    System.out.println("WAVE::AudioTrack   readCount:" + readCount);
                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
                        continue;
                    }
                    if (readCount != 0 && readCount != -1) {//一边播放一边写入语音数据
                        //判断AudioTrack未初始化，停止播放的时候释放了，状态就为STATE_UNINITIALIZED
                        if(mAudioTrack.getState() == mAudioTrack.STATE_UNINITIALIZED){
                            initData();
                        }
                        mAudioTrack.play();
                        mAudioTrack.write(tempBuffer, 0, readCount);
                    }
                    if(readCount==-1)
                    {
                        stopflag = stopflag +1;
                    }
                    else
                    {
                        stopflag =0;
                    }

                }
                //关闭流
                is.close();
                //关闭客户端
                socket.close();
                System.out.println("WAVE::AudioTrack  stopPlay");
                stopPlay();//播放完就停止播放

            } catch (Exception e) {


                e.printStackTrace();
            }
        }

    };



    /**
     * 启动播放
     *
     * @param
     */
    public void startPlay() {
        try {
            System.out.println("WAVE::AudioTrack  startPlay");
            startThread();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        try {
            destroyThread();//销毁线程
            if (mAudioTrack != null) {
                if (mAudioTrack.getState() == AudioRecord.STATE_INITIALIZED) {//初始化成功
                    mAudioTrack.stop();//停止播放
                }
                if (mAudioTrack != null) {
                    mAudioTrack.release();//释放audioTrack资源
                }
            }
            if (mDis != null) {
                mDis.close();//关闭数据输入流
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}