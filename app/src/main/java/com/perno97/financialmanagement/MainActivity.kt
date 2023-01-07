package com.perno97.financialmanagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.commit
import com.perno97.financialmanagement.databinding.ActivityMainBinding
import com.perno97.financialmanagement.fragments.IncomingMovementsFragment

class MainActivity : AppCompatActivity() {

    private val channelId: String = "incoming_movements_channel"

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        createNotificationChannel()
        val view = binding.root
        setContentView(view)
        if (intent.action == "ACTION_INCOMING_MOVEMENTS") {
            supportFragmentManager.commit {
                add(R.id.fragment_container_view, IncomingMovementsFragment())
                addToBackStack(null)
            }
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}