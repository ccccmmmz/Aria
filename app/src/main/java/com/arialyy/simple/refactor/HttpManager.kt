package com.arialyy.simple.refactor

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class HttpManager {

    private val mHttpClient by lazy {

    }


    private fun createHttpClient(){
        OkHttpClient.Builder()
            .callTimeout(10000L, TimeUnit.MILLISECONDS)
            .build()
    }

}