package com.amed.kerdoindextrainer.view.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.amed.kerdoindextrainer.R
import com.amed.kerdoindextrainer.databinding.FragmentProfileBinding
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseAuthManager
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseCloudManager
import com.amed.kerdoindextrainer.fireBaseManagers.hasConnection
import com.amed.kerdoindextrainer.model.json.SharedPreferencesManager
import com.amed.kerdoindextrainer.model.sha256
import com.amed.kerdoindextrainer.view.AboutFragment
import kotlinx.coroutines.*


class ProfileFragment : Fragment() {

    private val TAG = "kerdoindex.ProfileSport"
    private var binding: FragmentProfileBinding? = null
    private var sharedPreferencesManager: SharedPreferencesManager? = null
    private var fireBaseAuthManager: FireBaseAuthManager? = null
    private var fireBaseCloudManager: FireBaseCloudManager? = null
    private var checkingReachability = CoroutineScope(Dispatchers.IO)
    private val aboutFragment = AboutFragment()
    private val registrationFragment = RegistrationFragment()
    private val loginFragment = LoginFragment()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        sharedPreferencesManager = SharedPreferencesManager(requireActivity())
        fireBaseAuthManager = FireBaseAuthManager(requireActivity())
        fireBaseCloudManager = FireBaseCloudManager(requireActivity())

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }

    // слушатели кнопок
    private fun clickListeners() {
        Log.i(TAG, "clickListeners: entrance")
        // прослушивает кнопку регистрации
        binding?.registrationButton?.setOnClickListener {
            Log.i(TAG, "buttonListeners: registrationButton: entrance")
            openFragment(registrationFragment)
        }
        // прослушивает кнопку логина
        binding?.loginButton?.setOnClickListener {
            Log.i(TAG, "buttonListeners: loginButton: entrance")
            openFragment(loginFragment)
        }
        // прослушивает кнопку выхода
        binding?.logoutButton?.setOnClickListener {
            Log.i(TAG, "buttonListeners: logoutButton: entrance")
            logoutAction()
        }
        // прослушивает кнопку экрана about
        binding?.aboutButton?.setOnClickListener {
            Log.i(TAG, "buttonListeners: aboutButton: entrance")
            openFragment(aboutFragment)
        }
        // прослушивает кнопку изменения имени
        binding?.renameButton?.setOnClickListener {
            Log.i(TAG, "buttonListeners: renameButton: entrance")
            sharedPreferencesManager?.saveYourName(binding?.yourNameTF?.text.toString())
            fireBaseCloudManager?.updateNameInCloudData()
            Log.i(TAG, "buttonListeners: renameButton: exit")
        }
        // прослушивает кнопку назад
        binding?.backTVInProfileFragment?.setOnClickListener {
            Log.i(TAG, "buttonListeners: backTVInProfileFragment: entrance")
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    // действия при нажатии кнопки выхода
    private fun logoutAction() {
        Log.i(TAG, "logoutAction: entrance")
        AlertDialog.Builder(requireActivity())
            .setTitle("Log out of your account")
            .setMessage("Are you sure you want logout?")
            .setNegativeButton("Logout") { _, _ ->
                Log.i(TAG, "loginAction: logout: entrance")
                fireBaseAuthManager?.logOut()
                sharedPreferencesManager?.deleteUserInfo()
                updateViewsViews()
            }.setNeutralButton("Delete Account") { _, _ ->
                Log.i(TAG, "loginAction: delete Account: entrance")
                fireBaseCloudManager?.deleteInCloudData()
                fireBaseAuthManager?.deleteAccount(:: resultLogin)
            }
            .setPositiveButton("Cancel", null).show()
    }

    // результат удаления
    private fun resultLogin(state: Int, desc: String) {
        Log.i(TAG, "resultLogin: entrance")
        when (state) {
            0 -> {  //  удачное удаление
                Log.i(TAG, "resultLogin: state = $state")
                sharedPreferencesManager?.deleteUserInfo()
                updateViewsViews()
            }
            1 -> {  //  НЕудачное удаление
                Log.i(TAG, "resultLogin: state = $state")
                AlertDialog.Builder(requireActivity())
                    .setTitle("Error when deleting a user")
                    .setPositiveButton("OK") { _, _ ->
                        //binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                        Log.i(TAG, "resultAuth: AlertDialog: OK")
                    }.show()
            }
            else -> {   //  НЕудачное удаление
                Log.i(TAG, "resultLogin: state = $state")
            }
        }
    }

    // действия при успешном входе
    private fun authTrueAction() {
        Log.i(TAG, "authTrueAction: entrance")
        binding?.loginMainTV?.text = "You loggined as " + sharedPreferencesManager?.getYourEmail()
        binding?.yourNameTF?.setText(sharedPreferencesManager?.getYourName())
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            launch(Dispatchers.Main) {
                binding?.yourNameTF?.setText(sharedPreferencesManager?.getYourName())
            }
        }
        binding?.authInfoTextView?.setText("Click on the red button to logout")
        binding?.logoutButton?.visibility = Button.VISIBLE
        binding?.logoutButton?.layoutParams?.height = 150
        binding?.regisLoginConstraintLayout?.visibility = ConstraintLayout.INVISIBLE
        binding?.regisLoginConstraintLayout?.layoutParams?.height = 1
        binding?.nameConstraintLayout?.visibility = ConstraintLayout.VISIBLE
        binding?.nameConstraintLayout?.layoutParams?.height = 300
        Log.i(TAG, "authTrueAction: exit")
    }

    // действия при НЕ успешном входе
    private fun authFalseAction() {
        Log.i(TAG, "authFalseAction: entrance")
        binding?.loginMainTV?.text = "Log in or register in the system"
        binding?.yourNameTF?.setText("")
        binding?.authInfoTextView?.setText("Log in if you already have an account, or register")
        binding?.logoutButton?.visibility = Button.INVISIBLE
        binding?.logoutButton?.layoutParams?.height = 1
        binding?.regisLoginConstraintLayout?.visibility = ConstraintLayout.VISIBLE
        binding?.regisLoginConstraintLayout?.layoutParams?.height = 150
        binding?.nameConstraintLayout?.visibility = ConstraintLayout.INVISIBLE
        binding?.nameConstraintLayout?.layoutParams?.height = 1
        Log.i(TAG, "authFalseAction: exit")
    }

    // обновление view
    private fun updateViewsViews() {
        Log.i(TAG, "updateViewsViews: entrance")
        if (fireBaseAuthManager?.stateAuth() == true) {
            Log.i(TAG, "settingsViews: stateAuth = true")
            authTrueAction()
        } else {
            Log.i(TAG, "settingsViews: stateAuth = false")
            authFalseAction()
        }
        Log.i(TAG, "updateViewsViews: exit")
    }

    // настройка view
    private fun settingsViews() {
        Log.i(TAG, "settingsViews: entrance")
        binding?.yourNameTF?.setText("")
        startCheckingReachability()
        clickListeners()    // запуск слушателей нажатий

        binding?.logoutButton?.visibility = Button.INVISIBLE
        binding?.regisLoginConstraintLayout?.visibility = ConstraintLayout.INVISIBLE
        binding?.nameConstraintLayout?.visibility = ConstraintLayout.INVISIBLE

        Log.i(TAG, "settingsViews: exit")
    }

    // работа с фрагментами
    private fun openFragment(fragment: Fragment) {
        Log.i(TAG, "openFragment: entrance: $fragment")
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        //fragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_in_right)
        fragmentTransaction.replace(R.id.container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    override fun onResume() {
        super.onResume()
        settingsViews()
        updateViewsViews()
    }

    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView: entrance")
        checkingReachability.cancel()
        binding = null
        super.onDestroyView()
    }

    // состояние интернета
    // наблюдение за ним
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
                            binding?.logoutButton?.isClickable = true
                            binding?.registrationButton?.isClickable = true
                        }
                    }

                    false -> {
                        launch(Dispatchers.Main) {
                            binding?.loginButton?.isClickable = false
                            binding?.logoutButton?.isClickable = false
                            binding?.registrationButton?.isClickable = false
                        }
                    }
                }
                delay(1000)
            }
        }
    }
}