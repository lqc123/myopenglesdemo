package com.lqc.myopenglesdemo.render

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.lqc.myopenglesdemo.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * FileName: MyRender
 * Author: liuqiancheng
 * Date: 2019/10/10 15:24
 * Description:基础图形绘制
 */
internal class SimpleRender : GLSurfaceView.Renderer {

    /**
     * 点的坐标
     */
    private val vertexPoints = floatArrayOf(
        -0.5f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, 0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )
    /**
     * 顶点颜色
     */
    private val vertexColors =
        floatArrayOf(
            0.0f, 1.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 1.0f, 1.0f,
            0.0f, 1.0f, 0.0f, 1.0f
        )
    /**
     * 顶点索引
     */
    private val index =
        shortArrayOf(
            0, 1, 2, 0, 2, 3
        )

    val vertexPositionBuffer =
        ByteBuffer.allocateDirect(vertexPoints.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(vertexPoints).position(0)
    val vertexColorBuffer =
        ByteBuffer.allocateDirect(vertexColors.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(vertexColors).position(0)

    val indexBuffer =
        ByteBuffer.allocateDirect(index.size * 2).order(ByteOrder.nativeOrder())
            .asShortBuffer().put(index).position(0)

    /**
     * es2.0
     * attribute 传入的属性值
     * varying 多个着色器可用于相互传值
     * uniform 统一变量
     *
     * es3.0
     * in 传入的属性值
     * out 多个着色器可用于相互传值
     *  uniform 统一变量
     */


    /**
     * 顶点着色器
     */
//    private val vertexShader = (
//            "#version 300 es \n" +
//                    "layout (location = 0) in vec4 vPosition;\n"
//                    + "layout (location = 1) in vec4 aColor;\n"
//                    + "out vec4 vColor;\n"
//                    + "void main() { \n"
//                    + "gl_Position  = vPosition;\n"
//                    + "gl_PointSize = 30.0;\n"
//                    + "vColor = aColor;\n"
//                    + "}\n")

    private val vertexShader = (
                    "attribute  vec4 vPosition;\n"
                    + "attribute  vec4 aColor;\n"
                    + "varying  vec4 vColor;\n"
                    + "void main() { \n"
                    + "gl_Position  = vPosition;\n"
                    + "gl_PointSize = 30.0;\n"
                    + "vColor = aColor;\n"
                    + "}\n")
    /**
     * 片段着色器
     */
//    private val fragmentShader = (
//            "#version 300 es \n" +
//                    "precision mediump float;\n"
//                    + "in vec4 vColor;\n"
//                    + "out vec4 fragColor;\n"
//                    + "void main() { \n"
//                    + "fragColor = vColor; \n"
//                    + "}\n")


    private val fragmentShader = (
                    "precision mediump float;\n"
                    + "varying vec4 vColor;\n"
                    + "void main() { \n"
                    + "gl_FragColor = vColor; \n"
                    + "}\n")


    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexPositionBuffer)

        GLES30.glVertexAttribPointer(1, 4, GLES30.GL_FLOAT, false, 0, vertexColorBuffer)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glEnableVertexAttribArray(1)
        //GL_TRIANGLE_STRIP  偶数(n-2)(n-1)(n)   奇数 (n-1)(n-2)(n)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP,0,4)
//        GLES30.glDrawElements(GLES30.GL_TRIANGLES, index.size, GL10.GL_UNSIGNED_SHORT, indexBuffer)
        //禁止顶点数组的句柄
//        GLES30.glDisableVertexAttribArray(0)
//        GLES30.glDisableVertexAttribArray(1)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        val compileVertexShader =
            ShaderUtils.compileVertexShader(vertexShader)
        val compileFragmentShader =
            ShaderUtils.compileFragmentShader(fragmentShader)
        val linkProgram = ShaderUtils.linkProgram(
            compileVertexShader,
            compileFragmentShader
        )
        //在OpenGLES环境中使用程序片段
        GLES30.glUseProgram(linkProgram)
    }

}