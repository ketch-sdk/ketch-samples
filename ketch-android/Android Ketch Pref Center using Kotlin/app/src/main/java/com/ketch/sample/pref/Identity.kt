package com.ketch.sample.pref

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Identity(val code: String, val value: String) : Parcelable
