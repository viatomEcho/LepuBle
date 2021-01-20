package com.lepu.demo.ui.o2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ConnectO2ViewModel : ViewModel() {

    val _text = MutableLiveData<String>().apply {
        value = "bind o2ring"
    }

    var text: LiveData<String> = _text
}