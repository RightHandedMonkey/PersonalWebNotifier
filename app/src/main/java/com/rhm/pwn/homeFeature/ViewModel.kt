package com.rhm.pwn.homeFeature

import com.rhm.pwn.model.URLCheck

data class ViewModel(
        val urlChecks: List<URLCheck>,
        val imageIsLoading: Boolean,
        val isDebug: Boolean,
        val openEditUrl: URLCheck? = null
)
