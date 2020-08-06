package com.lqc.myopenglesdemo

import android.graphics.Color
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

/**
 * FileName: NativeActivity
 * Author: liuqiancheng
 * Date: 2019/12/9 17:24
 * Description:
 * Version: 1.0.0
 */
class NativeActivity : AppCompatActivity() {
    init {
        System.loadLibrary("native-window")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native)
        val sv = findViewById<SurfaceView>(R.id.sv)
        sv.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceChanged(
                holder: SurfaceHolder?,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
            }

            override fun surfaceCreated(holder: SurfaceHolder?) {
                drawColor(sv.holder.surface,Color.RED)
            }


        })
    }
    external fun drawColor(surface: Surface, color: Int)

}