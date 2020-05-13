package com.wen.app.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.content.FileProvider
import java.io.*
import java.util.zip.CRC32

object ApkUtils {
    const val BUFFER_SIZE = 4096

    /**
     * to get the path of apk installed
     * @param context the Context
     * @param packageName package name
     * @return path of apk
     */
    fun getSourceApkPath(context: Context, packageName: String): String? {
        if (TextUtils.isEmpty(packageName)) {
            return null
        }
        try {
            val info =
                context.packageManager.getApplicationInfo(packageName, 0)
            return info.sourceDir
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * to get current version num for check
     * @param context the Context
     * @param packageName package name
     * @return version code
     */
    @Suppress("DEPRECATION")
    fun getVersionCode(
        context: Context,
        packageName: String
    ): Long {
        try {
            return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(packageName, 0).versionCode.toLong()
            } else {
                context.packageManager.getPackageInfo(packageName, 0)
                    .longVersionCode
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return 0
    }

    /**
     * get crc32 value from a file
     * @param filepath file path
     * @return the crc32 value
     */
    @Throws(IOException::class)
    private fun getCRC32(filepath: String): Long {
        val inputStream: InputStream =
            BufferedInputStream(FileInputStream(filepath))
        val crc = CRC32()
        val bytes = ByteArray(BUFFER_SIZE)
        var cnt: Int
        while (inputStream.read(bytes).also { cnt = it } != -1) {
            crc.update(bytes, 0, cnt)
        }
        inputStream.close()
        return crc.value
    }

    /**
     * to check the file whether is full download
     * @param filePath input file path
     * @param chekcsum saved checksum
     * @return true if file is checked ok by checksum
     */
    fun checkFile(filePath: String, chekcsum: Long): Boolean {
        val file = File(filePath)
        if (file.exists()) {
            try {
                val fileChecksum = getCRC32(filePath)
                Log.d("wenpd", "chekcsum:$chekcsum fileChecksum:$fileChecksum")
                if (chekcsum == fileChecksum) {
                    return true
                }
                file.delete()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return false
    }

    /**
     * to install an apk
     * @param context Context
     * @param path file path
     */
    fun installApk(context: Context, path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileprovider",
            File(path)
        )
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(intent)
    }
}