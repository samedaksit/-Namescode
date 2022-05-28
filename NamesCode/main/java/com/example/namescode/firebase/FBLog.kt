package com.example.namescode.firebase

import com.example.namescode.model.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.subjects.BehaviorSubject

object FBLog {
    private val database = Firebase.database("https://namescode-b798f-default-rtdb.firebaseio.com/")

    private val refRooms = database.reference.child("rooms")

    private val gameLogRedSubject = BehaviorSubject.create<ArrayList<Log>>()

    fun observeGameLogRedSubject(): BehaviorSubject<ArrayList<Log>> = gameLogRedSubject

    private val gameLogBlueSubject = BehaviorSubject.create<ArrayList<Log>>()

    fun observeGameLogBlueSubject(): BehaviorSubject<ArrayList<Log>> = gameLogBlueSubject

    fun addToGameLogRed( event: String) {
        refRooms.child(Constants.roomId).child("gameLogRed").push().setValue(event)
    }

    fun addToGameLogBlue( event: String) {
        refRooms.child(Constants.roomId).child("gameLogBlue").push().setValue(event)
    }

    fun deleteGameLogs(){
        refRooms.child(Constants.roomId).child("gameLogBlue").removeValue()
        refRooms.child(Constants.roomId).child("gameLogRed").removeValue()
    }

    fun getGameLogRed() {
        refRooms.child(Constants.roomId).child("gameLogRed").addValueEventListener(redGameLogEventListener)
    }

    private val redGameLogEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            gameLogRedSubject.onNext(arrayListOf<Log>().apply {
                for (s in snapshot.children) {
                    val getLog = s.value.toString()
                    add(Log(getLog))
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getGameLogBlue() {
        refRooms.child(Constants.roomId).child("gameLogBlue").addValueEventListener(blueGameLogEventListener)
    }

    private val blueGameLogEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            gameLogBlueSubject.onNext(arrayListOf<Log>().apply {
                for (s in snapshot.children) {
                    val getLog = s.value.toString()
                    add(Log(getLog))
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun onDestroy() {
        refRooms.child(Constants.roomId).child("gameLogRed").removeEventListener(redGameLogEventListener)
        refRooms.child(Constants.roomId).child("gameLogBlue").removeEventListener(blueGameLogEventListener)
    }
}