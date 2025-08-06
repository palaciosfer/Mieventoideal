package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.Event

interface UserView {
    fun showLoading()
    fun hideLoading()
    fun showAvailableEvents(events: List<Event>)
    fun showError(message: String)
    fun showMessage(message: String)
    fun onParticipationRequested()
}