package com.amed.kerdoindextrainer.model.json

data class JsonStructure(
    var name1: String = "measures1",
    var measures1: MutableList<Measure> = mutableListOf(),
    var name2: String = "measures2",
    var measures2: MutableList<Measure> = mutableListOf(),
)