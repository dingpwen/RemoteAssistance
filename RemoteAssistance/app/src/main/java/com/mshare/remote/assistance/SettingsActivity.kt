package com.mshare.remote.assistance

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.mshare.remote.assistance.ui.settings.SettingsFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, SettingsFragment.newInstance())
                    .commitNow()
        }
    }

}
