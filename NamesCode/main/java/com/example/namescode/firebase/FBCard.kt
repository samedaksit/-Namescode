package com.example.namescode.firebase

import android.graphics.Color
import com.example.namescode.model.Card
import com.example.namescode.model.CardBasic
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.subjects.BehaviorSubject

object FBCard {
    private val database = Firebase.database("https://namescode-b798f-default-rtdb.firebaseio.com/")
    private val refAllCards = database.reference.child("allCards")
    private val refRoom = database.reference.child("rooms")

    var blueCardRemNumber = ""
    var redCardRemNumber = ""

    private val allCardsSubject = BehaviorSubject.create<ArrayList<CardBasic>>()

    fun observeGetAllCardsSubject(): BehaviorSubject<ArrayList<CardBasic>> = allCardsSubject

    private val allCardsEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            allCardsSubject.onNext(arrayListOf<CardBasic>().apply {
                clear()
                for (s in snapshot.children) {
                    val card = s.getValue(CardBasic::class.java)
                    if (card != null) {
                        add(card)
                    }
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getFromAllCards() {
        refAllCards.addValueEventListener(allCardsEventListener)
    }

    fun mixPickAndAddCards(cardList: ArrayList<CardBasic>) {
        val chosenCardList = arrayListOf<Card>()
        cardList.shuffle()
        for (i in 0..24) {
            when {
                i < 8 -> {
                    chosenCardList.add(Card(cardList[i], Color.BLUE))
                }
                i < 17 -> {
                    chosenCardList.add(Card(cardList[i], Color.RED))
                }
                i < 18 -> {
                    chosenCardList.add(Card(cardList[i], Color.BLACK))
                }

                else -> {
                    chosenCardList.add(Card(cardList[i], Color.WHITE))
                }
            }
        }
        chosenCardList.shuffle()
        chosenCardList.forEach {
            refRoom.child(Constants.roomId).child("chosenCards").push().setValue(it)
        }
    }


    private val chosenCardListSubject = BehaviorSubject.create<ArrayList<Card>>()

    fun observeChosenCardListSubject(): BehaviorSubject<ArrayList<Card>> = chosenCardListSubject

    private val chosenCardListEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            chosenCardListSubject.onNext(arrayListOf<Card>().apply {
                for (s in snapshot.children) {
                    val card = s.getValue(Card::class.java)
                    if (card != null) {
                        add(card)
                    }
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getChosenCardList() {
        refRoom.child(Constants.roomId).child("chosenCards")
            .addValueEventListener(chosenCardListEventListener)
    }

    fun deleteChosenCardList(){
        refRoom.child(Constants.roomId).child("chosenCards").removeValue()
    }

    fun addClickedCardPosition(position: Int, cardList: ArrayList<Card>) {
        refRoom.child(Constants.roomId).child("clickedCards").push().setValue(position)
        when (cardList[position].cardColor) {
            Color.RED -> {
                setRedCardNumber(redCardRemNumber.toInt().minus(1).toString())
            }
            Color.BLUE -> {
                setBlueCardNumber(blueCardRemNumber.toInt().minus(1).toString())
            }
            Color.BLACK -> {
                setBlackCardNumber("0")
            }
        }
    }

    fun deleteClickedCardList(){
        refRoom.child(Constants.roomId).child("clickedCards").removeValue()
    }

    private val clickedCardListSubject = BehaviorSubject.create<ArrayList<Int>>()

    fun observeClickedCardListSubject(): BehaviorSubject<ArrayList<Int>> = clickedCardListSubject

    private val clickedCardListEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            clickedCardListSubject.onNext(arrayListOf<Int>().apply {
                for (s in snapshot.children) {
                    val cardPosition = s.value.toString().toInt()
                    add(cardPosition)
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getClickedCardList() {
        refRoom.child(Constants.roomId).child("clickedCards")
            .addValueEventListener(clickedCardListEventListener)
    }

    fun setBlueCardNumber(number: String) {
        refRoom.child(Constants.roomId).child("remainingBlueCardNumber").setValue(number)
    }

    fun setRedCardNumber(number: String) {
        refRoom.child(Constants.roomId).child("remainingRedCardNumber").setValue(number)
    }

    fun setBlackCardNumber(number: String) {
        refRoom.child(Constants.roomId).child("remainingBlackCardNumber").setValue(number)
    }

    private val remainingBlackCardNumberSubject = BehaviorSubject.create<Int>()

    fun observeRemainingBlackCardNumberSubject(): BehaviorSubject<Int> =
        remainingBlackCardNumberSubject


    private val blackCardNumberEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val blackCardNumber = snapshot.getValue(String()::class.java).toString().toInt()
            remainingBlackCardNumberSubject.onNext(blackCardNumber)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getBlackCardNumber() {
        refRoom.child(Constants.roomId).child("remainingBlackCardNumber")
            .addValueEventListener(blackCardNumberEventListener)
    }

    private val remainingBlueCardNumberSubject = BehaviorSubject.create<Int>()

    fun observeRemainingBlueCardNumberSubject(): BehaviorSubject<Int> =
        remainingBlueCardNumberSubject

    private val blueCardNumberEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val blueCardNumber = snapshot.getValue(String()::class.java).toString().toInt()
            remainingBlueCardNumberSubject.onNext(blueCardNumber)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getBlueCardNumber() {
        refRoom.child(Constants.roomId).child("remainingBlueCardNumber")
            .addValueEventListener(blueCardNumberEventListener)
    }

    private val remainingRedCardNumberSubject = BehaviorSubject.create<Int>()

    fun observeRemainingRedCardNumberSubject(): BehaviorSubject<Int> =
        remainingRedCardNumberSubject

    private val redCardNumberEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val redCardNumber = snapshot.getValue(String()::class.java).toString().toInt()
            remainingRedCardNumberSubject.onNext(redCardNumber)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getRedCardNumber() {
        refRoom.child(Constants.roomId).child("remainingRedCardNumber")
            .addValueEventListener(redCardNumberEventListener)
    }

    fun onDestroy() {
        refRoom.child(Constants.roomId).child("remainingBlackCardNumber")
            .removeEventListener(blackCardNumberEventListener)
        refRoom.child(Constants.roomId).child("remainingBlueCardNumber")
            .removeEventListener(blueCardNumberEventListener)
        refRoom.child(Constants.roomId).child("remainingRedCardNumber")
            .removeEventListener(redCardNumberEventListener)
        refRoom.child(Constants.roomId).child("clickedCards")
            .removeEventListener(clickedCardListEventListener)
        refRoom.child(Constants.roomId).child("chosenCards")
            .removeEventListener(chosenCardListEventListener)
    }

}

