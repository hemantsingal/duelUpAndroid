package com.duelup.app.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.duelup.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val sounds = mutableMapOf<SoundEffect, Int>()
    var enabled = true
        private set

    fun setEnabled(value: Boolean) {
        enabled = value
    }

    init {
        sounds[SoundEffect.TAP] = soundPool.load(context, R.raw.sfx_tap, 1)
        sounds[SoundEffect.CORRECT] = soundPool.load(context, R.raw.sfx_correct, 1)
        sounds[SoundEffect.WRONG] = soundPool.load(context, R.raw.sfx_wrong, 1)
        sounds[SoundEffect.VICTORY] = soundPool.load(context, R.raw.sfx_victory, 1)
        sounds[SoundEffect.DEFEAT] = soundPool.load(context, R.raw.sfx_defeat, 1)
        sounds[SoundEffect.MATCH_FOUND] = soundPool.load(context, R.raw.sfx_match_found, 1)
        sounds[SoundEffect.QUESTION_IN] = soundPool.load(context, R.raw.sfx_question_in, 1)
        sounds[SoundEffect.SCORE_UP] = soundPool.load(context, R.raw.sfx_score_up, 1)
        sounds[SoundEffect.STREAK] = soundPool.load(context, R.raw.sfx_streak, 1)
        sounds[SoundEffect.COUNTDOWN] = soundPool.load(context, R.raw.sfx_countdown, 1)
        sounds[SoundEffect.TIMER_TICK] = soundPool.load(context, R.raw.sfx_timer_tick, 1)
        sounds[SoundEffect.TIMER_URGENT] = soundPool.load(context, R.raw.sfx_timer_urgent, 1)
    }

    fun play(effect: SoundEffect) {
        if (!enabled) return
        sounds[effect]?.let { soundId ->
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        soundPool.release()
    }
}

enum class SoundEffect {
    TAP,
    CORRECT,
    WRONG,
    VICTORY,
    DEFEAT,
    MATCH_FOUND,
    QUESTION_IN,
    SCORE_UP,
    STREAK,
    COUNTDOWN,
    TIMER_TICK,
    TIMER_URGENT
}
