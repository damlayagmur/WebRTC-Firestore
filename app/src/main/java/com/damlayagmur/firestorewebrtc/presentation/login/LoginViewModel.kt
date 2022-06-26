package com.damlayagmur.firestorewebrtc.presentation.login

import androidx.lifecycle.ViewModel
import com.damlayagmur.firestorewebrtc.domain.LoginRepository
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val loginRepository: LoginRepository) :
    ViewModel() {

}