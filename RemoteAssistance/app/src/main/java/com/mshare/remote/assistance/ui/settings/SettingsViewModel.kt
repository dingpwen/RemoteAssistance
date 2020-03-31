package com.mshare.remote.assistance.ui.settings

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {
    var login: MutableLiveData<Boolean> = MutableLiveData()

    init{
        login.value = false
    }
}
