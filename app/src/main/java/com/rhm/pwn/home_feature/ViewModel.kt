package com.rhm.pwn.home_feature

import com.rhm.pwn.model.URLCheck

data class ViewModel(
        val urlChecks: List<URLCheck>,
        val imageIsLoading: Boolean,
        val openEditUrl: URLCheck? = null
)
