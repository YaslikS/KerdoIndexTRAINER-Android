package com.amed.kerdoindextrainer.fireBaseManagers

import android.content.Context
import android.util.Log
import com.amed.kerdoindextrainer.model.json.SharedPreferencesManager
import com.amed.kerdoindextrainer.model.json.User
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class FireBaseCloudManager(context: Context) {

    private val TAG = "kiTRAINER.FBCM"
    private val db = Firebase.firestore
    private var sharedPreferencesManager: SharedPreferencesManager? = null

    init {
        sharedPreferencesManager = SharedPreferencesManager(context)
    }

    // создание пользователя
    fun addUserInCloudData() {
        Log.i(TAG, "addUserInCloudData: entrance")
        var user = User(
            sharedPreferencesManager?.getIdUser(),
            "t",
            name = sharedPreferencesManager?.getYourName(),
            email = sharedPreferencesManager?.getYourEmail(),
            sharedPreferencesManager?.getYourImageURL(),
            "",
            sharedPreferencesManager?.getLastDate(),
            "",
            "", "", "", "", "", ""
        )
        Log.i(
            TAG,
            "addUserInCloudData: user.id = " + user.id + " user.name = " + user.name + " user.email " + user.email
        )

        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .set(user)
            .addOnSuccessListener { documentReference ->
                Log.i(
                    TAG,
                    "addUserInCloudData: DocumentSnapshot added with ID: ${documentReference}"
                )
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "addUserInCloudData: Error add user.", exception)
            }
    }

    // обновление имени
    fun updateNameInCloudData() {
        Log.i(
            TAG,
            "updateNameInCloudData: userId = " + sharedPreferencesManager?.getIdUser()!!
                    + " nameUser = " + sharedPreferencesManager?.getYourName()!!
        )
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .update("name", sharedPreferencesManager?.getYourName())
            .addOnSuccessListener { documentReference ->
                Log.d(TAG,
                    "updateNameInCloudData: DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG,
                    "updateNameInCloudData: Error editing updating Name info.", exception)
            }
    }

    // обновление url иконки
    fun updateUrlIconInCloudData() {
        Log.i(
            TAG,
            "updateUrlIconInCloudData: userId = " + sharedPreferencesManager?.getIdUser()!!
                    + " iconUrlUser = { " + sharedPreferencesManager?.getYourImageURL()!! + " }"
        )
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .update("iconUrl", sharedPreferencesManager?.getYourImageURL())
            .addOnSuccessListener { documentReference ->
                Log.d(TAG,
                    "updateUrlIconInCloudData: DocumentSnapshot added with ID: ${documentReference}")
            }
            .addOnFailureListener { exception ->
                Log.w(TAG,
                    "updateUrlIconInCloudData: Error editing user info.", exception)
            }
    }

    // удаление пользователя
    fun deleteInCloudData() {
        Log.i(TAG, "deleteInCloudData: entrance")
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .delete()
            .addOnSuccessListener { documentReference ->
                Log.d(
                    TAG,
                    "deleteInCloudData: DocumentSnapshot added with ID: ${documentReference}"
                )
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "deleteInCloudData: Error delete documents.", exception)
            }
    }

    // прикрепление тренера за спорстменом
    fun saveSportsman(sportsman: String, resultSaveSportsman: (Int) -> Unit) {
        Log.i(TAG, "saveSportsman: entrance")
        var gettedSportsman: QueryDocumentSnapshot? = null
        db.collection("users")
            .whereEqualTo("email", sportsman)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    gettedSportsman = document
                }
                if (gettedSportsman != null) {
                    Log.d(TAG, "${gettedSportsman!!.id} => ${gettedSportsman!!.data}")

                    db.collection("users")
                        .document(gettedSportsman!!.id)
                        .update("trainerId", sharedPreferencesManager?.getIdUser())
                        .addOnSuccessListener { documentReference ->
                            Log.d(
                                TAG, "saveSportsman: DocumentSnapshot added with ID: ${documentReference}"
                            )
                            resultSaveSportsman(1)
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG, "saveSportsman: Error saving sportsman info.", exception)
                            resultSaveSportsman(0)
                        }
                } else {
                    Log.d(TAG, "saveSportsman: Error getting documents")
                    resultSaveSportsman(0)
                }
            }
    }

    // открепление спортсмена от тренера
    fun deleteSportsman(id: String, resultDeleteSportsman: (Int) -> Unit) {
        db.collection("users")
            .document(id)
            .update("trainerId", "")
            .addOnSuccessListener { documentReference ->
                Log.d(
                    TAG,
                    "deleteSportsman: DocumentSnapshot added with ID: ${documentReference}"
                )
                resultDeleteSportsman(1)
            }
            .addOnFailureListener { exception ->
                Log.w(
                    TAG,
                    "deleteSportsman: Error deleting user info.", exception
                )
                resultDeleteSportsman(0)
            }
    }

    // получение данных пользователя
    fun getCloudUserData(){
        Log.i(TAG, "getCloudUserData: entrance")
        db.collection("users")
            .document(sharedPreferencesManager?.getIdUser()!!)
            .get()
            .addOnSuccessListener { document ->
                val name = document.get("name") as String
                Log.i(TAG, "getCloudUserData: name user = $name")
                sharedPreferencesManager?.saveYourName(name)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "getCloudUserData: Error getting documents.", exception)
            }
    }

    // получение списка пользователей
    suspend fun getCloudData(resultSaveSportsman: (Int) -> Unit) = callbackFlow<User> {
        Log.i(TAG, "getCloudData: entrance")
        db.collection("users")
            .whereEqualTo("trainerId", sharedPreferencesManager?.getIdUser())
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d(TAG, "${document!!.id} => ${document!!.data}")
                    val sportsman = User(
                        id = document.id,
                        email = document.get("email").toString(),
                        name = document.get("name").toString(),
                    )
                    trySend(sportsman)
                }
                Log.i(TAG, "getCloudData: documents.isEmpty = " + documents.isEmpty)
                if (documents.isEmpty)
                    resultSaveSportsman(0)
                else
                    resultSaveSportsman(1)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "getCloudData: Error getting documents.", exception)
            }
        awaitClose {} // suspends the current coroutine until the channel
        // is either closed or canceled and invokes the given block before resuming the coroutine.
    }

    // получение данных спортсмена
    suspend fun getSportsmanData(id: String, resultSaveSportsman: (Int) -> Unit) =
        callbackFlow<User> {
            Log.i(TAG, "getSportsmanData: entrance: id = $id")
            db.collection("users")
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    Log.i(TAG, "getSportsmanData: getted document = " + document.get("lastDate"))
                    val sportsman = User(
                        id = document.id,
                        email = document.get("email").toString(),
                        name = document.get("name").toString(),
                        json = document.get("json").toString(),
                        lastDate = document.get("lastDate").toString(),
                    )
                    trySend(sportsman)
                    resultSaveSportsman(1)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "getSportsmanData: Error getting documents.", exception)
                    resultSaveSportsman(0)
                }
            awaitClose {} // suspends the current coroutine until the channel
            // is either closed or canceled and invokes the given block before resuming the coroutine.
        }

    // получение типа пользователя
    fun getTypeUser(email: String, resultGetTypeUser: (Int, String?) -> Unit){
        Log.i(TAG, "getTypeUser: entrance")
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                Log.i(TAG, "getTypeUser: type user = " + documents.documents[0].get("type"))
                val typeStr = documents.documents[0].get("type") as String
                resultGetTypeUser(1, typeStr)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "getTypeUser: Error getting documents.", exception)
                resultGetTypeUser(0, null)
            }
    }
}