package com.cout970.modeler.modeleditor

import com.cout970.modeler.model.Mesh
import com.cout970.modeler.model.ModelGroup
import com.cout970.modeler.model.Transformation
import com.cout970.modeler.modeleditor.action.ActionCreateCube
import com.cout970.modeler.modeleditor.action.ActionCreatePlane
import com.cout970.modeler.modeleditor.selection.ModelPath
import com.cout970.modeler.modeleditor.selection.SelectionGroup
import com.cout970.modeler.util.applyGroup
import com.cout970.vector.extensions.vec3Of

/**
 * Created by cout970 on 2016/12/09.
 */
class ModelInserter(val modelEditor: ModelEditor) {

    var groupCount = 0
    var insertPath = 0
    var insertPosition = vec3Of(0, 0, 0)

    fun insertMesh(mesh: Mesh) {
        modelEditor.apply {
            if (model.groups.isEmpty()) {
                insertGroup()
            }
            val insertSelection = SelectionGroup(listOf(ModelPath(insertPath)))
            updateModel(model.applyGroup(insertSelection) { group ->
                group.add(mesh)
            })
        }
    }

    fun insertGroup(group: ModelGroup = ModelGroup(listOf(), Transformation(insertPosition), "Group_${groupCount++}")) {
        modelEditor.apply {
            updateModel(model.copy(groups = model.groups + group))
        }
    }

    fun addCube() {
        modelEditor.historyRecord.doAction(ActionCreateCube(modelEditor))
    }

    fun addPlane() {
        modelEditor.historyRecord.doAction(ActionCreatePlane(modelEditor))
    }
}