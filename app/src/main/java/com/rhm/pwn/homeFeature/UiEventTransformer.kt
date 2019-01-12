package com.rhm.pwn.homeFeature

class UiEventTransformer : (UiEvent) -> HomeFeature.Wish {
    override fun invoke(event: UiEvent): HomeFeature.Wish = when(event) {
        is UiEvent.ViewClicked -> HomeFeature.Wish.HomeScreenView(event.id)
        is UiEvent.DebugClicked -> HomeFeature.Wish.HomeScreenDebug
        is UiEvent.EditClicked -> HomeFeature.Wish.HomeScreenEditOpen(event.id)
        is UiEvent.EditCancelClicked -> HomeFeature.Wish.HomeScreenEditCancel
        is UiEvent.EditSaveClicked -> HomeFeature.Wish.HomeScreenEditSave(event.urlCheck)
        is UiEvent.EditDeleteClicked -> HomeFeature.Wish.HomeScreenEditDelete(event.id)
    }
}