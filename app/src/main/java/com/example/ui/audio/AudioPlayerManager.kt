package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class AudioPlayerManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var mediaPlayer: MediaPlayer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var activeUtteranceId: String? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        _isPlaying.value = true
                    }
                }

                override fun onDone(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        _isPlaying.value = false
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (utteranceId == activeUtteranceId) {
                        _isPlaying.value = false
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (utteranceId == activeUtteranceId) {
                        _isPlaying.value = false
                    }
                }
            })
        }
    }

    fun play(
        text: String,
        languageCode: String,
        isSlow: Boolean = false,
        audioUrl: String? = null
    ) {
        stop()

        // If a remote audio URL is supplied, try MediaPlayer first
        if (!audioUrl.isNullOrBlank() && (audioUrl.startsWith("http://") || audioUrl.startsWith("https://"))) {
            try {
                mediaPlayer = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                            .build()
                    )
                    setDataSource(audioUrl)
                    if (isSlow && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        playbackParams = playbackParams.setSpeed(0.7f)
                    }
                    setOnPreparedListener { mp ->
                        _isPlaying.value = true
                        mp.start()
                    }
                    setOnCompletionListener {
                        _isPlaying.value = false
                        it.release()
                        mediaPlayer = null
                    }
                    setOnErrorListener { mp, _, _ ->
                        _isPlaying.value = false
                        mp.release()
                        mediaPlayer = null
                        // Fallback to TTS on media error
                        speakViaTts(text, languageCode, isSlow)
                        true
                    }
                    prepareAsync()
                }
                return
            } catch (_: Exception) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

        // Fallback to TextToSpeech engine
        speakViaTts(text, languageCode, isSlow)
    }

    private fun speakViaTts(text: String, languageCode: String, isSlow: Boolean) {
        if (!isTtsReady || tts == null || text.isBlank()) {
            _isPlaying.value = false
            return
        }

        val locale = getLocaleForCode(languageCode)
        tts?.language = locale
        tts?.setSpeechRate(if (isSlow) 0.65f else 0.95f)

        val utteranceId = UUID.randomUUID().toString()
        activeUtteranceId = utteranceId
        _isPlaying.value = true

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    fun stop() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
        } catch (_: Exception) {}

        try {
            tts?.stop()
        } catch (_: Exception) {}

        _isPlaying.value = false
        activeUtteranceId = null
    }

    fun release() {
        stop()
        try {
            tts?.shutdown()
            tts = null
            isTtsReady = false
        } catch (_: Exception) {}
    }

    private fun getLocaleForCode(code: String): Locale {
        return when (code.lowercase().trim()) {
            "en" -> Locale.ENGLISH
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "pt" -> Locale("pt", "PT")
            "ja" -> Locale.JAPANESE
            "ko" -> Locale.KOREAN
            "tr" -> Locale("tr", "TR")
            "ar" -> Locale("ar")
            "ru" -> Locale("ru", "RU")
            "zh" -> Locale.CHINESE
            else -> Locale(code)
        }
    }
}

@Composable
fun rememberAudioPlayerManager(): AudioPlayerManager {
    val context = LocalContext.current
    val manager = remember { AudioPlayerManager(context) }
    DisposableEffect(manager) {
        onDispose {
            manager.release()
        }
    }
    return manager
}
