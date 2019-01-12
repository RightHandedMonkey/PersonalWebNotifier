package com.rhm.pwn.homeFeature

import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.rhm.pwn.homeFeature.HomeFeatureOld.Wish
import com.rhm.pwn.homeFeature.HomeFeatureOld.Effect
import com.rhm.pwn.homeFeature.HomeFeatureOld.State
import com.rhm.pwn.model.URLCheck
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class HomeFeatureOld : ActorReducerFeature<Wish, Effect, State, Nothing>(
        initialState = State(),
        actor = ActorImpl(),
        reducer = ReducerImpl()
) {

    data class State(
            val isLoading: Boolean = false,
            val editShown: Boolean = false,
            val editUrlCheck: URLCheck? = null,
            val list: List<URLCheck> = emptyList())


    sealed class Wish {
        object HomeScreenLoading : Wish()
        data class HomeScreenView(val urlCheck: URLCheck)
        data class HomeScreenEditOpen(val urlCheck: URLCheck)
        object HomeScreenEditCancel : Wish()
        data class HomeScreenEditSave(val urlCheck: URLCheck)
        data class HomeScreenEditDelete(val urlCheck: URLCheck)
        data class HomeScreenEditSelect(val urlCheck: URLCheck)
    }

    sealed class Effect {
        object StartedLoading : Effect()
        data class FinishedWithSuccess(val urlChecks: List<URLCheck>) : Effect()
        data class FinishedWithFailure(val throwable: Throwable) : Effect()
    }

    class ActorImpl : Actor<State, Wish, Effect> {
        private val service: Observable<List<URLCheck>> = TODO()

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
        }
    }

}