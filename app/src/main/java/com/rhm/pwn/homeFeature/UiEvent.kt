package com.rhm.pwn.homeFeature

import com.rhm.pwn.model.URLCheck

sealed class UiEvent {
    data class ViewClicked(val urlCheck: URLCheck) : UiEvent()
    data class EditClicked(val urlCheck: URLCheck) : UiEvent()
    object EditCancelClicked : UiEvent()
    data class EditSaveClicked(val urlCheck: URLCheck) : UiEvent()
    data class EditDeleteClicked(val urlCheck: URLCheck) : UiEvent()
    object DebugClicked : UiEvent()
}