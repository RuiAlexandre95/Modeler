package com.cout970.modeler.gui.leguicomp

import com.cout970.modeler.core.config.Config
import com.cout970.modeler.gui.GuiResources
import com.cout970.modeler.util.IPropertyBind
import com.cout970.modeler.util.toColor
import com.cout970.reactive.dsl.transparent
import org.liquidengine.legui.icon.Icon
import org.liquidengine.legui.icon.ImageIcon
import org.liquidengine.legui.style.border.SimpleLineBorder
import org.liquidengine.legui.component.ToggleButton as LeguiToggleButton


/**
 * Created by cout970 on 2017/09/08.
 */

class ToggleButton(
        val id: String = "",
        var icon: String = "",
        val default: Boolean = false,
        posX: Float = 0f, posY: Float = 0f,
        sizeX: Float = 16f, sizeY: Float = 16f
) : LeguiToggleButton("", posX, posY, sizeX, sizeY), IResourceReloadable {

    var properties = mapOf<String, IPropertyBind<Boolean>>()

    init {
        transparent()
        toggledBackgroundColor = Config.colorPalette.selectedButton.toColor()
        style.border = SimpleLineBorder(Config.colorPalette.selectedButton.toColor(), 1f)
    }

    override fun isToggled(): Boolean = properties[id]?.get() ?: default

    override fun setToggled(toggled: Boolean) {
        properties[id]?.set(toggled)
        super.setToggled(toggled)
        style.border = SimpleLineBorder(Config.colorPalette.selectedButton.toColor(), 1f)
        style.border.isEnabled = toggled
    }

    fun bindProperties(map: Map<String, IPropertyBind<Boolean>>) {
        properties = map
        isToggled = isToggled
    }

    override fun loadResources(resources: GuiResources) {
        resources.getIcon("active_" + icon)?.let {
            setImage(ImageIcon(it))
        }
        resources.getIcon("disable_" + icon)?.let {
            backgroundIcon = ImageIcon(it)
        }
    }

    fun setImage(active: Icon) {
        backgroundIcon = active
        togglededBackgroundIcon = active
    }
}