package com.lqc.myopenglesdemo.render

import android.app.Activity
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.view.Surface
import com.example.myapplication.R
import com.lqc.myopenglesdemo.ResReadUtils
import com.lqc.myopenglesdemo.ShaderUtils
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * FileName: CameraTextureRender
 * Author: liuqiancheng
 * Date: 2019/11/4 17:35
 * Description: 相机预览实施处理
 */
class CameraTextureRender : GLSurfaceView.Renderer {
    private var glSurfaceView: GLSurfaceView
    private var camera: Camera

    constructor(glSurfaceView: GLSurfaceView) {
        this.glSurfaceView = glSurfaceView
        this.camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK)
        setCameraDisplayOrientation(0,camera)
    }

    private fun setCameraDisplayOrientation(cameraId: Int, camera: Camera) {
        val targetActivity = glSurfaceView.getContext() as Activity
        val info = Camera.CameraInfo()
        Camera.getCameraInfo(cameraId, info)
        val rotation = targetActivity.windowManager.defaultDisplay
            .rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        camera.setDisplayOrientation(result)
    }


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
     * (s,t) 需要旋转90度
     */
//    private val textureVertexPoints = floatArrayOf(
//        0.5f, 0.5f, //纹理坐标V0
//        1f, 0f, //纹理坐标V1
//        0f, 0f, //纹理坐标V2
//        0f, 1.0f, //纹理坐标V3
//        1f, 1.0f    //纹理坐标V4
//    )

    /**
     * 正常坐标
     * 0,0  左下
     * 1,0 右下
     * 0,1  左上
     * 1,1 左下
     *
     */
//    private val textureVertexPoints = floatArrayOf(
//        0.5f, 0.5f, //纹理坐标V0
//        1f, 0f, //纹理坐标V1
//        0f, 0f, //纹理坐标V2
//        0f, 1.0f, //纹理坐标V3
//        1f, 1.0f    //纹理坐标V4
//    )
    /**
     *
     */
    private val textureVertexPoints = floatArrayOf(
        0.5f, 0.5f, //纹理坐标V0
        1f, 1f, //纹理坐标V1 右上
        0f, 1f, //纹理坐标V2 左上
        0f, 0.0f, //纹理坐标V3  左下
        1f, 0.0f    //纹理坐标V4右下
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
            "layout (location = 1) in vec4 aTexPosition;\n" +
            "out vec2 vTexPosition;\n" +
            //纹理矩阵
            "uniform mat4 uTextureMatrix;\n" +
            "uniform vec3 uFilter;\n" +
            "out vec3 vFilter;\n" +
            "void main(){\n" +
            //只要xy
            "vTexPosition=(uTextureMatrix * aTexPosition).xy;\n" +
            "gl_Position=aPosition;\n" +
            "vFilter=uFilter;\n" +
            "}"

    private val fragmentShader = "#version 300 es\n" +

            //这行是OpenGL ES 2.0中的声明"
//            "#extension GL_OES_EGL_image_external : require\n"
            //这行是OpenGL ES 3.0中的声明,opengl只支持rbga纹理格式，而camera返回数据格式为nv21（yuv420p）,所以必须要添加拓展
            "#extension GL_OES_EGL_image_external_essl3 : require\n" +
            //声明一定要要在变量之前
            "precision mediump float;\n" +
            //uniform统一变量，纹理单元，由于GPU绘制纹理数量有限，不能直接给着色器传递纹理，需要使用纹理单元保存
            //二维纹理数据数组
            "uniform samplerExternalOES yuvTexSampler;\n" +
            "in vec3 vFilter;\n" +
            "out vec4 vFragColor;\n" +
            "in vec2 vTexPosition;\n" +
            "void main(){\n" +
            //texture2D，着色器函数，根据纹理坐标读取特定颜色
            "vec4 nColor=texture(yuvTexSampler,vec2(vTexPosition.x,vTexPosition.y));\n" +
            " float c=nColor.r*vFilter.r+nColor.g*vFilter.g+nColor.b*vFilter.b;\n" +
            " vFragColor=vec4(c,c,c,nColor.a);\n" +
            "}"


    private lateinit var vertexBuffer: Buffer
    private lateinit var indexBuffer: Buffer
    private lateinit var texVertexBuffer: Buffer
    private var imageTexture = 0
    private var linkProgram = 0
    private var uFilterLocation = 0
    private var uTextureSamplerLocation = 0
    private var uuTextureMatrix = 0
    private lateinit var surfaceTexture: SurfaceTexture
    private val transformMatrix = FloatArray(16)
    val texture: IntArray = intArrayOf(1)

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
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
            ShaderUtils.compileFragmentShader(ResReadUtils.readResource(R.raw.fragment_filler_blur_shader))
        linkProgram = ShaderUtils.linkProgram(
            compileVertexShader,
            compileFragmentShader
        )
        GLES30.glUseProgram(linkProgram)
        uFilterLocation = GLES30.glGetUniformLocation(linkProgram, "uFilter")
//        uTextureSamplerLocation = GLES30.glGetUniformLocation(linkProgram, "yuvTexSampler")
        uuTextureMatrix = GLES30.glGetUniformLocation(linkProgram, "uTextureMatrix")
        bindTexture()
        initCamera()
    }

    private fun initCamera() {

        surfaceTexture = SurfaceTexture(imageTexture)
        surfaceTexture.setOnFrameAvailableListener {
            glSurfaceView.requestRender()
        }
        camera.setPreviewCallback(object :Camera.PreviewCallback{
            override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {

            }

        })
        //设置SurfaceTexture作为相机预览输出
        try {
            camera.setPreviewTexture(surfaceTexture)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        //开启相机预览
        camera.startPreview()
    }



    /**
     * 绑定纹理
     */
    private fun bindTexture() {

        //获取纹理对象索引
        GLES30.glGenTextures(1, texture, 0)
        imageTexture = texture[0]
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, imageTexture)

        //设置纹理过滤参数
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MIN_FILTER,
            GLES30.GL_NEAREST
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_MAG_FILTER,
            GLES30.GL_LINEAR
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_S,
            GLES30.GL_CLAMP_TO_EDGE
        )
        GLES30.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            GLES30.GL_TEXTURE_WRAP_T,
            GLES30.GL_CLAMP_TO_EDGE
        )

        //取消绑定
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
//        GLES30.glUseProgram(linkProgram)
        //更新纹理图像
        surfaceTexture.updateTexImage()
        surfaceTexture.getTransformMatrix(transformMatrix)

        //激活纹理，把活动的纹理设置为纹理0
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        //更新参数
        GLES30.glUniform3fv(uFilterLocation, 1, floatArrayOf(0.299f, 0.587f, 0.114f), 0)
        //绑定纹理
        GLES30.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, imageTexture)
        //将此纹理单元床位片段着色器的uTextureSampler外部纹理采样器
//        GLES30.glUniform1i(uTextureSamplerLocation, 0)

        GLES30.glUniformMatrix4fv(uuTextureMatrix, 1,false, transformMatrix, 0)

        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, 0, vertexBuffer)

        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 2, GLES30.GL_FLOAT, false, 0, texVertexBuffer)




        GLES30.glDrawElements(
            GLES30.GL_TRIANGLES,
            vertexIndex.size,
            GLES30.GL_UNSIGNED_SHORT,
            indexBuffer
        )

    }
    fun destory(){

        if(imageTexture!=0){
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)
            GLES30.glDeleteTextures(1,texture,0)
        }
        if(linkProgram!=0){
            GLES30.glDeleteProgram(linkProgram)
        }
    }
}