package com.amed.kerdoindextrainer.model.json

import android.util.Log
import com.google.gson.Gson

class MeasureJsonManager {

    private val TAG = "kiTRAINER.MJM"

    //  парсинг json
    fun parcingJson(json: String) : JsonStructure {
        Log.i(TAG, "parcingJson: entrance")
        val js = Gson().fromJson(json, JsonStructure::class.java)
        Log.i(TAG, "parcingJson: parced json = { js.measures1" + js.measures1 + " js.measures2 " + js.measures2 + " }")
        return js
    }

    //  создание json
    fun createJson(kerdo1Mas: MutableList<Measure>, kerdo2Mas: MutableList<Measure>) : String {
        Log.i(TAG, "createJson: entrance")
        var js = JsonStructure("measures1", kerdo1Mas, "measures2", kerdo2Mas)
        var json = Gson().toJson(js)
        Log.i(TAG, "createJson: created json = { $json }")
        return json
    }

}

