package com.safeher.app.core.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorderManager(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFilePath: String? = null

    fun startRecording(): String? {
        stopRecording() // Clean up any active session

        val incidentDir = File(context.filesDir, "incidents").apply {
            if (!exists()) mkdirs()
        }

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val audioFile = File(incidentDir, "INCIDENT_AUDIO_$timeStamp.m4a")
        currentFilePath = audioFile.absolutePath

        recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setOutputFile(audioFile.absolutePath)

            try {
                prepare()
                start()
            } catch (e: Exception) {
                e.printStackTrace()
                release()
                return null
            }
        }

        return currentFilePath
    }

    fun stopRecording(): String? {
        val path = currentFilePath
        recorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                // Ignore stop crash if recording was zero-length
            }
            release()
        }
        recorder = null
        currentFilePath = null
        return path
    }

    fun isRecording(): Boolean = recorder != null
}
