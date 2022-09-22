package com.ketch.sample.prefcenter

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Consent(
    @SerializedName("purposes")
    val purposes: Map<String, Boolean> = emptyMap()
): Parcelable
