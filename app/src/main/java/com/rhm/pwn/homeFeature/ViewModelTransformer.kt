package com.rhm.pwn.homeFeature

class ViewModelTransformer : (HomeFeature.State) -> ViewModel {

    override fun invoke(featureState: HomeFeature.State): ViewModel {

        return ViewModel(
                featureState.list,
                featureState.isLoading,
                featureState.debug,
                featureState.editUrlCheck
        )
    }
}