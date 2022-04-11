package com.arialyy.simple.refactor

import com.arialyy.aria.util.BufferedRandomAccessFile
import java.io.RandomAccessFile

class RandomAccessFileKit {
    //文件路径
    private var mFilePath = ""

    private val mFile by lazy { BufferedRandomAccessFile(mFilePath, "rw") }

    fun test(){

    }
}