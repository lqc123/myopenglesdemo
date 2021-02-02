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
            val constructor = clazz.constructors[0]
            val render: Renderer
            render = if (constructor.parameterTypes.size == 1) {
                constructor.newInstance(this) as Renderer
            } else {
                constructor.newInstance() as Renderer
            }
            //opengles新版本api，默认不配置，需要自己设置否则模板测试无作用
            //设置配置选择器，该选择器将选择至少具有指定的depthSize和stencilSize以及恰好具有指定的redSize，greenSize，blueSize和alphaSize的配置。
            //如果调用此方法，则必须在调用setRenderer(GLSurfaceView.Renderer)之前调用它。
            //如果未调用setEGLConfigChooser方法，则默认情况下，视图将选择深度缓冲区深度至少为16位的RGB_888表面。
            setEGLConfigChooser(8, 8, 8, 8, 16, 8)
            setRenderer(render)
            renderMode = RENDERMODE_WHEN_DIRTY
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}