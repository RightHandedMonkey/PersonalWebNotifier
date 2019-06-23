package com.rhm.pwn.home_feature

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
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
import com.badoo.mvicore.element.NewsPublisher
import com.rhm.pwn.BuildConfig
import com.rhm.pwn.R
import com.rhm.pwn.debug.DebugActivity
import com.rhm.pwn.home.URLCheckAdapter
import com.rhm.pwn.home.URLCheckDialog
import com.rhm.pwn.model.PWNDatabase
import com.rhm.pwn.model.URLCheck
import com.rhm.pwn.model.URLCheckChangeNotifier
import com.rhm.pwn.model.URLCheckSelectedAction
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_pwnhome.*
import kotlinx.android.synthetic.main.fragment_pwnhome.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class HomeFeatureMVICoreActivity : ObservableSourceActivity<UiEvent>(), Consumer<ViewModel> {

    //General ToDos
    //TODO: Android lifecycle comoonents
    //TODO: Storage of data remotely synced to the user's Google account
    //TODO: Material Components
    //TODO: UI Components

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
                Log.i("SAMB", "launching handleEditURLCheck(${vm.openEditUrl}")
                handleEditURLCheck(vm.openEditUrl)
            }
            if (vm.viewUrl != null) {
                Log.i("SAMB", "launching handleViewURLCheck(${vm.viewUrl})")
                handleViewURLCheck(vm.viewUrl)
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
        bindings.setup(this)

        checkForDeepLink()
    }

    private fun bindViewActions() {
        val list: MutableList<URLCheck> = ArrayList()
        list.add(URLCheck())
        urlc_recycler_view.layoutManager = LinearLayoutManager(this)
        urlc_recycler_view.adapter = URLCheckAdapter(list, object : URLCheckSelectedAction {
            override fun onSelectedURLCheck(urlc: URLCheck) {
                //does not use MVI, because we don't want to relaunch each time the feature updates
                //Problem is on a save of an existing item, it saves to the DB which triggers another event before the dialog is dismissed
                handleViewURLCheck(urlc)
            }

            override fun onEditURLCheck(urlc: URLCheck): Boolean {
                handleEditURLCheck(urlc)
                return true
            }
        })

        fab.setOnClickListener {
            handleEditURLCheck(URLCheck())
        }
    }

    @SuppressLint("CheckResult")
    private fun checkForDeepLink(): Boolean {
        val extras = intent.extras
        if (extras != null) {
            val value = extras.getInt(URLCheck.CLASSNAME, -1)
            val url = extras.getString(URLCheck.URL, "")
            if (value > 0 && !TextUtils.isEmpty(url)) {
                val builder = CustomTabsIntent.Builder()
                val customTabsIntent = builder.build()
                Log.i("SAMB", "Deep link found: value=\"$value\", url=\"$url\"")

                customTabsIntent.launchUrl(this, Uri.parse(url))
                GlobalScope.launch {
                    Log.i("SAMB", "Deep link coroutine launched")
                    val urlc = PWNDatabase.getInstance(this@HomeFeatureMVICoreActivity).urlCheckDao()[value]
                    urlc.hasBeenUpdated = false
                    PWNDatabase.getInstance(this@HomeFeatureMVICoreActivity).urlCheckDao().update(urlc)
                    Log.i("SAMB", "Deep link marked as read: url=\"${urlc.url}\"")
                    URLCheckChangeNotifier.getNotifier().update(true)
                    Log.i("SAMB", "Deep link coroutine complete")
                }
                return true
            }
            Log.i("SAMB", "Deep link not found: value=\"$value\", url=\"$url\"")
        } else {
            Log.i("SAMB", "Deep link not found - extras null")
        }
        return false
    }

    private fun handleEditURLCheck(urlc: URLCheck?) {
        Log.i("SAMB", this.javaClass.name + ", handleEditURLCheck() called for '$urlc', dialog is '$dialog'")
        if (urlc == null) {
            dialog?.dismiss()
            return
        }
        if (dialog == null) {
            Log.i("SAMB", this.javaClass.name + ", new dialog created")
            dialog = URLCheckDialog()
        }

        val b = Bundle()
        b.putSerializable(URLCheck.CLASSNAME, urlc)
        dialog?.arguments = b
        Log.i("SAMB", this.javaClass.name + ", showing dialog '$dialog'")
        dialog?.show(this@HomeFeatureMVICoreActivity.supportFragmentManager, URLCheckDialog::class.java.name)
    }

    @SuppressLint("CheckResult")
    private fun handleViewURLCheck(urlc: URLCheck) {
        Log.d("SAMB", this.javaClass.name + ", handleViewURLCheck() called for $urlc")
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(urlc.getUrl()))
        URLCheckChangeNotifier.getNotifier().update(true)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(urlc.id)

        GlobalScope.launch {
            urlc.hasBeenUpdated = false
            PWNDatabase.getInstance(this@HomeFeatureMVICoreActivity.applicationContext).urlCheckDao().update(urlc)
        }
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