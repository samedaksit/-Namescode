package com.example.namescode.firebase

import com.example.namescode.model.Player
import com.example.namescode.model.subject.Roles
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.reactivex.rxjava3.subjects.BehaviorSubject

object FBPlayer {
    private val database = Firebase.database("https://namescode-b798f-default-rtdb.firebaseio.com/")
    private val refPlayer = database.reference.child("players")
    private val refRooms = database.reference.child("rooms")
    private var lastPlayerId: Query? = null

    private val newPlayerIdSubject = BehaviorSubject.create<String>()
    private val getRolesSubject = BehaviorSubject.create<Roles>()

    fun observeNewPlayerIdSubject(): BehaviorSubject<String> = newPlayerIdSubject
    fun observeRolesSubject(): BehaviorSubject<Roles> = getRolesSubject

    fun createPlayer(player: Player, uid: String) {
        val playerId = player.player_id
        val playerName = player.player_name
        refPlayer.child("$playerId").child("player_uid").setValue(uid)
        refPlayer.child("$playerId").child("player_id").setValue(playerId)
        refPlayer.child("$playerId").child("player_name").setValue(playerName)
    }


    private val getLastPlayerIdEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val s = snapshot.children.iterator()
            var newId = ""
            while (s.hasNext()) {
                val getPlayer = s.next().getValue(Player::class.java)
                if (getPlayer != null) {
                    val lastPlayerId = getPlayer.player_id?.toInt()!!
                    newId = lastPlayerId.plus(1).toString()
                }
            }
            newPlayerIdSubject.onNext(newId)
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getNewPlayerId() {
        refPlayer.limitToLast(1).addListenerForSingleValueEvent(getLastPlayerIdEventListener)
        lastPlayerId = refPlayer.limitToLast(1)
    }

    fun onDestroyLogin() {
        lastPlayerId?.removeEventListener(getLastPlayerIdEventListener)
    }


    fun getRedNarratorNameList() {
        refRooms.child(Constants.roomId).child("roles").child("RED_NARRATOR")
            .addValueEventListener(redNarratorNameListEventListener)
    }

    private val redNarratorNameListEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            getRolesSubject.onNext(Roles("redNarrator", arrayListOf<String>().apply {
                for (s in snapshot.children) {
                    val name = s.getValue(String()::class.java).toString()
                    add(name)
                }
            }))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getRedSpyNameList() {
        refRooms.child(Constants.roomId).child("roles").child("RED_SPY")
            .addValueEventListener(redSpyNameListEventListener)
    }

    private val redSpyNameListEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            getRolesSubject.onNext(Roles("redSpy", arrayListOf<String>().apply {
                for (s in snapshot.children) {
                    val name = s.getValue(String()::class.java).toString()
                    add(name)
                }
            }))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getBlueSpyNameList() {
        refRooms.child(Constants.roomId).child("roles").child("BLUE_SPY")
            .addValueEventListener(blueSpyNameListEventListener)
    }

    private val blueSpyNameListEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            getRolesSubject.onNext(Roles("blueSpy", arrayListOf<String>().apply {
                for (s in snapshot.children) {
                    val name = s.getValue(String()::class.java).toString()
                    add(name)
                }
            }))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun getBlueNarratorNameList() {
        refRooms.child(Constants.roomId).child("roles").child("BLUE_NARRATOR")
            .addValueEventListener(blueNarratorNameListEventListener)
    }

    private val blueNarratorNameListEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            getRolesSubject.onNext(Roles("blueNarrator", arrayListOf<String>().apply {
                for (s in snapshot.children) {
                    val name = s.getValue(String()::class.java).toString()
                    add(name)
                }
            }))
        }

        override fun onCancelled(error: DatabaseError) {
        }
    }

    fun deleteRoles() {
        refRooms.child(Constants.roomId).child("roles").removeValue()
    }

    fun deleteYourRole(playerId: String) {
        deleteRedNarrator(playerId)
        deleteBlueNarrator(playerId)
        deleteRedSpy(playerId)
        deleteBlueSpy(playerId)
    }

    private fun deleteRedNarrator(playerId: String) {
        refRooms.child(Constants.roomId).child("roles").child("RED_NARRATOR").child(playerId)
            .removeValue()
    }

    private fun deleteBlueNarrator(playerId: String) {
        refRooms.child(Constants.roomId).child("roles").child("BLUE_NARRATOR").child(playerId)
            .removeValue()
    }

    private fun deleteRedSpy(playerId: String) {
        refRooms.child(Constants.roomId).child("roles").child("RED_SPY").child(playerId)
            .removeValue()
    }

    private fun deleteBlueSpy(playerId: String) {
        refRooms.child(Constants.roomId).child("roles").child("BLUE_SPY").child(playerId)
            .removeValue()
    }

    fun onDestroy() {
        refRooms.child(Constants.roomId).child("roles").child("BLUE_NARRATOR")
            .removeEventListener(blueNarratorNameListEventListener)
        refRooms.child(Constants.roomId).child("roles").child("RED_NARRATOR")
            .removeEventListener(redNarratorNameListEventListener)
        refRooms.child(Constants.roomId).child("roles").child("RED_SPY")
            .removeEventListener(redSpyNameListEventListener)
        refRooms.child(Constants.roomId).child("roles").child("BLUE_SPY")
            .removeEventListener(blueSpyNameListEventListener)
    }

}