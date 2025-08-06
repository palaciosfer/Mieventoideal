package com.mievento.ideal.presentation.presenters

import com.mievento.ideal.data.models.Event

interface EventDetailView {
    fun showLoading()
    fun hideLoading()
    fun showEventDetails(event: Event)
    fun showError(message: String)
    fun onEventDeleted()
    fun onEventUpdated()
}