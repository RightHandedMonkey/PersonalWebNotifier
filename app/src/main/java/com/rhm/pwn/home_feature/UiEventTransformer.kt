package com.rhm.pwn.home_feature

import android.util.Log

class UiEventTransformer : (UiEvent) -> HomeFeature.Wish {
    override fun invoke(event: UiEvent): HomeFeature.Wish {
        Log.d("SAMB", this.javaClass.name + ", invoke() called for $event")
        return when(event) {
            is UiEvent.ViewClicked -> HomeFeature.Wish.HomeScreenView(event.urlCheck)
            is UiEvent.DebugClicked -> HomeFeature.Wish.HomeScreenDebug
            is UiEvent.EditClicked -> HomeFeature.Wish.HomeScreenEditOpen(event.urlCheck)
            is UiEvent.EditCancelClicked -> HomeFeature.Wish.HomeScreenEditCancel
            is UiEvent.EditSaveClicked -> HomeFeature.Wish.HomeScreenEditSave(event.urlCheck)
            is UiEvent.EditDeleteClicked -> HomeFeature.Wish.HomeScreenEditDelete(event.urlCheck)
        }
    }
}