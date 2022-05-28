package com.example.namescode.ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.namescode.R
import com.example.namescode.databinding.FragmentRoomSelectBinding
import com.example.namescode.extensions.disposeIfNeed
import com.example.namescode.firebase.Constants
import com.example.namescode.firebase.FBRoom
import com.example.namescode.firebase.FBUser
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.collections.ArrayList

class RoomSelectFragment : Fragment() {
    private var _binding: FragmentRoomSelectBinding? = null
    private val binding get() = _binding!!

    private val roomList = ArrayList<String>()
    private val filteredRoomList = ArrayList<String>()
    private lateinit var sharedPrefsPlayer: SharedPreferences
    private lateinit var sharedPrefsInfoBoxes: SharedPreferences

    private var allRoomDisposable: Disposable? = null
    private var joinRoomDisposable: Disposable? = null
    private var roomAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activity
                        ?.supportFragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.fragmentHolder, LoginFragment())
                        ?.commitAllowingStateLoss()
                }
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoomSelectBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fragmentRoomSelectToolbar.title = getString(R.string.title_select_game_room)
        (activity as AppCompatActivity).setSupportActionBar(binding.fragmentRoomSelectToolbar)
        sharedPrefsPlayer = requireActivity().getSharedPreferences("player", Context.MODE_PRIVATE)
        sharedPrefsInfoBoxes =
            requireActivity().getSharedPreferences("infoBox", Context.MODE_PRIVATE)
        val dialogShow = sharedPrefsInfoBoxes.getBoolean("roomSelect", true)
        if (dialogShow){
            val dialog = layoutInflater.inflate(R.layout.info_box_room_select, null)
            val doNotShow = dialog.findViewById<CheckBox>(R.id.roomSelectCB)
            val okButton = dialog.findViewById<Button>(R.id.okButtonRS)
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            builder.setTitle(getString(R.string.info_box_title))
            builder.setView(dialog)
            val creator = builder.create()
            okButton.setOnClickListener { creator.dismiss() }
            doNotShow.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPrefsInfoBoxes.edit().putBoolean("roomSelect", false).apply()
                } else {
                    sharedPrefsInfoBoxes.edit().putBoolean("roomSelect", true).apply()
                }
            }
            creator.show()
        }

        FBRoom.getRooms()
        observeRoomList()
        listItemClick()

        binding.searchRoomListSV.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filteredRoomList.clear()
                    for (room in roomList) {
                        if (newText.let { room.contains(it) }) {
                            filteredRoomList.add(room)
                        }
                    }
                    val filteredRoomAdapter = ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_list_item_1,
                        filteredRoomList
                    )
                    binding.roomsListView.adapter = filteredRoomAdapter
                }
                return false
            }
        })
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


    private fun observeRoomList() {
        allRoomDisposable = FBRoom.observeRoomListSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                roomList.clear()
                roomList.addAll(it)
                roomAdapter =
                    ArrayAdapter(
                        requireActivity(),
                        android.R.layout.simple_list_item_1,
                        roomList
                    )

                binding.roomsListView.adapter = roomAdapter
            }, {})

        binding.createRoomButton.setOnClickListener {
            createRoomAlertDialog()
        }
    }

    private fun listItemClick() {
        binding.roomsListView.setOnItemClickListener { _, _, position, _ ->
            if (filteredRoomList.isNotEmpty()) {
                val filteredId = filteredRoomList[position]
                FBRoom.getRoomPassword(filteredId)
                joinToARoom(filteredId.toInt())
            } else {
                val roomId = roomList[position]
                FBRoom.getRoomPassword(roomId)
                joinToARoom(roomId.toInt())
            }
        }
    }

    private fun joinToARoom(position: Int) {
        val playerId = sharedPrefsPlayer.getInt("player_id", 999999)
        val playerName = sharedPrefsPlayer.getString("player_name", "").toString()
        val roomId = roomList[position]
        joinToARoomAlertDialog(roomId, playerId, playerName)
    }

    private fun joinToARoomAlertDialog(roomId: String, playerId: Int, playerName: String) {
        val joinRoomDialog = layoutInflater.inflate(R.layout.dialog_view_join_room, null)
        val passwordET = joinRoomDialog.findViewById<TextView>(R.id.enterPasswordET)
        val loginButton = joinRoomDialog.findViewById<Button>(R.id.dialogLoginButton)
        val cancelButton = joinRoomDialog.findViewById<Button>(R.id.dialogCancelJoinButton)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.enter_room_title, roomId))
        builder.setView(joinRoomDialog)
        val dialog = builder.create()
        loginButton.setOnClickListener {
            val password = passwordET.text.toString()
            joinRoomDisposable = FBRoom.observeRoomPasswordSubject()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    joinRoomDisposable.disposeIfNeed()
                    if (it == password) {
                        Constants.roomId = roomId
                        FBRoom.createRoom(
                            false,
                            roomId,
                            it,
                            playerId.toString(),
                            playerName
                        )
                        transaction()
                        passwordET.text = ""
                    } else if (it != password) {
                        passwordET.text = ""
                        Toast.makeText(
                            activity,
                            getString(R.string.wrong_password),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }, {})
            dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun createRoomAlertDialog() {
        val createRoomDialog = layoutInflater.inflate(R.layout.dialog_view_create_room, null)
        val roomNameET = createRoomDialog.findViewById<EditText>(R.id.roomNameET)
        val passwordET = createRoomDialog.findViewById<EditText>(R.id.passwordET)
        val playerId = sharedPrefsPlayer.getInt("player_id", 999999)
        val playerName = sharedPrefsPlayer.getString("player_name", "")
        val createButton = createRoomDialog.findViewById<Button>(R.id.dialogCreateRoomButton)
        val cancelButton = createRoomDialog.findViewById<Button>(R.id.dialogCancelButton)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(getString(R.string.create_game_room))
        builder.setView(createRoomDialog)
        val dialog = builder.create()

        createButton.setOnClickListener {
            val roomName = roomNameET.text.toString()
            val password = passwordET.text.toString()
            Constants.roomId = roomList.size.toString()
            if (password != "") {
                FBRoom.createRoom(
                    true,
                    roomName,
                    password,
                    playerId.toString(),
                    playerName.toString()
                )
                transaction()
            } else {
                Toast.makeText(
                    activity,
                    getString(R.string.please_enter_password),
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun transaction() {
        activity
            ?.supportFragmentManager
            ?.beginTransaction()
            ?.replace(R.id.fragmentHolder, GameFragment())
            ?.commitAllowingStateLoss()
    }

    override fun onDestroyView() {
        FBRoom.onDestroyOnRoomSelect()
        allRoomDisposable.disposeIfNeed()
        joinRoomDisposable.disposeIfNeed()
        _binding = null
        super.onDestroyView()
    }

}