package com.rhm.pwn.home

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.rhm.pwn.R

import kotlinx.android.synthetic.main.fragment_pwnhome.*

class HomeFeatureMVICoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_pwnhome)
        setSupportActionBar(findViewById(R.id.toolbar))
    }

}
