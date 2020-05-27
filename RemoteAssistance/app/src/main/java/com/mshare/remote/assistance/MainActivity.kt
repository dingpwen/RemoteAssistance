package com.mshare.remote.assistance

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioRecord
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.mshare.remote.assistance.util.AudioUtils
import com.mshare.remote.assistance.util.OkHttpUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.toByteString
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    private lateinit var mRemoteImg:ImageView
    private lateinit var recordBtn:Button
    private var mGoalToken:String? = null
    private var retry = 0
    private var needRetry = true
    private val mHandler = MainHadler(this@MainActivity)
    private var needCheckPermission = true

    companion object{
        private const val MSG_RETRY = 0x121
        private const val MSG_INVALD = 0x122
        private const val MSG_ERROR = 0x123
        private const val RETRY_DURING:Long = 1000

        private class MainHadler(activity:MainActivity):Handler() {
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

    @SuppressLint("ClickableViewAccessibility")
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
        /*findViewById<Button>(R.id.start).setOnClickListener {
            startWebSocket()
        }*/
        recordBtn = findViewById(R.id.start)
        recordBtn.setOnTouchListener{_, event ->
            when(event.action) {
                MotionEvent.ACTION_DOWN -> startRecord()
                MotionEvent.ACTION_UP -> stopRecord()
                else -> {}
            }
            false
        }
        recordBtn.isEnabled = false

        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onResume() {
        super.onResume()
        if(needCheckPermission) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            //do something
            needCheckPermission = false
        }
    }

    private fun setRecordEnable(enable:Boolean) {
        GlobalScope.launch(Dispatchers.Main){
            recordBtn.isEnabled = enable
        }
    }

    /************************** WebSocket ***************************/
    private lateinit var webSocketListener: MainWebSocketListener
    private fun startWebSocket() {
        val map = HashMap<String, String>()
        map[Constants.WS_MSG_TOKEN_SELF] = Constants.getUserToken(this)
        webSocketListener = MainWebSocketListener()
        OkHttpUtil.createWebSocket(Constants.getWSServerHost(), webSocketListener, map)
    }

    private fun setRemoteImage(jpgBytes:ByteArray) {
        runOnUiThread {
            // 原始预览数据生成的bitmap
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(jpgBytes, 0, jpgBytes.size, null)
            mRemoteImg.setImageBitmap(bitmap)
        }
    }

    private inner class MainWebSocketListener: WebSocketListener() {
        private var mWebSocket: WebSocket? = null
        private var mOpened = false
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            mWebSocket = webSocket
            mOpened = true
            needRetry = false
            setRecordEnable(true)
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
                    finish()
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
            setRecordEnable(false)
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

        fun sendByteMsg(byteString:ByteString, type:Byte) {
            if(mOpened) {
                val bytes = ByteArray(1 + byteString.size)
                bytes[0] = type
                System.arraycopy(byteString.toByteArray(), 0, bytes, 1, byteString.size)
                mWebSocket?.send(bytes.toByteString())
            }
        }
    }

    private fun showInvalidToast() {
        Toast.makeText(this@MainActivity, "Your friend is offline." , Toast.LENGTH_LONG).show()
    }

    private fun showErrorToast() {
        Toast.makeText(this@MainActivity, "connect error." , Toast.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecord()
    }

    /***************** Audio Record *****************************/
    private var bufferSizeInBytes = 0
    private var audioRecord: AudioRecord? = null
    @Volatile private var isRecording = false
    //private var curRecordThread:AudioRecordThread? = null
    private var curRecordJob: Job? = null

    private fun createAudioRecord() {
        bufferSizeInBytes = AudioRecord.getMinBufferSize(AudioUtils.AUDIO_SAMPLE_RATE, AudioUtils.CHANNEL_CONFIG, AudioUtils.AUDIO_FORMAT)
        audioRecord = AudioRecord(AudioUtils.AUDIO_SOURCE, AudioUtils.AUDIO_SAMPLE_RATE, AudioUtils.CHANNEL_CONFIG, AudioUtils.AUDIO_FORMAT, bufferSizeInBytes)
    }

    private fun startRecord():Int {
        if(isRecording) {
            return AudioUtils.E_STATE_RECODING
        }
        if(audioRecord == null) {
            createAudioRecord()
        }
        audioRecord?.startRecording()
        isRecording = true
        recordBtn.setText(R.string.in_record)
        curRecordJob = GlobalScope.launch { doAudioRecord() }
        return AudioUtils.SUCCESS
    }

    private fun stopRecord() {
        if(audioRecord != null) {
            isRecording = false
            curRecordJob?.cancel()
            curRecordJob = null
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordBtn.setText(R.string.start_record)
        }
    }

    private fun doAudioRecord(){
        val audioBuffer:ByteBuffer = ByteBuffer.allocateDirect(bufferSizeInBytes * 100).order(ByteOrder.LITTLE_ENDIAN)
        var readSize:Int
        try {
            while (isRecording) {
                readSize = audioRecord!!.read(audioBuffer, audioBuffer.capacity())
                if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                    Log.d("wenpd", "Could not read audio data")
                    break
                }
                webSocketListener.sendByteMsg(audioBuffer.toByteString(), Constants.DATA_TYPE_AUDIO)
                audioBuffer.clear()
            }
        } catch (e:InterruptedException) {
            e.printStackTrace()
        }
    }

    /*private inner class AudioRecordThread:Thread(){
        override fun run() {
            val audioBuffer:ByteBuffer = ByteBuffer.allocateDirect(bufferSizeInBytes * 100).order(ByteOrder.LITTLE_ENDIAN)
            var readSize:Int
            try {
                while (isRecording) {
                    readSize = audioRecord!!.read(audioBuffer, audioBuffer.capacity())
                    if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize == AudioRecord.ERROR_BAD_VALUE) {
                        Log.d("wenpd", "Could not read audio data")
                        break
                    }
                    webSocketListener.sendByteMsg(audioBuffer.toByteString(), Constants.DATA_TYPE_AUDIO)
                    audioBuffer.clear()
                }
            } catch (e:InterruptedException) {
                e.printStackTrace()
            }
        }
    }*/
}
