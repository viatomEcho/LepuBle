package com.lepu.demo.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }

    private val _button = MutableLiveData<String>().apply {
        value = "O2Ring"
    }
    val text: LiveData<String> = _text
    val button: LiveData<String> = _button
}