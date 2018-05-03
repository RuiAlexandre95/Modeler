package com.cout970.modeler.gui.rcomponents

import com.cout970.modeler.core.config.Config
import com.cout970.modeler.core.export.*
import com.cout970.modeler.core.log.print
import com.cout970.modeler.core.project.IProjectPropertiesHolder
import com.cout970.modeler.core.search.SearchDatabase.options
import com.cout970.modeler.gui.GuiState
import com.cout970.modeler.gui.leguicomp.*
import com.cout970.modeler.util.toColor
import com.cout970.modeler.util.toPointerBuffer
import com.cout970.reactive.core.*
import com.cout970.reactive.dsl.*
import com.cout970.reactive.nodes.child
import com.cout970.reactive.nodes.comp
import com.cout970.reactive.nodes.div
import com.cout970.reactive.nodes.style
import org.joml.Vector4f
import org.liquidengine.legui.component.Button
import org.liquidengine.legui.component.CheckBox
import org.liquidengine.legui.component.TextInput
import org.liquidengine.legui.component.event.checkbox.CheckBoxChangeValueEvent
import org.liquidengine.legui.component.event.selectbox.SelectBoxChangeSelectionEvent
import org.liquidengine.legui.component.event.textinput.TextInputContentChangeEvent
import org.liquidengine.legui.component.optional.align.HorizontalAlign
import org.liquidengine.legui.icon.CharIcon
import org.liquidengine.legui.style.border.SimpleLineBorder
import org.lwjgl.PointerBuffer
import org.lwjgl.util.tinyfd.TinyFileDialogs

data class PopUpProps(val state: GuiState, val propertyHolder: IProjectPropertiesHolder) : RProps

class PopUp : RStatelessComponent<PopUpProps>() {

    override fun RBuilder.render() = div("PopUp") {
        val popup = props.state.popup
        style {
            if (popup == null) {
                hide()
            } else {
                backgroundColor { Vector4f(1f, 1f, 1f, 0.15f) }
            }
        }

        postMount {
            fill()
        }

        popup?.let {
            when (it.name) {
                "import" -> child(ImportDialog::class, PopupReturnProps(it.returnFunc))
                "export" -> child(ExportDialog::class, PopupReturnProps(it.returnFunc))
                "export_texture" -> child(ExportTextureDialog::class, PopupReturnProps(it.returnFunc))
                "config" -> child(ConfigMenu::class, ConfigMenuProps(it.returnFunc, props.propertyHolder))
            }
        }
    }
}

data class PopupReturnProps(val returnFunc: (Any?) -> Unit) : RProps

data class ImportDialogState(val text: String, val option: Int, val flipUV: Boolean, val forceUpdate: Boolean) : RState


class ImportDialog : RComponent<PopupReturnProps, ImportDialogState>() {

    companion object {
        private val options = listOf("Obj (*.obj)", "Techne (*.tcn, *.zip)", "Minecraft (*.json)",
                "Tabula (*.tbl)", "MCX (*.mcx)", "Project (*.pff)", "GL Transport Format (*.gltf)")

        private val extensions = listOf("*.obj", "*.tcn", "*.json", "*.tbl", "*.mcx", "*.pff", "*.gltf")
                .toPointerBuffer()
    }

    override fun getInitialState() = ImportDialogState("", 0, false, false)

    override fun RBuilder.render() = div("ImportPopup") {
        style {
            background { darkestColor }
            borderColor { color { greyColor } }
            borderSize = 2f
            width = 460f
            height = 240f
        }

        postMount {
            center()
        }

        // first line
        +FixedLabel("Import Model", 0f, 8f, 460f, 24f).apply {
            textState.fontSize = 22f
        }

        //second line
        +FixedLabel("Path", 25f, 50f, 64f, 24f).apply {
            textState.fontSize = 20f
            textState.horizontalAlign = HorizontalAlign.LEFT
        }

        +TextInput(state.text, 90f, 50f, 250f, 24f).apply {
            on<TextInputContentChangeEvent<TextInput>> {
                setState { copy(text = it.newValue) }
            }
        }

        +TextButton("", "Select", 360f, 50f, 80f, 24f).apply {

            onRelease {
                val file = try {
                    TinyFileDialogs.tinyfd_openFileDialog(
                            "Import",
                            "",
                            extensions,
                            "Model Files (*.tcn, *.obj, *.json, *.tbl, *.mcx, *.pff, *.gltf)",
                            false
                    )
                } catch (e: Exception) {
                    null
                }
                if (file != null) {
                    val newOption = when {
                        file.endsWith(".obj") -> 0
                        file.endsWith(".zip") || file.endsWith(".tcn") -> 1
                        file.endsWith(".json") -> 2
                        file.endsWith(".tbl") -> 3
                        file.endsWith(".mcx") -> 4
                        file.endsWith(".pff") -> 5
                        file.endsWith(".gltf") -> 6
                        else -> state.option
                    }
                    setState { copy(text = file, option = newOption, forceUpdate = !forceUpdate) }
                }
            }
        }

        //third line
        +FixedLabel("Format", 25f, 100f, 64f, 24f).apply {
            textState.fontSize = 20f
            textState.horizontalAlign = HorizontalAlign.LEFT
        }

        comp(DropDown()) {
            style {
                posX = 90f
                posY = 100f
                sizeX = 350f
                sizeY = 24f
                elementHeight = 22f
                buttonWidth = 22f
                visibleCount = 4
                options.forEach { addElement(it) }
                setSelected(state.option, true)
            }

            childrenAsNodes()

            on<SelectBoxChangeSelectionEvent<DropDown>> {
                setState { copy(option = options.indexOf(it.newValue)) }
            }
        }

        //fourth line
        +CheckBox("Flip UV", 360f, 150f, 80f, 24f).apply {

            background { buttonColor }
            textState.fontSize = 18f
            textState.padding.x = 5f
            isChecked = state.flipUV
            style.setBorderRadius(0f)

            if (state.option != 0) { // disable
                isEnabled = false
                textState.textColor = Config.colorPalette.darkestColor.toColor()
                (iconChecked as CharIcon).color = Config.colorPalette.darkestColor.toColor()
                (iconUnchecked as CharIcon).color = Config.colorPalette.darkestColor.toColor()
            } else { // enable
                textState.textColor = Config.colorPalette.textColor.toColor()
                (iconChecked as CharIcon).color = Config.colorPalette.whiteColor.toColor()
                (iconUnchecked as CharIcon).color = Config.colorPalette.whiteColor.toColor()
            }

            on<CheckBoxChangeValueEvent<CheckBox>> {
                setState { copy(flipUV = it.isNewValue) }
            }
        }

        //fifth line
        +TextButton("", "Replace", 180f, 200f, 80f, 24f).apply {
            onClick {
                props.returnFunc(ImportProperties(
                        path = state.text,
                        format = ImportFormat.values()[state.option],
                        flipUV = state.flipUV,
                        append = false
                ))
            }
        }

        +TextButton("", "Append", 270f, 200f, 80f, 24f).apply {
            onClick {
                props.returnFunc(ImportProperties(
                        path = state.text,
                        format = ImportFormat.values()[state.option],
                        flipUV = state.flipUV,
                        append = true
                ))
            }
        }

        +TextButton("", "Cancel", 360f, 200f, 80f, 24f).apply {
            onClick {
                props.returnFunc(null)
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: PopupReturnProps, nextState: ImportDialogState): Boolean {
        return state.flipUV != nextState.flipUV || state.option != nextState.option || state.forceUpdate != nextState.forceUpdate
    }
}

data class ExportDialogState(val text: String, val prefix: String, val selection: Int, var forceUpdate: Boolean) : RState

class ExportDialog : RComponent<PopupReturnProps, ExportDialogState>() {

    companion object {
        private val options = listOf("Obj (*.obj)", "MCX (*.mcx)", "GLTF (*.gltf)")
        private val exportExtensionsObj = listOf("*.obj").toPointerBuffer()
        private val exportExtensionsMcx = listOf("*.mcx").toPointerBuffer()
        private val exportExtensionsGltf = listOf("*.gltf").toPointerBuffer()

        private fun getExportFileExtensions(format: ExportFormat): PointerBuffer = when (format) {
            ExportFormat.OBJ -> exportExtensionsObj
            ExportFormat.MCX -> exportExtensionsMcx
            ExportFormat.GLTF -> exportExtensionsGltf
        }
    }

    override fun getInitialState() = ExportDialogState("", "magneticraft:blocks/", 1, false)

    override fun RBuilder.render() = div("ExportDialog") {
        style {
            background { darkestColor }
            style.border = SimpleLineBorder(Config.colorPalette.greyColor.toColor(), 2f)
            width = 460f
            height = 240f
        }

        postMount {
            center()
        }

        // first line
        +FixedLabel("Export Model", 0f, 8f, 460f, 24f).apply {
            textState.fontSize = 22f
        }

        //second line
        +FixedLabel("Format", 25f, 50f, 400f, 24f).apply {
            textState.fontSize = 20f
            textState.horizontalAlign = HorizontalAlign.LEFT
        }


        +TextButton("", "Obj (*.obj)", 90f, 50f, 110f, 24f).apply {
            if (state.selection != 0) background { darkColor }

            onClick {
                setState { copy(selection = 0) }
            }
        }

        +TextButton("", "MCX (*.mcx)", 210f, 50f, 110f, 24f).apply {
            if (state.selection != 1) background { darkColor }

            onClick {
                setState { copy(selection = 1) }
            }
        }

        +TextButton("", "GLTF (*.gltf)", 330f, 50f, 110f, 24f).apply {
            if (state.selection != 2) background { darkColor }

            onClick {
                setState { copy(selection = 2) }
            }
        }

        //third line
        +FixedLabel("Path", 25f, 100f, 400f, 24f).apply {
            textState.fontSize = 20f
            textState.horizontalAlign = HorizontalAlign.LEFT
        }

        comp(TextInput(state.text, 90f, 100f, 250f, 24f)) {
            on<TextInputContentChangeEvent<TextInput>> {
                setState { copy(text = it.newValue, forceUpdate = false) }
            }
        }

        comp(TextButton("", "Select", 360f, 100f, 80f, 24f)) {
            onRelease {
                val file = try {
                    TinyFileDialogs.tinyfd_saveFileDialog(
                            "Export",
                            "model." + ExportFormat.values()[state.selection].name.toLowerCase(),
                            getExportFileExtensions(ExportFormat.values()[state.selection]),
                            options[state.selection]
                    )
                } catch (e: Exception) {
                    e.print(); null
                }

                if (file != null) {
                    setState { copy(text = file, forceUpdate = true) }
                }
            }
        }

        //fourth line

        +FixedLabel("Prefix", 25f, 150f, 400f, 24f).apply {
            textState.fontSize = 20f
            textState.horizontalAlign = HorizontalAlign.LEFT
        }

        comp(TextInput(state.prefix, 90f, 150f, 350f, 24f)) {
            on<TextInputContentChangeEvent<TextInput>> {
                setState { copy(prefix = it.newValue, forceUpdate = false) }
            }
        }

        //fifth line
        +TextButton("", "Export", 270f, 200f, 80f, 24f).apply {
            onClick {
                props.returnFunc(ExportProperties(
                        path = state.text,
                        format = ExportFormat.values()[state.selection],
                        domain = "domain",
                        materialLib = "materials"
                ))
            }
        }

        +TextButton("", "Cancel", 360f, 200f, 80f, 24f).apply {
            onClick {
                props.returnFunc(null)
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: PopupReturnProps, nextState: ExportDialogState): Boolean {
        return state.selection != nextState.selection || nextState.forceUpdate
    }
}

data class ExportTextureDialogState(val text: String, val size: Int, var forceUpdate: Boolean) : RState

class ExportTextureDialog : RComponent<PopupReturnProps, ExportTextureDialogState>() {

    companion object {
        private val exportExtensionsPng = listOf("*.png").toPointerBuffer()
    }

    override fun getInitialState() = ExportTextureDialogState("", 64, false)

    override fun RBuilder.render() = div("ExportDialog") {
        style {
            background { darkestColor }
            style.border = SimpleLineBorder(Config.colorPalette.greyColor.toColor(), 2f)
            width = 460f
            height = 240f
        }

        postMount {
            center()
        }

        // first line
        +FixedLabel("Export Texture Template", 0f, 8f, 460f, 24f).apply {
            textState.fontSize = 22f
        }

        //second line
        +FixedLabel("Scale", 25f, 50f, 400f, 24f).apply {
            textState.fontSize = 20f
            textState.horizontalAlign = HorizontalAlign.LEFT
        }

        comp(TextInput(state.size.toString(), 90f, 50f, 250f, 24f)) {
            style {
                horizontalAlign = HorizontalAlign.RIGHT
                fontSize(20f)
            }

            on<TextInputContentChangeEvent<TextInput>> {
                setState { copy(size = it.newValue.toIntOrNull() ?: size) }
            }
        }

        //third line
        +FixedLabel("Path", 25f, 100f, 400f, 24f).apply {
            textState.fontSize = 20f
            textState.horizontalAlign = HorizontalAlign.LEFT
        }

        comp(TextInput(state.text, 90f, 100f, 250f, 24f)) {
            on<TextInputContentChangeEvent<TextInput>> {
                setState { copy(text = it.newValue, forceUpdate = false) }
            }
        }

        comp(TextButton("", "Select", 360f, 100f, 80f, 24f)) {
            onRelease {
                val file = try {
                    TinyFileDialogs.tinyfd_saveFileDialog(
                            "Export Texture Template",
                            "template.png",
                            exportExtensionsPng,
                            "PNG texture (*.png)"
                    )
                } catch (e: Exception) {
                    e.print(); null
                }

                if (file != null) {
                    setState { copy(text = file, forceUpdate = true) }
                }
            }
        }

        //fourth line

        //fifth line
        +TextButton("", "Export", 270f, 200f, 80f, 24f).apply {
            onClick {
                props.returnFunc(ExportTextureProperties(
                        path = state.text,
                        size = state.size
                ))
            }
        }

        +TextButton("", "Cancel", 360f, 200f, 80f, 24f).apply {
            onClick {
                props.returnFunc(null)
            }
        }
    }

    override fun shouldComponentUpdate(nextProps: PopupReturnProps, nextState: ExportTextureDialogState): Boolean {
        return state.size != nextState.size || nextState.forceUpdate
    }
}
