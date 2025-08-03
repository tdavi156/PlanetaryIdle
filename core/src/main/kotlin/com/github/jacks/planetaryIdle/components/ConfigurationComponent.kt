package com.github.jacks.planetaryIdle.components

enum class ConfigurationType {
    UNDEFINED, SCORE_RESOURCE, PLANET_RESOURCE, MULTIPLIER;
}

data class ConfigurationComponent(
    var configurationName : String = "",
    var configurationType : ConfigurationType = ConfigurationType.UNDEFINED
)
