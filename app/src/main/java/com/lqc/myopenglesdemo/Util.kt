package com.lqc.myopenglesdemo

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * FileName: Util
 * Author: liuqiancheng
 * Date: 2019/11/13 10:34
 * Description:
 * Version: 1.0.0
 */
object Util {
    @JvmStatic
    fun main(args: Array<String>){
        test(args)
    }
    fun test(args: Array<String>) = runBlocking {

        val job = launch {

            repeat(10) { i ->

                println("挂起中------$i")
                //每次循环，暂停1秒
                delay(1000L)

            }
        }

        val job2 = async {
            //挂起5秒
            delay(5000L)
            //使用注解标注此处返回的是 async 的闭包
            return@async "我是 async 返回的内容"
        }

        /**
         * await 是一个阻塞式方法
         * 会将主线程停在这里
         * 当 job2 挂起5秒结束，返回内容
         * await 接受到内容，主线程才继续向下执行-->开始等待
         */

        println("主线程开始等待-----")
        println("主线程等待结束-----取消launch开启的协程")


        println("job2 返回的内容：${job2.await()}")

        job.cancel()//协程的启动和停止都是代码可控的

        println("主线程执行完毕，即将推出-----")

    }
}