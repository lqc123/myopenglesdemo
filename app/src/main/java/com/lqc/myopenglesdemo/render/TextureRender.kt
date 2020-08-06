package com.lqc.myopenglesdemo.render

import android.graphics.BitmapFactory
import android.opengl.*
import com.lqc.myopenglesdemo.MyApp
import com.example.myapplication.R
import com.lqc.myopenglesdemo.ShaderUtils
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * FileName: TextureRender
 * Author: liuqiancheng
 * Date: 2019/11/4 9:21
 * Description: 加载图片纹理，纹理是图片、或者算法生成的分形数据
 */
internal class TextureRender : GLSurfaceView.Renderer {
    /**
     * 顶点左边范围是-1到1
     */
    private val vertexPoints = floatArrayOf(
        0f, 0f, 0f, //顶点坐标V0
        1f, 1f, 0f, //顶点坐标V1
        -1f, 1f, 0f, //顶点坐标V2
        -1f, -1f, 0f, //顶点坐标V3
        1f, -1f, 0f     //顶点坐标V4
    )

    /**
     * 纹理坐标 ,纹理坐标范围是0到1
     * (s,t)
     */
    private val textureVertexPoints = floatArrayOf(
        0.5f, 0.5f, //纹理坐标V0
        1f, 0f, //纹理坐标V1
        0f, 0f, //纹理坐标V2
        0f, 1.0f, //纹理坐标V3
        1f, 1.0f    //纹理坐标V4
    )

    /**
     * 索引
     */
    private val vertexIndex = shortArrayOf(
        0, 1, 2, //V0,V1,V2 三个顶点组成一个三角形
        0, 2, 3, //V0,V2,V3 三个顶点组成一个三角形
        0, 3, 4, //V0,V3,V4 三个顶点组成一个三角形
        0, 4, 1   //V0,V4,V1 三个顶点组成一个三角形
    )


    private val vertexShader = "#version 300 es \n" +
            //layout指定属性的位置
            "layout (location = 0) in vec4 aPosition;\n" +
            "layout (location = 1) in vec2 aTexPosition;\n" +
            "out vec2 vTexPosition;\n" +
            "uniform vec3 uFilter;\n" +
            "out vec3 vFilter;\n" +
            "uniform mat4 uMatrix;\n" +
            "void main(){\n" +
            "vTexPosition=aTexPosition;\n" +
            "gl_Position=uMatrix*aPosition;\n" +
            "vFilter=uFilter;\n" +
            "gl_PointSize=30.0;\n" +
            "}"

    private val fragmentShader = "#version 300 es\n" +
            "precision mediump float;\n" +
            //uniform统一变量，纹理单元，由于GPU绘制纹理数量有限，不能直接给着色器传递纹理，需要使用纹理单元保存
            //二维纹理数据数组
            "uniform sampler2D uTextureUnit;\n" +
           "out vec4 gl_FragColor;\n" +
            "in vec2 vTexPosition;\n" +
            "in vec3 vFilter;\n" +
            "void main(){\n" +
            //texture2D，着色器函数，根据纹理坐标读取特定颜色    es3.0 texture
            "vec4 nColor=texture(uTextureUnit,vec2(vTexPosition.x,vTexPosition.y));\n" +
            " float c=nColor.r*vFilter.r+nColor.g*vFilter.g+nColor.b*vFilter.b;\n" +
            //3.0 vFragColor  2.0为gl_FragColor
            "gl_FragColor=vec4(c,c,c,nColor.a);\n" +
            "}"


    private lateinit var vertexBuffer: Buffer
    private lateinit var indexBuffer: Buffer
    private lateinit var texVertexBuffer: Buffer
    private var imageTexture = 0
    private var linkProgram = 0
    private var uFilterLocation = 0
    private var uMatrixLocation = 0
    private val uMatrix:FloatArray= FloatArray(16)
    private val uModuleMatrix:FloatArray= FloatArray(16)

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
//        val aspectRatio = if (width > height)
//            width.toFloat() / height.toFloat()
//        else
//            height.toFloat() / width.toFloat()
//        val aspectRatio=1.5f
//        if (width > height) {
//            //横屏
//            Matrix.orthoM(uMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f)
//        } else {
//            //竖屏
//            Matrix.orthoM(uMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f)
//        }
//        Matrix.setRotateM(uModuleMatrix, 0, 0f, 1f, 0f, 0f)
//        Matrix.rotateM(uModuleMatrix,0,90f,1f,0f,0f)
//        Matrix.multiplyMM()
        Matrix.setIdentityM(uModuleMatrix,0)
//        Matrix.scaleM(uModuleMatrix,0,2f,0.5f,1f)
        Matrix.rotateM(uModuleMatrix,0,90f,1.0f, 1.0f, 1.0f)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.5f, 0.5f, 0.5f, 0.5f)
        vertexBuffer =
            ByteBuffer.allocateDirect(vertexPoints.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(vertexPoints).position(0)

        texVertexBuffer =
            ByteBuffer.allocateDirect(textureVertexPoints.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(textureVertexPoints).position(0)

        indexBuffer =
            ByteBuffer.allocateDirect(vertexIndex.size * 2).order(ByteOrder.nativeOrder())
                .asShortBuffer().put(vertexIndex).position(0)

        val compileVertexShader =
            ShaderUtils.compileVertexShader(vertexShader)
        val compileFragmentShader =
            ShaderUtils.compileFragmentShader(fragmentShader)
        linkProgram = ShaderUtils.linkProgram(
            compileVertexShader,
            compileFragmentShader
        )
        GLES30.glUseProgram(linkProgram)
        uFilterLocation = GLES30.glGetUniformLocation(linkProgram, "uFilter")
        uMatrixLocation = GLES30.glGetUniformLocation(linkProgram, "uMatrix")

        val texture: IntArray = intArrayOf(1)
        //获取纹理对象索引
        GLES30.glGenTextures(1, texture, 0)
        imageTexture = texture[0]
        val option = BitmapFactory.Options()
        option.inScaled = false
        val bitmap =
            BitmapFactory.decodeResource(
                MyApp.sContext.resources,
                R.drawable.image_0, option)
        if (bitmap == null) {
            GLES30.glDeleteTextures(1, texture, 0)
            return
        }
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)

        GLES30.glTexParameteri(
            GLES30.GL_TEXTURE_2D,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_LINEAR_MIPMAP_LINEAR
        )
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)

        //加载bitmap到纹理中
        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, bitmap, 0)
        //生成mip贴图
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D)

        // 数据如果已经被加载进OpenGL,则可以回收该bitmap
        bitmap.recycle()
        //取消绑定
        GLES20.glBindTexture(GLES30.GL_TEXTURE_2D, 0)


    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)
        //激活纹理，把活动的纹理设置为纹理0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //更新参数
        GLES30.glUniform3fv(uFilterLocation,1, floatArrayOf(0.299f, 0.587f, 0.114f),0)
        GLES30.glUniformMatrix4fv(uMatrixLocation,1,false,uModuleMatrix,0)
        //绑定纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, imageTexture)
        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            vertexIndex.size,
            GLES30.GL_UNSIGNED_SHORT,
            indexBuffer
        )


    }

}