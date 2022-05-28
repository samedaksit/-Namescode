package com.example.namescode.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.namescode.R
import com.example.namescode.databinding.FragmentLoginBinding
import com.example.namescode.firebase.Constants
import com.example.namescode.firebase.FBPlayer
import com.example.namescode.firebase.FBUser
import com.example.namescode.model.Player
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val mAuth = Firebase.auth

    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity
                        ?.supportFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.fragmentHolder, OpeningFragment())
                        ?.commitAllowingStateLoss()
                }
            })

        sharedPrefs = requireActivity().getSharedPreferences("player", Context.MODE_PRIVATE)
        binding.fragmentLoginToolbar.title = getString(R.string.title_login)
        (activity as AppCompatActivity).setSupportActionBar(binding.fragmentLoginToolbar)

        setEditText()
        loginButtonClick()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_report_error -> {
                val reportError = layoutInflater.inflate(R.layout.dialog_view_report_error, null)
                val reportErrorET = reportError.findViewById<EditText>(R.id.reportErrorET)
                val reportButton = reportError.findViewById<Button>(R.id.dialogReportErrorButton)
                val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                builder.setTitle(getString(R.string.report_error))
                builder.setView(reportError)
                val dialog = builder.create()
                reportButton.setOnClickListener {
                    val errorText = reportErrorET.text
                    if (errorText != null && errorText.toString() != "") {
                        FBUser.reportError(errorText.toString(), Constants.playerId)
                    }
                    dialog.dismiss()
                }
                dialog.show()
                return true
            }
            R.id.item_advice_word -> {
                val suggestWordLayout =
                    layoutInflater.inflate(R.layout.dialog_view_advice_word, null)
                val suggestedWordET = suggestWordLayout.findViewById<EditText>(R.id.suggestedWordET)
                val suggestButton = suggestWordLayout.findViewById<Button>(R.id.dialogSuggestButton)
                val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                builder.setTitle(getString(R.string.advice_word))
                builder.setView(suggestWordLayout)
                val dialog = builder.create()
                suggestButton.setOnClickListener {
                    val word = suggestedWordET.text
                    if (word != null && word.toString() != "") {
                        FBUser.suggestWord(word.toString(), Constants.playerId)
                    }

                    dialog.dismiss()
                }
                dialog.show()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun loginButtonClick() {
        val createdId = sharedPrefs.getInt("player_id", 9999999)
        val playerUID = mAuth.uid
        binding.loginButton.setOnClickListener {
            val playerName = binding.playerNameET.text.toString()
            if (binding.playerNameET.text.isNotEmpty()) {
                if (createdId == 9999999) {
                    FBPlayer.getNewPlayerId()
                    FBPlayer.observeNewPlayerIdSubject()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (playerUID != null) {
                                FBPlayer.createPlayer(Player(it, playerName), playerUID)
                            }
                            sharedPrefs.edit().putInt("player_id", it.toString().toInt()).apply()
                        }, {})
                } else {
                    if (playerUID != null) {
                        FBPlayer.createPlayer(Player(createdId.toString(), playerName), playerUID)
                    }
                }
                sharedPrefs.edit().putString("player_name", playerName).apply()
                transaction()
            } else {
                activity?.run {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_enter_username),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun transaction() {
        activity
            ?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragmentHolder, RoomSelectFragment())
            ?.commitAllowingStateLoss()
    }

    private fun setEditText() {
        val oldName = sharedPrefs.getString("player_name", "")
        binding.playerNameET.setText(oldName)
    }

    override fun onDestroyView() {
        FBPlayer.onDestroyLogin()
        _binding = null
        super.onDestroyView()
    }
}