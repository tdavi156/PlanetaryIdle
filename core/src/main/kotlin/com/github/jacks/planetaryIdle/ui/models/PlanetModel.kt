package com.github.jacks.planetaryIdle.ui.models

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.scenes.scene2d.Event
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.jacks.planetaryIdle.components.ResourceComponent
import com.github.jacks.planetaryIdle.events.BuyResourceEvent
import com.github.jacks.planetaryIdle.events.GameCompletedEvent
import com.github.jacks.planetaryIdle.events.ResetGameEvent
import com.github.jacks.planetaryIdle.events.ResourceUpdateEvent
import com.github.jacks.planetaryIdle.events.SaveGameEvent
import com.github.jacks.planetaryIdle.events.UpdateBuyAmountEvent
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.World
import ktx.app.gdxError
import ktx.log.logger
import ktx.preferences.flush
import ktx.preferences.get
import ktx.preferences.set
import java.math.*

class PlanetModel(
    world : World,
    stage : Stage
) : PropertyChangeSource(), EventListener {

    private val preferences : Preferences by lazy { Gdx.app.getPreferences("planetaryIdlePrefs") }
    private val resourceComponents : ComponentMapper<ResourceComponent> = world.mapper()
    private val resourceEntities = world.family(allOf = arrayOf(ResourceComponent::class))

    var totalPopulationAmount by propertyNotify(BigDecimal(preferences["totalPopulation", "10"]))
    var availablePopulationAmount by propertyNotify(BigDecimal(preferences["availablePopulation", "10"]))
    var populationGainPerSecond by propertyNotify(BigDecimal(preferences["populationGainRate", "0"]))
    var buyAmount by propertyNotify(preferences["buyAmount", 1f])

    var wheatAmount by propertyNotify(BigInteger(preferences["wheat_amount", "0"]))
    var cornAmount by propertyNotify(BigInteger(preferences["corn_amount", "0"]))
    var lettuceAmount by propertyNotify(BigInteger(preferences["lettuce_amount", "0"]))
    var carrotsAmount by propertyNotify(BigInteger(preferences["carrots_amount", "0"]))
    var tomatoesAmount by propertyNotify(BigInteger(preferences["tomatoes_amount", "0"]))
    var broccoliAmount by propertyNotify(BigInteger(preferences["broccoli_amount", "0"]))
    var onionsAmount by propertyNotify(BigInteger(preferences["onions_amount", "0"]))
    var potatoesAmount by propertyNotify(BigInteger(preferences["potatoes_amount", "0"]))

    var wheatMultiplier by propertyNotify(BigDecimal(preferences["wheat_multiplier", "1"]))
    var cornMultiplier by propertyNotify(BigDecimal(preferences["corn_multiplier", "1"]))
    var lettuceMultiplier by propertyNotify(BigDecimal(preferences["lettuce_multiplier", "1"]))
    var carrotsMultiplier by propertyNotify(BigDecimal(preferences["carrots_multiplier", "1"]))
    var tomatoesMultiplier by propertyNotify(BigDecimal(preferences["tomatoes_multiplier", "1"]))
    var broccoliMultiplier by propertyNotify(BigDecimal(preferences["broccoli_multiplier", "1"]))
    var onionsMultiplier by propertyNotify(BigDecimal(preferences["onions_multiplier", "1"]))
    var potatoesMultiplier by propertyNotify(BigDecimal(preferences["potatoes_multiplier", "1"]))

    var wheatCost by propertyNotify(BigDecimal(preferences["wheat_cost", "10"]))
    var cornCost by propertyNotify(BigDecimal(preferences["corn_cost", "100"]))
    var lettuceCost by propertyNotify(BigDecimal(preferences["lettuce_cost", "1000"]))
    var carrotsCost by propertyNotify(BigDecimal(preferences["carrots_cost", "10000"]))
    var tomatoesCost by propertyNotify(BigDecimal(preferences["tomatoes_cost", "100000"]))
    var broccoliCost by propertyNotify(BigDecimal(preferences["broccoli_cost", "1000000"]))
    var onionsCost by propertyNotify(BigDecimal(preferences["onions_cost", "10000000"]))
    var potatoesCost by propertyNotify(BigDecimal(preferences["potatoes_cost", "100000000"]))

    var gameCompleted by propertyNotify(false)

    init {
        stage.addListener(this)
    }

    override fun handle(event: Event?): Boolean {
        when (event) {
            is BuyResourceEvent -> {
                val entity = getEntityByName(event.resourceType) ?: gdxError("No Entity with foodType: ${event.resourceType}")
                val rscComp = resourceComponents[entity]
                updatePopulation(rscComp)
                updateModelAmount(rscComp)
                updateResourceComponent(rscComp)
                updateModel(rscComp)
                updateModelPopulationRate()
            }
            is ResourceUpdateEvent -> {
                val amount = event.amount
                availablePopulationAmount += amount
                totalPopulationAmount = (totalPopulationAmount + amount).coerceAtMost(BigDecimal("1E308"))
                preferences.flush {
                    this["availablePopulation"] = availablePopulationAmount.toString()
                    this["totalPopulation"] = totalPopulationAmount.toString()
                }
            }
            is UpdateBuyAmountEvent -> {
                buyAmount = event.amount
                preferences.flush { this["buyAmount"] = buyAmount }
                updateModel()
            }
            is SaveGameEvent -> {

            }
            is ResetGameEvent -> {
                resetGameValues()
            }
            is GameCompletedEvent -> {
                gameCompleted = true
            }
            else -> return false
        }
        return true
    }

    private fun getEntityByName(name : String) : Entity? {
        resourceEntities.forEach { entity ->
            if (resourceComponents[entity].name == name) {
                return entity
            }
        }
        return null
    }

    /**
     * Update the ResourceComponent value for:
     * amount of a given ResourceComponent
     */
    private fun updateResourceComponent(rscComp : ResourceComponent) {
        rscComp.amountOwned += buyAmount.toBigDecimal().toBigInteger()
    }

    /**
     * Update the Model value for:
     * availablePopulation given the purchased ResourceComponent
     */
    private fun updatePopulation(rscComp : ResourceComponent) {
        availablePopulationAmount -= calculateCost(rscComp)
        preferences.flush { this["availablePopulation"] = availablePopulationAmount.toString() }
    }

    /**
     * Update the Model values for:
     * amount of a given ResourceComponent
     */
    private fun updateModelAmount(rscComp: ResourceComponent) {
        val bigBuyAmount = buyAmount.toBigDecimal().toBigInteger()
        when (rscComp.name) {
            "wheat" -> {
                wheatAmount += bigBuyAmount
                preferences.flush { this["wheat_amount"] = wheatAmount.toString() }
            }
            "corn" -> {
                cornAmount += bigBuyAmount
                preferences.flush { this["corn_amount"] = cornAmount.toString() }
            }
            "lettuce" -> {
                lettuceAmount += bigBuyAmount
                preferences.flush { this["lettuce_amount"] = lettuceAmount.toString() }
            }
            "carrots" -> {
                carrotsAmount += bigBuyAmount
                preferences.flush { this["carrots_amount"] = carrotsAmount.toString() }
            }
            "tomatoes" -> {
                tomatoesAmount += bigBuyAmount
                preferences.flush { this["tomatoes_amount"] = tomatoesAmount.toString() }
            }
            "broccoli" -> {
                broccoliAmount += bigBuyAmount
                preferences.flush { this["broccoli_amount"] = broccoliAmount.toString() }
            }
            "onions" -> {
                onionsAmount += bigBuyAmount
                preferences.flush { this["onions_amount"] = onionsAmount.toString() }
            }
            "potatoes" -> {
                potatoesAmount += bigBuyAmount
                preferences.flush { this["potatoes_amount"] = potatoesAmount.toString() }
            }
        }
    }

    /**
     * Update the Model values for:
     * populationGainRate
     */
    private fun updateModelPopulationRate() {
        var popGain = BigDecimal(0)
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            if (rscComp.name == "population") return@forEach
            popGain += (rscComp.baseValue * rscComp.multiplier * rscComp.amountOwned.toBigDecimal())
        }
        populationGainPerSecond = popGain
        preferences.flush { this["populationGainRate"] = populationGainPerSecond.toString() }
    }

    /**
     * Update the Model values for:
     * multiplier and cost for each ResourceComponent
     */
    private fun updateModel() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            updateModel(rscComp)
        }
    }

    /**
     * Update the Model values for:
     * multiplier and cost for a given ResourceComponent
     */
    private fun updateModel(rscComp : ResourceComponent) {
        when (rscComp.name) {
            "wheat" -> {
                wheatMultiplier = rscComp.multiplier
                wheatCost = calculateCost(rscComp)
                preferences.flush {
                    this["wheat_multiplier"] = wheatMultiplier.toString()
                    this["wheat_cost"] = wheatCost.toString()
                }
            }
            "corn" -> {
                cornMultiplier = rscComp.multiplier
                cornCost = calculateCost(rscComp)
                preferences.flush {
                    this["corn_multiplier"] = cornMultiplier.toString()
                    this["corn_cost"] = cornCost.toString()
                }
            }
            "lettuce" -> {
                lettuceMultiplier = rscComp.multiplier
                lettuceCost = calculateCost(rscComp)
                preferences.flush {
                    this["lettuce_multiplier"] = lettuceMultiplier.toString()
                    this["lettuce_cost"] = lettuceCost.toString()
                }
            }
            "carrots" -> {
                carrotsMultiplier = rscComp.multiplier
                carrotsCost = calculateCost(rscComp)
                preferences.flush {
                    this["carrots_multiplier"] = carrotsMultiplier.toString()
                    this["carrots_cost"] = carrotsCost.toString()
                }
            }
            "tomatoes" -> {
                tomatoesMultiplier = rscComp.multiplier
                tomatoesCost = calculateCost(rscComp)
                preferences.flush {
                    this["tomatoes_multiplier"] = tomatoesMultiplier.toString()
                    this["tomatoes_cost"] = tomatoesCost.toString()
                }
            }
            "broccoli" -> {
                broccoliMultiplier = rscComp.multiplier
                broccoliCost = calculateCost(rscComp)
                preferences.flush {
                    this["broccoli_multiplier"] = broccoliMultiplier.toString()
                    this["broccoli_cost"] = broccoliCost.toString()
                }
            }
            "onions" -> {
                onionsMultiplier = rscComp.multiplier
                onionsCost = calculateCost(rscComp)
                preferences.flush {
                    this["onions_multiplier"] = onionsMultiplier.toString()
                    this["onions_cost"] = onionsCost.toString()
                }
            }
            "potatoes" -> {
                potatoesMultiplier = rscComp.multiplier
                potatoesCost = calculateCost(rscComp)
                preferences.flush {
                    this["potatoes_multiplier"] = potatoesMultiplier.toString()
                    this["potatoes_cost"] = potatoesCost.toString()
                }
            }
        }
    }

    /**
     * @param ResourceComponent
     * Calculates the cost of a resource relative to the buyAmount and accounts
     * for part of the purchase bridging the price increase threshold.
     * @return Float
     */
    private fun calculateCost(rscComp: ResourceComponent) : BigDecimal {
        val _100 = BigInteger("100")
        val cost = rscComp.cost
        val nextCost = rscComp.nextCost
        val amount = rscComp.amountOwned
        return if (buyAmount == 100f || (buyAmount == 10f && amount.mod(_100) > BigInteger("90"))) {
            (cost * ((((amount / _100) + BigInteger("1")) * _100).subtract(amount).toBigDecimal())) + (nextCost * (amount.mod(BigInteger("${buyAmount.toInt()}")).toBigDecimal()))
        } else {
            cost * (buyAmount.toBigDecimal())
        }
    }

    private fun resetGameValues() {
        resourceEntities.forEach { entity ->
            val rscComp = resourceComponents[entity]
            rscComp.amountOwned = BigInteger("0")
        }
        totalPopulationAmount = BigDecimal("10")
        availablePopulationAmount = BigDecimal("10")
        populationGainPerSecond = BigDecimal("0")
        buyAmount = 1f

        wheatAmount = BigInteger("0")
        cornAmount = BigInteger("0")
        lettuceAmount = BigInteger("0")
        carrotsAmount = BigInteger("0")
        tomatoesAmount = BigInteger("0")
        broccoliAmount = BigInteger("0")
        onionsAmount = BigInteger("0")
        potatoesAmount = BigInteger("0")

        wheatMultiplier = BigDecimal("1")
        cornMultiplier = BigDecimal("1")
        lettuceMultiplier = BigDecimal("1")
        carrotsMultiplier = BigDecimal("1")
        tomatoesMultiplier = BigDecimal("1")
        broccoliMultiplier = BigDecimal("1")
        onionsMultiplier = BigDecimal("1")
        potatoesMultiplier = BigDecimal("1")

        wheatCost = BigDecimal("10")
        cornCost = BigDecimal("100")
        lettuceCost = BigDecimal("1000")
        carrotsCost = BigDecimal("10000")
        tomatoesCost = BigDecimal("100000")
        broccoliCost = BigDecimal("1000000")
        onionsCost = BigDecimal("10000000")
        potatoesCost = BigDecimal("100000000")

        gameCompleted = false

        preferences.flush {
            preferences.clear()
        }
    }

    companion object {
        private val log = logger<PlanetModel>()
    }
}
