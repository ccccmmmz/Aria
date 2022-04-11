package com.arialyy.simple.refactor

import kotlinx.coroutines.*

object CoroutinueKit {
    private val mDefaultScope by lazy { MainScope() }

    fun executeIoTask(consume: ()-> Unit) {
        mDefaultScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                consume()
            }
        }
    }


    fun executeDefaultTask(consume: ()-> Unit){
        mDefaultScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default){
                consume()
            }
        }
    }
}