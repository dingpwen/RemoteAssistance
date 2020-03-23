package com.mshare.remote.assistance

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    private lateinit var mRemoteImg:ImageView
    private var mGoalToken:String? = null
    private var retry = 0
    private var needRetry = true
    private val mHandler = MainHadler(this@MainActivity)

    companion object{
        const val MSG_RETRY = 0x121
        const val MSG_INVALD = 0x122
        const val MSG_ERROR = 0x123
        const val RETRY_DURING:Long = 1000

        class MainHadler(activity:MainActivity):Handler() {
            private val mActivity = WeakReference<MainActivity>(activity)
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when(msg.what) {
                    MSG_INVALD -> mActivity.get()?.showInvalidToast()
                    MSG_RETRY -> mActivity.get()?.startWebSocket()
                    MSG_ERROR ->  mActivity.get()?.showErrorToast()
                    else -> {
                        Log.d("wenpd", "error msg")
                    }
                }

            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mRemoteImg = findViewById(R.id.remote_img)
        mGoalToken = intent.extras?.getString("token")
        if(mGoalToken == null) {
            finish()
            return
        }
        mHandler.sendEmptyMessageDelayed(MSG_RETRY, RETRY_DURING)
        findViewById<Button>(R.id.start).setOnClickListener {
            startWebSocket()
        }
    }

    private fun startWebSocket() {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = Constants.getUserToken(this)
        Constants.createWebSocket(Constants.getWSServerHost(), MainWebSocketListener(), map)
    }

    private fun setRemoteImage(jpgBytes:ByteArray) {
        runOnUiThread {
            // 原始预览数据生成的bitmap
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.size, null)
            mRemoteImg.setImageBitmap(bitmap)
        }
    }

    inner class MainWebSocketListener: WebSocketListener() {
        private var mWebSocket: WebSocket? = null
        private var mOpened = false
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            mOpened = true
            needRetry = false
            mHandler.removeMessages(MSG_RETRY)
            sendHelpMsg()
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val obj:JSONObject
            try {
                obj = JSONObject(text)
            } catch(je: JSONException) {
                je.printStackTrace()
                return
            }
            Log.d("wenpd", "message:$text")
            when(obj.getString(Constants.WS_MSG_COMMAND_SELF)) {
                Constants.WS_MSG_COMMAND_END -> {
                    mHandler.sendEmptyMessage(MSG_INVALD)
                    closeWebSocket()
                }
                Constants.WS_MSG_COMMAND_HELP -> sendInvaldMsg()
                Constants.WS_MSG_COMMAND_INVALID -> closeWebSocket()
                else -> {
                    Log.d("wenpd", "message:$text")
                }
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            val type:Byte = bytes[0]
            if(type > Constants.DATA_TYPE_AUDIO) {
                return
            }
            val data = bytes.substring(1).toByteArray()
            if(type == Constants.DATA_TYPE_IMAGE) {
                setRemoteImage(data)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            mOpened = false
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.d("wenpd", "onFailure:$t")
            if(needRetry) {
                if (++retry < Constants.WS_CONN_RETRY_TIMES) {
                    mHandler.sendEmptyMessageDelayed(MSG_RETRY, RETRY_DURING)
                } else {
                    needRetry = false
                    finish()
                }
            }
            mHandler.sendEmptyMessage(MSG_ERROR)
        }

        private fun closeWebSocket() {
            if(mOpened) {
                mWebSocket?.close(1000, "Normal")//RFC 6455
                mOpened = false
            }
        }

        private fun sendHelpMsg() {
            val obj = JSONObject()
            obj.put(Constants.WS_MSG_COMMAND_SELF, Constants.WS_MSG_COMMAND_HELP)
            obj.put(Constants.WS_MSG_TOKEN_GOAL, mGoalToken)
            mWebSocket?.send(obj.toString())
        }

        private fun sendInvaldMsg() {
            val obj = JSONObject()
            obj.put(Constants.WS_MSG_COMMAND_SELF, Constants.WS_MSG_COMMAND_HELP)
            obj.put(Constants.WS_MSG_TOKEN_GOAL, mGoalToken)
            mWebSocket?.send(obj.toString())
        }
    }

    private fun showInvalidToast() {
        Toast.makeText(this@MainActivity, "Your friend is offline." , Toast.LENGTH_LONG).show()
    }

    private fun showErrorToast() {
        Toast.makeText(this@MainActivity, "connect error." , Toast.LENGTH_LONG).show()
    }
}
