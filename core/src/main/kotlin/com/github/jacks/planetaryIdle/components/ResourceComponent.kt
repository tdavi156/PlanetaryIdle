package com.github.jacks.planetaryIdle.components

data class ResourceComponent(
    var resourceName : String = "",
    var resourceCost : Float = 0f,
    var baseUpdateDuration : Float = -10f,
    var currentUpdateDuration : Float = 0f,
    var resourceValue : Float = 0f,
    var amountOwned : Int = 0
) {
}
