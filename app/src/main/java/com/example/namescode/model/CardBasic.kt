package com.example.namescode.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class CardBasic(
    var cardId: String? = null,
    var cardWord: String? = null
)
