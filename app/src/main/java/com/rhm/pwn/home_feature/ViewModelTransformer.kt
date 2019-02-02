package com.rhm.pwn.home_feature

class ViewModelTransformer : (HomeFeature.State) -> ViewModel {

    override fun invoke(featureState: HomeFeature.State): ViewModel {

        return ViewModel(
                featureState.list,
                featureState.isLoading,
                featureState.editUrlCheck
        )
    }
}