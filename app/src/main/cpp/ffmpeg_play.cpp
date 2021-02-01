#include <jni.h>

#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <assert.h>

#define LOG_TAG "FFMPEG_PLAY"
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG, __VA_ARGS__)
extern "C" {
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libswresample/swresample.h"
#include "libavutil/opt.h"
#include "libavutil/imgutils.h"
void throwExp(JNIEnv *env, const char *name, const char *desc) {
    jclass clazz = env->FindClass(name);
    if (clazz) {
        env->ThrowNew(clazz, desc);
    }
}

static double r2d(AVRational r) {
    LOGD("r.num= %d r.den=%d", r.num, r.den);
    return r.num == 0 || r.den == 0 ? 0 : (double) r.num / (double) r.den;

}

void play(JNIEnv *env, jobject surface, jstring path) {
    const char *url = env->GetStringUTFChars(path, JNI_FALSE);
    av_register_all();
    avformat_network_init();
    avcodec_register_all();
    AVFormatContext *avFormatContext = avformat_alloc_context();
    avformat_open_input(&avFormatContext, url, NULL, NULL);
    avformat_find_stream_info(avFormatContext, NULL);
    int videoIndex = 0;
    for (int i = 0; i < avFormatContext->nb_streams; ++i) {
        AVStream *stream = avFormatContext->streams[i];
        if (stream->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;
        } else if (stream->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {

        }
    }
    AVCodec *avCodec = avcodec_find_decoder(
            avFormatContext->streams[videoIndex]->codecpar->codec_id);
    AVCodecContext *avCodecContext = avcodec_alloc_context3(avCodec);
    avcodec_parameters_to_context(avCodecContext, avFormatContext->streams[videoIndex]->codecpar);
    avcodec_open2(avCodecContext, NULL, NULL);
    AVFrame *avFrame = av_frame_alloc();
    AVPacket *avPacket = av_packet_alloc();
    while (1) {
        int result = av_read_frame(avFormatContext, avPacket);
        if (result != 0) {
            LOGD("av_read_frame-->>%d", result);
            break;
        }
        if (avPacket->stream_index != videoIndex) {
            continue;
        }
        result = avcodec_send_packet(avCodecContext, avPacket);
        if (result != 0) {
            LOGD("avcodec_send_packet-->>%d", result);
            continue;
        }
        LOGD("avcodec_send_packet success");
        av_packet_unref(avPacket);
        while (1) {
            result = avcodec_receive_frame(avCodecContext, avFrame);
            if (result != 0) {
                LOGD("avcodec_receive_frame-->>%d", result);
                break;
            }
            LOGD("avcodec_receive_frame success");
        }
        av_frame_unref(avFrame);
    }
}

JNIEXPORT void JNICALL
Java_com_lqc_myopenglesdemo_PlayActivity_play(JNIEnv *env, jobject, jobject surface,
                                                 jstring path) {

    const char *url = env->GetStringUTFChars(path, JNI_FALSE);
    //初始化解封装
    av_register_all();
    //初始化全局网络组件，可选推荐使用，在使用网络协议的场景中这是必选的(rtfp http)
    avformat_network_init();
    //初始化所有的解码器
    avcodec_register_all();

    AVFormatContext *avFormatContext = NULL;
    //指定输入的格式，如果为NULL,将自动检测输入格式，所以可置为NULL
    //AVInputFormat *fmt = NULL;
    //打开输入文件，可以是本地视频或者网络视频
    int result = avformat_open_input(&avFormatContext, url, NULL, NULL);

    //打开输入内容失败
    if (result != 0) {
        LOGD("avformat_open_input failed!:%s", av_err2str(result));
        return;
    }

    //打开输入成功
    LOGD("avformat_open_input success!:%s", url);

    //读取媒体文件的分组以获得流信息。这个对于没有标题的文件格式（如MPEG）很有用。这个函数还计算实际的帧率在
    //MPEG-2重复的情况下帧模式。
    result = avformat_find_stream_info(avFormatContext, NULL);
    if (result < 0) {
        LOGD("avformat_find_stream_info failed: %s", av_err2str(result));
    }

    //获取到了输入文件信息，打印一下视频时长和nb_streams
    LOGD("duration = %lld nb_streams=%d", avFormatContext->duration, avFormatContext->nb_streams);

    //分离音视频，获取音视频在源文件中的streams index
    int videoIndex = 0;
    int audioIndex = 0;
    int fps = 0;
    for (int i = 0; i < avFormatContext->nb_streams; i++) {
        AVStream *avStream = avFormatContext->streams[i];
        if (avStream->codecpar->codec_type == AVMEDIA_TYPE_VIDEO) {
            //找到视频index
            videoIndex = i;
            LOGD("video index = %d", videoIndex);
            //FPS是图像领域中的定义，是指画面每秒传输帧数
            fps = r2d(avStream->avg_frame_rate);

            LOGD("video info ---- fps = %d fps den= %d fps num= %d width=%d height=%d code id=%d format=%d",
                 fps,
                 avStream->avg_frame_rate.den,
                 avStream->avg_frame_rate.num,
                 avStream->codecpar->width,
                 avStream->codecpar->height,
                 avStream->codecpar->codec_id,
                 avStream->codecpar->format
            );

        } else if (avStream->codecpar->codec_type == AVMEDIA_TYPE_AUDIO) {
            //找到音频index
            audioIndex = i;
            LOGD("audio index = %d sampe_rate=%d channels=%d sample_format=%d",
                 audioIndex,
                 avStream->codecpar->sample_rate,
                 avStream->codecpar->channels,
                 avStream->codecpar->format
            );
        }
    }
    /***************************************video解码器*********************************************/
    //找到视频解码器(软解码)
    AVCodec *videoAVCodec = avcodec_find_decoder(
            avFormatContext->streams[videoIndex]->codecpar->codec_id);
    //硬解码，硬解码需要Jni_OnLoad中做设置否则ffmpeg_player_error: avcodec_open2 video failed!
    //AVCodec *videoAVCodec = avcodec_find_decoder_by_name("h264_mediacodec");
    if (videoAVCodec == NULL) {
        LOGD("avcodec_find_decoder failed !");
        return;
    }
    //初始化视频解码器上下文对象
    AVCodecContext *videoCodecContext = avcodec_alloc_context3(videoAVCodec);
    //根据所提供的编解码器的值填充编解码器上下文参数
    avcodec_parameters_to_context(videoCodecContext,
                                  avFormatContext->streams[videoIndex]->codecpar);
    //设置视频解码器解码的线程数，解码时将会以你设定的线程进行解码
//    videoCodecContext->thread_count = 8;
    //打开解码器
    result = avcodec_open2(videoCodecContext, NULL, NULL);
    if (result != 0) {
        LOGD("avcodec_open2 video failed! %s", av_err2str(result));
        return;
    }
    /***********************************************************************************************/
    /***************************************audio解码器*********************************************/

    //找到音频解码器(软解码)
    AVCodec *audioAVCodec = avcodec_find_decoder(
            avFormatContext->streams[videoIndex]->codecpar->codec_id);
    //硬解码
    //AVCodec *audioAVCodec = avcodec_find_decoder_by_name("h264_mediacodec");
    //初始化音频解码器上下文对象
    AVCodecContext *audioCodecContext = avcodec_alloc_context3(audioAVCodec);
    //根据所提供的编解码器的值填充编解码器上下文参数
    avcodec_parameters_to_context(audioCodecContext,
                                  avFormatContext->streams[videoIndex]->codecpar);
    //设置音频解码器解码的线程数，解码时将会以你设定的线程进行解码
//    audioCodecContext->thread_count = 1;
    //打开音频解码器
    result = avcodec_open2(audioCodecContext, NULL, NULL);
    if (result != 0) {
        LOGD("avcodec_open2 audio failed!%s", av_err2str(result));
        return;
    }
    /***********************************************************************************************/
    //通道数
    int64_t out_ch_layout = AV_CH_LAYOUT_STEREO;
    //采样位数
    AVSampleFormat out_sample_fmt = AV_SAMPLE_FMT_S16;
//输出的采样率必须与输入相同
    int out_sample_rate = audioCodecContext->sample_rate;
    struct SwrContext *swrContext = swr_alloc();
    swr_alloc_set_opts(swrContext, out_ch_layout, out_sample_fmt, out_sample_rate,
                       audioCodecContext->channel_layout, audioCodecContext->sample_fmt,
                       audioCodecContext->sample_rate, 0, NULL);
    uint8_t *audioBuffer = (uint8_t *) av_malloc(2 * 44100);

    swr_init(swrContext);
    int outChLayout = av_get_channel_layout_nb_channels(out_ch_layout);

    //压缩数据
    AVPacket *avPacket = av_packet_alloc();
    AVFrame *avFrame = av_frame_alloc();
    //创建ANtiveWindow
    ANativeWindow *aNativeWindow = ANativeWindow_fromSurface(env, surface);
    if (aNativeWindow == NULL) {
        LOGD("ANativeWindow create fail");
        return;
    }
    if (ANativeWindow_setBuffersGeometry(aNativeWindow, videoCodecContext->width,
                                         videoCodecContext->height, WINDOW_FORMAT_RGBA_8888) != 0) {
        ANativeWindow_release(aNativeWindow);
        LOGD("ANativeWindow_setBuffersGeometry  error");
        return;
    }
    ANativeWindow_acquire(aNativeWindow);
    //分配缓冲区
    uint8_t *argbBuffer[4] = {0};
    int lineSize[4] = {0};
    int w = videoCodecContext->width;
    int h = videoCodecContext->height;
    result = av_image_alloc(argbBuffer, lineSize, w, h,
                            AV_PIX_FMT_RGBA, 1);
//也可以使用此方法分配
//    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, w, h, 1);
//    uint8_t * buffer = (uint8_t *)av_malloc(numBytes*sizeof(uint8_t));
//    av_image_fill_arrays(avArgbFrame->data, avArgbFrame->linesize, buffer, AV_PIX_FMT_RGBA, w, h, 1);
    if (result < 0) {
        LOGD("av_image_alloc  error-->>%d", result);
        return;
    }
    struct SwsContext *swsContext = sws_getContext(w, h,
                                                   videoCodecContext->pix_fmt, w,
                                                   h, AV_PIX_FMT_RGBA,
                                                   SWS_BICUBIC, NULL, NULL, NULL);

    while (1) {
        //如果返回结果小于0，则读取结束
        if (av_read_frame(avFormatContext, avPacket) != 0) {
            //清除缓存区
//            avcodec_send_packet(avCodecContext, NULL);
            break;
        }
        AVCodecContext *codecContext = videoCodecContext;
        if (avPacket->stream_index == audioIndex) {
            codecContext = audioCodecContext;
        }
        //将packet发送到解码器解码  老版本使用 avcodec_decode_video2
        result = avcodec_send_packet(codecContext, avPacket);
        if (result != 0) {
            LOGD("avcodec_send_packet-->> %d", result);
            continue;
        }

        //回收packet
        av_packet_unref(avPacket);


        for (;;) {
            //取出解码数据
            result = avcodec_receive_frame(videoCodecContext, avFrame);
            //失败
            if (result != 0) {
                LOGD("avcodec_receive_frame failed! %s", av_err2str(result));
                break;
            }
            if (codecContext == videoCodecContext) {
                LOGD("avcodec_send_packet video success");
                sws_scale(swsContext, avFrame->data, avFrame->linesize, 0,
                          videoCodecContext->height, argbBuffer, lineSize);
                //如果是视频
                ANativeWindow_Buffer buffer;
                if (ANativeWindow_lock(aNativeWindow, &buffer, NULL) == 0) {
                    uint8_t *dstData = (uint8_t *) buffer.bits;
                    int stride = buffer.stride * 4;
                    for (int i = 0; i < h; ++i) {
                        memcpy(dstData + i * stride, argbBuffer[0] + i * lineSize[0], stride);
                    }
                    ANativeWindow_unlockAndPost(aNativeWindow);
                }
            } else {
                LOGD("avcodec_send_packet audio success");
                const uint8_t *data = (uint8_t *) avFrame->data;
                swr_convert(swrContext, &audioBuffer, 2 * 44100, &data, avFrame->nb_samples);
                //获取pcm大小
                int size = av_samples_get_buffer_size(NULL, outChLayout, avFrame->nb_samples,
                                                      AV_SAMPLE_FMT_S16, 1);
            }
        }
        av_frame_unref(avFrame);
    }
    swr_free(&swrContext);
    sws_freeContext(swsContext);
    ANativeWindow_release(aNativeWindow);
    av_packet_free(&avPacket);
    av_frame_free(&avFrame);
    avcodec_close(videoCodecContext);
    avformat_close_input(&avFormatContext);
    env->ReleaseStringUTFChars(path, url);
}

}
