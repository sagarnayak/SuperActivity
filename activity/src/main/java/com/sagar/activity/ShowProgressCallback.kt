package com.sagar.activity

@Suppress("unused")
interface ShowProgressCallback {
    fun showProgress(numberOfLoader: Int = 1)
    fun showProgress(numberOfLoader: Int = 1, showLoaderAccordingToSpeed: Boolean = true)
    fun hideProgress()
    fun hideProgressForced()
    fun isAnyThingInProgress(): Boolean
}