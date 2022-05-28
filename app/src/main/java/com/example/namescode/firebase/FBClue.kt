package com.example.namescode.firebase

import android.graphics.Color
import com.example.namescode.model.subject.ClueSubject
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.subjects.BehaviorSubject

object FBClue {
    private val database = Firebase.database("https://namescode-b798f-default-rtdb.firebaseio.com/")
    private val refRooms = database.reference.child("rooms")


    private val givenClueSubject = BehaviorSubject.create<ClueSubject>()

    fun observeGivenClueSubject(): BehaviorSubject<ClueSubject> = givenClueSubject

    fun giveClue( clue: String, teamColor: String) {
        if (teamColor == "red") {
            refRooms.child(Constants.roomId).child("clues").child("redClues").push().setValue(clue)
        } else {
            refRooms.child(Constants.roomId).child("clues").child("blueClues").push().setValue(clue)
        }

    }

    fun deleteClues(){
        refRooms.child(Constants.roomId).child("clues").removeValue()
    }

    fun getRedTeamLastClue() {
        refRooms.child(Constants.roomId).child("clues").child("redClues").limitToLast(1)
            .addValueEventListener(redTeamGivenClueEventListener)
    }

    private val redTeamGivenClueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val i = snapshot.children.iterator()
            var clue = ""
            while (i.hasNext()) {
                clue = i.next().getValue(String()::class.java).toString()
            }
            givenClueSubject.onNext(ClueSubject(Color.RED, clue))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getBlueTeamLastClue() {
        refRooms.child(Constants.roomId).child("clues").child("blueClues").limitToLast(1)
            .addValueEventListener(blueTeamGivenClueEventListener)
    }

    private val blueTeamGivenClueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val i = snapshot.children.iterator()
            var clue = ""
            while (i.hasNext()) {
                clue = i.next().getValue(String()::class.java).toString()
            }
            givenClueSubject.onNext(ClueSubject(Color.BLUE, clue))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun onDestroy() {
        refRooms.child(Constants.roomId).child("clues").child("redClues").removeEventListener(
            redTeamGivenClueEventListener
        )
        refRooms.child(Constants.roomId).child("clues").child("blueClues").removeEventListener(
            blueTeamGivenClueEventListener
        )
    }
}