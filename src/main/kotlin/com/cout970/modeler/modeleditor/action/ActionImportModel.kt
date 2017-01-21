package com.cout970.modeler.modeleditor.action

import com.cout970.modeler.log.print
import com.cout970.modeler.model.Model
import com.cout970.modeler.modeleditor.ModelEditor
import com.cout970.modeler.resource.ResourceLoader

/**
 * Created by cout970 on 2017/01/02.
 */
class ActionImportModel(val modelEditor: ModelEditor,
                        val resourceLoader: ResourceLoader,
                        val path: String,
                        val function: () -> Model) : IAction {

    val oldModel = modelEditor.model

    override fun run() {
        try {
            val newModel = function()
            modelEditor.selectionManager.clearSelection()
            newModel.groups.map { it.material }.distinct().forEach {
                it.loadTexture(resourceLoader)
            }
            modelEditor.updateModel(newModel)
        } catch(e: Exception) {
            e.print()
        }
    }

    override fun undo() {
        modelEditor.updateModel(oldModel)
    }

    override fun toString(): String {
        return "ActionImportModel(path='$path')"
    }
}