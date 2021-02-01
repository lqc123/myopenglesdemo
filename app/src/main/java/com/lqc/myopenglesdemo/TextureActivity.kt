package com.lqc.myopenglesdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * FileName: TextureActivity
 * Author: liuqiancheng
 * Date: 2019/11/4 10:13
 */
class TextureActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val glSurfaceView = MyGLSurfaceView(this)


        setContentView(glSurfaceView)
    }
    companion object{
        const val TYPE="TYPE"
        @JvmStatic
        fun start(context: Context, typeName: String){
            val intent = Intent(context, TextureActivity::class.java)
            intent.putExtra(TYPE, typeName)
            context.startActivity(intent)
        }
    }



}