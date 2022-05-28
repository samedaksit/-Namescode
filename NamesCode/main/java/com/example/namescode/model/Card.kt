package com.example.namescode.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Card(
    var cardBasic: CardBasic? = null,
    var cardColor: Int? = null,
    var cardClickable: Boolean? = false,
    var cardShowColor: Boolean? = false
)
