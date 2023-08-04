package com.amed.kerdoindextrainer.model.json

data class Measure(
    val id: String? = "",       //  id
    val DAD: String? = "",      //  давление
    val Pulse: String? = "",    //  пульс
    val KerdoIndex: String? = "",   //  индекс
    val number: String? = "",   //  номер измерения
    val date: String? = "",     //  дата/время
    val desc: String? = "",     //  описание
    val f1: String? = "",       //  резервное поле
    val f2: String? = "",       //  резервное поле
    val f3: String? = "",       //  резервное поле
    val f4: String? = "",       //  резервное поле
    val f5: String? = "",       //  резервное поле
)
