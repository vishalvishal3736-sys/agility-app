package com.shuttlepulse.agility.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shuttlepulse.agility.data.local.seed.AuthenticBadmintonDrillsSeed
import com.shuttlepulse.agility.domain.model.DominantHand
import com.shuttlepulse.agility.domain.model.UserPreferences
import com.shuttlepulse.agility.domain.model.WorkoutCircuit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.shuttlePulseDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "shuttlepulse_user_prefs"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val context: Context
) {
    private val json = Json { ignoreUnknownKeys = true }

    private object Keys {
        val DOMINANT_HAND = stringPreferencesKey("dominant_hand")
        val AUDIO_DUCKING = booleanPreferencesKey("audio_ducking_enabled")
        val METRONOME_TICK = booleanPreferencesKey("metronome_tick_enabled")
        val VOICE_CALLOUTS = booleanPreferencesKey("voice_callouts_enabled")
        val BPM_CALIBRATION_SCALE = floatPreferencesKey("bpm_calibration_scale")
        val ACTIVE_CIRCUIT_JSON = stringPreferencesKey("active_circuit_json")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.shuttlePulseDataStore.data.map { prefs ->
        val handStr = prefs[Keys.DOMINANT_HAND] ?: DominantHand.RIGHT.name
        val hand = runCatching { DominantHand.valueOf(handStr) }.getOrDefault(DominantHand.RIGHT)
        UserPreferences(
            dominantHand = hand,
            audioDuckingEnabled = prefs[Keys.AUDIO_DUCKING] ?: true,
            metronomeTickEnabled = prefs[Keys.METRONOME_TICK] ?: true,
            voiceCalloutsEnabled = prefs[Keys.VOICE_CALLOUTS] ?: true,
            bpmCalibrationScale = prefs[Keys.BPM_CALIBRATION_SCALE] ?: 1.0f
        )
    }

    val activeCircuitFlow: Flow<WorkoutCircuit> = context.shuttlePulseDataStore.data.map { prefs ->
        val raw = prefs[Keys.ACTIVE_CIRCUIT_JSON]
        if (raw.isNullOrBlank()) {
            AuthenticBadmintonDrillsSeed.preBuiltCircuits.first()
        } else {
            runCatching { json.decodeFromString<WorkoutCircuit>(raw) }
                .getOrElse { AuthenticBadmintonDrillsSeed.preBuiltCircuits.first() }
        }
    }

    suspend fun setDominantHand(hand: DominantHand) {
        context.shuttlePulseDataStore.edit { prefs ->
            prefs[Keys.DOMINANT_HAND] = hand.name
        }
    }

    suspend fun updateAudioPreferences(
        duckingEnabled: Boolean,
        metronomeEnabled: Boolean,
        voiceEnabled: Boolean,
        bpmScale: Float
    ) {
        context.shuttlePulseDataStore.edit { prefs ->
            prefs[Keys.AUDIO_DUCKING] = duckingEnabled
            prefs[Keys.METRONOME_TICK] = metronomeEnabled
            prefs[Keys.VOICE_CALLOUTS] = voiceEnabled
            prefs[Keys.BPM_CALIBRATION_SCALE] = bpmScale.coerceIn(0.75f, 1.25f)
        }
    }

    suspend fun saveActiveCircuit(circuit: WorkoutCircuit) {
        context.shuttlePulseDataStore.edit { prefs ->
            prefs[Keys.ACTIVE_CIRCUIT_JSON] = json.encodeToString(circuit)
        }
    }
}
