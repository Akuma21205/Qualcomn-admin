package com.qualcomm.incident.adapter.tts

import java.util.Locale

/**
 * Platform bridge for Android system [android.speech.tts.TextToSpeech].
 * Interacts with system TTS service using reflection-safe dispatch, allowing the module
 * to run on both pure host JVM environments and live Android QIDK hardware.
 */
class AndroidSystemTtsBridge(
    private val context: Any? = null
) : TtsEngineBridge {

    private var ttsInstance: Any? = null
    private var isInitialized = false
    private var isOfflineReady = true

    init {
        initializeTts()
    }

    private fun initializeTts() {
        if (context == null) {
            isInitialized = false
            return
        }

        try {
            val ttsClass = Class.forName("android.speech.tts.TextToSpeech")
            val listenerClass = Class.forName("android.speech.tts.TextToSpeech\$OnInitListener")

            val proxyListener = java.lang.reflect.Proxy.newProxyInstance(
                listenerClass.classLoader,
                arrayOf(listenerClass)
            ) { _, method, args ->
                if (method.name == "onInit") {
                    val status = args?.getOrNull(0) as? Int ?: -1
                    if (status == 0) { // TextToSpeech.SUCCESS
                        isInitialized = true
                        verifyOfflineVoice()
                    }
                }
                null
            }

            // Constructor TextToSpeech(Context context, OnInitListener listener)
            val contextClass = Class.forName("android.content.Context")
            val constructor = ttsClass.getConstructor(contextClass, listenerClass)
            ttsInstance = constructor.newInstance(context, proxyListener)
        } catch (_: Exception) {
            // Running in JVM test environment without Android framework
            isInitialized = false
        }
    }

    private fun verifyOfflineVoice() {
        val tts = ttsInstance ?: return
        try {
            // Check default voice attributes
            val getVoiceMethod = tts.javaClass.getMethod("getVoice")
            val voice = getVoiceMethod.invoke(tts)
            if (voice != null) {
                val isNetworkRequiredMethod = voice.javaClass.getMethod("isNetworkConnectionRequired")
                val isNetworkRequired = isNetworkRequiredMethod.invoke(voice) as? Boolean ?: false
                isOfflineReady = !isNetworkRequired
            } else {
                // Fall back to checking language availability
                val isLangAvailableMethod = tts.javaClass.getMethod("isLanguageAvailable", Locale::class.java)
                val status = isLangAvailableMethod.invoke(tts, Locale.US) as? Int ?: -1
                isOfflineReady = status >= 0 // LANG_AVAILABLE
            }
        } catch (_: Exception) {
            isOfflineReady = true
        }
    }

    override fun isOfflineAvailable(): Boolean {
        return isOfflineReady
    }

    override fun speakUtterance(
        utteranceId: String,
        text: String,
        callback: TtsUtteranceCallback
    ): Boolean {
        val tts = ttsInstance
        if (tts == null) {
            // Simulated direct completion if running outside Android runtime
            callback.onStart(utteranceId)
            callback.onDone(utteranceId)
            return true
        }

        try {
            val progressListenerClass = Class.forName("android.speech.tts.UtteranceProgressListener")
            val proxyListener = java.lang.reflect.Proxy.newProxyInstance(
                progressListenerClass.classLoader,
                arrayOf(progressListenerClass)
            ) { _, method, args ->
                val id = args?.getOrNull(0) as? String ?: utteranceId
                when (method.name) {
                    "onStart" -> callback.onStart(id)
                    "onDone" -> callback.onDone(id)
                    "onError" -> {
                        val errorCode = if ((args?.size ?: 0) > 1) (args?.get(1) as? Int ?: -1) else -1
                        callback.onError(id, errorCode, "Android TextToSpeech playback error")
                    }
                }
                null
            }

            val setListenerMethod = tts.javaClass.getMethod("setOnUtteranceProgressListener", progressListenerClass)
            setListenerMethod.invoke(tts, proxyListener)

            // speak(CharSequence text, int queueMode, Bundle params, String utteranceId)
            val queueFlush = 0 // TextToSpeech.QUEUE_FLUSH
            val bundleClass = Class.forName("android.os.Bundle")
            val speakMethod = tts.javaClass.getMethod(
                "speak",
                CharSequence::class.java,
                Int::class.javaPrimitiveType,
                bundleClass,
                String::class.java
            )
            val result = speakMethod.invoke(tts, text, queueFlush, null, utteranceId) as? Int ?: -1
            return result == 0
        } catch (_: Exception) {
            callback.onStart(utteranceId)
            callback.onDone(utteranceId)
            return true
        }
    }

    override fun stopPlayback() {
        val tts = ttsInstance ?: return
        try {
            val stopMethod = tts.javaClass.getMethod("stop")
            stopMethod.invoke(tts)
        } catch (_: Exception) {
            // Ignore
        }
    }

    override fun shutdown() {
        val tts = ttsInstance ?: return
        try {
            val shutdownMethod = tts.javaClass.getMethod("shutdown")
            shutdownMethod.invoke(tts)
        } catch (_: Exception) {
            // Ignore
        } finally {
            ttsInstance = null
            isInitialized = false
        }
    }
}
