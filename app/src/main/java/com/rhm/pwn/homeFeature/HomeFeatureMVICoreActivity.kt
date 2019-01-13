package com.rhm.pwn.homeFeature

import android.os.Bundle
import com.badoo.mvicore.android.AndroidBindings
import com.badoo.mvicore.binder.using
import com.rhm.pwn.R
import com.rhm.pwn.home.URLCheckAdapter
import com.rhm.pwn.model.PWNDatabase
import com.rhm.pwn.model.URLCheck
import com.rhm.pwn.model.URLCheckSelectedAction
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_pwnhome.*
import kotlinx.android.synthetic.main.fragment_pwnhome.*

class HomeFeatureMVICoreActivity : ObservableSourceActivity<UiEvent>(), Consumer<ViewModel> {

    override fun accept(vm: ViewModel?) {
        vm?.let {
            (urlc_recycler_view.adapter as URLCheckAdapter).setValues(vm.urlChecks.toMutableList())
        }
    }

    private lateinit var bindings: HomeFeatureActivityBindings


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = HomeFeatureActivityBindings(this, HomeFeature(PWNDatabase.getInstance(applicationContext).urlCheckDao().allObservable()))
        setContentView(R.layout.activity_pwnhome)
        setSupportActionBar(findViewById(R.id.toolbar))
        bindViewActions()
        bindings.setup(this)
    }

    private fun bindViewActions() {
        if (urlc_recycler_view.adapter == null) {
            urlc_recycler_view.adapter = URLCheckAdapter(ArrayList(), object : URLCheckSelectedAction {
                override fun onSelectedURLCheck(urlc: URLCheck) {
                    onNext(UiEvent.ViewClicked(urlc))
                }

                override fun onEditURLCheck(urlc: URLCheck): Boolean {
                    onNext(UiEvent.EditClicked(urlc))
                    return true
                }
            })
        }
        fab.setOnClickListener { onNext(UiEvent.EditClicked(URLCheck())) }
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