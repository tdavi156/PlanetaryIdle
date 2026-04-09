package com.github.jacks.planetaryIdle.ui.views

import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.github.jacks.planetaryIdle.ui.Buttons
import com.github.jacks.planetaryIdle.ui.Labels
import com.github.jacks.planetaryIdle.ui.models.SettingsModel
import ktx.actors.txt
import ktx.scene2d.KTable
import ktx.scene2d.KWidget
import ktx.scene2d.Scene2DSkin
import ktx.scene2d.Scene2dDsl
import ktx.scene2d.actor
import ktx.scene2d.label
import ktx.scene2d.table
import ktx.scene2d.textButton

class SettingsView(
    private val model: SettingsModel,
    skin: Skin,
) : Table(skin), KTable {

    private lateinit var masterValueLabel: Label
    private lateinit var musicValueLabel: Label
    private lateinit var effectsValueLabel: Label
    private lateinit var saveBtn: TextButton
    private lateinit var cancelBtn: TextButton

    init {
        val vm = model
        setFillParent(true)

        table { outerCell ->
            pad(12f)

            label("Settings", Labels.MEDIUM.skinKey) { it.colspan(1).center().padBottom(10f) }
            row()

            // Row: Master Volume
            table { rowCell ->
                pad(4f)
                label("Master Volume:", Labels.SMALL.skinKey) { it.left().expandX() }
                textButton("<", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
                    cell.width(34f).height(30f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            vm.masterVolume = (vm.masterVolume - 10).coerceIn(0, 100)
                        }
                    })
                }
                this@SettingsView.masterValueLabel = label("100%", Labels.SMALL.skinKey) { it.width(55f).center() }
                textButton(">", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
                    cell.width(34f).height(30f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            vm.masterVolume = (vm.masterVolume + 10).coerceIn(0, 100)
                        }
                    })
                }
                rowCell.fillX().padBottom(4f)
            }
            row()

            // Row: Music Volume
            table { rowCell ->
                pad(4f)
                label("Music Volume:", Labels.SMALL.skinKey) { it.left().expandX() }
                textButton("<", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
                    cell.width(34f).height(30f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            vm.musicVolume = (vm.musicVolume - 10).coerceIn(0, 100)
                        }
                    })
                }
                this@SettingsView.musicValueLabel = label("100%", Labels.SMALL.skinKey) { it.width(55f).center() }
                textButton(">", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
                    cell.width(34f).height(30f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            vm.musicVolume = (vm.musicVolume + 10).coerceIn(0, 100)
                        }
                    })
                }
                rowCell.fillX().padBottom(4f)
            }
            row()

            // Row: Effects Volume
            table { rowCell ->
                pad(4f)
                label("Effects Volume:", Labels.SMALL.skinKey) { it.left().expandX() }
                textButton("<", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
                    cell.width(34f).height(30f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            vm.effectsVolume = (vm.effectsVolume - 10).coerceIn(0, 100)
                        }
                    })
                }
                this@SettingsView.effectsValueLabel = label("100%", Labels.SMALL.skinKey) { it.width(55f).center() }
                textButton(">", Buttons.GREY_BUTTON_SMALL.skinKey) { cell ->
                    cell.width(34f).height(30f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) {
                            vm.effectsVolume = (vm.effectsVolume + 10).coerceIn(0, 100)
                        }
                    })
                }
                rowCell.fillX().padBottom(4f)
            }
            row()

            // Row: Save / Cancel
            table { rowCell ->
                pad(4f)
                this@SettingsView.saveBtn = textButton("Save", Buttons.GREEN_BUTTON_MEDIUM.skinKey) { cell ->
                    cell.width(100f).height(35f).padRight(10f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) { vm.save() }
                    })
                }
                this@SettingsView.cancelBtn = textButton("Cancel", Buttons.RED_BUTTON_MEDIUM.skinKey) { cell ->
                    cell.width(100f).height(35f)
                    addListener(object : ChangeListener() {
                        override fun changed(event: ChangeEvent, actor: Actor) { vm.cancel() }
                    })
                }
                rowCell.fillX().center().padBottom(2f)
            }

            outerCell.expand().center().width(380f)
        }

        // Data bindings
        model.onPropertyChange(SettingsModel::masterVolume)  { v -> masterValueLabel.txt  = "$v%" }
        model.onPropertyChange(SettingsModel::musicVolume)   { v -> musicValueLabel.txt   = "$v%" }
        model.onPropertyChange(SettingsModel::effectsVolume) { v -> effectsValueLabel.txt = "$v%" }
    }
}

@Scene2dDsl
fun <S> KWidget<S>.settingsView(
    model: SettingsModel,
    skin: Skin = Scene2DSkin.defaultSkin,
    init: SettingsView.(S) -> Unit = {},
): SettingsView = actor(SettingsView(model, skin), init)
