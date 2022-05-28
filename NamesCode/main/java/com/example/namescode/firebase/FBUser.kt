package com.example.namescode.firebase

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FBUser {
    private val database = Firebase.database("https://namescode-b798f-default-rtdb.firebaseio.com/")

    fun reportError(errorExp: String, playerId: Int) {
        database.reference.child("fromUsers/errorsFromUsers").push()
            .setValue("$errorExp --- $playerId")
    }

    fun suggestWord(word: String, playerId: Int) {
        database.reference.child("fromUsers/suggestedWords").push()
            .setValue("$word --- $playerId")
    }
}