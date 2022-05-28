package com.example.namescode.firebase

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.subjects.BehaviorSubject

object FBTurn {
    private val database = Firebase.database("https://namescode-b798f-default-rtdb.firebaseio.com/")
    private val refRooms = database.reference.child("rooms")

    fun setTurn( turn: String ) {
        println("salih setTurn $turn")
        refRooms.child(Constants.roomId).child("turn").setValue(turn)
    }

    private val getTurnSubject = BehaviorSubject.create<String>()

    fun observeTurnSubject(): BehaviorSubject<String> = getTurnSubject

    private val getTurnEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val turn = snapshot.getValue(String()::class.java).toString()
            getTurnSubject.onNext(turn)
            println("salih getTurn $turn")
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getTurn() {
        refRooms.child(Constants.roomId).child("turn")
            .addValueEventListener(getTurnEventListener)
    }

    fun onDestroy() {
        refRooms.child(Constants.roomId).child("turn")
            .removeEventListener(getTurnEventListener)
    }

}