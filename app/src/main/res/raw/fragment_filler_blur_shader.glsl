#version 300 es


// #extension GL_OES_EGL_image_external : require\n
//这行是OpenGL ES 3.0中的声明,opengl只支持rbga纹理格式，而camera返回数据格式为nv21（yuv420p）,所以必须要添加拓展
 #extension GL_OES_EGL_image_external_essl3 : require
//声明一定要要在变量之前
 precision mediump float;
//uniform统一变量，纹理单元，由于GPU绘制纹理数量有限，不能直接给着色器传递纹理，需要使用纹理单元保存
//二维纹理数据数组
uniform samplerExternalOES yuvTexSampler;
in vec3 vFilter;
out vec4 vFragColor;
in vec2 vTexPosition;
void main(){
//texture2D，着色器函数，根据纹理坐标读取特定颜色
	   float blurStep = 0.5;
       float step = blurStep / 100.0;
    vec4 sample0=texture(yuvTexSampler,vec2(vTexPosition.x - step, vTexPosition.y - step));
    vec4 sample1=texture(yuvTexSampler,vec2(vTexPosition.x + step, vTexPosition.y + step));
    vec4 sample2=texture(yuvTexSampler,vec2(vTexPosition.x + step, vTexPosition.y - step));
    vec4 sample3=texture(yuvTexSampler,vec2(vTexPosition.x - step, vTexPosition.y + step));
   vFragColor = (sample0 + sample1 + sample2 + sample3) / 4.0;
 }
