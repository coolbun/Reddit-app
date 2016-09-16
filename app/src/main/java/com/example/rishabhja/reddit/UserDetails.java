package com.example.rishabhja.reddit;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableField;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserDetails {

    public UserDetails(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @SerializedName("name")
    private String name;

    public void setName(String name) {
        this.name = name;
    }
}
