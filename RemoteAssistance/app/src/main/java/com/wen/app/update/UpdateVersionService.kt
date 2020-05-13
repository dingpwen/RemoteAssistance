package com.wen.app.update

import android.app.IntentService
import android.app.PendingIntent
import android.app.PendingIntent.CanceledException
import android.content.Intent
import android.util.Log
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.util.OkHttpUtil
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class UpdateVersionService:IntentService("UpdateVersionService") {
    companion object{
        private const val NOTIFY_COUNT = 20
        private var apkChecksum: Long = 0
        private var patchChecksum: Long = 0
    }

    override fun onHandleIntent(intent: Intent?) {
        if(intent == null) {
            return
        }
        val curVersion = ApkUtils.getVersionCode(applicationContext, Constants.PACKAGE)
        val map = HashMap<String, String>()
        val type = intent.getIntExtra("type", 0)
        apkChecksum = intent.getStringExtra("checksum1")!!.toLong()
        patchChecksum = intent.getStringExtra("checksum2")!!.toLong()
        map["project"] = Constants.PROJECT
        map["base"] = "$curVersion"
        val version = intent.getLongExtra("version", 0)
        map["version"] =  "$version"
        map["type"] = "$type"
        val patchFilePath = Constants.getPatchPath(this)
        val newFilePath = Constants.getNewApkPath(this)
        var noDownload = false
        if (type == 0) {
            noDownload = ApkUtils.checkFile(patchFilePath, patchChecksum)
            if(noDownload) {
                BsPatch.apply(newFilePath, ApkUtils.getSourceApkPath(this,
                    Constants.PACKAGE), patchFilePath)
            }

        }
        if(noDownload || type == 1) {
            noDownload = ApkUtils.checkFile(newFilePath, apkChecksum)
            if(noDownload) {
                sendProgress(intent, 1)
                ApkUtils.installApk(this, newFilePath)
                return
            }
            if(type == 0) {
                noDownload = true
                sendProgress(intent, -2)
            }
        }
        if(!noDownload) {
            val filePath = if(type == 0) patchFilePath else newFilePath
            downloadApk(intent, map, type, filePath)
        }
    }

    private fun downloadApk(
        intent: Intent,
        params: HashMap<String, String>,
        type: Int,
        filePath: String
    ) {
        OkHttpUtil.baseGet(Constants.getVersionDataUrl(), params, object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                sendProgress(intent, -1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body: ResponseBody = response.body!!
                val available = body.contentLength()
                Log.d("wenpd","available:$available")
                val `in` = body.byteStream()
                if (available > 0) {
                    val out =
                        FileOutputStream(File(filePath))
                    val buffer = ByteArray(ApkUtils.BUFFER_SIZE)
                    var len: Int
                    var count: Long = 0
                    var i = 0
                    val notifyCount =
                        (available / (ApkUtils.BUFFER_SIZE * NOTIFY_COUNT)).toInt()
                    while (`in`.read(buffer).also { len = it } != -1) {
                        out.write(buffer, 0, len)
                        count += len.toLong()
                        if (notifyCount == i) {
                            val progress = (count.toFloat() / available * 100).toInt()
                            Log.d("wenpd", "progress:$progress")
                            sendProgress(intent, 0, progress)
                            i = 0
                        } else {
                            ++i
                        }
                    }
                    out.flush()
                    out.close()
                    `in`.close()
                    applyPatch(intent, type)
                } else {
                    sendProgress(intent, -1)
                }
            }
        })
    }

    /**
     * After download, merge the patch and install apk
     * @param intent intent input intent[.startService]
     * @param type apk type
     */
    private fun applyPatch(intent: Intent, type: Int) {
        val context = applicationContext
        val patchFilePath = Constants.getPatchPath(context)
        val newFilePath = Constants.getNewApkPath(context)
        if (type == 0) {
            if (ApkUtils.checkFile(patchFilePath, patchChecksum)) {
                BsPatch.apply(
                    newFilePath, ApkUtils.getSourceApkPath(
                        context,
                        context.packageName
                    ), patchFilePath
                )
            } else {
                sendProgress(intent, -2)
                return
            }
        }
        if (ApkUtils.checkFile(newFilePath, apkChecksum)) {
            sendProgress(intent, 1)
            ApkUtils.installApk(context, newFilePath)
        } else {
            sendProgress(intent, -2)
        }
    }

    private fun sendProgress(intent: Intent, type: Int) {
        sendProgress(intent, type, 0)
    }

    /**
     * Notify the main activity to update the progress
     * @param intent input intent[.startService]
     * @param type apk type
     * @param progress 1~100
     */
    private fun sendProgress(intent: Intent, type: Int, progress: Int) {
        val context = applicationContext
        val pendingIntent =
            intent.getParcelableExtra<PendingIntent>("pendingIntent")
        val data = Intent()
        data.putExtra("type", type)
        if (type == 0) {
            data.putExtra("progress", progress)
        }
        if (pendingIntent != null) {
            try {
                pendingIntent.send(context, 200, data)
            } catch (e: CanceledException) {
                e.printStackTrace()
            }
        }
    }
}