package com.github.jacks.planetaryIdle.components

data class CreateComponent(
    var c_resourceName : String = "",
    var c_resourceCost : Float = 0f,
    var c_baseUpdateDuration : Float = -10f,
    var c_currentUpdateDuration : Float = 0f,
    var c_resourceValue : Float = 0f,
    var c_amountOwned : Int = 0
) {
}
