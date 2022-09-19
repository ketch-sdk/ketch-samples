package com.example.helloworld

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Consent(
    @SerializedName("purposes")
    val purposes: Purposes
): Parcelable

@Parcelize
data class Purposes(
    @SerializedName("analytics")
    val analytics: Boolean = false
): Parcelable
