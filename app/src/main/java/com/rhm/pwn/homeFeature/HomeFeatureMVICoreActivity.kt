package com.rhm.pwn.homeFeature

import android.os.Bundle
import com.badoo.mvicore.android.AndroidBindings
import com.badoo.mvicore.binder.using
import com.rhm.pwn.R
import com.rhm.pwn.model.PWNDatabase
import io.reactivex.functions.Consumer

class HomeFeatureMVICoreActivity : ObservableSourceActivity<UiEvent>(), Consumer<ViewModel> {

    override fun accept(t: ViewModel?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val bindings = HomeFeatureActivityBindings(this, HomeFeature(PWNDatabase.getInstance(applicationContext).urlCheckDao().allObservable))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_pwnhome)
        setSupportActionBar(findViewById(R.id.toolbar))
        bindings.setup(this)
    }
}

class HomeFeatureActivityBindings(
        view: HomeFeatureMVICoreActivity,
        private val feature: HomeFeature
) : AndroidBindings<HomeFeatureMVICoreActivity>(view) {
    override fun setup(view: HomeFeatureMVICoreActivity) {
        binder.bind(feature to view using ViewModelTransformer())
        binder.bind(view to feature using UiEventTransformer())
    }
}