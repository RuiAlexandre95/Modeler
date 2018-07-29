package com.cout970.modeler.render.tool

import com.cout970.glutilities.structure.Timer
import com.cout970.matrix.api.IMatrix4
import com.cout970.modeler.api.animation.*
import com.cout970.modeler.api.model.ITransformation
import com.cout970.modeler.api.model.`object`.IGroupRef
import com.cout970.modeler.api.model.selection.IObjectRef
import com.cout970.modeler.core.model.TRSTransformation
import com.cout970.modeler.core.model.TRTSTransformation
import com.cout970.modeler.gui.Gui

class Animator {

    lateinit var gui: Gui

    var zoom = 1f
    var offset = 0f
    var animationTime = 0f

    var selectedChannel: IChannelRef? = null
        set(value) {
            field = value
            selectedKeyframe = null
            sendUpdate()
        }

    var selectedKeyframe: Int? = null
        set(value) {
            field = value
            sendUpdate()
        }

    var animationState = AnimationState.STOP
        set(value) {
            field = value
            sendUpdate()
        }

    val animation get() = gui.programState.animation

    fun sendUpdate() {
        gui.listeners.runGuiCommand("updateAnimation")
    }

    fun updateTime(timer: Timer) {
        if (animationState != AnimationState.STOP && gui.state.modelSelection.isNonNull()) {
            gui.state.cursor.update(gui)
        }
        when (animationState) {
            AnimationState.FORWARD -> {
                animationTime += timer.delta.toFloat()
                animationTime %= animation.timeLength
            }
            AnimationState.BACKWARD -> {
                animationTime -= timer.delta.toFloat()
                if (animationTime < 0) {
                    animationTime += animation.timeLength
                }
            }
            else -> Unit
        }
    }

    fun animate(anim: IAnimation, group: IGroupRef, obj: IObjectRef): IMatrix4 {

        val now = animationTime
        return anim.channels
                .filter { it.value.enabled }
                .filter { (chanRef) ->
                    val target = anim.channelMapping[chanRef]

                    when (target) {
                        is AnimationTargetGroup -> {
                            target.ref == group
                        }
                        is AnimationTargetObject -> target.ref == obj
                        else -> false
                    }
                }
                .map { it.value }
                .fold(TRSTransformation.IDENTITY as ITransformation) { acc, c ->
                    val (prev, next) = getPrevAndNext(now, c.keyframes)
                    acc + interpolate(now, prev, next)
                }
                .matrix
    }

    fun interpolate(time: Float, prev: IKeyframe, next: IKeyframe): ITransformation {
        if (next.time == prev.time) return next.value

        val size = next.time - prev.time
        val step = (time - prev.time) / size

        return interpolate(prev.value, next.value, step)
    }

    fun interpolate(a: ITransformation, b: ITransformation, delta: Float): ITransformation {
        return when {
            a is TRSTransformation && b is TRSTransformation -> a.lerp(b, delta)
            a is TRTSTransformation && b is TRTSTransformation -> a.lerp(b, delta)
            a is TRTSTransformation && b is TRSTransformation -> a.lerp(b.toTRTS(), delta)
            a is TRTSTransformation && b is TRSTransformation -> a.lerp(b.toTRTS(), delta)
            else -> error("Unknown ITransformation pair: $a, $b")
        }
    }

    fun getPrevAndNext(time: Float, keyframes: List<IKeyframe>): Pair<IKeyframe, IKeyframe> {
        val next = keyframes.firstOrNull { it.time > time } ?: keyframes.first()
        val prev = keyframes.lastOrNull { it.time <= time } ?: keyframes.last()

        return prev to next
    }
}