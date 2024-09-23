package com.example.scounter

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepViewModel : ViewModel() {
    private val _stepCount = MutableLiveData<Int>().apply { value = 0 }
    val stepCount: LiveData<Int> get() = _stepCount

    fun updateStepCount(newCount: Int) {
        _stepCount.value = newCount
    }

    fun resetStepCount() {
        _stepCount.value = 0
    }
}
