package com.cout970.modeler.model

interface IElementGroup : IElement {

    val elements: List<IElement>
    val name: String

    fun deepCopy(elements: List<IElement>): IElementGroup
}