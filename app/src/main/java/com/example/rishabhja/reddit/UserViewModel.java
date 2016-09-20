package com.example.rishabhja.reddit;

import android.databinding.ObservableField;

import java.util.Observable;

/**
 * Created by rishabh.ja on 20/09/16.
 */
public class UserViewModel {
    public final ObservableField<String> name=new ObservableField<>();
    public final ObservableField<String> logout=new ObservableField<>();
}
