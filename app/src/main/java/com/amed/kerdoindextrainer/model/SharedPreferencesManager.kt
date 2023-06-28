package com.amed.kerdoindextrainer.model

import android.content.Context
import android.content.SharedPreferences
import android.util.Log


class SharedPreferencesManager(context: Context) {

    private val settings: SharedPreferences = context.getSharedPreferences("kiTRAINER", 0);
    private val prefEditor: SharedPreferences.Editor = settings?.edit()!!
    private val TAG = "kiTRAINER.SharedPref"

    // SAVE
    // id пользователя
    fun saveIdUser(idUser: String) {
        prefEditor.putString("idUser", idUser)
        prefEditor.apply()
        Log.i(TAG, "saveYourName: sharedPref!!.getYourName() = " + getIdUser())
    }

    // имя пользователя
    fun saveYourName(name: String) {
        prefEditor.putString("yourName", name)
        prefEditor.apply()
        Log.i(TAG, "saveYourName: sharedPref!!.getYourName() = " + getYourName())
    }

    // email пользователя
    fun saveYourEmail(emailAddress: String) {
        prefEditor.putString("emailAddress", emailAddress)
        prefEditor.apply()
    }

    // пароль к аккаунту
    fun savePassword(password: String) {
        // TODO: сделать хешеирование
        prefEditor.putString("password", password)
        prefEditor.apply()
        Log.i(TAG, "savePassword: sharedPref!!.getPassword() = " + getPassword())
    }

    // ссылка на аватарку пользователя
    fun saveYourImageURL(yourImageURL: String) {
        prefEditor.putString("yourImageURL", yourImageURL)
        prefEditor.apply()
    }

    // GET
    fun getIdUser(): String {
        return settings.getString("idUser", "0")!!
    }

    // имя пользователя
    fun getYourName(): String {
        return settings.getString("yourName", "0")!!
    }

    // email пользователя
    fun getYourEmail(): String {
        return settings?.getString("emailAddress", "0")!!
    }

    // пароль к аккаунту
    fun getPassword(): String {
        return settings.getString("password", "0")!!
    }

    // ссылка на аватарку пользователя
    fun getYourImageURL(): String {
        return settings?.getString("yourImageURL", "")!!
    }

    // массив в формате json с измерениями
    fun getJson(): String {
        return settings?.getString("json", "empty")!!
    }

    // дата последней синхронизации измерений
    fun getLastDate(): String {
        return settings?.getString("lastDate","")!!
    }


    // MANAGE
    // стереть все настройки
    fun clearAll() {
        prefEditor.clear()
    }


}