package com.rhm.pwn.model

/**
 * Created by sambo on 9/2/17.
 */

interface URLCheckSelectedAction {
    fun onSelectedURLCheck(urlc: URLCheck)
    fun onEditURLCheck(urlc: URLCheck): Boolean
}
