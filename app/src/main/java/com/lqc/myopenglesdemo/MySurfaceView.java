package com.lqc.myopenglesdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.myapplication.R;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private static final String TAG = "MySurfaceView";
    // 是否绘制
    private volatile boolean mIsDrawing;
    // SurfaceView 控制器
    private SurfaceHolder mSurfaceHolder;
    // 画笔
    private Paint mPaint;
    // 画布
    private Canvas mCanvas;
    // 独立的线程
    private Thread mThread;
    // 绘制的图像
    private Bitmap mBitmap;

    public MySurfaceView(Context context) {
        super(context);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    private void init() {
        mSurfaceHolder = getHolder();
        // 注册回调事件
        mSurfaceHolder.addCallback(this);


        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "onSurfaceCreated");
        mThread = new Thread(this, "Renderer");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Log.d(TAG,"onSurfaceChanged. format:{}, width:{}, height:{}", format, width, height);
        // 加载图像，并开启线程
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_background);
        mIsDrawing = true;
        mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "onSurfaceDestroyed");
        // 不再绘制，线程终止
        mIsDrawing = false;
        mThread.interrupt();
    }

    @Override
    public void run() {
        while (mIsDrawing) {
            Log.d(TAG, "draw canvas");
            // 锁定画布，获得画布对象
            mCanvas = mSurfaceHolder.lockCanvas();
            if (mCanvas != null) {
                try {
                    //使用画布做具体的绘制
                    draw();
                    // 线程休眠 100 ms
                    Thread.sleep(100);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                } finally {
                    // 解锁画布，提交绘制，显示内容
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }
    }

    private void draw() {
        mCanvas.drawBitmap(mBitmap, 0, 0, mPaint);
    }
}