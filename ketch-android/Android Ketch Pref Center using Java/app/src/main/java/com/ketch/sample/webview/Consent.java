package com.ketch.sample.webview;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Consent {
    @SerializedName("purposes")
    @Nullable
    public Map<String, Boolean> purposes;

    @SerializedName("vendors")
    @Nullable
    public List<String> vendors;

    @Override
    public String toString() {
        return "Consent{" +
                "purposes=" + purposes +
                ", vendors=" + vendors +
                '}';
    }
}
