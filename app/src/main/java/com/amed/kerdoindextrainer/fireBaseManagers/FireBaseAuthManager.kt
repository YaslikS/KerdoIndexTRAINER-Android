package com.amed.kerdoindextrainer.fireBaseManagers

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.amed.kerdoindextrainer.model.SharedPreferencesManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class FireBaseAuthManager(context: Context) {

    private val TAG = "kiTRAINER.FBAM"
    private var fbAuth: FirebaseAuth? = null
    private var context = context
    var authWas = false
    private var sharedPreferencesManager: SharedPreferencesManager? = null

    var emailUser: String? = null

    init {
        fbAuth = FirebaseAuth.getInstance()
        sharedPreferencesManager = SharedPreferencesManager(context)
    }

    // состояние авторизации
    fun stateAuth(): Boolean{
        return if (fbAuth?.currentUser != null){     // авторизован
            Log.i(TAG, "stateAuth: entrance: mAuth?.currentUser != null")
            emailUser = fbAuth?.currentUser?.email!!
            true
        } else {                                    //  не авторизован
            Log.i(TAG, "stateAuth: entrance: mAuth?.currentUser == null")
            false
        }
    }

    // повторная авторизация
    fun reAuth(){
        Log.i(TAG, "reAuth: entrance")
        fbAuth?.currentUser?.reauthenticate(EmailAuthProvider.getCredential(
            sharedPreferencesManager?.getYourEmail()!!,
            sharedPreferencesManager?.getPassword()!!
        ))?.addOnSuccessListener {
            Log.i(TAG, "reAuth: User re-authenticated")
        }?.addOnFailureListener {
            Log.i(TAG, "reAuth: User NOT re-authenticated")
        }
    }

    // авторизация
    fun auth(email: String, pass: String, resultAuth: (Int) -> Unit) {
        Log.i(TAG, "auth: entrance")
        fbAuth!!.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG,
                    "auth: User auth Successful " + fbAuth?.currentUser?.uid
                )
                emailUser = fbAuth?.currentUser?.email!!
                sharedPreferencesManager?.saveIdUser(fbAuth?.currentUser?.uid!!)
                authWas = true
                login(email, pass, resultAuth)
            } else {
                Log.i(TAG, "auth: User auth failed")
                resultAuth(0)
            }
        }
        Log.i(TAG, "auth: exit")
    }

    // вход
    fun login(email: String, pass: String, resultAuth: (Int) -> Unit) {
        reAuth()
        Log.i(TAG, "login: entrance")
        fbAuth!!.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.i(TAG, "login: User login Successful " + fbAuth?.currentUser?.uid)
                sharedPreferencesManager?.saveIdUser(fbAuth?.currentUser?.uid!!)
                emailUser = fbAuth?.currentUser?.email!!
                resultAuth(1)
            } else {
                Log.i(TAG, "login: User login failed")
                auth(email, pass, resultAuth)
            }
        }
    }

    // подтверждение по почте
    private fun sendEmailVerf() {
        val user = fbAuth?.currentUser
        user?.sendEmailVerification()?.addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(context, "Проверьте ваш Email", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(context, "Send Email Failed", Toast.LENGTH_SHORT)
            }
        }
    }

    // выход из аккаунта
    fun logOut() {
        FirebaseAuth.getInstance().signOut()
        Log.i(TAG, "logOut: User logOut Successful " + fbAuth?.currentUser?.uid)
    }

    // удаление аккаунта
    fun deleteAccount(){
        Log.i(TAG, "deleteAccount: entrance " + fbAuth?.currentUser?.uid)
        //logOut()
        fbAuth?.currentUser?.delete()?.addOnSuccessListener {
            Log.i(TAG, "deleteAccount: User delete Successful " + fbAuth?.currentUser?.uid)
        }?.addOnFailureListener {
            Log.i(TAG, "deleteAccount: User delete failed " + fbAuth?.currentUser?.uid)
        }
    }

}