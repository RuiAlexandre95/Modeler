package com.cout970.modeler.view.scene

import com.cout970.glutilities.event.EnumKeyState
import com.cout970.glutilities.event.EventKeyUpdate
import com.cout970.modeler.event.IEventController
import com.cout970.modeler.event.IEventListener
import com.cout970.modeler.modeleditor.ModelEditor
import com.cout970.modeler.util.absolutePosition
import com.cout970.modeler.view.controller.SceneController
import com.cout970.modeler.view.render.RenderManager
import com.cout970.modeler.window.WindowHandler
import com.cout970.vector.extensions.vec2Of

/**
 * Created by cout970 on 2017/01/02.
 */
class TextureScene(modelEditor: ModelEditor, windowHandler: WindowHandler, controller: SceneController) : Scene(
        modelEditor, windowHandler, controller) {

    init {
        camera = Camera.DEFAULT.copy(angleX = 0.0, angleY = 0.0)
    }

    override fun render(renderManager: RenderManager) {
        if (size.x < 1 || size.y < 1) return

        renderManager.modelRenderer.run {
            val model = sceneController.modelProvider.model
            val selection = sceneController.modelProvider.selectionManager.selection
            matrixP = createOrthoMatrix()
            matrixV = camera.matrixForUV

            val y = parent.size.y - (position.y + size.y)
            windowHandler.saveViewport(vec2Of(absolutePosition.x, y), vec2Of(size.x, size.y)) {
                renderUV(model, selection)
            }
        }
    }

    override fun registerListeners(eventHandler: IEventController) {
        eventHandler.addListener(EventKeyUpdate::class.java, object : IEventListener<EventKeyUpdate> {
            override fun onEvent(e: EventKeyUpdate): Boolean {
                if (e.keyState == EnumKeyState.PRESS) {

                }
                return false
            }
        })
    }
}