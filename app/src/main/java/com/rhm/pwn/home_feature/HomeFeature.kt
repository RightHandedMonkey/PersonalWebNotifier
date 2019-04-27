package com.rhm.pwn.home_feature

import android.util.Log
import com.rhm.pwn.home_feature.HomeFeature.State
import com.rhm.pwn.home_feature.HomeFeature.Wish
import com.rhm.pwn.home_feature.HomeFeature.Effect
import com.rhm.pwn.home_feature.HomeFeature.News
import com.badoo.mvicore.element.Actor
import com.badoo.mvicore.element.Bootstrapper
import com.badoo.mvicore.element.NewsPublisher
import com.badoo.mvicore.element.Reducer
import com.badoo.mvicore.feature.ActorReducerFeature
import com.rhm.pwn.model.URLCheck
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class HomeFeature(getListService: Observable<List<URLCheck>>) : ActorReducerFeature<Wish, Effect, State, News>(
        initialState = State(),
        actor = ActorImpl(getListService),
        reducer = ReducerImpl(),
        bootstrapper = BootstrapperImpl(),
        newsPublisher = NewsPublisherImpl()
) {

    data class State(
            val isLoading: Boolean = false,
            val editShown: Boolean = false,
            val editUrlCheck: URLCheck? = null,
            val viewUrlCheck: URLCheck? = null,
            val debug: Boolean = false,
            val list: List<URLCheck> = emptyList())

    sealed class Wish {
        object HomeScreenLoading : Wish()
        data class HomeScreenView(val urlCheck: URLCheck) : Wish()
        data class HomeScreenEditOpen(val urlCheck: URLCheck) : Wish()
        object HomeScreenDebug : Wish()
    }

    sealed class Effect {
        object StartedLoading : Effect()
        data class FinishedWithSuccess(val urlChecks: List<URLCheck>) : Effect()
        data class FinishedWithFailure(val throwable: Throwable) : Effect()
        data class StatelessShowEditDialog(val urlCheck: URLCheck) : Effect()
        data class StatelessLaunchViewPage(val urlCheck: URLCheck) : Effect()
    }

    sealed class News {
        data class ShowEditDialog(val urlCheck: URLCheck) : News()
        data class LaunchViewPage(val urlCheck: URLCheck) : News()
    }

    class NewsPublisherImpl : NewsPublisher<Wish, Effect, State, News> {
        override fun invoke(wish: Wish, effect: Effect, state: State): News? = when (effect) {
            is Effect.StatelessShowEditDialog -> News.ShowEditDialog(effect.urlCheck)
            is Effect.StatelessLaunchViewPage -> News.LaunchViewPage(effect.urlCheck)
            else -> null
        }
    }

    class ActorImpl(private val service: Observable<List<URLCheck>>) : Actor<State, Wish, Effect> {
        override fun invoke(state: State, wish: Wish): Observable<Effect> {
            Log.d("SAMB", this.javaClass.name + ", invoke() called $wish")
            return when (wish) {
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
    }

    class ReducerImpl : Reducer<State, Effect> {
        override fun invoke(state: State, effect: Effect): State {
            Log.d("SAMB", this.javaClass.name + ", invoke() called $effect")

            return when (effect) {
                is Effect.StartedLoading -> state.copy(
                        isLoading = true
                )
                is Effect.FinishedWithSuccess -> {
                    Log.d("SAMB", this.javaClass.name + ", items found: ${effect.urlChecks.size}")
                    state.copy(
                        isLoading = false,
                        list = effect.urlChecks
                )

                }
                is Effect.FinishedWithFailure -> state.copy(
                        isLoading = false
                )
                is Effect.StatelessShowEditDialog -> state.copy(editUrlCheck = effect.urlCheck, viewUrlCheck = null)
                is Effect.StatelessLaunchViewPage -> state.copy(viewUrlCheck = effect.urlCheck, editUrlCheck = null)
            }
        }
    }

    class BootstrapperImpl : Bootstrapper<Wish> {
        override fun invoke(): Observable<Wish> = Observable.just(Wish.HomeScreenLoading as Wish).observeOn(AndroidSchedulers.mainThread())
    }
}
