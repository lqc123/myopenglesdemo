//
// Created by admin on 2019/12/9.
//
#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
void throwExp(JNIEnv *env,char *typeName, char *desc){
   jclass jclazz=env->FindClass(typeName);
   if(NULL!=jclazz){
       env->ThrowNew(jclazz,desc);
   }
}

extern "C" JNIEXPORT void JNICALL Java_com_example_myapplication_NativeActivity_drawColor(JNIEnv *env, jobject, jobject surface, jint color){
    int a=color>>24&0xff;
    int r=color>>16&0xff;
    int g=color>>8&0xff;
    int b=color&0xff;
    int abgr=a<<24|b<<16|g<<8|r;

    //创建本地surface
    ANativeWindow* aNativeWindow= ANativeWindow_fromSurface(env,surface);
    if(aNativeWindow== NULL){
        throwExp(env, "java/lang/RuntimeException","native window null");
        return;
    }
    if(ANativeWindow_setBuffersGeometry(aNativeWindow,300,300,WINDOW_FORMAT_RGBA_8888)<0){
        throwExp(env, "java/lang/RuntimeException","ANativeWindow_setBuffersGeometry error");
        ANativeWindow_release(aNativeWindow) ;
        aNativeWindow=NULL;
        return;
    }
    ANativeWindow_acquire(aNativeWindow);
    ANativeWindow_Buffer buffer;
    if(ANativeWindow_lock(aNativeWindow,&buffer,NULL)<0){
        throwExp(env, "java/lang/RuntimeException","ANativeWindow_lock error");
        ANativeWindow_release(aNativeWindow) ;
        aNativeWindow=NULL;
        return;
    }
    int32_t* data=(int32_t*)buffer.bits;
    for (int i = 0; i < buffer.height; ++i) {
        for (int j=0;j<buffer.width;j++){
            data[j]=abgr;
        }
        data+=buffer.stride;
    }
    ANativeWindow_unlockAndPost(aNativeWindow);
}
