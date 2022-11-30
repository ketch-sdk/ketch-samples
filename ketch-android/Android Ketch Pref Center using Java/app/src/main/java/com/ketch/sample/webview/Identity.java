package com.ketch.sample.webview;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class Identity implements Parcelable {
    private final String code;
    private final String value;

    public Identity(String code, String value) {
        this.code = code;
        this.value = value;
    }

    protected Identity(Parcel in) {
        code = in.readString();
        value = in.readString();
    }

    public static final Creator<Identity> CREATOR = new Creator<Identity>() {
        @Override
        public Identity createFromParcel(Parcel in) {
            return new Identity(in);
        }

        @Override
        public Identity[] newArray(int size) {
            return new Identity[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Identity)) return false;
        Identity identity = (Identity) o;
        return getCode().equals(identity.getCode()) && getValue().equals(identity.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getValue());
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(code);
        dest.writeString(value);
    }
}
