package com.example.namescode.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.collections.ArrayList

object FBRoom {
    private val database = Firebase.database("https://namescode-b798f-default-rtdb.firebaseio.com/")
    private val refRooms = database.reference.child("rooms")


    private val roomNameSubject = BehaviorSubject.create<String>()
    private val roomListSubject = BehaviorSubject.create<ArrayList<String>>()

    fun observeRoomNameSubject(): BehaviorSubject<String> = roomNameSubject
    fun observeRoomListSubject(): BehaviorSubject<ArrayList<String>> = roomListSubject

    fun createRoom(
        isNew: Boolean,
        roomName: String,
        password: String,
        playerId: String,
        playerName: String
    ) {
        refRooms.child(Constants.roomId).child("players").child(playerId).setValue(playerName)
        if (isNew) {
            refRooms.child(Constants.roomId).child("turn").setValue("RED_NARRATOR")
            refRooms.child(Constants.roomId).child("roomPassword").setValue(password)
            refRooms.child(Constants.roomId).child("isFirstTime").setValue("true")
            refRooms.child(Constants.roomId).child("roomName").setValue(roomName)
            refRooms.child(Constants.roomId).child("remainingBlueCardNumber").setValue("8")
            refRooms.child(Constants.roomId).child("remainingBlackCardNumber").setValue("1")
            refRooms.child(Constants.roomId).child("remainingRedCardNumber").setValue("9")
        }
    }

    fun removePlayerFromRoom(playerId: String) {
        refRooms.child(Constants.roomId).child("players").child(playerId).removeValue()
    }

    private val roomPasswordSubject = BehaviorSubject.create<String>()

    fun observeRoomPasswordSubject(): BehaviorSubject<String> = roomPasswordSubject

    private val roomPasswordEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val roomPassword = snapshot.getValue(String()::class.java).toString()
            roomPasswordSubject.onNext(roomPassword)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getRoomPassword(roomId: String) {
        refRooms.child(roomId).child("roomPassword").addListenerForSingleValueEvent(
            roomPasswordEventListener
        )
    }

    fun setIfFirstTime(isFirstTime: String) {
        refRooms.child(Constants.roomId).child("isFirstTime").setValue(isFirstTime)
    }

    private val isFirstTimeSubject = BehaviorSubject.create<String>()

    fun observeIsFirstTimeSubject(): BehaviorSubject<String> = isFirstTimeSubject

    fun checkIfFirstTime() {
        refRooms.child(Constants.roomId).child("isFirstTime")
            .addListenerForSingleValueEvent(checkFirstTimeEventListener)
    }

    private val checkFirstTimeEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val isFirstTime = snapshot.getValue(String()::class.java).toString()
            isFirstTimeSubject.onNext(isFirstTime)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getRooms() {
        refRooms.addValueEventListener(getRoomListEventListener)
    }

    private val getRoomListEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            roomListSubject.onNext(arrayListOf<String>().apply {
                for (s in snapshot.children) {
                    val roomKey = s.key.toString()
                    add(roomKey)
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getRoomName() {
        refRooms.child(Constants.roomId).child("roomName")
            .addListenerForSingleValueEvent(getRoomNameEventListener)
    }

    private val getRoomNameEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val name = snapshot.getValue(String()::class.java).toString()
            roomNameSubject.onNext(name)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun setPlayerType(playerRole: String, playerId: String, playerName: String) {
        refRooms.child(Constants.roomId).child("roles").child(playerRole).child(playerId)
            .setValue(playerName)
    }

    fun onDestroyOnGame() {
        refRooms.child(Constants.roomId).child("roomName")
            .removeEventListener(getRoomNameEventListener)
        refRooms.child(Constants.roomId).child("isFirstTime")
            .removeEventListener(checkFirstTimeEventListener)
    }

    fun destroyPasswordEventListener() {
        refRooms.child(Constants.roomId).child("roomPassword")
            .removeEventListener(roomPasswordEventListener)
    }

    fun onDestroyOnRoomSelect() {
        refRooms.removeEventListener(getRoomListEventListener)
    }
}