package com.rhm.pwn.home_feature

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import com.badoo.mvicore.android.AndroidBindings
import com.badoo.mvicore.binder.using
import com.rhm.pwn.BuildConfig
import com.rhm.pwn.R
import com.rhm.pwn.debug.DebugActivity
import com.rhm.pwn.home.URLCheckAdapter
import com.rhm.pwn.home.URLCheckDialog
import com.rhm.pwn.model.PWNDatabase
import com.rhm.pwn.model.URLCheck
import com.rhm.pwn.model.URLCheckChangeNotifier
import com.rhm.pwn.model.URLCheckSelectedAction
import io.reactivex.Completable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_pwnhome.*
import kotlinx.android.synthetic.main.fragment_pwnhome.*

class HomeFeatureMVICoreActivity : ObservableSourceActivity<UiEvent>(), Consumer<ViewModel> {

    private var dialog: URLCheckDialog? = null

    override fun accept(vm: ViewModel?) {
        Log.d("SAMB", this.javaClass.name + ", accept() called")
        vm?.let {
            if (vm.urlChecks.isNotEmpty()) {
                (urlc_recycler_view.adapter as URLCheckAdapter).setValues(vm.urlChecks.toMutableList())
                urlc_recycler_view.visibility = View.VISIBLE
                empty_view.visibility = View.GONE
            } else {
                urlc_recycler_view.visibility = View.GONE
                empty_view.visibility = View.VISIBLE
            }
            if (vm.openEditUrl != null) {
                handleEditURLCheck(vm.openEditUrl)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (BuildConfig.DEBUG) {
            menuInflater.inflate(R.menu.menu_pwn_home, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.itemId == R.id.action_debug && BuildConfig.DEBUG) {
            val i = Intent(this, DebugActivity::class.java)
            startActivity(i)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private lateinit var bindings: HomeFeatureActivityBindings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pwnhome)
        setSupportActionBar(findViewById(R.id.toolbar))
        bindViewActions()
        bindings = HomeFeatureActivityBindings(this, HomeFeature(PWNDatabase.getInstance(applicationContext).urlCheckDao().allObservable().toObservable()))
        checkForDeepLink()
        bindings.setup(this)
    }

    private fun bindViewActions() {
        val list: MutableList<URLCheck> = ArrayList()
        list.add(URLCheck())
        urlc_recycler_view.layoutManager = LinearLayoutManager(this)
        urlc_recycler_view.adapter = URLCheckAdapter(list, object : URLCheckSelectedAction {
            override fun onSelectedURLCheck(urlc: URLCheck) {
                onNext(UiEvent.ViewClicked(urlc))
            }

            override fun onEditURLCheck(urlc: URLCheck): Boolean {
                onNext(UiEvent.EditClicked(urlc))
                return true
            }
        })

        fab.setOnClickListener { onNext(UiEvent.EditClicked(URLCheck())) }
    }

    @SuppressLint("CheckResult")
    private fun checkForDeepLink() {
        val extras = intent.extras
        if (extras != null) {
            val value = extras.getInt(URLCheck.CLASSNAME, -1)
            val url = extras.getString(URLCheck.URL, "")
            if (value > 0 && !TextUtils.isEmpty(url)) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                customTabsIntent.launchUrl(this, Uri.parse(url))
                Completable.fromAction {
                    val urlc = PWNDatabase.getInstance(this).urlCheckDao()[value]
                    urlc.hasBeenUpdated = false
                    PWNDatabase.getInstance(this).urlCheckDao().update(urlc)
                }.subscribeOn(Schedulers.io())
                        .subscribe { URLCheckChangeNotifier.getNotifier().update(true) }
            }
        }
    }

    private fun handleEditURLCheck(urlc: URLCheck?) {
        // Create an instance of the dialog fragment and show it
        if (dialog == null) {
            dialog = URLCheckDialog()
        }
        val b = Bundle()
        b.putSerializable(URLCheck.CLASSNAME, urlc)
        dialog?.arguments = b
        dialog?.show(this@HomeFeatureMVICoreActivity.supportFragmentManager, URLCheckDialog::class.java.name)
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