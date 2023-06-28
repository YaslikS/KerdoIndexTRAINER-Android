package com.amed.kerdoindextrainer.view

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
import android.widget.TextView
import com.amed.kerdoindextrainer.R
import com.amed.kerdoindextrainer.databinding.FragmentAddBinding
import com.amed.kerdoindextrainer.databinding.FragmentMainBinding
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseCloudManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AddFragment : Fragment() {

    private var binding: FragmentAddBinding? = null
    private val TAG = "kiTRAINER.AddFragment"
    private var emailValid = false
    private var fireBaseCloudManager: FireBaseCloudManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentAddBinding.inflate(inflater, container, false)

        fireBaseCloudManager = FireBaseCloudManager(requireActivity())

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }

    private fun clickListeners() {
        binding?.addButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: addButton: entrance")
            fireBaseCloudManager?.saveSportsman(
                binding?.textInputEditText?.text.toString(),
                ::resultSaveSportsman
            )
        }
        binding?.backTVInProfileFragment?.setOnClickListener {
            Log.i(TAG, "clickListeners: backTVInProfileFragment: entrance")
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun resultSaveSportsman(state: Int) {
        when (state) {
            0 -> {  //  неудачное сохранение
                Log.i(TAG, "resultSaveSportsman: entrance")
                binding?.errorTV?.text = "Couldn't find such sportsman"
                binding?.errorTV?.visibility = TextView.VISIBLE
            }
            1 -> {  //  удачное сохранение
                Log.i(TAG, "resultSaveSportsman: entrance")
                binding?.successSavedTV?.visibility = TextView.VISIBLE
                binding?.errorTV?.visibility = TextView.INVISIBLE
                CoroutineScope(Dispatchers.Main).launch {
                    delay(2000)
                    binding?.successSavedTV?.visibility = TextView.INVISIBLE
                    activity?.supportFragmentManager?.popBackStack()
                }
            }
        }
    }

    // слушатели EditTexts
    private fun checkEmailETs() {
        //  changed textfield yourEmailTF
        binding?.textInputEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                Log.i(TAG, "checkEmailETs: textInputEditText")
                emailETsError(binding?.textInputEditText!!)
            }
        })
    }

    // если адрес неправильный, выводим ошибки
    private fun emailETsError(edt: EditText) {
        Log.i(TAG, "emailETsError: entrance")
        if (!isEmailValid(edt.text.toString())!!) { //  если почта невалидная
            Log.i(TAG, "emailETsError: email is not valid")
            binding?.errorTV?.text = "Wrong address"
            if (edt.text.toString() == "")
                binding?.errorTV?.visibility = TextView.INVISIBLE
            else
                binding?.errorTV?.visibility = TextView.VISIBLE
            binding?.addButton?.isClickable = false
            when (edt.id) {
                R.id.yourEmailTF -> {
                    emailValid = false
                    Log.i(TAG, "yourEmailTF: email is not valid")
                }
            }
        } else {                                    //  если почта валидная
            Log.i(TAG, "emailETsError: email is valid")
            binding?.errorTV?.visibility = TextView.INVISIBLE
            binding?.addButton?.isClickable = true
            when (edt.id) {
                R.id.yourEmailTF -> {
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

    override fun onResume() {
        super.onResume()
        clickListeners()
        checkEmailETs()
        binding?.errorTV?.visibility = TextView.INVISIBLE
        binding?.textInputEditText?.setText("")
        binding?.addButton?.isClickable = false
    }

    // после уничтожение экрана...
    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView: entrance")
        binding = null  //  ...уничтожаем объектов view-элементов
        super.onDestroyView()
    }
}