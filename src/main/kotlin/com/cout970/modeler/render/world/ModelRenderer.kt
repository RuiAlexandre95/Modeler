package com.cout970.modeler.render.world

import com.cout970.glutilities.tessellator.BufferPTNC
import com.cout970.glutilities.tessellator.DrawMode
import com.cout970.glutilities.tessellator.VAO
import com.cout970.matrix.extensions.Matrix4
import com.cout970.modeler.api.model.IModel
import com.cout970.modeler.api.model.selection.*
import com.cout970.modeler.core.config.Config
import com.cout970.modeler.core.model.mesh.MeshFactory
import com.cout970.modeler.core.model.selection.ObjectRef
import com.cout970.modeler.render.tool.*
import com.cout970.modeler.util.getColor
import com.cout970.vector.extensions.Vector2
import com.cout970.vector.extensions.Vector3
import com.cout970.vector.extensions.plus
import com.cout970.vector.extensions.vec3Of
import org.lwjgl.opengl.GL11

/**
 * Created by cout970 on 2017/06/09.
 */

class ModelRenderer {

    var modelCache: List<VAO> = mutableListOf()
    var selectionCache = AutoCache(CacheFlags.MODEL, CacheFlags.SELECTION_MODEL, CacheFlags.CURSOR)
    var modelHash = -1

    fun renderModels(ctx: RenderContext, model: IModel) {

        val modelToRender = ctx.gui.state.tmpModel ?: model

        renderModel(ctx, modelToRender)
        renderSelection(ctx, modelToRender)
    }

    fun renderSelection(ctx: RenderContext, modelToRender: IModel) {
        val selectionBox = ctx.gui.modelAccessor.modelSelectionHandler.getSelection()
        val selection = selectionBox.getOrNull() ?: return

        val vao = selectionCache.getOrCreate(ctx) { buildSelection(ctx, modelToRender, selection) }
        ctx.shader.apply {
            useTexture.setInt(0)
            useColor.setInt(1)
            useLight.setInt(0)
            matrixM.setMatrix4(Matrix4.IDENTITY)
            GL11.glLineWidth(Config.selectionThickness * 20f)
            accept(vao)
            GL11.glLineWidth(1f)
        }
    }

    fun renderModel(ctx: RenderContext, model: IModel) {

        if (modelCache.isEmpty() || modelCache.size != model.objects.size || model.hashCode() != modelHash) {
            modelHash = model.hashCode()
            modelCache.forEach { it.close() }
            modelCache = buildCache(modelCache, ctx.buffer, model)
        }

        val map = model.objects
                .mapIndexed { ind, iObject -> ind to iObject }
                .filter { (first) -> model.visibilities[first] }
                .groupBy { it.second.material }

        map.forEach { material, list ->
            model.getMaterial(material).bind()
            list.forEach { (objIndex, _) ->
                ctx.shader.apply {
                    useTexture.setBoolean(ctx.gui.state.useTexture)
                    useColor.setBoolean(ctx.gui.state.useColor)
                    useLight.setBoolean(ctx.gui.state.useLight)
                    showHiddenFaces.setBoolean(ctx.gui.state.showHiddenFaces)
                    matrixM.setMatrix4(Matrix4.IDENTITY)
                    accept(modelCache[objIndex])
                    showHiddenFaces.setBoolean(false)
                }
            }
        }
    }

    private fun buildCache(list: List<VAO>, buffer: BufferPTNC, model: IModel): List<VAO> {
        list.forEach { it.close() }
        return model.objects
                .map { it.mesh }
                .map { it.createVao(buffer, getColor(it.hashCode())) }
    }

    private fun buildSelection(ctx: RenderContext, modelToRender: IModel,
                               selection: ISelection): VAO = when (selection.selectionType) {
        SelectionType.OBJECT -> ctx.buffer.build(DrawMode.LINES) { appendObjectSelection(modelToRender, selection) }
        SelectionType.FACE -> ctx.buffer.build(DrawMode.LINES) { appendFaceSelection(modelToRender, selection) }
        SelectionType.EDGE -> ctx.buffer.build(DrawMode.LINES) { appendEdgeSelection(modelToRender, selection) }
        SelectionType.VERTEX -> ctx.buffer.build(DrawMode.QUADS) { appendVertexSelection(modelToRender, selection) }
    }

    private fun BufferPTNC.appendObjectSelection(modelToRender: IModel,
                                                             selection: ISelection) {

        val objSel = modelToRender.objects.filterIndexed { index, _ ->
            selection.isSelected(ObjectRef(index))
        }
        objSel.forEach {
            it.mesh.forEachEdge { (a, b) ->
                add(a.pos, Vector2.ORIGIN, Vector3.ZERO, Config.colorPalette.modelSelectionColor)
                add(b.pos, Vector2.ORIGIN, Vector3.ZERO, Config.colorPalette.modelSelectionColor)
            }
        }
    }

    private fun BufferPTNC.appendFaceSelection(modelToRender: IModel,
                                                           selection: ISelection) {

        val pairs = selection.refs
                .filterIsInstance<IFaceRef>()
                .map { modelToRender.getObject(ObjectRef(it.objectIndex)) to it }

        pairs.forEach { (obj, ref) ->
            val face = obj.mesh.faces[ref.faceIndex]

            for (index in 0 until face.vertexCount) {
                val next = (index + 1) % face.vertexCount
                add(obj.mesh.pos[face.pos[index]], Vector2.ORIGIN, Vector3.ZERO,
                        Config.colorPalette.modelSelectionColor)
                add(obj.mesh.pos[face.pos[next]], Vector2.ORIGIN, Vector3.ZERO, Config.colorPalette.modelSelectionColor)
            }
        }
    }

    private fun BufferPTNC.appendEdgeSelection(modelToRender: IModel,
                                                           selection: ISelection) {

        val pairs = selection.refs
                .filterIsInstance<IEdgeRef>()
                .map { modelToRender.getObject(ObjectRef(it.objectIndex)) to it }

        pairs.forEach { (obj, ref) ->
            add(obj.mesh.pos[ref.firstIndex], Vector2.ORIGIN, Vector3.ZERO, Config.colorPalette.modelSelectionColor)
            add(obj.mesh.pos[ref.secondIndex], Vector2.ORIGIN, Vector3.ZERO, Config.colorPalette.modelSelectionColor)
        }
    }


    private fun BufferPTNC.appendVertexSelection(modelToRender: IModel,
                                                             selection: ISelection) {

        val pairs = selection.refs
                .filterIsInstance<IPosRef>()
                .map { modelToRender.getObject(ObjectRef(it.objectIndex)) to it }

        pairs.forEach { (obj, ref) ->
            val point = obj.mesh.pos[ref.posIndex]
            MeshFactory.createCube(vec3Of(0.5), vec3Of(-0.25) + point)
                    .append(this, Config.colorPalette.modelSelectionColor)
        }
    }
}