package com.shuttlepulse.agility.presentation.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.SoundPool
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.sin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Hybrid Zero-Latency Audio & Cadence Engine:
 * 1. Uses [AudioAttributes.USAGE_GAME] on [SoundPool] and low-latency [AudioTrack] static buffers
 *    so cadence ticks (880Hz), accent beats (1320Hz), countdown pips (660Hz), and burst whistles
 *    (1760Hz + 2640Hz dual-harmonic) route through Android's Fast Mixer (<10ms latency).
 * 2. Dedicated single-thread audio dispatcher ("ShuttlePulseAudioThread") for AudioTrack playback.
 * 3. [TextToSpeech] configured at 1.35f speech rate and 1.05f pitch for snappy, punchy athletic cues.
 * 4. Single-owner [AudioManager] transient music ducking ([AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK]).
 */
@Singleton
class CadenceSoundEngine @Inject constructor(
    private val context: Context
) : TextToSpeech.OnInitListener {

    private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val audioExecutor = Executors.newSingleThreadExecutor { Thread(it, "ShuttlePulseAudioThread") }
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val soundPool: SoundPool
    private val loadedSoundIds = ConcurrentHashMap.newKeySet<Int>()

    private var tts: TextToSpeech? = null
    private var toneGenerator: ToneGenerator? = null

    private var staticTickTrack: AudioTrack? = null
    private var staticAccentTrack: AudioTrack? = null
    private var staticWhistleTrack: AudioTrack? = null

    private var metronomeTickId: Int = 0
    private var comboBounceId: Int = 0
    private var countdownPipId: Int = 0
    private var burstWhistleId: Int = 0
    private var switchSidesId: Int = 0

    var isSoundPoolReady: Boolean = false
        private set

    var isAudioTrackReady: Boolean = false
        private set

    var isTtsReady: Boolean = false
        private set

    var audioDuckingEnabled: Boolean = true
    var metronomeTickEnabled: Boolean = true
    var voiceCalloutsEnabled: Boolean = true

    private var activeFocusRequest: AudioFocusRequest? = null

    init {
        // USAGE_GAME + CONTENT_TYPE_SONIFICATION routes to STREAM_MUSIC on Android's Fast Mixer path
        // so ticks have sub-10ms latency and remain audible even if the phone ringer is on silent.
        val fastMixerAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    setAllowedCapturePolicy(AudioAttributes.ALLOW_CAPTURE_BY_ALL)
                }
            }
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(fastMixerAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId != 0) {
                loadedSoundIds.add(sampleId)
                isSoundPoolReady = true
            }
        }

        initProgrammaticAudioTracks()
        synthesizeAndLoadSoundEffects()

        runCatching {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        }

        runCatching {
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            runCatching {
                val speechAttrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(speechAttrs)
                tts?.language = Locale.US
                tts?.setSpeechRate(1.35f)
                tts?.setPitch(1.05f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        // Focus is already requested prior to speakCue() if audioDuckingEnabled
                    }

                    override fun onDone(utteranceId: String?) {
                        abandonDuckFocus()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        abandonDuckFocus()
                    }
                })
                isTtsReady = true
            }
        }
    }

    private fun initProgrammaticAudioTracks() {
        runCatching {
            // 880Hz crisp 32ms standard cadence tick PCM buffer
            val tickPcm = generatePcmBuffer(frequencyHz = 880.0, durationMs = 32, harmonicFreqHz = 1760.0, harmonic = 0.15)
            staticTickTrack = createStaticAudioTrack(tickPcm)

            // 1320Hz 38ms accent beat PCM buffer
            val accentPcm = generatePcmBuffer(frequencyHz = 1320.0, durationMs = 38, harmonicFreqHz = 2640.0, harmonic = 0.20)
            staticAccentTrack = createStaticAudioTrack(accentPcm)

            // Dual-harmonic 1760Hz + 2640Hz sharp 140ms burst whistle PCM buffer
            val whistlePcm = generatePcmBuffer(frequencyHz = 1760.0, durationMs = 140, harmonicFreqHz = 2640.0, harmonic = 0.40)
            staticWhistleTrack = createStaticAudioTrack(whistlePcm)

            isAudioTrackReady = (staticTickTrack != null && staticWhistleTrack != null)
        }
    }

    private fun createStaticAudioTrack(pcmBytes: ByteArray): AudioTrack? {
        return runCatching {
            val sampleRate = 44100
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()

            val builder = AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(format)
                .setBufferSizeInBytes(pcmBytes.size.coerceAtLeast(1024))
                .setTransferMode(AudioTrack.MODE_STATIC)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
            }

            builder.build().apply {
                write(pcmBytes, 0, pcmBytes.size)
            }
        }.getOrNull()
    }

    private fun isSampleLoaded(sampleId: Int): Boolean {
        return sampleId != 0 && loadedSoundIds.contains(sampleId)
    }

    /**
     * Plays a zero-latency cadence tick (800Hz blip, or 960Hz accent on beat 1) synchronized
     * with the visual stick-figure and foot-stance animation.
     */
    fun playCadenceTick(isAccentBeat: Boolean = false) {
        if (!metronomeTickEnabled) return

        val targetId = if (isAccentBeat && isSampleLoaded(comboBounceId)) {
            comboBounceId
        } else {
            metronomeTickId
        }

        if (isSampleLoaded(targetId)) {
            val rate = if (isAccentBeat) 1.08f else 1.0f
            val vol = if (isAccentBeat) 0.86f else 0.58f
            soundPool.play(targetId, vol, vol, 1, 0, rate)
            return
        }

        val track = if (isAccentBeat) staticAccentTrack ?: staticTickTrack else staticTickTrack
        if (track != null) {
            audioExecutor.execute {
                runCatching {
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                    }
                    track.reloadStaticData()
                    track.play()
                }
            }
            return
        }

        runCatching {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 24)
        }
    }

    /**
     * Plays a distinct 3-2-1 countdown pip (660Hz tone) during the final 3 seconds of Prep or Rest
     * (never suppressed if metronome ticks are toggled off).
     */
    fun playCountdownPip(isFinalSecond: Boolean = false) {
        if (isSampleLoaded(countdownPipId)) {
            val rate = if (isFinalSecond) 1.25f else 1.0f
            val vol = if (isFinalSecond) 0.90f else 0.72f
            soundPool.play(countdownPipId, vol, vol, 2, 0, rate)
            return
        }
        playCadenceTick(isAccentBeat = isFinalSecond)
    }

    /**
     * Plays a sharp dual-harmonic 1760Hz + 2640Hz burst whistle + haptic pulse + optional concise spoken TTS trigger.
     */
    fun playExplosiveBurstWhistle(spokenTrigger: String? = null) {
        if (isSampleLoaded(burstWhistleId)) {
            soundPool.play(burstWhistleId, 1.0f, 1.0f, 3, 0, 1.02f)
        } else {
            val track = staticWhistleTrack
            if (track != null) {
                audioExecutor.execute {
                    runCatching {
                        if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                            track.stop()
                        }
                        track.reloadStaticData()
                        track.play()
                    }
                }
            } else {
                runCatching {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 110)
                }
            }
        }
        vibratePulse(65L)
        if (!spokenTrigger.isNullOrBlank()) {
            speakCue(spokenTrigger)
        }
    }

    fun playUnilateralSwitchCue(spokenSwitch: String) {
        if (isSampleLoaded(switchSidesId)) {
            soundPool.play(switchSidesId, 0.96f, 0.96f, 3, 0, 1.0f)
        } else {
            playExplosiveBurstWhistle(null)
        }
        vibratePulse(110L)
        speakCue(spokenSwitch)
    }

    /**
     * Fires a 240 BPM diagnostic tapping burst (4 ticks at exact 250ms spacing + whistle + TTS check)
     * for the Built-In Diagnostics Screen self-test.
     */
    fun runDiagnostic240BpmBurstTest() {
        engineScope.launch {
            repeat(4) { idx ->
                playCadenceTick(isAccentBeat = (idx == 0))
                delay(250L)
            }
            playExplosiveBurstWhistle("Split step, lunge!")
        }
    }

    fun testCadenceTick() = playCadenceTick(isAccentBeat = false)
    fun testAccentBeat() = playCadenceTick(isAccentBeat = true)
    fun testBurstWhistle() = playExplosiveBurstWhistle("Explode! Racket lunge!")
    fun testCountdownPip() = playCountdownPip(isFinalSecond = true)

    fun speakCue(text: String) {
        if (!voiceCalloutsEnabled || !isTtsReady || text.isBlank()) return
        runCatching {
            if (audioDuckingEnabled) {
                requestTransientMayDuckFocus()
            }
            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "shuttlepulse_${System.nanoTime()}"
            )
        }
    }

    fun stopSpeaking() {
        runCatching {
            if (tts?.isSpeaking == true) {
                tts?.stop()
                abandonDuckFocus()
            }
        }
    }

    fun requestTransientMayDuckFocus(): Boolean {
        val am = audioManager ?: return false
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = activeFocusRequest ?: AudioFocusRequest.Builder(
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                )
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setAcceptsDelayedFocusGain(false)
                    .setWillPauseWhenDucked(false)
                    .build()
                    .also { activeFocusRequest = it }

                am.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
                ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
            }
        }.getOrDefault(false)
    }

    fun abandonDuckFocus() {
        val am = audioManager ?: return
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                activeFocusRequest?.let { am.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        }
    }

    private fun vibratePulse(durationMs: Long) {
        runCatching {
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        }
    }

    private fun generatePcmBuffer(
        frequencyHz: Double,
        durationMs: Int,
        harmonicFreqHz: Double = frequencyHz * 2.0,
        harmonic: Double = 0.20
    ): ByteArray {
        val sampleRate = 44100
        val numSamples = (sampleRate * durationMs) / 1000
        val pcm = ByteArray(numSamples * 2)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate.toDouble()
            // Fast 2ms attack so the acoustic transient aligns immediately with the visual beat
            val attackSamples = (sampleRate * 0.002).coerceAtLeast(1.0)
            val attack = (i.toDouble() / attackSamples).coerceAtMost(1.0)
            val decay = (1.0 - (i.toDouble() / numSamples.toDouble())).coerceIn(0.0, 1.0)
            val env = attack * decay * decay
            val wave = (sin(2.0 * PI * frequencyHz * t) + sin(2.0 * PI * harmonicFreqHz * t) * harmonic) /
                (1.0 + harmonic)
            val sample = (wave * env * Short.MAX_VALUE * 0.90).toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            pcm[i * 2] = (sample.toInt() and 0xFF).toByte()
            pcm[i * 2 + 1] = ((sample.toInt() shr 8) and 0xFF).toByte()
        }
        return pcm
    }

    private fun synthesizeAndLoadSoundEffects() {
        runCatching {
            // Standard tick 880Hz (32ms), Accent 1320Hz (38ms), Countdown Pip 660Hz (90ms), Whistle 1760+2640Hz (140ms)
            val tickFile = writeSynthesizedWav("sp_v3_tick_880.wav", 880.0, 32, 1760.0, 0.15)
            val bounceFile = writeSynthesizedWav("sp_v3_accent_1320.wav", 1320.0, 38, 2640.0, 0.20)
            val pipFile = writeSynthesizedWav("sp_v3_pip_660.wav", 660.0, 90, 1320.0, 0.15)
            val burstFile = writeSynthesizedWav("sp_v3_whistle_1760_2640.wav", 1760.0, 140, 2640.0, 0.40)
            val switchFile = writeSynthesizedWav("sp_v3_switch_1100.wav", 1100.0, 150, 1650.0, 0.25)

            metronomeTickId = soundPool.load(tickFile.absolutePath, 1)
            comboBounceId = soundPool.load(bounceFile.absolutePath, 1)
            countdownPipId = soundPool.load(pipFile.absolutePath, 1)
            burstWhistleId = soundPool.load(burstFile.absolutePath, 1)
            switchSidesId = soundPool.load(switchFile.absolutePath, 1)
        }
    }

    private fun writeSynthesizedWav(
        fileName: String,
        frequencyHz: Double,
        durationMs: Int,
        harmonicFreqHz: Double = frequencyHz * 2.0,
        harmonic: Double = 0.20
    ): File {
        val file = File(context.cacheDir, fileName)
        if (file.exists() && file.length() > 100L) return file

        val sampleRate = 44100
        val pcm = generatePcmBuffer(frequencyHz, durationMs, harmonicFreqHz, harmonic)
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray(Charsets.US_ASCII))
            putInt(pcm.size + 36)
            put("WAVE".toByteArray(Charsets.US_ASCII))
            put("fmt ".toByteArray(Charsets.US_ASCII))
            putInt(16)
            putShort(1)
            putShort(1)
            putInt(sampleRate)
            putInt(sampleRate * 2)
            putShort(2)
            putShort(16)
            put("data".toByteArray(Charsets.US_ASCII))
            putInt(pcm.size)
        }.array()

        FileOutputStream(file).use { out ->
            out.write(header)
            out.write(pcm)
        }
        return file
    }

    fun release() {
        abandonDuckFocus()
        runCatching { audioExecutor.shutdown() }
        runCatching { staticTickTrack?.release() }
        runCatching { staticAccentTrack?.release() }
        runCatching { staticWhistleTrack?.release() }
        runCatching { toneGenerator?.release() }
        runCatching { soundPool.release() }
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
    }
}
