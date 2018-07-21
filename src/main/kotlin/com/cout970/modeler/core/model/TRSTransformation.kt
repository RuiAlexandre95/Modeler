package com.cout970.modeler.core.model

import com.cout970.matrix.api.IMatrix4
import com.cout970.modeler.api.model.ITransformation
import com.cout970.modeler.util.*
import com.cout970.vector.api.IQuaternion
import com.cout970.vector.api.IVector3
import com.cout970.vector.extensions.*
import org.joml.Matrix4d
import org.joml.Quaterniond

/**
 * Created by cout970 on 2017/05/14.
 */
data class TRSTransformation(
        val translation: IVector3 = Vector3.ORIGIN,
        val rotation: IQuaternion = Quaternion.IDENTITY,
        val scale: IVector3 = Vector3.ONE
) : ITransformation {

    companion object {
        val IDENTITY = TRSTransformation(Vector3.ORIGIN, Quaternion.IDENTITY, Vector3.ONE)

        @Suppress("UnnecessaryVariable")
        fun fromRotationPivot(pivot: IVector3, rotation: IQuaternion): TRSTransformation {
            val preRotation = pivot
            val postRotation = -pivot

            val matrix = Matrix4d().apply {
                translate(preRotation.xd, preRotation.yd, preRotation.zd)
                rotate(rotation.toJOML())
                translate(postRotation.xd, postRotation.yd, postRotation.zd)
            }

            val pos = vec3Of(matrix.m30(), matrix.m31(), matrix.m32())
            val rot = Quaterniond().setFromUnnormalized(matrix).toIQuaternion()

            return TRSTransformation(pos, rot, Vector3.ONE)
        }

        fun fromRotationPivot(pivot: IVector3, rotation: IVector3): TRSTransformation {
            return fromRotationPivot(pivot, quatOfAngles(rotation))
        }
    }

    // Gson pls
    private constructor() : this(Vector3.ORIGIN, Quaternion.IDENTITY, Vector3.ONE)

    override val matrix: IMatrix4 by lazy {
        Matrix4d().apply {
            translate(translation.xd, translation.yd, translation.zd)
            rotate(rotation.toJOML())
            scale(scale.xd, scale.yd, scale.zd)
        }.toIMatrix()
    }

    fun merge(other: TRSTransformation): TRSTransformation {
        return TRSTransformation(
                translation = other.rotation.transform(this.translation) + other.translation,
                rotation = other.rotation * this.rotation,
                scale = this.scale * other.scale
        )
    }

    fun lerp(other: TRSTransformation, step: Float): TRSTransformation {
        return TRSTransformation(
                translation = this.translation.interpolate(other.translation, step.toDouble()),
                rotation = this.rotation.lerp(other.rotation, step.toDouble()),
                scale = this.scale.interpolate(other.scale, step.toDouble())
        )
    }

    fun toTRTS(): TRTSTransformation {
        return TRTSTransformation(
                translation = translation,
                rotation = rotation.toAxisRotations(),
                pivot = Vector3.ZERO,
                scale = scale
        )
    }

    override fun plus(other: ITransformation): ITransformation {
        return when (other) {
            is TRSTransformation -> this.merge(other)
            is TRTSTransformation -> this.merge(other.toTRS())
            else -> error("Unknown ITransformation type: $other, ${other::class.java.name}")
        }
    }
}

fun ITransformation.toTRS() = when (this) {
    is TRSTransformation -> this
    is TRTSTransformation -> this.toTRS()
    else -> error("Type: ${javaClass.name}")
}