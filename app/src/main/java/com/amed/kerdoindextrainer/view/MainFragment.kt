package com.amed.kerdoindextrainer.view

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import com.amed.kerdoindextrainer.R
import com.amed.kerdoindextrainer.adapters.RecyclerItemClickListener
import com.amed.kerdoindextrainer.adapters.SportsmanAdapter
import com.amed.kerdoindextrainer.databinding.FragmentMainBinding
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseAuthManager
import com.amed.kerdoindextrainer.fireBaseManagers.FireBaseCloudManager
import com.amed.kerdoindextrainer.fireBaseManagers.hasConnection
import com.amed.kerdoindextrainer.model.json.SharedPreferencesManager
import com.amed.kerdoindextrainer.model.json.User
import com.amed.kerdoindextrainer.view.profile.ProfileFragment
import kotlinx.coroutines.*

class MainFragment : Fragment() {

    private var binding: FragmentMainBinding? = null
    private val TAG = "kiTRAINER.MainFragment"
    private val addFragment = AddFragment()
    private val profileFragment = ProfileFragment()
    private val detailsFragment = DetailsFragment()
    private var sportsmanAdapter: SportsmanAdapter? = null
    private var sportsmanList: MutableList<User> = mutableListOf()
    private var checkingReachability = CoroutineScope(Dispatchers.IO)
    private var fireBaseCloudManager: FireBaseCloudManager? = null
    private var fireBaseAuthManager: FireBaseAuthManager? = null
    private var sharedPreferencesManager: SharedPreferencesManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(TAG, "onCreateView: entrance")
        binding = FragmentMainBinding.inflate(inflater, container, false)

        sharedPreferencesManager = SharedPreferencesManager(requireActivity())
        fireBaseCloudManager = FireBaseCloudManager(requireActivity())
        fireBaseAuthManager = FireBaseAuthManager(requireActivity())

        Log.i(TAG, "onCreateView: exit")
        return binding?.root
    }

    override fun onStart() {
        Log.i(TAG, "onStart: entrance")
        super.onStart()
        settingsViews()
        tryAuth()
        Log.i(TAG, "onStart: exit")
    }

    // результат получения
    private fun resultGettingSportsman(state: Int) {
        when (state) {
            0 -> {  //  неудачное получение
                Log.i(TAG, "resultGettingSportsman: entrance")
                binding?.baseIsEmptyCL?.visibility = ConstraintLayout.VISIBLE
            }
            1 -> {  //  удачное получение
                Log.i(TAG, "resultGettingSportsman: entrance")
                binding?.baseIsEmptyCL?.visibility = ConstraintLayout.INVISIBLE
                sportsmanAdapter?.setContentList(sportsmanList)
                if (sportsmanList.isEmpty())
                    binding?.noAuthCL?.visibility = ConstraintLayout.VISIBLE
                else
                    binding?.noAuthCL?.visibility = ConstraintLayout.INVISIBLE
            }
        }
    }

    // настройка view
    private fun settingsViews() {
        Log.i(TAG, "settingsViews: entrance")
        tryAuth()
        CoroutineScope(Dispatchers.IO).launch {
            fireBaseCloudManager?.getCloudData(::resultGettingSportsman)?.collect { sportsman ->
                Log.i(TAG, "settingsViews: CoroutineScope: $sportsman")
                sportsmanList.add(sportsman)
            }
        }
        clickListeners()

        sportsmanAdapter = SportsmanAdapter()
        sportsmanAdapter?.setContentList(sportsmanList)
        binding?.recyclerView?.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding?.recyclerView?.adapter = sportsmanAdapter
        Log.i(TAG, "settingsViews: exit")
    }

    // попытка авторизации
    private fun tryAuth() {
        Log.i(TAG, "tryAuth: entrance")
        fireBaseAuthManager?.reAuth(::reAuthResult)
        Log.i(TAG, "tryAuth: exit")
    }

    // результат ре-авторизации
    private fun reAuthResult(state: Int, desc: String) {
        Log.i(TAG, "reAuthResult: entrance")
        when (state) {
            0 -> {  //  удачный вход
                Log.i(TAG, "reAuthResult: state = $state")
                binding?.noAuthCL?.visibility = ConstraintLayout.INVISIBLE
                //fireBaseCloudManager?.getCloudData()
            }

            4 -> {  //  проблема с интернетом
                Log.i(TAG, "reAuthResult: state = $state")
                AlertDialog.Builder(requireActivity())
                    .setTitle("Check your internet connection")
                    .setPositiveButton("OK") { _, _ ->
                        //binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                        Log.i(TAG, "reAuthResult: AlertDialog: OK")
                    }.show()
            }

            else -> {
                Log.i(TAG, "reAuthResult: state = $state")
                AlertDialog.Builder(requireActivity())
                    .setTitle("Unexpected login error. Try login manually")
                    .setPositiveButton("OK") { _, _ ->
                        //binding?.progressCL?.visibility = ConstraintLayout.INVISIBLE
                        Log.i(TAG, "reAuthResult: AlertDialog: OK")
                    }.show()
                binding?.hatCL?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.redGraph
                    )
                )
                binding?.noAuthCL?.visibility = ConstraintLayout.VISIBLE
                binding?.appTitle?.text = "You not logged in!"
                CoroutineScope(Dispatchers.IO).launch {
                    delay(3000)
                    binding?.hatCL?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.backgroundLinear
                        )
                    )
                    delay(6000)
                    launch(Dispatchers.Main) { binding?.appTitle?.text = "KerdoIndexSPORT" }
                }
            }
        }
    }

    // обработка результата авторизации
    private fun resultAuth(state: Int) {
        when (state) {
            0 -> {  //  неудачная авторизация
                Log.i(TAG, "resultAuth: entrance: state = $state")
                binding?.hatCL?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.redGraph
                    )
                )
                binding?.noAuthCL?.visibility = ConstraintLayout.VISIBLE
                binding?.appTitle?.text = "You not logged in!"
                CoroutineScope(Dispatchers.IO).launch {
                    delay(3000)
                    binding?.hatCL?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireActivity(),
                            R.color.backgroundLinear
                        )
                    )
                    delay(6000)
                    launch(Dispatchers.Main) { binding?.appTitle?.text = "KerdoIndexSPORT" }
                }
            }
            1 -> {  //  удачная авторизация
                Log.i(TAG, "resultAuth: entrance: state = $state")
                binding?.noAuthCL?.visibility = ConstraintLayout.INVISIBLE
            }
        }
    }

    private fun clickListeners() {
        binding!!.recyclerView.addOnItemTouchListener(
            RecyclerItemClickListener(
                binding!!.recyclerView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        Log.i(TAG, "clickListeners: RecyclerItemClickListener")
                        detailsFragment.idSportsman = sportsmanList[position].id.toString()
                        detailsFragment.nameSportsman = sportsmanList[position].name.toString()
                        openFragment(detailsFragment)
                    }
                })
        )
        binding?.addSportsmanIB?.setOnClickListener {
            Log.i(TAG, "clickListeners: addSportsmanIB")
            openFragment(addFragment)
        }
        binding?.GoToProfileButton?.setOnClickListener {
            Log.i(TAG, "clickListeners: GoToProfileButton")
            openFragment(profileFragment)
        }
    }

    private fun openFragment(twoFrag: Fragment) {
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager!!.beginTransaction()
        fragmentTransaction.apply {
            replace(R.id.container, twoFrag)
            addToBackStack(null)
            commit()
        }
    }

    // состояние интернета
    // наблюдение за ним
    private fun startCheckingReachability() {
        Log.i(TAG, "startCheckingReachability: entrance")
        checkingReachability = CoroutineScope(Dispatchers.IO)
        checkingReachability?.launch(Dispatchers.IO) {
            while (true) {
                when (hasConnection(requireActivity())) {
                    true -> {
                        //Log.i(TAG, "startCheckingReachability: true")
                        launch(Dispatchers.Main) {
                            binding?.offlineModeButton?.visibility = Button.INVISIBLE
                            binding?.addSportsmanIB?.isEnabled = true
                        }
                    }
                    false -> {
                        //Log.i(TAG, "startCheckingReachability: false")
                        launch(Dispatchers.Main) {
                            binding?.offlineModeButton?.visibility = Button.VISIBLE
                            binding?.addSportsmanIB?.isEnabled = false
                        }
                    }
                }
                delay(500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        startCheckingReachability()
    }

    override fun onPause() {
        super.onPause()
        sportsmanList.clear()
        checkingReachability.cancel()
    }

    // после уничтожение экрана...
    override fun onDestroyView() {
        Log.i(TAG, "onDestroyView: entrance")
        binding = null  //  ...уничтожаем объектов view-элементов
        super.onDestroyView()
    }
}