package com.rhm.pwn.home_feature

import com.rhm.pwn.model.URLCheck

sealed class UiEvent {
    data class ViewClicked(val urlCheck: URLCheck) : UiEvent()
    data class EditClicked(val urlCheck: URLCheck) : UiEvent()
    object DebugClicked : UiEvent()
}