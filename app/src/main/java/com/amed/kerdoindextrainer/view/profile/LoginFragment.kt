package com.amed.kerdoindextrainer.view.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import com.amed.kerdoindextrainer.R
import com.amed.kerdoindextrainer.databinding.FragmentLoginBinding
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseAuthManager
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseCloudManager
import com.amed.kerdoindextrainer.fireBaseManagers.hasConnection
import com.amed.kerdoindextrainer.model.json.SharedPreferencesManager
import com.amed.kerdoindextrainer.model.sha256
import kotlinx.android.synthetic.main.fragment_registration.emailEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val TAG = "kerdoindex.LoginFragment"
    private var binding: FragmentLoginBinding? = null
    private var sharedPreferencesManager: SharedPreferencesManager? = null
    private var fireBaseAuthManager: FireBaseAuthManager? = null
    private var fireBaseCloudManager: FireBaseCloudManager? = null
    private var checkingReachability = CoroutineScope(Dispatchers.IO)

    var emailValid = false
    var passValid = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        sharedPreferencesManager = SharedPreferencesManager(requireActivity())
        fireBaseAuthManager = FireBaseAuthManager(requireActivity())
        fireBaseCloudManager = FireBaseCloudManager(requireActivity())

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }

    // слушатели кнопок
    private fun clickListeners() {
        Log.i(TAG, "clickListeners: entrance")
        // прослушивает кнопку логина
        binding?.loginButton?.setOnClickListener {
            Log.i(TAG, "buttonListeners: loginButton: entrance")
            if (passValid && emailValid) {
                Log.i(
                    TAG,
                    "buttonListeners: loginButton: emailValid && passValid && nameValid == true"
                )
                fireBaseAuthManager?.login(
                    binding?.emailEditText?.text.toString(),
                    binding?.passEditText?.text.toString()?.sha256()!!,
                    ::resultLogin
                )
                binding?.loginProgressBar?.visibility = ProgressBar.VISIBLE
            } else {
                Log.i(
                    TAG,
                    "buttonListeners: loginButton: emailValid && passValid && nameValid == false"
                )
                if (binding?.emailEditText?.text.toString() == "") {
                    binding?.emailEditText?.error = "Enter email"
                }
                if (binding?.passEditText?.text.toString() == "") {
                    binding?.passEditText?.error = "Enter password"
                }
            }
        }
        // прослушивает кнопку назад
        binding?.backTVInProfileFragment?.setOnClickListener {
            Log.i(TAG, "buttonListeners: backTVInProfileFragment: entrance")
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    // результат авторизации
    private fun resultLogin(state: Int, desc: String) {
        Log.i(TAG, "resultAuth: entrance")
        when (state) {
            0 -> {  //  удачный вход
                Log.i(TAG, "resultAuth: state = $state")
                //  проверка на тип пользователя
                fireBaseCloudManager?.getTypeUser(
                    emailEditText.text.toString(),
                    ::resultTypeUser
                )
            }

            1 -> {  //  НЕудачный вход
                Log.i(TAG, "resultAuth: state = $state")
                binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                AlertDialog.Builder(requireActivity())
                    .setTitle("Error: $desc")
                    .setPositiveButton("OK") { _, _ ->
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }

            2 -> {  //  неправильный пароль
                Log.i(TAG, "resultAuth: state = $state")
                binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                AlertDialog.Builder(requireActivity())
                    .setTitle("Incorrect password")
                    .setPositiveButton("OK") { _, _ ->
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }

            3 -> {  //  такого пользователя нет
                Log.i(TAG, "resultAuth: state = $state")
                binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                AlertDialog.Builder(requireActivity())
                    .setTitle("There is no such user")
                    .setPositiveButton("OK") { _, _ ->
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }

            4 -> {  //  проблема с интернетом
                Log.i(TAG, "resultAuth: state = $state")
                binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                AlertDialog.Builder(requireActivity())
                    .setTitle("Check your internet connection")
                    .setPositiveButton("OK") { _, _ ->
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }

            else -> {
                binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                Log.i(TAG, "resultAuth: state = $state")
            }
        }
    }

    // результат проверки на тип пользователя
    private fun resultTypeUser(state: Int, typeUser: String?) {
        Log.i(TAG, "resultTypeUser: entrance")
        when (state) {
            1 -> {  //  удачная проверка
                Log.i(TAG, "resultTypeUser: state = 1")
                if (typeUser == "t") {
                    Log.i(TAG, "resultTypeUser: entrance")
                    fireBaseCloudManager?.getCloudUserData()
                    sharedPreferencesManager?.saveYourEmail(binding?.emailEditText?.text.toString())
                    sharedPreferencesManager?.savePassword(
                        binding?.passEditText?.text.toString().sha256()
                    )
                    binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                    activity?.supportFragmentManager?.popBackStack()
                } else {
                    binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                    fireBaseAuthManager?.logOut()
                    sharedPreferencesManager?.deleteUserInfo()
                    AlertDialog.Builder(requireActivity())
                        .setTitle("You are already registered as a trainer")
                        .setPositiveButton("OK") { _, _ ->
                            Log.i(TAG, "resultAuth: AlertDialog: OK")
                        }.show()
                }
            }

            0 -> {
                Log.i(TAG, "resultTypeUser: state = 1")
                binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
                fireBaseAuthManager?.logOut()
                sharedPreferencesManager?.deleteUserInfo()
                AlertDialog.Builder(requireActivity())
                    .setTitle("Error in defining your role")
                    .setPositiveButton("OK") { _, _ ->
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }
        }
    }

    // слушатели EditTexts
    private fun checkEmailETs() {
        //  changed textfield yourEmailTF
        binding?.emailEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                binding?.emailEditText?.error = null
                Log.i(
                    TAG,
                    "checkEmailETs: yourEmailTF: sharedPreferencesManage!!.getPassword() = " + sharedPreferencesManager!!.getYourEmail()
                )
                emailETsError(binding?.emailEditText!!)
            }
        })
        //  changed textfield yourPassTF
        binding?.passEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                binding?.passEditText?.error = null
                Log.i(
                    TAG,
                    "checkEmailETs: yourPassTF: sharedPreferencesManage!!.getPassword() = " + sharedPreferencesManager!!.getPassword()
                )
                if (binding?.passEditText?.text?.isNotEmpty()!!
                    && binding?.passEditText?.text?.length!! >= 8
                    && !binding?.passEditText?.text?.contains(" ")!!
                ) {
                    Log.i(TAG, "checkEmailETs: yourPassTF: passValid = true")
                    passValid = true
                    binding?.passEditText?.error = null
                } else {
                    Log.i(TAG, "checkEmailETs: yourPassTF: passValid = false")
                    passValid = false
                    binding?.passEditText?.error =
                        "The password must be at least 8 characters and do not contain spaces"
                }
            }
        })
    }

    // если адрес неправильный, выводим ошибки
    private fun emailETsError(edt: EditText) {
        Log.i(TAG, "emailETsError: entrance")
        if (!isEmailValid(edt.text.toString())!!) { //  если почта невалидная
            Log.i(TAG, "emailETsError: email is not valid")
            edt.error = "Неправильный адрес"
            when (edt.id) {
                R.id.emailEditText -> {
                    emailValid = false
                    Log.i(TAG, "yourEmailTF: email is not valid")
                }
            }
        } else {                                    //  если почта валидная
            Log.i(TAG, "emailETsError: email is valid")
            when (edt.id) {
                R.id.emailEditText -> {
                    emailValid = true
                    Log.i(TAG, "yourEmailTF: email is valid")
                }
            }
        }
    }

    // проверяем адрес
    private fun isEmailValid(email: CharSequence?): Boolean? {
        Log.i(TAG, "isEmailValid: entrance")
        Log.i(TAG, "isEmailValid: email: $email")
        Log.i(
            TAG,
            "isEmailValid: result: ${email?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() }}"
        )
        return email?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() }
    }

    // настройка view
    private fun settingsViews() {
        Log.i(TAG, "settingsViews: entrance")
        binding?.emailEditText?.setText("")
        binding?.passEditText?.setText("")
        startCheckingReachability()
        checkEmailETs()     // запуск слушателей editText
        clickListeners()    // запуск слушателей нажатий
        binding?.loginProgressBar?.visibility = ProgressBar.INVISIBLE
    }

    override fun onResume() {
        super.onResume()
        settingsViews()
    }

    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView: entrance")
        binding = null
        checkingReachability.cancel()
        super.onDestroyView()
    }

    private fun startCheckingReachability() {
        Log.i(TAG, "startCheckingReachability: entrance")
        checkingReachability = CoroutineScope(Dispatchers.IO)
        checkingReachability?.launch(Dispatchers.IO) {
            Log.i(TAG, "startCheckingReachability: checkingReachability?.launch")
            while (true) {
                when (hasConnection(requireActivity())) {
                    true -> {
                        launch(Dispatchers.Main) {
                            binding?.loginButton?.isClickable = true
                        }
                    }

                    false -> {
                        launch(Dispatchers.Main) {
                            binding?.loginButton?.isClickable = false
                        }
                    }
                }
                delay(1000)
            }
        }
    }

}