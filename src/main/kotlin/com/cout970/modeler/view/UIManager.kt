package com.cout970.modeler.view

import com.cout970.modeler.event.EventHandler
import com.cout970.modeler.project.ProjectManager
import com.cout970.modeler.util.ITickeable
import com.cout970.modeler.view.controller.ButtonController
import com.cout970.modeler.view.controller.ModuleController
import com.cout970.modeler.view.controller.SceneController
import com.cout970.modeler.view.gui.RootFrame
import com.cout970.modeler.view.gui.TextureHandler
import com.cout970.modeler.view.scene.SceneModel
import com.cout970.modeler.view.scene.SceneTexture
import com.cout970.modeler.window.WindowHandler

/**
 * Created by cout970 on 2016/12/27.
 */

class UIManager(
        val windowHandler: WindowHandler,
        private val eventHandler: EventHandler,
        renderManager: RenderManager,
        textureHandler: TextureHandler,
        private val projectManager: ProjectManager) : ITickeable {

    val sceneController: SceneController
    val moduleController: ModuleController
    val buttonController: ButtonController

    val rootFrame: RootFrame

    init {
        renderManager.uiManager = this
        buttonController = ButtonController(projectManager, this)
        rootFrame = RootFrame(eventHandler, windowHandler, buttonController)
        sceneController = SceneController(projectManager.modelEditor, eventHandler, rootFrame, windowHandler.timer)
        moduleController = ModuleController(projectManager.modelEditor, rootFrame, buttonController, eventHandler,
                textureHandler)
        showScenes(0)
    }

    fun showScenes(layout: Int) {
        sceneController.scenes.clear()
        when (layout) {
            1 -> {
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
            }
            2 -> {
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
            }
            3 -> {
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneTexture(projectManager.modelEditor, windowHandler, sceneController)
            }
            4 -> {
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneTexture(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
            }
            else -> {
                sceneController.scenes += SceneModel(projectManager.modelEditor, windowHandler, sceneController)
            }
        }
        sceneController.refreshScenes()
    }

    override fun tick() {
        windowHandler.resetViewport()
        moduleController.tick()
        sceneController.tick()
    }
}