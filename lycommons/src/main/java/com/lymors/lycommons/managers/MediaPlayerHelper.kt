package com.lymors.lycommons.managers

import android.content.Context
import android.media.MediaPlayer
import java.io.IOException
class MediaPlayerHelper () {


    private val mediaPlayer: MediaPlayer = MediaPlayer()
    private var isPlaying = false
    var totalDuration=0
    var currentPosition=0
        get() {
            return mediaPlayer.currentPosition
        }
    companion object {
        @Volatile
        private var instance: MediaPlayerHelper? = null
        fun getInstance(): MediaPlayerHelper {
            return instance ?: synchronized(this) {
                instance ?: MediaPlayerHelper().also { instance = it }
            }
        }
    }

    fun getDuration(uri:String):Int{
       return try {
            mediaPlayer.run {
                setDataSource(uri)
                prepareAsync()
                mediaPlayer.duration
            }
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
    }

    fun playAudio(uri:String):Boolean {
        if (!isPlaying){
            try {
                mediaPlayer.apply {
                    reset()
                    setDataSource(uri)
                    prepare()
                    start()
                    totalDuration=mediaPlayer.duration
                }
                isPlaying=true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }else{
            isPlaying=false
//            stopPlaying()
//            playAudio(uri)
        }
        return isPlaying
    }

    private fun pauseAudio() {
        mediaPlayer.pause()
    }

//    private fun stopPlaying() {
//        mediaPlayer.stop()
//        mediaPlayer.release()
//    }


    fun getCurrentPosition(currentPosition:(Int)->Unit):Int{
        mediaPlayer.setOnPreparedListener {
            this.currentPosition=mediaPlayer.currentPosition
            currentPosition.invoke(mediaPlayer.currentPosition)
        }
        return this.currentPosition
    }



    fun seekToo(value:Int){
        mediaPlayer.seekTo(value)
    }

    fun onComplete(callback:(MediaPlayer) -> Unit){
        mediaPlayer.setOnCompletionListener {
            callback(it)
        }
    }


}


