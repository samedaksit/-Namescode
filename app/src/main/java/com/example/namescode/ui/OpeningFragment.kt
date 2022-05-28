package com.example.namescode.ui

import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.namescode.R
import com.example.namescode.databinding.FragmentOpeningBinding
import com.example.namescode.firebase.Constants
import com.example.namescode.firebase.FBUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class OpeningFragment : Fragment() {

    private var _binding: FragmentOpeningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOpeningBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mAuth = Firebase.auth
        if (mAuth.currentUser == null) {
            activity?.let {
                mAuth.signInAnonymously().addOnCompleteListener(it) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(activity, "Giriş yapıldı.", Toast.LENGTH_SHORT).show()
                        binding.playButton.visibility = View.VISIBLE
                        binding.progressButton.visibility = View.GONE
                    } else {
                        Toast.makeText(
                            activity,
                            "Bir hata ile karşılaşıldı. Lütfen hata bildirin.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.playButton.visibility = View.GONE
                        binding.progressButton.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            binding.playButton.visibility = View.VISIBLE
            binding.progressButton.visibility = View.GONE
        }

        binding.fragmentOpeningToolbar.title = getString(R.string.app_name)
        (activity as AppCompatActivity).setSupportActionBar(binding.fragmentOpeningToolbar)

        binding.playButton.setOnClickListener {
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentHolder, LoginFragment())
                .commitAllowingStateLoss()
        }
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
                val builder = AlertDialog.Builder(requireActivity())
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
                val builder = AlertDialog.Builder(requireActivity())
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


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

}