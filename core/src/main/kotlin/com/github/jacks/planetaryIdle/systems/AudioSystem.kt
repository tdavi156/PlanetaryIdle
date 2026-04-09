package com.github.jacks.planetaryIdle.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.jacks.planetaryIdle.events.AchievementCompletedEvent
import com.github.jacks.planetaryIdle.events.BarnUnlockedEvent
import com.github.jacks.planetaryIdle.events.BuyBarnUpgradeEvent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.KitchenUnlockedEvent
import com.github.quillraven.fleks.IntervalSystem
import ktx.assets.disposeSafely
import ktx.log.logger

class AudioSystem : EventListener, IntervalSystem() {

    private val settingsSystem: SettingsSystem by lazy { world.system<SettingsSystem>() }

    private val soundCache    = mutableMapOf<String, Sound>()
    private val soundRequests = mutableMapOf<String, Sound>()
    private var music: Music? = null
    private var musicStarted  = false

    override fun onTick() {
        // Start background music on first tick once LibGDX audio is ready
        if (!musicStarted && BACKGROUND_MUSIC.isNotBlank()) {
            musicStarted = true
            val musicFile = Gdx.files.internal(BACKGROUND_MUSIC)
            if (musicFile.exists()) {
                music = Gdx.audio.newMusic(musicFile).apply {
                    isLooping = true
                    volume = musicVolume()
                    play()
                }
            } else {
                log.debug { "Background music file not found: $BACKGROUND_MUSIC" }
            }
        }

        if (soundRequests.isEmpty()) return

        val vol = effectsVolume()
        soundRequests.values.forEach { it.play(vol) }
        soundRequests.clear()
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is BuyResourceEvent      -> queueSound(SFX_BUY_CROP)
            is BuyBarnUpgradeEvent   -> queueSound(SFX_BUY_UPGRADE)
            is BarnUnlockedEvent     -> queueSound(SFX_BARN_UNLOCKED)
            is AchievementCompletedEvent -> queueSound(SFX_ACHIEVEMENT)
            is KitchenUnlockedEvent  -> queueSound(SFX_KITCHEN_UNLOCKED)
        }
        return false
    }

    private fun queueSound(path: String) {
        if (path.isBlank()) return
        if (path in soundRequests) return

        val soundFile = Gdx.files.internal(path)
        if (!soundFile.exists()) {
            log.debug { "Sound file not found: $path" }
            return
        }
        val sound = soundCache.getOrPut(path) { Gdx.audio.newSound(soundFile) }
        soundRequests[path] = sound
    }

    private fun masterVolume()  = settingsSystem.settings.masterVolume  / 100f
    private fun musicVolume()   = (settingsSystem.settings.musicVolume   / 100f) * masterVolume()
    private fun effectsVolume() = (settingsSystem.settings.effectsVolume / 100f) * masterVolume()

    /** Called when settings are saved so music volume updates immediately. */
    fun applyMusicVolume() {
        music?.volume = musicVolume()
    }

    override fun onDispose() {
        music.disposeSafely()
        soundCache.values.forEach { it.disposeSafely() }
    }

    companion object {
        private val log = logger<AudioSystem>()

        // ── Asset paths ───────────────────────────────────────────────────────
        // Set to a non-blank path once the audio files are ready.
        const val BACKGROUND_MUSIC    = "" // e.g. "audio/background_music.wav"
        const val SFX_BUY_CROP        = "" // e.g. "audio/buy_crop.wav"
        const val SFX_BUY_UPGRADE     = "" // e.g. "audio/buy_upgrade.wav"
        const val SFX_BARN_UNLOCKED   = "" // e.g. "audio/barn_unlocked.wav"
        const val SFX_ACHIEVEMENT     = "" // e.g. "audio/achievement.wav"
        const val SFX_KITCHEN_UNLOCKED = "" // e.g. "audio/kitchen_unlocked.wav"
    }
}
