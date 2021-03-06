package com.cout970.modeler.controller.usecases

import com.cout970.modeler.api.model.IModel
import com.cout970.modeler.api.model.`object`.Group
import com.cout970.modeler.api.model.`object`.IObject
import com.cout970.modeler.api.model.`object`.MutableGroupTree
import com.cout970.modeler.api.model.`object`.RootGroupRef
import com.cout970.modeler.controller.tasks.ITask
import com.cout970.modeler.controller.tasks.TaskUpdateModel
import com.cout970.modeler.core.model.TRSTransformation
import com.cout970.modeler.core.model.`object`.Object
import com.cout970.modeler.core.model.`object`.ObjectCube
import com.cout970.modeler.core.model.mesh.MeshFactory
import com.cout970.modeler.core.model.mutate
import com.cout970.modeler.core.model.ref
import com.cout970.vector.extensions.Quaternion
import com.cout970.vector.extensions.Vector3
import com.cout970.vector.extensions.vec3Of

/**
 * Created by cout970 on 2017/07/19.
 */

@UseCase("cube.mesh.new")
private fun newObject(model: IModel): ITask {
    val obj = Object(
            name = "Object ${model.objects.size}",
            mesh = MeshFactory.createCube(Vector3.ONE, Vector3.ORIGIN),
            transformation = TRSTransformation(vec3Of(4, 16, 4), Quaternion.IDENTITY, vec3Of(8, 8, 8))
    )

    return addObject(model, obj)
}

@UseCase("cube.template.new")
private fun newObjectCube(model: IModel): ITask {
    val obj = ObjectCube(
            name = "Object ${model.objects.size}",
            transformation = TRSTransformation(vec3Of(4, 16, 4), Quaternion.IDENTITY, vec3Of(8, 8, 8))
    )
    return addObject(model, obj)
}

private fun addObject(model: IModel, obj: IObject): TaskUpdateModel {
    val newModel = model.addObjects(listOf(obj))
    return TaskUpdateModel(model, newModel)
}

@UseCase("group.add")
private fun addGroup(model: IModel): ITask {
    val group = Group("Group ${model.tree.objects[RootGroupRef].size}")
    val newGroupTree = model.tree.mutate { children.add(MutableGroupTree(group.ref)) }
    val newModel = model.addGroup(group).withGroupTree(newGroupTree)
    return TaskUpdateModel(model, newModel)
}