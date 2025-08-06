package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.AuthResponse

interface RegisterView {
    fun showLoading()
    fun hideLoading()
    fun showError(message: String)
    fun showMessage(message: String)
    fun onRegistrationSuccess(response: AuthResponse)
}