package com.rhm.pwn.homeFeature

import com.rhm.pwn.model.URLCheck

sealed class UiEvent {
    data class ViewClicked(val id: Int) : UiEvent()
    data class EditClicked(val id: Int) : UiEvent()
    object EditCancelClicked : UiEvent()
    data class EditSaveClicked(val urlCheck: URLCheck) : UiEvent()
    data class EditDeleteClicked(val id: Int) : UiEvent()
    object DebugClicked : UiEvent()
}