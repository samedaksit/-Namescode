package com.example.namescode.ui

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.namescode.R
import com.example.namescode.databinding.ActivityMainBinding
import com.example.namescode.databinding.ActivityMainBinding.inflate
import com.example.namescode.firebase.Constants
import com.example.namescode.firebase.FBUser

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var playerId: Int = 9999999
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        sharedPreferences = getSharedPreferences("player", MODE_PRIVATE)
        playerId = sharedPreferences.getInt("player_id", 9999999)
        Constants.playerId = playerId
        supportFragmentManager
            .beginTransaction()
            .add(R.id.fragmentHolder, OpeningFragment())
            .commitAllowingStateLoss()
    }

}