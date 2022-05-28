package com.example.namescode.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Player(
    var player_id: String? = "",
    var player_name: String? = ""
)
