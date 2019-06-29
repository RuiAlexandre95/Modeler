package com.cout970.modeler.core.export

import com.cout970.modeler.api.model.IModel
import com.cout970.modeler.api.model.`object`.IObject
import com.cout970.modeler.api.model.material.IMaterial
import com.cout970.modeler.core.model.Model
import com.cout970.modeler.core.model.`object`.Object
import com.cout970.modeler.core.model.material.MaterialRefNone
import com.cout970.modeler.core.model.material.TexturedMaterial
import com.cout970.modeler.core.model.mesh.FaceIndex
import com.cout970.modeler.core.model.mesh.Mesh
import com.cout970.modeler.core.model.ref
import com.cout970.modeler.core.resource.ResourcePath
import com.cout970.modeler.util.Direction
import com.cout970.vector.api.IVector2
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.times
import com.google.gson.GsonBuilder
import java.io.OutputStream

/**
 * Created by cout970 on 2017/01/26.
 */
private val GSON = GsonBuilder()
        .registerTypeAdapter(IVector3::class.java, Vector3Serializer())
        .registerTypeAdapter(IVector2::class.java, Vector2Serializer())
        .registerTypeAdapter(QuadIndices::class.java, QuadIndicesSerializer())
        .setPrettyPrinting()
        .create()!!


class McxExporter {

    fun export(output: OutputStream, model: IModel, args: McxExportProperties) {

        val posSet = mutableSetOf<IVector3>()
        val texSet = mutableSetOf<IVector2>()

        model.objects.forEach {
            posSet += it.mesh.pos
            texSet += it.mesh.tex
        }

        val pos = posSet.toList()
        val tex = texSet.toList()
        val indices = mutableListOf<QuadIndices>()
        val parts = mutableListOf<Part>()
        var particleTexture: String? = null

        model.objects.forEach { obj ->

            val name = model.getMaterial(obj.material).name
                .replace("\\.png$".toRegex(), "")
                .replace(" ", "_")
                .toLowerCase()

            val texture = "${args.domain}/$name"
            val mesh = obj.mesh
            val localIndices = mesh.faces.map { face ->
                val (ap, bp, cp, dp) = face.pos
                val (at, bt, ct, dt) = face.tex
                QuadIndices(
                        pos.indexOf(mesh.pos[ap]),
                        pos.indexOf(mesh.pos[bp]),
                        pos.indexOf(mesh.pos[cp]),
                        pos.indexOf(mesh.pos[dp]),
                        tex.indexOf(mesh.tex[at]),
                        tex.indexOf(mesh.tex[bt]),
                        tex.indexOf(mesh.tex[ct]),
                        tex.indexOf(mesh.tex[dt])
                )
            }
            if (particleTexture == null) particleTexture = texture

            parts += Part(obj.name, indices.size, indices.size + localIndices.size, null, texture)
            indices += localIndices
        }

        val data = ModelData(
                useAmbientOcclusion = true,
                use3dInGui = true,
                particleTexture = particleTexture ?: "${args.domain}/unknown",
                parts = parts,
                quads = QuadStorage(pos.map { it * (1 / 16.0) }, tex, indices)
        )

        val str = GSON.toJson(data)
        output.write(str.toByteArray())
        output.close()
    }


}

class McxImporter {

    fun import(path: ResourcePath): IModel {

        val model = GSON.fromJson(path.inputStream().reader(), ModelData::class.java)

        val materialPaths = (listOf(model.particleTexture) + model.parts.map { it.texture }).toSet()

        val materials = materialPaths.associate { matPath ->
            val name = matPath.substringAfter(':').substringAfterLast('/')
            matPath to TexturedMaterial(name, ResourcePath.textureFromResourceLocation(matPath, path))
        }
        val objects = model.parts.map { it.toObject(materials, model) }

        return Model.of(objects, materials.values.toList())
    }

    fun Part.toObject(materials: Map<String, IMaterial>, model: ModelData): IObject {
//        if(side == null && (to - from) == 6){ // probably a cube
//
//        }
        // not a cube
        val storage = model.quads
        val quadIndices = storage.indices.subList(from, to)
        val pos = quadIndices.flatMap {
            listOf(storage.pos[it.a], storage.pos[it.b], storage.pos[it.c], storage.pos[it.d])
        }
        val tex = quadIndices.flatMap {
            listOf(storage.tex[it.at], storage.tex[it.bt], storage.tex[it.ct], storage.tex[it.dt])
        }
        val faces = quadIndices.map {
            FaceIndex.from(
                    listOf(
                            pos.indexOf(storage.pos[it.a]),
                            pos.indexOf(storage.pos[it.b]),
                            pos.indexOf(storage.pos[it.c]),
                            pos.indexOf(storage.pos[it.d])
                    ),
                    listOf(
                            tex.indexOf(storage.tex[it.at]),
                            tex.indexOf(storage.tex[it.bt]),
                            tex.indexOf(storage.tex[it.ct]),
                            tex.indexOf(storage.tex[it.dt])
                    )
            )
        }

        val mesh = Mesh(pos.map { it * 16.0 }, tex, faces)

        return Object(name, mesh, materials[texture]?.ref ?: MaterialRefNone)
    }
}


data class ModelData(
        val useAmbientOcclusion: Boolean,
        val use3dInGui: Boolean,
        val particleTexture: String,
        val parts: List<Part>,
        val quads: QuadStorage
)

data class Part(val name: String, val from: Int, val to: Int, val side: Direction?, val texture: String)

class QuadStorage(val pos: List<IVector3>, val tex: List<IVector2>, val indices: List<QuadIndices>) {

    override fun toString(): String {
        return "QuadStorage(pos=[${pos.size}], tex=[${tex.size}], indices=[${indices.size}])"
    }
}

class QuadIndices(val a: Int, val b: Int, val c: Int, val d: Int,
                  val at: Int, val bt: Int, val ct: Int, val dt: Int)