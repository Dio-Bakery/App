package com.example.nightelf;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * 绘制微多谱勒图片的窗口
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder mHolder;
    //用于绘图的canvas
    private Canvas mCanvas;
    //子线程标志位
    private boolean mIsDrawing;
    Random r =new Random();
    int updateSize = 1;
//    public int[][] recdata = new int[96][20];
    public int[] recdata = new int[96*updateSize];

    int x = 0;
    int y = 400;
    int oldX = 0;
    int oldY = 0;


    private Path mPath = new Path();
    private Paint mPaint = new Paint();
    int[][] data = new int[96][140];
    boolean flag = true;
    boolean lastflag = true;


    public void rec_Data(byte[] bytes1) {
//        for(int i=0;i<=20;i++)
//        {
//            for(int j=0;j<96;j++)
//            {
//                recdata[j][i] = socket_data[j][i];
//            }
//
//        }

                    for(int i=0;i<updateSize;i++)
                    {
                        for(int j=0;j<96;j++)
                        {
                            recdata[i*96+j]=(int) bytes1[i*96+j] & 0xff;
                        }

                    }

        flag = false;//每接收一次数据就把flag翻转一次，只有flag翻转后才更新一次图片
    }

    public MySurfaceView(Context context) {
        super(context);
        initView();
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        mHolder = getHolder();
        mHolder.addCallback(this);
        setFocusable(true);
        setFocusableInTouchMode(true);
        this.setKeepScreenOn(true);
        mPath.moveTo(x, y);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(0xffaf7869);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;

        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
    }

    @Override
    public void run() {
        while (mIsDrawing) {


            if(!flag)
            {
                draw(recdata);
                flag = true;
            }


        }
    }

    void draw(int[] RecData) {
        try {
            mCanvas = mHolder.lockCanvas();
            mCanvas.drawColor(0xFF341C0C);
            mCanvas.drawPath(mPath, mPaint);


            //数据更新

//            for(int i=134;i>=20;i--)
//            {
//                data[i] = data[i-20];
//                //data[i] = (int) (20 * Math.sin(x *  Math.PI / 180) + 400+r.nextInt(70));
//                //System.out.println(data[i]);
//            }
//
//            //一次更新20个点
//            for(int i=0;i<=20;i++)
//            {
//                data[i] = (int) (30 * Math.sin(x *  Math.PI / 180) +r.nextInt(70));
//            }
//
//
//            //System.out.println("IUUUUUUUUUUUUUIIIIIIIIIS");
//            for(int i =1;i<=134;i++)
//            {
//                x = 8*i;
//                y = data[i]+ 400;
//                mCanvas.drawLine(x-8, data[i-1]+ 400, x, y, mPaint);
//
//
//            }

            for(int i=134;i>=updateSize;i--)
            {
                for(int j=0;j<96;j++)
                {
                    data[j][i] = data[j][i-updateSize];
                }

                //data[][i] = (int) (20 * Math.sin(x *  Math.PI / 180) + 400+r.nextInt(70));
                //
            }

            //一次更新updateSize个点
            for(int i=0;i<updateSize;i++)
            {
                for(int j=0;j<96;j++)
                {
                    data[j][i] = RecData[j+i*96];
                }

            }


            //System.out.println("IUUUUUUUUUUUUUIIIIIIIIIS");
            for(int i =1;i<=134;i++)
            {
                for(int j=0;j<96;j++)
                {

                    x = 8*i;
//                    mPaint.setColor(0xff0000ff);//蓝色
//                    mCanvas.drawLine(x-8, j, x, j, mPaint);
                    mPaint.setColor(Color.argb(100,colorData[(255-data[j][i])*3],colorData[(255-data[j][i])*3+1],colorData[(255-data[j][i])*3+2]));
                    mCanvas.drawRect(x-8,j*5,x,j*5+5,mPaint); // 以原始Canvas画出一个矩形1

                }

                //mPath.lineTo(x, y);

            }
            Thread.sleep(5);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != mCanvas) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }
    int[] colorData = new int[]{254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 254, 204, 85, 253, 204, 86, 252, 205, 85, 253, 205, 86, 252, 204, 86, 252, 204, 87, 252, 204, 87, 251, 205, 87, 251, 205, 87, 250, 206, 88, 250, 205, 88, 250, 206, 89, 250, 206, 88, 250, 206, 89, 250, 207, 89, 249, 206, 90, 248, 207, 90, 249, 207, 90, 248, 207, 91, 248, 207, 91, 247, 208, 91, 247, 207, 91, 246, 208, 91, 246, 208, 92, 246, 209, 92, 245, 208, 93, 246, 209, 93, 245, 209, 94, 244, 210, 94, 244, 209, 94, 244, 209, 95, 243, 210, 95, 243, 211, 95, 242, 210, 96, 242, 211, 96, 241, 211, 97, 241, 211, 97, 240, 211, 98, 240, 212, 98, 239, 212, 99, 238, 212, 99, 238, 212, 99, 238, 212, 101, 237, 213, 101, 236, 213, 101, 236, 213, 101, 235, 214, 102, 234, 214, 103, 234, 214, 103, 233, 214, 104, 232, 214, 104, 232, 214, 105, 230, 214, 105, 230, 215, 106, 229, 215, 107, 229, 215, 107, 227, 215, 107, 226, 215, 108, 226, 215, 108, 225, 215, 109, 224, 216, 110, 223, 215, 111, 221, 216, 111, 221, 216, 112, 220, 216, 112, 219, 216, 113, 218, 215, 114, 217, 216, 115, 215, 216, 115, 214, 216, 115, 213, 216, 116, 212, 216, 117, 210, 216, 117, 209, 216, 119, 207, 216, 119, 206, 216, 120, 205, 216, 120, 203, 216, 122, 201, 215, 123, 199, 216, 123, 197, 216, 124, 195, 216, 126, 194, 216, 127, 191, 216, 127, 189, 216, 128, 187, 216, 130, 185, 216, 131, 182, 216, 132, 180, 216, 133, 178, 216, 134, 175, 216, 135, 173, 216, 136, 170, 216, 138, 167, 216, 139, 165, 216, 140, 162, 216, 141, 160, 216, 143, 157, 216, 143, 154, 216, 145, 151, 216, 146, 150, 216, 147, 147, 216, 149, 144, 216, 150, 141, 216, 151, 139, 216, 152, 135, 216, 154, 133, 216, 155, 130, 216, 156, 127, 216, 158, 125, 216, 159, 121, 216, 160, 119, 216, 162, 116, 216, 162, 113, 216, 163, 111, 216, 165, 108, 216, 166, 105, 215, 167, 103, 216, 168, 100, 216, 170, 98, 215, 171, 95, 214, 172, 93, 214, 174, 90, 214, 175, 87, 214, 176, 85, 213, 177, 82, 213, 178, 80, 212, 179, 78, 212, 180, 76, 211, 181, 74, 211, 182, 72, 209, 183, 70, 208, 184, 68, 208, 184, 66, 207, 186, 63, 207, 187, 62, 206, 188, 60, 205, 189, 58, 204, 189, 57, 203, 190, 55, 202, 191, 54, 200, 192, 53, 200, 192, 52, 198, 193, 50, 197, 193, 49, 196, 194, 48, 195, 194, 48, 194, 195, 47, 193, 195, 46, 191, 196, 45, 190, 196, 45, 188, 197, 44, 187, 196, 43, 186, 197, 42, 185, 198, 42, 183, 198, 42, 182, 198, 41, 180, 198, 41, 178, 199, 40, 177, 199, 39, 175, 199, 40, 173, 199, 39, 172, 200, 40, 170, 200, 39, 168, 200, 39, 167, 201, 39, 165, 201, 40, 163, 201, 40, 161, 201, 39, 159, 201, 40, 157, 201, 39, 155, 201, 39, 153, 202, 40, 151, 202, 39, 149, 203, 40, 147, 203, 39, 145, 203, 40, 143, 203, 40, 141, 203, 40, 138, 203, 40, 137, 203, 39, 135, 203, 39, 132, 203, 39, 130, 204, 39, 128, 204, 40, 126, 203, 40, 124, 203, 39, 121, 204, 39, 120, 204, 39, 117, 204, 40, 115, 204, 39, 113, 204, 40, 111, 203, 40, 109, 204, 39, 107, 204, 39, 105, 204, 39, 102, 204, 40, 101, 204, 40, 98, 204, 39, 97, 204, 39, 95, 204, 39, 93, 204, 40, 91, 204, 39, 89, 205, 40, 87, 204, 40, 85, 205, 39, 83, 204, 39, 82, 204, 40, 80, 204, 40, 78, 204, 40, 76, 204, 39, 74, 205, 39, 72, 205, 39, 71, 205, 40, 69, 205, 39, 67, 205, 40, 66, 205, 40, 64, 204, 39, 63, 204, 39, 62, 205, 40, 60, 205, 40, 59, 205, 40, 57, 204, 39, 56, 205, 40, 54, 204, 39, 54, 205, 40, 52, 205, 40, 51, 205, 39, 50, 205, 40, 49, 205, 39, 49, 205, 39, 47, 205, 39, 47, 205, 39, 47, 205, 40, 47, 205, 40, 47, 205, 39, 47, 205, 39, 47, 205, 39, 47, 205, 40, 47, 205, 40, 47, 205, 40, 47, 205, 39, 47, 205, 40, 47, 205};

}
