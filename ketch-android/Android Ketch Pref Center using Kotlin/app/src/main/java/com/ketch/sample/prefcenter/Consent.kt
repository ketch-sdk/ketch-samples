package com.ketch.sample.prefcenter

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Consent(
    @SerializedName("IABUSPrivacy_String")
    val IABUSPrivacy_String: String?,

    @SerializedName("IABTCF_TCString")
    val IABTCF_TCString: String?
): Parcelable
