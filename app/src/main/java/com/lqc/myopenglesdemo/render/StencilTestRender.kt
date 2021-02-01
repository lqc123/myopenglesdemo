package com.lqc.myopenglesdemo.render

import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.lqc.myopenglesdemo.ShaderUtils
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * FileName: MyRender
 * Author: liuqiancheng
 * Date: 2019/10/10 15:24
 * Description:模板测试
 */
internal class StencilTestRender : GLSurfaceView.Renderer {

    /**
     * 点的坐标
     */
    private val vertexPoints = floatArrayOf(
        -1f, 1f, 0.0f,
        -1f, -1f, 0.0f,
        1f, -1f, 0.0f,
        1f, 1f, 0.0f
    )
    private val vertexPoints2 = floatArrayOf(
        -0.5f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f,
        0.5f, 0.5f, 0.0f
    )


    /**
     * 顶点索引
     */
    private val index =
        shortArrayOf(
            0, 2, 1, 0, 3, 2
        )
    private var mProgram:Int=0

    val vertexPositionBuffer =
        ByteBuffer.allocateDirect(vertexPoints.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(vertexPoints).position(0)
    val vertexPositionBuffer2 =
        ByteBuffer.allocateDirect(vertexPoints2.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().put(vertexPoints2).position(0)

    val indexBuffer =
        ByteBuffer.allocateDirect(index.size * 2).order(ByteOrder.nativeOrder())
            .asShortBuffer().put(index).position(0)

    private val vertexShader = (
            "attribute  vec4 vPosition;\n"
                    + "void main() { \n"
                    + "gl_Position  = vPosition;\n"
                    + "}\n")

    private val fragmentShader = (
            "precision mediump float;\n"
                    + "uniform vec4 vColor;\n"
                    + "void main() { \n"
                    + "gl_FragColor = vColor; \n"
                    + "}\n")


    override fun onDrawFrame(gl: GL10?) {

//        disable()
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT or GLES30.GL_STENCIL_BUFFER_BIT)

//        GLES30.glStencilMask(0x00);	// 保证在绘制地板的时候不会更新模板缓冲

        enableDebugWrite()
        drawRect(vertexPositionBuffer, floatArrayOf(1f, 1f, 1f, 1f))

        enableDebugWrite()
        drawRect(vertexPositionBuffer, floatArrayOf(1f, 1f, 1f, 1f))

        enableDebugWrite()
        drawRect(vertexPositionBuffer, floatArrayOf(1f, 1f, 1f, 1f))

        enableDebugWrite()
        drawRect(vertexPositionBuffer, floatArrayOf(1f, 1f, 1f, 1f))

        enableDebugTest(1, false)
        drawRect(vertexPositionBuffer2, floatArrayOf(0f, 0f, 1f, 0f))
        enableDebugTest(2, false)
        drawRect(vertexPositionBuffer2, floatArrayOf(0f, 1f, 0f, 0f))
        enableDebugTest(3, true)
        drawRect(vertexPositionBuffer2, floatArrayOf(1f, 0f, 0f, 0f))

        GLES30.glDisable(GLES30.GL_STENCIL_TEST)


    }

    private fun drawRect(vertex: Buffer, color: FloatArray) {
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertex)
        // get handle to fragment shader's vColor member

        // get handle to fragment shader's vColor member
       val colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")

        GLES20.glUniform4fv(colorHandle, 1, color, 0)
        GLES30.glEnableVertexAttribArray(0)
        // GL_TRIANGLE_STRIP  偶数(n-2)(n-1)(n)   奇数 (n-1)(n-2)(n)
        GLES30.glDrawElements(GLES30.GL_TRIANGLES, index.size, GL10.GL_UNSIGNED_SHORT, indexBuffer)
        //        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 3)
        GLES30.glDisableVertexAttribArray(0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        GLES30.glClearColor(1f, 1f, 1f, 1f)
        val compileVertexShader =
            ShaderUtils.compileVertexShader(vertexShader)
        val compileFragmentShader =
            ShaderUtils.compileFragmentShader(fragmentShader)
        mProgram = ShaderUtils.linkProgram(
            compileVertexShader,
            compileFragmentShader
        )
        //在OpenGLES环境中使用程序片段
        GLES30.glUseProgram(mProgram)
    }

    fun enableDebugWrite() {
        GLES30.glEnable(GLES30.GL_STENCIL_TEST)
        //用于已储存的模板值和ref之间进行比较,所有的片段都应该更新模板缓冲
        GLES30.glStencilFunc(GLES30.GL_ALWAYS, 0x1, 0xff)
        // The test always passes so the first two values are meaningless
        GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_INCR);
        GLES30.glColorMask(true, true, true, true)
        //启用模板缓冲写入
        GLES30.glStencilMask(0xff)

    }

    fun enableDebugTest(value: Int, greater: Boolean) {
        GLES30.glEnable(GLES30.GL_STENCIL_TEST)
        GLES30.glStencilFunc(
            if (greater) GLES30.GL_LESS else GLES30.GL_EQUAL,
            value,
            0xff
        )
        // We only want to test, let's keep everything
        GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP, GLES30.GL_KEEP)
        //禁止模板缓冲的写入
        GLES30.glStencilMask(0)

    }
    fun enableWrite(incrementThreshold:Int) {
        GLES30.glEnable(GLES30.GL_STENCIL_TEST)
        if (incrementThreshold > 0) {
            GLES30.glStencilFunc(GLES30.GL_ALWAYS, 1, 0xff);
            // The test always passes so the first two values are meaningless
            GLES30.glStencilOp(GLES30.GL_INCR, GLES30.GL_INCR, GLES30.GL_INCR);
        } else {
            GLES30.glStencilFunc(GLES30.GL_ALWAYS,1, 0xff);
            // The test always passes so the first two values are meaningless
            GLES30.glStencilOp(GLES30.GL_KEEP, GLES30.GL_KEEP,GLES30. GL_REPLACE);
        }
        GLES30.glColorMask(false,false,false,false);
        GLES30. glStencilMask(0xff);
    }
    fun disable() {
        GLES30.glDisable(GLES30.GL_STENCIL_TEST);
    }


}