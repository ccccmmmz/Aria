package com.arialyy.simple.refactor.http

import android.util.Log
import com.amitshekhar.model.Response
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.internal.http.HttpHeaders
import okio.Buffer
import java.io.EOFException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.log

class HttpLogInterceptor : Interceptor {
    private val TAG = "网络请求"
    private val UTF8 = Charset.forName("UTF-8")
    private val DEVIDE = "\r\n"
    private var mPrintFlag = false

    private val mLogLinkList by lazy { LinkedList<String>() }

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response? {
        val request = chain.request()
        val requestBody = request.body()
        val stringBuilder = StringBuilder(DEVIDE)
        stringBuilder.append(request.method()).append("--->").append(request.url()).append("\r\n")
        if (requestBody != null) {
            if (!bodyHasUnknownEncoding(request.headers())) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                var charset = UTF8
                val contentType = requestBody.contentType()
                if (contentType != null) {
                    charset = contentType.charset(UTF8)
                }
                if (isPlaintext(buffer)) {
                    stringBuilder.append(buffer.readString(charset)).append(DEVIDE)
                }
            }
        }
        //end request

        val startTime = System.nanoTime()
        var proceed: okhttp3.Response? = null
        try {
            proceed = chain.proceed(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)
        var responseBodyContent: String? = ""
        var mediaType: MediaType? = null
        proceed?.let {
            val responseBody = it.body()
            mediaType = responseBody?.contentType()
            if (HttpHeaders.hasBody(it)) {
                if (bodyHasUnknownEncoding(it.headers()).not()) {
                    val source = responseBody!!.source()
                    source.request(9223372036854775807L)
                    val buffer = source.buffer()
//                    var gzippedLength: Long? = null
//
//                    if ("gzip".equals(it.headers().get("Content-Encoding"), ignoreCase = true)) {
//                        gzippedLength = buffer.size()
//                        var gzippedResponseBody: GzipSource? = null
//                        try {
//                            gzippedResponseBody = GzipSource(buffer.clone())
//                            buffer = Buffer()
//                            buffer.writeAll(gzippedResponseBody)
//                        } finally {
//                            gzippedResponseBody?.close()
//                        }
//                    }

                    var charset = UTF8
                    val contentType = responseBody.contentType()
                    if (contentType != null) {
                        charset = contentType.charset(UTF8)
                    }

                    if (isPlaintext(buffer)) {
                        responseBodyContent = buffer.readString(charset)
                        stringBuilder.append("cost = ").append(tookMs).append("ms-->").append(responseBodyContent)

                    } else{
                        responseBodyContent = responseBody.string()
                    }

                    enqueue(stringBuilder)
                } else {
                    responseBodyContent = responseBody?.string()
                }
            } else {
                responseBodyContent = responseBody?.string()
            }

        }

//        val content = proceed?.body().toString()
//        enqueue(content)



        return proceed?.newBuilder()?.body(okhttp3.ResponseBody.create(mediaType, responseBodyContent))?.build()
    }

    @Synchronized
    private fun enqueue(content: StringBuilder) {
        mLogLinkList.add(content.toString())
        notifyFetch()
    }

    @Synchronized
    private fun notifyFetch() {
        if (mPrintFlag) {
            return
        }
        if (mLogLinkList.isEmpty()) {
            return
        }
        mPrintFlag = true
        val poll = mLogLinkList.poll()
        logContent(poll)
    }

    private fun logContent(request: String) {
        Log.d(TAG, request)
        mPrintFlag = false
        notifyFetch()
    }

    private fun bodyHasUnknownEncoding(headers: Headers?): Boolean {
        val contentEncoding = headers?.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals(
            "identity",
            ignoreCase = true
        ) && !contentEncoding.equals("gzip", ignoreCase = true)
    }

    private fun isPlaintext(buffer: Buffer): Boolean {
        return try {
            val prefix = Buffer()
            val byteCount = if (buffer.size < 64L) buffer.size else 64L
            buffer.copyTo(prefix, 0L, byteCount)
            var i = 0
            while (i < 16 && !prefix.exhausted()) {
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
                ++i
            }
            true
        } catch (var6: EOFException) {
            false
        }
    }
}