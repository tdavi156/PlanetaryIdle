package com.github.jacks.planetaryIdle.components

enum class ConfigurationType {
    UNDEFINED, POPULATION, PLANET_RESOURCE;
}

data class ConfigurationComponent(
    var configurationName : String = "",
    var configurationType : ConfigurationType = ConfigurationType.UNDEFINED
)
