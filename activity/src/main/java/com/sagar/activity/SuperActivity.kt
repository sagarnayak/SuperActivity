@file:Suppress("LeakingThis")

package com.sagar.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sagar.dialog.DialogContracts
import com.sagar.dialog.DialogUtil
import com.sagar.modelsandenums.KeywordsAndConstants.END_SELF
import com.sagar.modelsandenums.models.Result
import com.sagar.progress.ProgressUtil
import com.sagar.utils.CustomTabHelper
import com.sagar.utils.NetworkUtil

@Suppress("unused")
@SuppressLint("Registered")
abstract class SuperActivity :
    AppCompatActivity(),
    ShowMessageCallback,
    ShowProgressCallback {

    inline fun <reified T> fromJson(json: String): T {
        return Gson().fromJson(json, object : TypeToken<T>() {}.type)
    }

    private var pressBackAgainToExitString = ""

    abstract fun setPressBackAgainToExitMessageString(): String

    private var colorPrimaryForExternalBrowser = 0

    abstract fun setColorPrimaryForExternalBrowser(): Int

    fun toJson(argument: Any) = Gson().toJson(argument)!!

    private val dialogUtil: DialogUtil =
        DialogUtil(this)

    private val progressUtil: ProgressUtil =
        ProgressUtil(this)

    fun isConnectedToNetwork() = NetworkUtil.isConnected(this)

    override fun showProgress(numberOfLoader: Int) {
        progressUtil.show(numberOfLoader)
    }

    override fun showProgress(numberOfLoader: Int, showLoaderAccordingToSpeed: Boolean) {
        progressUtil.show(numberOfLoader)
    }

    override fun hideProgress() {
        try {
            progressUtil.hide()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    override fun isAnyThingInProgress(): Boolean {
        return progressUtil.isAnyThingInProgress()
    }

    override fun hideProgressForced() {
        progressUtil.hideForced()
    }

    override fun showProgressDialog(): (progress: Float) -> Unit {
        return dialogUtil.showProgressDialog()
    }

    override fun hideDialog() {
        dialogUtil.hideDialog()
    }

    override fun handleGenericResult(result: Result) {
        hideProgress()
        if (result.type.equals("network error", true)) {
            showMessageWithOneButton(
                result.getMessageToShow(),
                callback = object : DialogContracts.CallBack {
                    override fun buttonClicked() {
                        super.buttonClicked()
                        sendBroadcast(Intent(END_SELF))
                    }
                }
            )
        } else
            showMessageInDialog(result.getMessageToShow())
    }

    override fun showMessageInDialog(message: String) {
        dialogUtil.showMessage(
            message = message
        )
    }

    override fun showMessageWithOneButton(
        message: String,
        callback: DialogContracts.CallBack,
        cancellable: Boolean,
        buttonText: String,
        image: Drawable?,
        heading: String?
    ) {
        dialogUtil.showMessage(
            message = message,
            callBack = callback,
            cancellable = cancellable,
            buttonText = buttonText,
            image = image,
            heading = heading
        )
    }

    override fun showMessageWithTwoButton(
        message: String,
        callback: DialogContracts.MultiButtonCallBack,
        cancellable: Boolean,
        buttonOneText: String,
        buttonTwoText: String,
        image: Drawable?,
        heading: String?
    ) {
        dialogUtil.showMessage(
            message = message,
            cancellable = cancellable,
            multiButtonCallBack = callback,
            buttonOneText = buttonOneText,
            buttonTwoText = buttonTwoText,
            image = image,
            heading = heading
        )
    }

    override fun showSuccessDialog(
        message: String,
        waitMills: Long,
        viewOnlyDialogCallBack: DialogContracts.ViewOnlyDialogCallBack?
    ) {
        dialogUtil.showSuccessDialog(
            message, waitMills, viewOnlyDialogCallBack
        )
    }

    override fun showFailDialog(
        message: String,
        waitMills: Long,
        viewOnlyDialogCallBack: DialogContracts.ViewOnlyDialogCallBack?
    ) {
        dialogUtil.showFailDialog(
            message, waitMills, viewOnlyDialogCallBack
        )
    }

    fun View.setMarginTop(marginTop: Int) {
        val menuLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
        menuLayoutParams.setMargins(0, marginTop, 0, 0)
        this.layoutParams = menuLayoutParams
    }

    private var askedForExit = false

    protected fun askForAppExit() {
        if (pressBackAgainToExitString.isEmpty()) {
            pressBackAgainToExitString = setPressBackAgainToExitMessageString()
        }
        if (askedForExit)
            finish()
        else {
            askedForExit = true
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    askedForExit = false
                },
                2000
            )
            Toast.makeText(
                this,
                pressBackAgainToExitString,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /*fun openInInternalBrowser(webViewItem: WebViewItem) {
        startActivity(
            Intent(
                this,
                WebView::class.java
            )
                .putExtra(
                    INTENT_DATA,
                    toJson(webViewItem)
                )
        )
    }*/

    fun openInExternalBrowser(url: String) {
        if (colorPrimaryForExternalBrowser == 0) {
            colorPrimaryForExternalBrowser = setColorPrimaryForExternalBrowser()
        }
        CustomTabsIntent.Builder().apply {
            val customTabColorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setNavigationBarColor(
                    ContextCompat.getColor(
                        this@SuperActivity,
                        colorPrimaryForExternalBrowser
                    )
                )
                .setToolbarColor(
                    ContextCompat.getColor(
                        this@SuperActivity,
                        colorPrimaryForExternalBrowser
                    )
                )
                .setSecondaryToolbarColor(
                    ContextCompat.getColor(
                        this@SuperActivity,
                        colorPrimaryForExternalBrowser
                    )
                )
                .build()
            setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, customTabColorSchemeParams)
            setShowTitle(true)
            setStartAnimations(
                this@SuperActivity,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            setExitAnimations(
                this@SuperActivity,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            CustomTabHelper().getPackageNameToUse(
                this@SuperActivity,
                url
            )?.let {
                val customTabIntent = build()
                customTabIntent.apply {
                    intent.setPackage(it)
                    launchUrl(
                        this@SuperActivity,
                        Uri.parse(url)
                    )
                }
            } ?: run {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                )
            }
        }
    }
}