package com.lqc.myopenglesdemo

import android.app.Activity
import android.content.Context
import android.opengl.GLSurfaceView

/**
 * Description:
 * Author: liuqiancheng
 * Date: 21-2-1 下午5:28
 */
internal class MyGLSurfaceView(context: Context) :
    GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(3)
        val typeName = (context as Activity).intent.getStringExtra(TextureActivity.TYPE)
        try {
            val clazz = Class.forName(typeName)
            val constructor = clazz.getConstructor()
            val render: Renderer
            render = if (constructor.parameterTypes.size == 1) {
                constructor.newInstance(this) as Renderer
            } else {
                constructor.newInstance() as Renderer
            }
            setEGLConfigChooser(8, 8, 8, 8, 16, 8)
            setRenderer(render)
            renderMode = RENDERMODE_WHEN_DIRTY
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}