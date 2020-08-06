package com.lqc.myopenglesdemo

import android.app.Application
import android.content.Context
/**
 * * FileName: MyApp
 * Author: liuqiancheng
 * Date: 2019/11/4 14:09
 * Description:
 * Version: 1.0.0
 */
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        sContext = this
    }

    companion object {
        lateinit var sContext: Context
    }
}