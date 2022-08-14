package com.example.nightelf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 入侵检测界面
 *
 */

public class InvadeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invade);
        initViews();
    }

    ImageButton audioBtn = null;
    boolean audio_flag = false;
    MediaPlayer mMediaPlayer = null;
    //初始化控件
    private void initViews() {

        audioBtn = findViewById(R.id.AudioInvade);
        audio_flag = true;
        audioBtn.setImageResource(R.drawable.stop);//将播放的图标切换为暂停
        //播放音频
        mMediaPlayer = MediaPlayer.create(this, R.raw.audioattention);
        mMediaPlayer.start();
        mMediaPlayer.setOnCompletionListener(mCompletionListener);//监听播放完成,后自动开启通话
    }

    //控制语音的按键
    public void AudioConnect(View view) {
        if(audio_flag)//如果按下时正在录音，则停止录音，并将录音内容发送出去
        {
            audioBtn.setImageResource(R.drawable.play);//将播放的图标切换为开始
            System.out.println("WAVE::Audio  Stop ");
            AudioStop();
            audio_flag = false;
        }
        else//开始录音
        {
            System.out.println("WAVE::Audio start");
            audioBtn.setImageResource(R.drawable.stop);//将播放的图标切换为暂停
            AudioStart();
            audio_flag = true;
        }
    }
    //设置一个监听器，用于监听语音提示的关闭
    private MediaPlayer.OnCompletionListener mCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // Now that the sound file has finished playing, release the media player resources.
            System.out.println("delete Player:" );
            releaseMediaPlayer();
        }
    };

    //在播放完语音提示后，开始联通音频，双工通信
    private void releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.

        if (mMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        AudioStart();
    }

    //返回按钮
    public void ignore(View view) {
        if(audioContinueFlag)//如果点击返回时还没有关闭双工通信就关闭
        {
            AudioStop();
        }
        Intent intent = new Intent(InvadeActivity.this,WaveActivity.class);
        startActivity(intent);

    }

    /**
     音频收发
     */

    boolean audioContinueFlag= false;
    private void AudioStart() //开启通信
    {

        System.out.println("Audio Start ");
        creatAudioRecord();//初始化录音发送线程
        initData();//初始化接收播放线程
        startPlay();//开启接收播放线程
        startRecord();//开启录音发送线程
        audioContinueFlag= true;

    }

    private void AudioStop() //关闭通信
    {
        audioContinueFlag= false;
        System.out.println("Audio Stop ");
        stopRecord();//关闭录音发送线程
        stopPlay();//关闭接收播放线程
    }

    // 音频获取源
    private int audioSource = MediaRecorder.AudioSource.MIC;
    // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    private static int sampleRateInHz = 14000;
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
        new Thread(new InvadeActivity.AudioRecordThread()).start();
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

//        String host = "192.168.43.77" +
//                "";//client_host_ip.getText().toString();
//        String port = "8090";//;client_port.getText().toString();
        try {
            //创建客户端对象
            Socket socket = new Socket(getString(R.string.aoHost),Integer.parseInt(getString(R.string.aoPort)));

            System.out.println("SEND START ");

            //读取刚刚录制的音频文件内数据

            //获取客户端对象的输出流
            OutputStream outputStream = socket.getOutputStream();
//                    outputStream.write("PCMSEND".getBytes());//发送控制指令，启用传输音频功能

            outputStream.flush();//为了提高效率当write()的时候不一定直接发过去，有可能先缓存起来一起发。flush()的作用就是强制性地将缓存中的数据发出去


            System.out.println("打印客户端中的内容：" + socket);

            //发送文件大小


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
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
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
     * 从socket
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {


//            String host = "192.168.43.77";//client_host_ip.getText().toString();
//            String port = "8091";//;client_port.getText().toString();
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