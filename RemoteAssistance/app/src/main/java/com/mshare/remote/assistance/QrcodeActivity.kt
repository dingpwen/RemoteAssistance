package com.mshare.remote.assistance

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException

class QrcodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode)
        createQrImg()
    }

    private fun createQrImg() {
        try {
            val token = Constants.getUserToken(this)
            val hints = mutableMapOf<EncodeHintType, String>()
            hints[EncodeHintType.CHARACTER_SET] =  "UTF-8"
            val result = MultiFormatWriter().encode(token, BarcodeFormat.QR_CODE,Constants.QRCODE_IMG_SIZE,Constants.QRCODE_IMG_SIZE, hints)//通过字符串创建二维矩阵
            val width = result.width
            val height = result.height
            val pixels = Array(width * height){0}

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if(result.get(x, y)) MyUtils.BLACK else MyUtils.WHITE
                }
            }

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)//创建位图
            bitmap.setPixels(pixels.toIntArray(), 0, width, 0, 0, width, height)//设置位图像素集为数组
            findViewById<ImageView>(R.id.qrcode_img).setImageBitmap(bitmap)//显示二维码
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
