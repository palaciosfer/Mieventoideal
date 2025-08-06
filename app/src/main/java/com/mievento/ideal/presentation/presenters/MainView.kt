package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.Event

interface MainView {
    fun showLoading()
    fun hideLoading()
    fun showEvents(events: List<Event>)
    fun showError(message: String)
    fun showMessage(message: String)
    fun onEventDeleted()
    fun onEventStatusUpdated()
}