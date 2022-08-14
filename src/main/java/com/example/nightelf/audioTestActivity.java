package com.example.nightelf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 测试用的文件
 * 实际APP中没有用到
 */


public class audioTestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_test);

        // 重新设置界面大小
        init();
    }

    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 16000;
    // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
    private static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
    private static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // 缓冲区字节大小
    private int bufferSizeInBytes = 0;
    private Button Start;
    private Button Stop;
    private AudioRecord audioRecord;
    private boolean isRecord = false;// 设置正在录制的状态
    //AudioName裸音频数据文件
    //"/storage/emulated/0/Android/data/com.example.nightelf/files/Music/1.wav"
    private static final String AudioName = "/storage/emulated/0/Android/data/com.example.nightelf/files/Music/love.raw";
    //NewAudioName可播放的音频文件
    private static final String NewAudioName = "/storage/emulated/0/Android/data/com.example.nightelf/files/Music/new.wav";

//    //配置PCM播放器
//    private AudioTrackManager myAudioTrack = new AudioTrackManager() ;



    private void init() {
        Start = (Button) this.findViewById(R.id.start);
        Stop = (Button) this.findViewById(R.id.stop);
        Start.setOnClickListener(new TestAudioListener());
        Stop.setOnClickListener(new TestAudioListener());
        test = findViewById(R.id.test);
        creatAudioRecord();
        RecordTestEvent();

    }

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
//        audioRecord = new AudioRecord(audioSource, sampleRateInHz,
//                channelConfig, audioFormat, bufferSizeInBytes);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
    }

    class TestAudioListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == Start) {
                startRecord();
            }
            if (v == Stop) {
                stopRecord();
            }

        }

    }

    private void startRecord() {
        audioRecord.startRecording();
        // 让录制状态为true
        isRecord = true;
        // 开启音频文件写入线程
        new Thread(new AudioRecordThread()).start();
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
            copyWaveFile(AudioName, NewAudioName);//给裸数据加上头文件
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
//        try {
//            File file = new File(AudioName);
//            if (file.exists()) {
//                file.delete();
//            }
//            fos = new FileOutputStream(file);// 建立一个可存取字节的文件
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        String host = "192.168.43.77" +
                "";//client_host_ip.getText().toString();
        String port = "8090";//;client_port.getText().toString();
                try {
                    //创建客户端对象
                    Socket socket = new Socket(host,Integer.parseInt(port));

                    System.out.println("SEND START ");

                    //读取刚刚录制的音频文件内数据



                    //获取客户端对象的输出流
                    OutputStream outputStream = socket.getOutputStream();
//                    outputStream.write("PCMSEND".getBytes());//发送控制指令，启用传输音频功能

                    outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去


                    System.out.println("打印客户端中的内容：" + socket);

                    //发送文件大小
                    byte[] fileSize = new byte[4];
                    fileSize = intToBytes2(bufferSizeInBytes*40);
                    System.out.println("audio:: fileSize:"+bufferSizeInBytes*40);
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
                                count=count+1;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if(count==39)
                        {
//                            outputStream.write(fileSize);
                            System.out.println("audio:: send data");
                            count=0;
                        }
//                        System.out.println("audio:: count:"+count);

                    }
                    //关闭客户端
                    socket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
////
//    private void writeDateTOFile() {
//        // new一个byte数组用来存一些字节数据，大小为缓冲区大小
//        byte[] audiodata = new byte[bufferSizeInBytes];
//        FileOutputStream fos = null;
//        int readsize = 0;
//
//        String host = "192.168.43.77" +
//                "";//client_host_ip.getText().toString();
//        String port = "8090";//;client_port.getText().toString();
//        try {
//
//
//            //读取刚刚录制的音频文件内数据
//
//            OutputStream outputStream = null;
//            Socket socket = null;
//            int count = 0;
//            while (isRecord == true) {
//                if(count==0)
//                {
//                    //创建客户端对象
//                    socket = new Socket(host,Integer.parseInt(port));
//                    outputStream = socket.getOutputStream();
//                    System.out.println("SEND START ");
//                }
//
//
//                readsize = audioRecord.read(audiodata, 0, bufferSizeInBytes);
////                        System.out.println("audio:: bufferSizeInBytes:"+bufferSizeInBytes);
//                if (AudioRecord.ERROR_INVALID_OPERATION != readsize) {
//                    try {
//                        //fos.write(audiodata);
//                        outputStream.write(audiodata);
//                        outputStream.flush();
//                        count=count+1;
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                if(count==39)
//                {
////                            outputStream.write(fileSize);
//                    System.out.println("audio:: send data");
//                    count=0;
//                    socket.close();
//                }
////                        System.out.println("audio:: count:"+count);
//
//
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//




//        try {
//            fos.close();// 关闭写入流
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }


    public static byte[] intToBytes2(int n){
        byte[] b = new byte[4];
        for(int i = 0;i < 4;i++){
            b[i] = (byte)(n >> (24 - i * 8));
        }
        return b;
    }

    // 这里得到可播放的音频文件
    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRateInHz;
        int channels = 2;
        long byteRate = 16 * sampleRateInHz * channels / 8;
        byte[] data = new byte[bufferSizeInBytes];
        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;
            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);
            while (in.read(data) != -1) {
                out.write(data);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 这里提供一个头信息。插入这些信息就可以得到可以播放的文件。
     * 为我为啥插入这44个字节，这个还真没深入研究，不过你随便打开一个wav
     * 音频的文件，可以发现前面的头文件可以说基本一样哦。每种格式的文件都有
     * 自己特有的头文件。
     */
    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels, long byteRate)
            throws IOException {
        byte[] header = new byte[44];
        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = 16; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, 44);
    }

    @Override
    protected void onDestroy() {
        close();
        super.onDestroy();
    }

    private void playPCM() {
        initData();
        System.out.println("AUdiotest:: test play");
        startPlay("/storage/emulated/0/Android/data/com.example.nightelf/files/Music/GEM.pcm");

    }


//    private void sendThread(final String host, final int port) {
//        new Thread() {
//            public void run() {
//                try {
//                    //创建客户端对象
//                    Socket socket = new Socket(host, port);
//
//                    System.out.println("SEND START ");
//
//                    //读取刚刚录制的音频文件内数据
//                    File file = new File(dataname);
//                    FileInputStream audiois = new FileInputStream(file);
//
//                    System.out.println("SEND dataname = "+dataname);
//                    System.out.println(audiois.available());
//                    byte []buffer = new byte[audiois.available()];
//                    audiois.read(buffer);
//
//
//                    //获取客户端对象的输出流
//                    OutputStream outputStream = socket.getOutputStream();
//                    outputStream.write("AUDIOSEND".getBytes());//发送控制指令，启用传输音频功能
//
//                    outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去
//
//                    outputStream.write(buffer);
//
//                    outputStream.write("theEnd".getBytes());
//
//                    outputStream.flush();
//                    System.out.println("打印客户端中的内容：" + socket);
//
//
//                    //拿到客户端输入流
//                    InputStream is = socket.getInputStream();
//                    //接收信息
//                    byte[] bytes1 = new byte[1024];
//                    //回应数据
//                    int n = is.read(bytes1);
//
//                    //关闭流
//                    is.close();
//                    //关闭客户端
//                    socket.close();
//
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//            //启动线程
//        }.start();
//    }


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
//    ：用于电话通话的音频流。
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
//        mAudioTrack = new AudioTrack(mStreamType, mSampleRateInHz,mChannelConfig,
//                mAudioFormat,mMinBufferSize,mMode);
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
     * 从文件
     */
//    Runnable recordRunnable = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                //设置线程的优先级
//
//                System.out.println("AUdioTrack::  Thread run");
//                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//                byte[] tempBuffer = new byte[mMinBufferSize];
//                int readCount = 0;
//                while (mDis.available() > 0) {
//                    readCount= mDis.read(tempBuffer);
//                    if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
//                        continue;
//                    }
//                    if (readCount != 0 && readCount != -1) {//一边播放一边写入语音数据
//                        //判断AudioTrack未初始化，停止播放的时候释放了，状态就为STATE_UNINITIALIZED
//                        if(mAudioTrack.getState() == mAudioTrack.STATE_UNINITIALIZED){
//                            initData();
//                        }
//                        mAudioTrack.play();
//                        mAudioTrack.write(tempBuffer, 0, readCount);
//                    }
//                }
//                stopPlay();//播放完就停止播放
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//    };
    /**
     * 播放线程
     * 从socket
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            String host = "192.168.43.77";//client_host_ip.getText().toString();
            String port = "8091";//;client_port.getText().toString();
            try {
                //创建客户端对象

                Socket socket = new Socket(host,Integer.parseInt(port));
                System.out.println("打印客户端中的内容：" + socket);
                System.out.println("AudioTrack::  recordRunnable START ");


                //获取客户端对象的输出流
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write("PCMREC".getBytes());//发送控制指令，启用传输音频功能

                outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去
                System.out.println("AudioTrack::  Thread run");
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);//设置线程优先级
                byte[] tempBuffer = new byte[mMinBufferSize];
                int readCount = 0;
                //拿到客户端输入流
                InputStream is = socket.getInputStream();

                int stopflag = 0;
                while (stopflag<10) {

                    readCount = is.read(tempBuffer);
                    System.out.println("AudioTrack::  readCount:" + readCount);
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
                System.out.println("AudioTrack:: stopPlay");
                stopPlay();//播放完就停止播放

            } catch (Exception e) {


                e.printStackTrace();
            }
        }

    };

    /**
     * 播放文件
     * @param path
     * @throws Exception
     */
    private void setPath(String path) throws Exception {
        File file = new File(path);
        mDis = new DataInputStream(new FileInputStream(file));
    }

    /**
     * 启动播放
     *
     * @param path
     */
    public void startPlay(String path) {
        try {
//            //AudioTrack未初始化
//            if(mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED){
//                throw new RuntimeException("The AudioTrack is not uninitialized");
//            }//AudioRecord.getMinBufferSize的参数是否支持当前的硬件设备
//            else if (AudioTrack.ERROR_BAD_VALUE == mMinBufferSize || AudioTrack.ERROR == mMinBufferSize) {
//                throw new RuntimeException("AudioTrack Unable to getMinBufferSize");
//            }else{
            setPath(path);
            startThread();
//            }

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

    Button test=null;

    private void RecordTestEvent() {
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("AudioTrack::   RecordTestEvent: start");
                playPCM();
            }
        });
    }



}