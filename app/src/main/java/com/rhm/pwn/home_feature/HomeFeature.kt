package com.rhm.pwn.home_feature

import com.rhm.pwn.home_feature.HomeFeature.State
import com.rhm.pwn.home_feature.HomeFeature.Wish
import com.rhm.pwn.home_feature.HomeFeature.Effect
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.rhm.pwn.model.URLCheck
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class HomeFeature(getListService: Observable<List<URLCheck>>) : ActorReducerFeature<Wish, Effect, State, Nothing>(
        initialState = State(),
        actor = ActorImpl(getListService),
        reducer = ReducerImpl()
) {

    data class State(
            val isLoading: Boolean = false,
            val editShown: Boolean = false,
            val editUrlCheck: URLCheck? = null,
            val debug: Boolean = false,
            val list: List<URLCheck> = emptyList())


    sealed class Wish {
        object HomeScreenLoading : Wish()
        data class HomeScreenView(val urlCheck: URLCheck) : Wish()
        data class HomeScreenEditOpen(val urlCheck: URLCheck) : Wish()
        object HomeScreenEditCancel : Wish()
        object HomeScreenDebug : Wish()
        data class HomeScreenEditSave(val urlCheck: URLCheck) : Wish()
        data class HomeScreenEditDelete(val urlCheck: URLCheck) : Wish()
    }

    sealed class Effect {
        object StartedLoading : Effect()
        data class FinishedWithSuccess(val urlChecks: List<URLCheck>) : Effect()
        data class FinishedWithFailure(val throwable: Throwable) : Effect()
        data class ShowEditDialog(val urlCheck: URLCheck) : Effect()
    }

    class ActorImpl(val service: Observable<List<URLCheck>>) : Actor<State, Wish, Effect> {
        override fun invoke(state: State, wish: Wish): Observable<Effect> = when (wish) {
            is Wish.HomeScreenLoading -> {
                if (!state.isLoading) {
                    service
                            .observeOn(AndroidSchedulers.mainThread())
                            .map { Effect.FinishedWithSuccess(urlChecks = it) as Effect }
                            .startWith(Effect.StartedLoading)
                            .onErrorReturn { Effect.FinishedWithFailure(it) }
                } else {
                    Observable.empty()

                }
            }
            is Wish.HomeScreenEditOpen -> {
                service.observeOn(AndroidSchedulers.mainThread())
                        .map { Effect.ShowEditDialog(wish.urlCheck)}
            }
            //TODO: Add remaining wishes
            else -> Observable.empty()
        }
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State = when (effect) {
            is Effect.StartedLoading -> state.copy(
                    isLoading = true
            )
            is Effect.FinishedWithSuccess -> state.copy(
                    isLoading = false,
                    list = effect.urlChecks
            )
            is Effect.FinishedWithFailure -> state.copy(
                    isLoading = false
            )
            is Effect.ShowEditDialog -> state.copy(editUrlCheck = effect.urlCheck)
        }
    }
}
