package com.github.jacks.planetaryIdle.configurations

data class Settings(
    var masterVolume: Int = 100,
    var musicVolume: Int = 100,
    var effectsVolume: Int = 100,
    var useLetterNotation: Boolean = true,
) {
    companion object {
        const val KEY_MASTER_VOLUME      = "settings_master_volume"
        const val KEY_MUSIC_VOLUME       = "settings_music_volume"
        const val KEY_EFFECTS_VOLUME     = "settings_effects_volume"
        const val KEY_NUMBER_NOTATION    = "settings_number_notation"
    }
}
