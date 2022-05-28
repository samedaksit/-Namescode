package com.example.namescode.ui

import android.animation.AnimatorSet
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.namescode.R
import com.example.namescode.databinding.FragmentGameBinding
import com.example.namescode.firebase.*
import com.example.namescode.model.Card
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList
import android.animation.ObjectAnimator
import android.widget.CheckBox
import androidx.cardview.widget.CardView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class GameFragment : Fragment(), CardsAdapter.OnItemClickListener {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPrefsPlayer: SharedPreferences
    private lateinit var sharedPrefsInfoBoxes: SharedPreferences
    private lateinit var playerName: String
    private lateinit var playerId: String

    private val cardList = ArrayList<Card>()
    private val wordNumberList = ArrayList<Int>(listOf(1, 2, 3, 4, 5, 6, 7, 8, 9))

    private lateinit var playerRole: String
    private val redNar = "RED_NARRATOR"
    private val blueNar = "BLUE_NARRATOR"
    private val redSpy = "RED_SPY"
    private val blueSpy = "BLUE_SPY"
    private val noOne = "NO_ONE"

    private val redAdapter = LogAdapter()
    private val blueAdapter = LogAdapter()
    private var cardsAdapter = CardsAdapter(cardList, false, this)

    private val compositeDisposable = CompositeDisposable()
    private val compositeDisposable2 = CompositeDisposable()

    private var mInterstitialAd: InterstitialAd? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAd()
        fullScreenCallback()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(activity)
                        .setTitle(getString(R.string.check_if_want_to_exit))
                        .setPositiveButton(getString(R.string.log_out)) { _, _ ->
                            mInterstitialAd?.show(requireActivity())

                            activity
                                ?.supportFragmentManager
                                ?.beginTransaction()
                                ?.replace(R.id.fragmentHolder, RoomSelectFragment())
                                ?.commitAllowingStateLoss()
                        }
                        .setNegativeButton(getString(R.string.stay)) { _, _ -> }
                        .create().show()
                }
            })

        FBRoom.destroyPasswordEventListener()
        setSharedPrefs()
        compositeDisposable.clear()
        setToolBar()
        myEnter()
        gameDialog()

        playerRole = noOne

        setLogAdapters()
        setButtonClicks()
        setSpinners()
        endTurnButtonClicks()
        setIfPlayerRoleNoOne()

        getRoomName()
        observeIfFirstTime()
        getChosenCardList()
        getGivenClue()
        getRoles()
        FBCard.getClickedCardList()
        FBCard.getBlackCardNumber()
        FBCard.getRedCardNumber()
        FBCard.getBlueCardNumber()

        FBPlayer.deleteYourRole(playerId)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        menu.findItem(R.id.item_report_error).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.findItem(R.id.item_advice_word).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.findItem(R.id.item_invite_friend_info).isVisible = true
        menu.findItem(R.id.item_reset_game).isVisible = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_report_error -> {
                val reportError = layoutInflater.inflate(R.layout.dialog_view_report_error, null)
                val reportErrorET = reportError.findViewById<EditText>(R.id.reportErrorET)
                val reportButton = reportError.findViewById<Button>(R.id.dialogReportErrorButton)
                val builder = AlertDialog.Builder(activity)
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
            R.id.item_invite_friend_info -> {
                val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
                builder.setMessage(getString(R.string.invite_friend_info))
                builder.create().show()
                return true
            }
            R.id.item_advice_word -> {
                val suggestWordLayout =
                    layoutInflater.inflate(R.layout.dialog_view_advice_word, null)
                val suggestedWordET = suggestWordLayout.findViewById<EditText>(R.id.suggestedWordET)
                val suggestButton = suggestWordLayout.findViewById<Button>(R.id.dialogSuggestButton)
                val builder = AlertDialog.Builder(activity)
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
            R.id.item_reset_game -> {
                val resetGameLayout = layoutInflater.inflate(R.layout.dialog_view_reset_game, null)
                val resetGameButton = resetGameLayout.findViewById<Button>(R.id.resetButton)
                val cancelButton = resetGameLayout.findViewById<Button>(R.id.cancelButton)
                val builder = AlertDialog.Builder(activity)
                builder.setTitle(getString(R.string.reset_game))
                builder.setView(resetGameLayout)
                val dialog = builder.create()
                resetGameButton.setOnClickListener {
                    FBLog.deleteGameLogs()
                    FBPlayer.deleteRoles()
                    FBTurn.setTurn("RESET_GAME")
                    FBClue.deleteClues()
                    FBCard.deleteClickedCardList()
                    FBCard.deleteChosenCardList()
                    FBCard.getFromAllCards()
                    FBCard.observeGetAllCardsSubject()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ list ->
                            FBCard.mixPickAndAddCards(list)
                            compositeDisposable2.clear()
                        }, {}).also { disposable ->
                            compositeDisposable2.add(disposable)
                        }
                    FBCard.setBlackCardNumber(1.toString())
                    FBCard.setBlueCardNumber(8.toString())
                    FBCard.setRedCardNumber(9.toString())
                    FBTurn.setTurn("RED_NARRATOR")
                    binding.redTeamCardNumber.visibility = View.GONE
                    binding.blueTeamCardNumber.visibility = View.GONE
                    dialog.dismiss()
                }
                cancelButton.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    ////// adMob
    private fun setAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            requireContext(),
            Constants.adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    mInterstitialAd = null
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                }
            }
        )
    }

    private fun fullScreenCallback() {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
            }

            override fun onAdShowedFullScreenContent() {
                mInterstitialAd = null
            }
        }
    }

    ///////setAutoCloseKeyboardWithEnter
    private fun myEnter() {
        binding.blueGiveClueET.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                hideKeyboard()
                return@setOnKeyListener true
            }
            false
        }
        binding.redGiveClueET.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                hideKeyboard()
                return@setOnKeyListener true
            }
            false
        }
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus
        if (view != null) {
            val hideMe =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            hideMe.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    ///////setThings
    private fun setToolBar() {
        binding.fragmentGameToolbar.title = ""
        (activity as AppCompatActivity).setSupportActionBar(binding.fragmentGameToolbar)
    }

    private fun setLogAdapters() {
        binding.redLogRV.adapter = redAdapter
        binding.blueLogRV.adapter = blueAdapter
    }

    private fun setSharedPrefs() {
        sharedPrefsPlayer = requireActivity().getSharedPreferences("player", Context.MODE_PRIVATE)
        sharedPrefsInfoBoxes =
            requireActivity().getSharedPreferences("infoBox", Context.MODE_PRIVATE)
        playerName = sharedPrefsPlayer.getString("player_name", "").toString()
        playerId = sharedPrefsPlayer.getInt("player_id", 9999999).toString()
    }

    private fun setSpinners() {
        val spinnerAdapter = setSpinnerAdapter()
        binding.redWordNumberSpinner.adapter = spinnerAdapter
        binding.blueWordNumberSpinner.adapter = spinnerAdapter
    }

    private fun setSpinnerAdapter(): ArrayAdapter<Int> {
        val spinnerAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_list_item_1,
            wordNumberList
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return spinnerAdapter
    }

    private fun setIfPlayerRoleNoOne() {
        if (playerRole == noOne) {
            binding.infoText.text = getString(R.string.choose_role_to_join_game)
        }
    }

    //////alertDialog
    private fun gameDialog() {
        val showGameDialog = sharedPrefsInfoBoxes.getBoolean("gameDialog", true)
        val gameDialog = layoutInflater.inflate(R.layout.info_box_game, null)
        val gameCB = gameDialog.findViewById<CheckBox>(R.id.gameCB)
        val okButton = gameDialog.findViewById<Button>(R.id.oKButtonGame)
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
        if (showGameDialog) {
            builder.setTitle(getString(R.string.info_box_title))
            builder.setView(gameDialog)
            gameCB.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPrefsInfoBoxes.edit().putBoolean("gameDialog", false).apply()
                } else {
                    sharedPrefsInfoBoxes.edit().putBoolean("gameDialog", true).apply()
                }
            }
            val creator = builder.create()
            okButton.setOnClickListener {
                creator.dismiss()
            }
            creator.show()
        }
    }

    /////// buttonClickActions
    private fun setButtonClicks() {
        setPlayerType(binding.joinBlueNarratorButton, blueNar)
        setPlayerType(binding.joinBlueSpyButton, blueSpy)
        setPlayerType(binding.joinRedNarratorButton, redNar)
        setPlayerType(binding.joinRedSpyButton, redSpy)
    }

    private fun endTurnButtonClicks() {
        blueGiveClue()
        redGiveClue()
        blueEndGuessing()
        redEndGuessing()
    }

    private fun redEndGuessing() {
        binding.redEndGuessingButton.setOnClickListener {
            FBTurn.setTurn(blueNar)
            FBLog.addToGameLogRed(getString(R.string.guessing_ended, playerName))
        }
    }

    private fun blueEndGuessing() {
        binding.blueEndGuessingButton.setOnClickListener {
            FBTurn.setTurn(redNar)
            FBLog.addToGameLogBlue(getString(R.string.guessing_ended, playerName))
        }
    }

    private fun blueGiveClue() {
        binding.blueGiveClueButton.setOnClickListener {
            val clue = binding.blueGiveClueET.text.toString()
            val wordNumber = binding.blueWordNumberSpinner.selectedItem
            FBClue.giveClue("$clue #$wordNumber", "blue")
            FBTurn.setTurn(blueSpy)
            FBLog.addToGameLogBlue(
                getString(R.string.clue_given, playerName, clue, wordNumber)
            )
            binding.blueGiveClueET.text.clear()
        }
    }

    private fun redGiveClue() {
        binding.redGiveClueButton.setOnClickListener {
            val clue = binding.redGiveClueET.text.toString()
            val wordNumber = binding.redWordNumberSpinner.selectedItem
            FBClue.giveClue("$clue #$wordNumber", "red")
            FBTurn.setTurn(redSpy)
            FBLog.addToGameLogRed(
                getString(R.string.clue_given, playerName, clue, wordNumber)
            )
            binding.redGiveClueET.text.clear()
        }
    }

    ///////getThings
    private fun getRoomName() {
        FBRoom.getRoomName()
        FBRoom.observeRoomNameSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.fragmentGameToolbar.title = "$it #${Constants.roomId}"
            }, {}
            ).also { compositeDisposable.add(it) }
    }

    private fun observeIfFirstTime() {
        FBRoom.checkIfFirstTime()
        FBRoom.observeIsFirstTimeSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it == "true") {
                    FBCard.getFromAllCards()
                    FBCard.observeGetAllCardsSubject()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ list ->

                            FBCard.mixPickAndAddCards(list)
                            compositeDisposable2.clear()
                        }, {}).also { disposable ->
                            compositeDisposable2.add(disposable)
                        }

                    FBRoom.setIfFirstTime("false")
                    FBRoom.checkIfFirstTime()
                }
            }, {}).also { compositeDisposable.add(it) }
    }

    private fun getChosenCardList() {
        FBCard.getChosenCardList()
        FBCard.observeChosenCardListSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                cardList.clear()
                cardList.addAll(it)
                cardsAdapter = CardsAdapter(it, false, this)
                binding.cardsRecyclerView.adapter = cardsAdapter
                cardsAdapter.notifyDataSetChanged()
            }, {}).also { compositeDisposable.add(it) }
    }

    private fun getGivenClue() {
        FBClue.getRedTeamLastClue()
        FBClue.getBlueTeamLastClue()
        FBClue.observeGivenClueSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.color == Color.RED) {
                    binding.redGivenClueText.text = it.givenClue
                } else if (it.color == Color.BLUE) {
                    binding.blueGivenClueText.text = it.givenClue
                }
            }, {}
            ).also { compositeDisposable.add(it) }
    }

    private fun getRoles() {
        FBPlayer.getRedNarratorNameList()
        FBPlayer.getRedSpyNameList()
        FBPlayer.getBlueSpyNameList()
        FBPlayer.getBlueNarratorNameList()

        FBPlayer.observeRolesSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                when (it.role) {
                    "redNarrator" -> {
                        binding.redNarratorTV.text = ""
                        it.nameList.forEach { name ->
                            binding.redNarratorTV.text =
                                getString(
                                    R.string.red_narrator_names,
                                    binding.redNarratorTV.text,
                                    name
                                )
                        }
                    }
                    "redSpy" -> {
                        binding.redSpyTV.text = ""
                        it.nameList.forEach { name ->
                            binding.redSpyTV.text =
                                getString(R.string.red_spy_names, binding.redSpyTV.text, name)
                        }
                    }
                    "blueSpy" -> {
                        binding.blueSpyTV.text = ""
                        it.nameList.forEach { name ->
                            binding.blueSpyTV.text =
                                getString(R.string.blue_spy_names, binding.blueSpyTV.text, name)
                        }
                    }
                    "blueNarrator" -> {
                        binding.blueNarratorTV.text = ""
                        it.nameList.forEach { name ->
                            binding.blueNarratorTV.text =
                                getString(
                                    R.string.blue_narrator_names,
                                    binding.blueNarratorTV.text,
                                    name
                                )
                        }
                    }
                }
            }, {}).also { compositeDisposable.add(it) }
    }

    ///////setPlayerType functions
    private fun setPlayerType(button: Button, playerRole: String) {
        button.setOnClickListener {
            this.playerRole = playerRole
            FBRoom.setPlayerType(playerRole, playerId, playerName)
            setButtonsGone()
            listenTurn()
            if (playerRole == redNar || playerRole == blueNar) {
                narratorDialog()
            } else if (playerRole == redSpy || playerRole == blueSpy) {
                spyDialog()
            }
            binding.blueTeamCardNumber.visibility = View.VISIBLE
            binding.redTeamCardNumber.visibility = View.VISIBLE
            observeClickedCardList()
            observeRemainingCardNumberSubject()
            observeGameLogs()
        }
    }

    private fun observeClickedCardList() {
        FBCard.observeClickedCardListSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.forEach { position ->
                    cardList[position].cardShowColor = true
                }
                if (playerRole == redNar || playerRole == blueNar) {
                    cardsAdapter = CardsAdapter(cardList, true, this)
                    binding.cardsRecyclerView.adapter = cardsAdapter
                    cardsAdapter.notifyItemRangeChanged(0, 25)
                } else if (playerRole == redSpy || playerRole == blueSpy) {
                    cardsAdapter = CardsAdapter(cardList, false, this)
                    binding.cardsRecyclerView.adapter = cardsAdapter
                    cardsAdapter.notifyItemRangeChanged(0, 25)
                }
            }, {}).also { compositeDisposable.add(it) }
    }

    private fun observeRemainingCardNumberSubject() {
        FBCard.observeRemainingBlackCardNumberSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    if (it == 0) {
                        cardList.forEach { card ->
                            card.cardClickable = false
                        }
                        cardsAdapter = CardsAdapter(cardList, true, this)
                        binding.cardsRecyclerView.adapter = cardsAdapter
                        cardsAdapter.notifyItemRangeChanged(0, 25)
                    }
                }, {}).also { compositeDisposable.add(it) }

        FBCard.observeRemainingRedCardNumberSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val number = it.toString()
                    binding.redTeamCardNumber.text = number
                    FBCard.redCardRemNumber = number
                    if (it == 0) {
                        cardList.forEach { card ->
                            card.cardClickable = false
                        }
                        cardsAdapter = CardsAdapter(cardList, true, this)
                        binding.cardsRecyclerView.adapter = cardsAdapter
                        cardsAdapter.notifyItemRangeChanged(0, 25)
                        FBTurn.setTurn("RedTeamWon")
                    }
                }, {}).also { compositeDisposable.add(it) }

        FBCard.observeRemainingBlueCardNumberSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    val number = it.toString()
                    binding.blueTeamCardNumber.text = number
                    FBCard.blueCardRemNumber = number
                    if (it == 0) {
                        cardList.forEach { card ->
                            card.cardClickable = false
                        }
                        cardsAdapter = CardsAdapter(cardList, true, this)
                        binding.cardsRecyclerView.adapter = cardsAdapter
                        cardsAdapter.notifyItemRangeChanged(0, 25)
                        FBTurn.setTurn("BlueTeamWon")
                    }
                }, {}).also { compositeDisposable.add(it) }

    }

    private fun observeGameLogs() {
        FBLog.getGameLogRed()
        FBLog.observeGameLogRedSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.redLogRV.scrollToPosition(it.size - 1)
                redAdapter.setData(it)
            }, {}
            ).also { compositeDisposable.add(it) }

        FBLog.getGameLogBlue()
        FBLog.observeGameLogBlueSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.blueLogRV.scrollToPosition(it.size - 1)
                blueAdapter.setData(it)
            }, {}
            ).also { compositeDisposable.add(it) }
    }

    private fun setButtonsGone() {
        binding.joinBlueNarratorButton.visibility = View.INVISIBLE
        binding.joinBlueSpyButton.visibility = View.INVISIBLE
        binding.joinRedNarratorButton.visibility = View.INVISIBLE
        binding.joinRedSpyButton.visibility = View.INVISIBLE
    }

    private fun narratorDialog() {
        val narratorDialog = layoutInflater.inflate(R.layout.info_box_narrator, null)
        val narratorCB = narratorDialog.findViewById<CheckBox>(R.id.narratorCB)
        val okButton = narratorDialog.findViewById<Button>(R.id.okButtonNarrator)
        val showDialog = sharedPrefsInfoBoxes.getBoolean("narratorDialog", true)
        if (showDialog) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            builder.setTitle(getString(R.string.info_box_title))
            builder.setView(narratorDialog)
            val creator = builder.create()
            narratorCB.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPrefsInfoBoxes.edit().putBoolean("narratorDialog", false).apply()
                } else {
                    sharedPrefsInfoBoxes.edit().putBoolean("narratorDialog", true).apply()
                }
            }
            okButton.setOnClickListener {
                creator.dismiss()
            }
            creator.show()
        }
    }

    private fun spyDialog() {
        val spyDialog = layoutInflater.inflate(R.layout.info_box_spy, null)
        val spyCB = spyDialog.findViewById<CheckBox>(R.id.spyCB)
        val okButton = spyDialog.findViewById<Button>(R.id.okButtonSpy)
        val showDialog = sharedPrefsInfoBoxes.getBoolean("spyDialog", true)
        if (showDialog) {
            val builder = androidx.appcompat.app.AlertDialog.Builder(requireActivity())
            builder.setTitle(getString(R.string.info_box_title))
            builder.setView(spyDialog)
            val creator = builder.create()
            spyCB.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    sharedPrefsInfoBoxes.edit().putBoolean("spyDialog", false).apply()
                } else {
                    sharedPrefsInfoBoxes.edit().putBoolean("spyDialog", true).apply()
                }
            }
            okButton.setOnClickListener {
                creator.dismiss()
            }
            creator.show()
        }
    }

    ///////listenTurn functions
    private fun listenTurn() {
        FBTurn.getTurn()
        FBTurn.observeTurnSubject()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                setInfoTextByTurn(it)
                setCardsClickability(it)
                setGiveClueLayoutVisibility(it)
                setGuessingVisibility(it)
                if (it == "RESET_GAME") {
                    setButtonsVisible()
                    playerRole = noOne
                }
            }, {}).also { compositeDisposable.add(it) }
    }

    private fun setInfoTextByTurn(getTurn: String) {
        when (getTurn) {
            redNar -> {
                binding.infoText.text = context?.getString(R.string.turn_red_narrator)
            }
            blueNar -> {
                binding.infoText.text = context?.getString(R.string.turn_blue_narrator)
            }
            redSpy -> {
                binding.infoText.text = context?.getString(R.string.turn_red_spy)
            }
            blueSpy -> {
                binding.infoText.text = context?.getString(R.string.turn_blue_spy)
            }
            "finishedBlue" -> {
                binding.infoText.text = context?.getString(R.string.blue_team_won)
            }
            "finishedRed" -> {
                binding.infoText.text = context?.getString(R.string.red_team_won)
            }
            "BlueTeamWon" -> {
                binding.infoText.text = context?.getString(R.string.blue_team_won)
            }
            "RedTeamWon" -> {
                binding.infoText.text = context?.getString(R.string.red_team_won)
            }
            "RESET_GAME" -> {
                binding.infoText.text = context?.getString(R.string.choose_role_to_join_game)
            }
        }
    }

    private fun setCardsClickability(getTurn: String) {
        if (getTurn == playerRole && playerRole == redNar) {
            setCardsNotClickable()
        } else if (getTurn == playerRole && playerRole == blueNar) {
            setCardsNotClickable()
        } else if (getTurn == playerRole && playerRole == redSpy) {
            setCardsClickable()
        } else if (getTurn == playerRole && playerRole == blueSpy) {
            setCardsClickable()
        } else {
            setCardsNotClickable()
        }
    }

    private fun setCardsClickable() {
        cardList.forEach {
            it.cardClickable = true
        }
    }

    private fun setCardsNotClickable() {
        cardList.forEach {
            it.cardClickable = false
        }
    }

    private fun setGiveClueLayoutVisibility(getTurn: String) {
        if (getTurn == playerRole && playerRole == redNar) {
            binding.blueTeamGiveClueLayout.visibility = View.GONE
            binding.redTeamGiveClueLayout.visibility = View.VISIBLE
        } else if (getTurn == playerRole && playerRole == blueNar) {
            binding.redTeamGiveClueLayout.visibility = View.GONE
            binding.blueTeamGiveClueLayout.visibility = View.VISIBLE
        } else {
            binding.redTeamGiveClueLayout.visibility = View.GONE
            binding.blueTeamGiveClueLayout.visibility = View.GONE
        }
    }

    private fun setGuessingVisibility(getTurn: String) {
        if (getTurn == playerRole && playerRole == redSpy) {
            binding.blueTeamGuessingLayout.visibility = View.GONE
            binding.redTeamGuessingLayout.visibility = View.VISIBLE
            binding.redEndGuessingButton.visibility = View.VISIBLE
            binding.redGivenClueText.visibility = View.VISIBLE
        } else if (getTurn == playerRole && playerRole == blueSpy) {
            binding.redTeamGuessingLayout.visibility = View.GONE
            binding.blueTeamGuessingLayout.visibility = View.VISIBLE
            binding.blueEndGuessingButton.visibility = View.VISIBLE
            binding.blueGivenClueText.visibility = View.VISIBLE
        } else if (getTurn == blueSpy && (playerRole == redSpy || playerRole == redNar || playerRole == blueNar)) {
            binding.blueTeamGuessingLayout.visibility = View.VISIBLE
            binding.redTeamGuessingLayout.visibility = View.GONE
            binding.blueEndGuessingButton.visibility = View.GONE
            binding.blueGivenClueText.visibility = View.VISIBLE
        } else if (getTurn == redSpy && (playerRole == blueSpy || playerRole == blueNar || playerRole == redNar)) {
            binding.blueTeamGuessingLayout.visibility = View.GONE
            binding.redTeamGuessingLayout.visibility = View.VISIBLE
            binding.redEndGuessingButton.visibility = View.GONE
            binding.redGivenClueText.visibility = View.VISIBLE
        } else {
            binding.blueTeamGuessingLayout.visibility = View.GONE
            binding.redTeamGuessingLayout.visibility = View.GONE
            binding.blueGivenClueText.visibility = View.GONE
            binding.redGivenClueText.visibility = View.GONE
            binding.redEndGuessingButton.visibility = View.GONE
            binding.blueEndGuessingButton.visibility = View.GONE
        }
    }

    private fun setButtonsVisible() {
        binding.joinBlueNarratorButton.visibility = View.VISIBLE
        binding.joinBlueSpyButton.visibility = View.VISIBLE
        binding.joinRedNarratorButton.visibility = View.VISIBLE
        binding.joinRedSpyButton.visibility = View.VISIBLE
    }

    ////recyclerViewItemClicks
    override fun onItemClick(position: Int, cardView: View, imageHolder: CardView) {
        if (cardList[position].cardClickable == true && cardList[position].cardShowColor == false) {
            FBCard.addClickedCardPosition(position, cardList)

            when (binding.infoText.text) {
                getString(R.string.turn_red_spy) -> {
                    when (cardList[position].cardColor) {
                        Color.RED -> {
                            FBLog.addToGameLogRed(
                                getString(
                                    R.string.player_clicked_red_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                        }
                        Color.BLUE -> {
                            FBLog.addToGameLogRed(
                                getString(
                                    R.string.player_clicked_blue_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                            FBTurn.setTurn(blueNar)
                        }
                        Color.WHITE -> {
                            FBLog.addToGameLogRed(
                                getString(
                                    R.string.player_clicked_white_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                            FBTurn.setTurn(blueNar)
                        }
                        Color.BLACK -> {
                            FBLog.addToGameLogRed(
                                getString(
                                    R.string.player_clicked_black_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                            FBTurn.setTurn("finishedBlue")
                        }
                    }
                }
                getString(R.string.turn_blue_spy) -> {
                    when (cardList[position].cardColor) {
                        Color.BLUE -> {
                            FBLog.addToGameLogBlue(
                                getString(
                                    R.string.player_clicked_blue_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                        }
                        Color.RED -> {
                            FBLog.addToGameLogBlue(
                                getString(
                                    R.string.player_clicked_red_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                            FBTurn.setTurn(redNar)
                        }
                        Color.WHITE -> {
                            FBLog.addToGameLogBlue(
                                getString(
                                    R.string.player_clicked_white_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                            FBTurn.setTurn(redNar)
                        }
                        Color.BLACK -> {
                            FBLog.addToGameLogBlue(
                                getString(
                                    R.string.player_clicked_black_card,
                                    playerName,
                                    cardList[position].cardBasic?.cardWord
                                )
                            )
                            FBTurn.setTurn("finishedRed")
                        }
                    }
                }
            }
        } else if (cardList[position].cardShowColor == true && cardList[position].isScaled == false) {
            scaleAnimation(imageHolder, 0.5F)
            cardList[position].isScaled = true
        } else if (cardList[position].cardShowColor == true && cardList[position].isScaled == true) {
            scaleAnimation(imageHolder, 1F)
            cardList[position].isScaled = false
        }
    }

    private fun scaleAnimation(imageHolder: CardView, value: Float) {
        val scaleDownY = ObjectAnimator.ofFloat(imageHolder, "scaleY", value)
        scaleDownY.duration = 100
        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownY)
        scaleDownY.addUpdateListener {
            imageHolder.invalidate()
            imageHolder.pivotX = 0F
            imageHolder.pivotY = 0F
        }
        scaleDown.start()
    }

    ///onDestroy
    override fun onDestroyView() {
        if (compositeDisposable.isDisposed.not()) {
            compositeDisposable.dispose()
            compositeDisposable.clear()
        }
        FBLog.onDestroy()
        FBClue.onDestroy()
        FBTurn.onDestroy()
        FBPlayer.onDestroy()
        FBRoom.onDestroyOnGame()
        FBPlayer.deleteYourRole(playerId)
        FBCard.onDestroy()
        FBRoom.removePlayerFromRoom(playerId)
        _binding = null
        super.onDestroyView()
    }
}