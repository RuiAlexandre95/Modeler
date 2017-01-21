package com.cout970.modeler.view.popup

import com.cout970.modeler.export.ExportFormat
import com.cout970.modeler.log.print
import com.cout970.modeler.project.Project
import com.cout970.modeler.project.ProjectManager
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryUtil
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.awt.Point
import java.awt.Toolkit
import java.awt.image.BufferedImage
import javax.swing.JDialog
import javax.swing.JOptionPane

/**
 * Created by cout970 on 2016/12/29.
 */

val popupImage = BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB).apply {
    for (i in 0..15) {
        for (j in 0..15) {
            setRGB(i, j, 0xFFFFFF)
        }
    }
}

internal val importFileExtensions: PointerBuffer = MemoryUtil.memAllocPointer(3).apply {
    put(MemoryUtil.memUTF8("*.obj"))
    put(MemoryUtil.memUTF8("*.tcn"))
    put(MemoryUtil.memUTF8("*.json"))
    flip()
}

private val exportExtensionsObj: PointerBuffer = MemoryUtil.memAllocPointer(1).apply {
    put(MemoryUtil.memUTF8("*.obj"))
    flip()
}

private val saveFileExtension: PointerBuffer = MemoryUtil.memAllocPointer(1).apply {
    put(MemoryUtil.memUTF8("*.pff"))
    flip()
}

private var lastSaveFile: String? = null

fun saveProject(projectManager: ProjectManager) {
    if (lastSaveFile == null) {
        saveProjectAs(projectManager)
    } else {
        saveProject(projectManager, projectManager.project)
    }
}

fun saveProjectAs(projectManager: ProjectManager) {
    val file = TinyFileDialogs.tinyfd_saveFileDialog("Save As", "", saveFileExtension, "Project File Format (*.pff)")
    if (file != null) {
        lastSaveFile = if (file.endsWith(".pff")) file else file + ".pff"
        saveProject(projectManager, projectManager.project)
    }
}

fun newProject(projectManager: ProjectManager) {
    val res = JOptionPane.showConfirmDialog(null,
            "Do you want to create a new project? \nAll unsaved changes will be lost!")
    if (res != JOptionPane.OK_OPTION) return
    projectManager.project = Project(projectManager.project.owner, "Unnamed")
    projectManager.modelEditor.selectionManager.clearSelection()
}

fun loadProject(projectManager: ProjectManager) {
    val res = JOptionPane.showConfirmDialog(null,
            "Do you want to load a new project? \nAll unsaved changes will be lost!")
    if (res != JOptionPane.OK_OPTION) return

    val file = TinyFileDialogs.tinyfd_openFileDialog("Load", "", saveFileExtension, "Project File Format (*.pff)",
            false)
    if (file != null) {
        lastSaveFile = file
        try {
            val project = projectManager.exportManager.loadProject(lastSaveFile!!)
            project.model = project.model.copy()
            projectManager.project = project
        } catch (e: Exception) {
            e.print()
        }
    }
}

private fun saveProject(projectManager: ProjectManager, project: Project) {
    projectManager.exportManager.saveProject(lastSaveFile!!, project)
}

fun showImportModelPopup(projectManager: ProjectManager) {
    ImportDialog.show { prop ->
        if (prop != null) {
            projectManager.exportManager.importModel(prop)
        }
    }
}

fun showExportModelPopup(projectManager: ProjectManager) {
    ExportDialog.show { (path, format) ->
        if (path != null) {
            projectManager.exportManager.exportModel(path, format!!)
        }
    }
}

fun Missing(thing: String) {
    JOptionPane.showMessageDialog(null, "Operation not implemented yet: $thing")
}

@Suppress("UNUSED_PARAMETER")
fun getExportFileExtensions(format: ExportFormat): PointerBuffer {
    return exportExtensionsObj
}

fun JDialog.center() {
    val toolkit = Toolkit.getDefaultToolkit()
    val x = (toolkit.screenSize.width - width) / 2
    val y = (toolkit.screenSize.height - height) / 2
    location = Point(x, y)
}