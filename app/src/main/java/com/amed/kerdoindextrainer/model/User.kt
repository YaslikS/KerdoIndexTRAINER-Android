package com.amed.kerdoindextrainer.model

data class User(
    val id: String? = "",
    val type: String? = "",         //  тип пользователя s - спортсмен / t - тренер
    val name: String? = "",
    val email: String? = "",
    val iconUrl: String? = "",
    val trainerId: String? = "",
    val lastDate: String? = "",     // дата последней синхронизации измерений
    val json: String? = "",         // json с измерениями спортсмена
    val settings: String? = "",     //  на будущее
    val f1: String? = "",       //  резервное поле
    val f2: String? = "",       //  резервное поле
    val f3: String? = "",       //  резервное поле
    val f4: String? = "",       //  резервное поле
    val f5: String? = "",       //  резервное поле
)
