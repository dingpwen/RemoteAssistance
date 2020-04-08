package com.mshare.remote.assistance.util

import android.media.AudioFormat
import android.media.MediaRecorder

class AudioUtils private constructor(){
    companion object{
        const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC
        const val AUDIO_SAMPLE_RATE =8000
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val  AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        val instance = HOLDER.INSTANCE
        /*val instance:AudioUtils by lazy (mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            AudioUtils()
        }*/

        const val SUCCESS = 1000
        const val E_NOSDCARD = 1001
        const val E_STATE_RECODING = 1002
        const val E_UNKOWN = 1003
    }

    private object HOLDER{
        val INSTANCE = AudioUtils()
    }

}