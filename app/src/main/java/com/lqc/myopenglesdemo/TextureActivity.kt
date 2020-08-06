package com.lqc.myopenglesdemo

import android.content.Context
import android.content.Intent
import android.opengl.GLSurfaceView
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
        val typeName = intent.getStringExtra(TYPE)
        val glSurfaceView = GLSurfaceView(this)
        glSurfaceView.setEGLContextClientVersion(3)
        val clazz = Class.forName(typeName)
        val constructor = clazz.constructors[0]
        var render:GLSurfaceView.Renderer = if(constructor.parameterTypes.size==1){
            constructor.newInstance(glSurfaceView) as GLSurfaceView.Renderer
        }else{
            constructor.newInstance() as GLSurfaceView.Renderer
        }
        glSurfaceView.setRenderer(render)
        setContentView(glSurfaceView)
    }
    companion object{
        const val TYPE="TYPE"
        @JvmStatic
        fun start(context: Context,typeName:String){
            val intent = Intent(context, TextureActivity::class.java)
            intent.putExtra(TYPE,typeName)
            context.startActivity(intent)
        }
    }
}