
cmake_minimum_required(VERSION 3.4.1)

##官方标准配置
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
include_directories(${libs}/include)
# -L${libs}/${ANDROID_ABI}
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -Wall")
add_library(avcodec SHARED IMPORTED)
set_target_properties(avcodec
        PROPERTIES IMPORTED_LOCATION
        ${libs}/${ANDROID_ABI}/libavcodec.so)
add_library(avformat SHARED IMPORTED)
set_target_properties(avformat
        PROPERTIES IMPORTED_LOCATION
        ${libs}/${ANDROID_ABI}/libavformat.so)

add_library(avfilter SHARED IMPORTED)
set_target_properties(avfilter
        PROPERTIES IMPORTED_LOCATION
        ${libs}/${ANDROID_ABI}/libavfilter.so)
add_library(avutil SHARED IMPORTED)
set_target_properties(avutil
        PROPERTIES IMPORTED_LOCATION
        ${libs}/${ANDROID_ABI}/libavutil.so)

add_library(swresample SHARED IMPORTED)
set_target_properties(swresample
        PROPERTIES IMPORTED_LOCATION
        ${libs}/${ANDROID_ABI}/libswresample.so)
add_library(swscale SHARED IMPORTED)
set_target_properties(swscale
        PROPERTIES IMPORTED_LOCATION
        ${libs}/${ANDROID_ABI}/libswscale.so)
#FILE(GLOB )
add_library(native-window
        SHARED
        src/main/cpp/native_window.cpp)

target_link_libraries(native-window
        ${avcodec}
        avfilter
        avformat
        avutil
        swresample
        swscale
        ${OPENGL_LIB}
        android
        jnigraphics
        z
        OpenSLES
        log)
