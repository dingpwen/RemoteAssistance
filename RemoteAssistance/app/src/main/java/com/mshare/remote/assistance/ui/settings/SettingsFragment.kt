package com.mshare.remote.assistance.ui.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.mshare.remote.assistance.Constants
import com.mshare.remote.assistance.R
import java.lang.ref.WeakReference

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        const val FRAGMENT_DIALOG = "dialog"
        fun newInstance() = SettingsFragment()

        class ConfirmationDialog(viewModel: SettingsViewModel): DialogFragment() {
            private val viewModelReference = WeakReference<SettingsViewModel>(viewModel)
            override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
                return AlertDialog.Builder(requireActivity())
                        .setMessage(R.string.request_permission)
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            viewModelReference.get()!!.login.value = false
                            Constants.setLoginStatus(activity as Context, Constants.USER_LOGIN_STATUS_OFF)
                        }
                        .setNegativeButton(android.R.string.cancel
                        ) { _, _ ->
                            //do nothing
                        }
                        .create()
            }
        }
    }

    private lateinit var viewModel: SettingsViewModel
    private var loginPreference: Preference? = null
    private var logoutPreference: Preference? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        loginPreference = preferenceScreen.findPreference("login")
        logoutPreference = preferenceScreen.findPreference("logout")
        viewModel.login.observe(viewLifecycleOwner, Observer {
            if(it) {
                preferenceScreen.removePreference(loginPreference)
                preferenceScreen.addPreference(logoutPreference)
            } else {
                preferenceScreen.removePreference(logoutPreference)
                preferenceScreen.addPreference(loginPreference)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val loginState = Constants.getLoginStatus(activity as Context)
        viewModel.login.value = (loginState == Constants.USER_LOGIN_STATUS_ON)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        if(preference!!.key == "logout") {
            ConfirmationDialog(viewModel).show(childFragmentManager, FRAGMENT_DIALOG)
        }
        return super.onPreferenceTreeClick(preference)
    }
}
