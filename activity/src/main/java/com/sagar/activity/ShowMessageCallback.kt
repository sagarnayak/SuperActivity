package com.sagar.activity

import android.graphics.drawable.Drawable
import com.sagar.dialog.DialogContracts
import com.sagar.modelsandenums.models.Result

@Suppress("unused")
interface ShowMessageCallback {
    fun showMessageInDialog(message: String)

    fun showMessageWithOneButton(
        message: String,
        callback: DialogContracts.CallBack,
        cancellable: Boolean = false,
        buttonText: String = "",
        image: Drawable? = null,
        heading: String? = null
    )

    fun showMessageWithTwoButton(
        message: String,
        callback: DialogContracts.MultiButtonCallBack,
        cancellable: Boolean = false,
        buttonOneText: String = "",
        buttonTwoText: String = "",
        image: Drawable? = null,
        heading: String? = null
    )

    fun handleGenericResult(result: Result)

    fun showSuccessDialog(
        message: String = "",
        waitMills: Long = 3000L,
        viewOnlyDialogCallBack: DialogContracts.ViewOnlyDialogCallBack? = null
    )

    fun showFailDialog(
        message: String,
        waitMills: Long = 3000L,
        viewOnlyDialogCallBack: DialogContracts.ViewOnlyDialogCallBack? = null
    )

    fun showProgressDialog(): ((progress: Float) -> Unit)

    fun hideDialog()
}